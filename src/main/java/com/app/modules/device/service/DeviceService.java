package com.app.modules.device.service;


import com.app.common.base.search.dto.DynamicSearchRequest;
import com.app.common.base.search.service.GenericSpecification;
import com.app.common.config.MqttPublisher;
import com.app.common.enums.ActionStatus;
import com.app.common.enums.ActionType;
import com.app.common.enums.DeviceStatus;
import com.app.common.exception.ResourceNotFoundException;
import com.app.common.exception.DeviceTimeoutException;
import com.app.modules.device.dto.*;
import com.app.modules.device.entity.Device;
import com.app.modules.device.entity.DeviceHistory;
import com.app.modules.device.repository.DeviceHistoryRepository;
import com.app.modules.device.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceHistoryRepository deviceHistoryRepository;
    private final DeviceHistoryTxService deviceHistoryTxService;
    private final MqttPublisher mqttPublisher;

    private final Map<Integer, CompletableFuture<DeviceStatusMessage>> pendingRequests = new ConcurrentHashMap<>();

    public int mapDeviceToNumber(Device device) {
        if (device.getPinGpio() != null) {
            String pin = device.getPinGpio().trim();
            if ("D1".equalsIgnoreCase(pin)) return 1;
            if ("D2".equalsIgnoreCase(pin)) return 2;
        }
        if (device.getDeviceName() != null) {
            String name = device.getDeviceName();
            if (name.contains("2")) return 2;
            if (name.contains("1")) return 1;
        }
        String str = device.getId().toString();
        if (str.endsWith("02") || str.endsWith("2")) return 2;
        if (str.endsWith("01") || str.endsWith("1")) return 1;
        return 1;
    }

    public List<DeviceResponse> getAllDevices() {
        return deviceRepository.findAll().stream().map(d -> DeviceResponse.builder()
                .id(d.getId())
                .deviceName(d.getDeviceName())
                .deviceType(d.getDeviceType().name())
                .pinGpio(d.getPinGpio())
                .currentStatus(d.getCurrentStatus().name())
                .lastActiveAt(d.getLastActiveAt())
                .build()).toList();
    }

    public DeviceControlResponse controlDevice(UUID deviceId, DeviceControlRequest request) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị: " + deviceId));

        ActionType action = ActionType.valueOf(request.getAction().toUpperCase());
        int deviceNum = mapDeviceToNumber(device);

        // 1. Tạo bản ghi PENDING và commit ngay lập tức vào database qua transaction độc lập
        DeviceHistory history = deviceHistoryTxService.createPendingHistory(device, action, "WEB_DASHBOARD");

        CompletableFuture<DeviceStatusMessage> future = new CompletableFuture<>();
        pendingRequests.put(deviceNum, future);
        long startTime = System.currentTimeMillis();

        // 2. Gửi lệnh device/control/{id}
        String controlTopic = "device/control/" + deviceNum;
        String controlPayload = String.format("{\"action\":\"%s\"}", action.name());
        mqttPublisher.publish(controlTopic, controlPayload);
        log.info("[CONTROL] Đã gửi lệnh điều khiển tới {}: {}", controlTopic, controlPayload);

        try {
            // Chờ phản hồi tối đa 5000ms
            DeviceStatusMessage statusMsg = future.get(5000, TimeUnit.MILLISECONDS);
            long executionTimeMs = System.currentTimeMillis() - startTime;

            // 3. Phản hồi thành công -> Cập nhật SUCCESS vào DB
            String resolvedStatus = statusMsg.getStatus() != null ? statusMsg.getStatus().trim().toUpperCase() : action.name();
            deviceHistoryTxService.markSuccess(history.getId(), device.getId(), resolvedStatus, (int) executionTimeMs);

            log.info("Điều khiển thiết bị {} THÀNH CÔNG trong {} ms, trạng thái mới: {}",
                    device.getDeviceName(), executionTimeMs, resolvedStatus);

            return DeviceControlResponse.builder()
                    .actionId(history.getId())
                    .deviceId(device.getId())
                    .deviceName(device.getDeviceName())
                    .action(action.name())
                    .status("SUCCESS")
                    .executionTimeMs((int) executionTimeMs)
                    .confirmedAt(OffsetDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Timeout điều khiển thiết bị {}: {}", device.getDeviceName(), e.getMessage());

            deviceHistoryTxService.markError(history.getId(), "Thiết bị không phản hồi trong 5000ms (Gateway Timeout)");

            throw new DeviceTimeoutException("Thiết bị không phản hồi trong thời gian quy định (Timeout 5s)");
        } finally {
            pendingRequests.remove(deviceNum);
        }
    }

    public List<DeviceControlResponse> controlAllDevices(DeviceControlRequest request) {
        List<Device> devices = deviceRepository.findAll();
        if (devices.isEmpty()) {
            return List.of();
        }

        ActionType action = ActionType.valueOf(request.getAction().toUpperCase());
        long startTime = System.currentTimeMillis();

        // 1. Tạo bản ghi PENDING cho tất cả thiết bị và commit ngay lập tức vào database
        List<DeviceHistory> histories = deviceHistoryTxService.createPendingHistories(devices, action, "WEB_DASHBOARD");

        Map<Integer, Device> deviceNumMap = new java.util.HashMap<>();
        Map<Integer, DeviceHistory> historyMap = new java.util.HashMap<>();
        List<CompletableFuture<DeviceStatusMessage>> futures = new java.util.ArrayList<>();

        for (int i = 0; i < devices.size(); i++) {
            Device dev = devices.get(i);
            DeviceHistory hist = histories.get(i);
            int num = mapDeviceToNumber(dev);
            deviceNumMap.put(num, dev);
            historyMap.put(num, hist);

            CompletableFuture<DeviceStatusMessage> future = new CompletableFuture<>();
            pendingRequests.put(num, future);
            futures.add(future);
        }

        // 2. Gửi duy nhất 1 bản tin broadcast tới topic device/control/all
        String controlTopic = "device/control/all";
        String controlPayload = String.format("{\"action\":\"%s\"}", action.name());
        mqttPublisher.publish(controlTopic, controlPayload);
        log.info("[BROADCAST] Đã gửi lệnh điều khiển đồng thời tới {}: {}", controlTopic, controlPayload);

        try {
            // Chờ tất cả thiết bị phản hồi trong tối đa 5000ms
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(5000, TimeUnit.MILLISECONDS);
            long executionTimeMs = System.currentTimeMillis() - startTime;

            List<DeviceControlResponse> responses = new java.util.ArrayList<>();
            OffsetDateTime now = OffsetDateTime.now();

            for (Map.Entry<Integer, Device> entry : deviceNumMap.entrySet()) {
                int num = entry.getKey();
                Device dev = entry.getValue();
                DeviceHistory history = historyMap.get(num);
                CompletableFuture<DeviceStatusMessage> f = pendingRequests.get(num);

                DeviceStatusMessage statusMsg = f != null && f.isDone() ? f.getNow(null) : null;
                String resolvedStatus = (statusMsg != null && statusMsg.getStatus() != null)
                        ? statusMsg.getStatus().trim().toUpperCase() : action.name();

                deviceHistoryTxService.markSuccess(history.getId(), dev.getId(), resolvedStatus, (int) executionTimeMs);

                responses.add(DeviceControlResponse.builder()
                        .actionId(history.getId())
                        .deviceId(dev.getId())
                        .deviceName(dev.getDeviceName())
                        .action(action.name())
                        .status("SUCCESS")
                        .executionTimeMs((int) executionTimeMs)
                        .confirmedAt(now)
                        .build());
            }

            log.info("Điều khiển TẤT CẢ thiết bị THÀNH CÔNG trong {} ms", executionTimeMs);
            return responses;

        } catch (Exception e) {
            log.error("Timeout hoặc lỗi điều khiển tất cả thiết bị: {}", e.getMessage());

            // Đánh dấu bản ghi nào chưa hoàn thành là ERROR
            for (Map.Entry<Integer, DeviceHistory> entry : historyMap.entrySet()) {
                DeviceHistory history = entry.getValue();
                deviceHistoryTxService.markError(history.getId(), "Thiết bị không phản hồi trong 5000ms (Gateway Timeout)");
            }

            throw new DeviceTimeoutException("Một hoặc nhiều thiết bị không phản hồi trong thời gian quy định (Timeout 5s)");
        } finally {
            for (Integer num : deviceNumMap.keySet()) {
                pendingRequests.remove(num);
            }
        }
    }

    public void handleHardwareStatus(DeviceStatusMessage statusMsg) {
        int deviceNum = statusMsg.getDeviceId();
        String rawStatus = statusMsg.getStatus();
        String normalizedStatus = rawStatus != null ? rawStatus.trim().toUpperCase() : "OFF";
        statusMsg.setStatus(normalizedStatus);

        log.info("Nhận trạng thái từ thiết bị {}: {}", deviceNum, normalizedStatus);
        CompletableFuture<DeviceStatusMessage> future = pendingRequests.get(deviceNum);
        if (future != null) {
            log.info("[2-PHASE] Tìm thấy pending request cho thiết bị {}, completing future...", deviceNum);
            future.complete(statusMsg);
        } else {
            // Cập nhật trạng thái thiết bị nếu nhận được status ngoài luồng pending
            try {
                String pin = "D" + deviceNum;
                deviceRepository.findAll().stream()
                        .filter(d -> (d.getPinGpio() != null && d.getPinGpio().equalsIgnoreCase(pin))
                                || (d.getDeviceName() != null && d.getDeviceName().contains(String.valueOf(deviceNum)))
                                || d.getId().toString().endsWith("0" + deviceNum))
                        .findFirst()
                        .ifPresent(d -> {
                            d.setCurrentStatus(DeviceStatus.valueOf(normalizedStatus));
                            d.setLastActiveAt(OffsetDateTime.now());
                            deviceRepository.save(d);
                            log.info("[STATUS UPDATE] Đã cập nhật trạng thái thiết bị {} sang {}", d.getDeviceName(), normalizedStatus);
                        });
            } catch (Exception e) {
                log.warn("Không thể cập nhật trạng thái thiết bị ngoài luồng: {}", e.getMessage());
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<DeviceHistory> searchDeviceHistory(int page, int pageSize, DynamicSearchRequest request) {
        GenericSpecification<DeviceHistory> spec = new GenericSpecification<>();
        if (request != null && request.getFilters() != null) {
            request.getFilters().forEach(spec::add);
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (request != null && request.getSortBy() != null) {
            Sort.Direction direction = "ASC".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, request.getSortBy());
        }

        int safePage = Math.max(0, page - 1);
        int safePageSize = pageSize <= 0 ? 10 : pageSize;
        Pageable pageable = PageRequest.of(safePage, safePageSize, sort);
        return deviceHistoryRepository.findAll(spec, pageable);
    }
}