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
    private final MqttPublisher mqttPublisher;

    private final Map<Integer, CompletableFuture<DeviceStatusMessage>> pendingRequests = new ConcurrentHashMap<>();

    private int mapDeviceUuidToNumber(UUID deviceId) {
        String str = deviceId.toString();
        if (str.endsWith("01")) return 1;
        if (str.endsWith("02")) return 2;
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

    @Transactional
    public DeviceControlResponse controlDevice(UUID deviceId, DeviceControlRequest request) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị: " + deviceId));

        ActionType action = ActionType.valueOf(request.getAction().toUpperCase());
        int deviceNum = mapDeviceUuidToNumber(deviceId);

        // 1. Tạo bản ghi PENDING vào database
        DeviceHistory history = DeviceHistory.builder()
                .device(device)
                .action(action)
                .status(ActionStatus.PENDING)
                .source("WEB_DASHBOARD")
                .build();
        history = deviceHistoryRepository.save(history);

        CompletableFuture<DeviceStatusMessage> future = new CompletableFuture<>();
        pendingRequests.put(deviceNum, future);
        long startTime = System.currentTimeMillis();

        // 2. Pha 1: Gửi lệnh device/control/{id}
        String controlTopic = "device/control/" + deviceNum;
        String controlPayload = String.format("{\"action\":\"%s\"}", action.name());
        mqttPublisher.publish(controlTopic, controlPayload);
        log.info("[2-PHASE] [Pha 1] Đã gửi lệnh điều khiển tới {}: {}", controlTopic, controlPayload);

        try {
            // Chờ phản hồi tối đa 5000ms
            DeviceStatusMessage statusMsg = future.get(5000, TimeUnit.MILLISECONDS);
            long executionTimeMs = System.currentTimeMillis() - startTime;

            // 3. Pha 2 thành công -> Cập nhật SUCCESS vào DB
            device.setCurrentStatus(DeviceStatus.valueOf(statusMsg.getStatus()));
            device.setLastActiveAt(OffsetDateTime.now());
            deviceRepository.save(device);

            history.setStatus(ActionStatus.SUCCESS);
            history.setExecutionTimeMs((int) executionTimeMs);
            deviceHistoryRepository.save(history);

            log.info("[2-PHASE] Điều khiển thiết bị {} THÀNH CÔNG trong {} ms", device.getDeviceName(), executionTimeMs);

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
            log.error("[2-PHASE ERROR] Timeout điều khiển thiết bị {}: {}", device.getDeviceName(), e.getMessage());

            history.setStatus(ActionStatus.ERROR);
            history.setErrorMessage("Thiết bị không phản hồi trong 5000ms (Gateway Timeout)");
            deviceHistoryRepository.save(history);

            throw new DeviceTimeoutException("Thiết bị không phản hồi trong thời gian quy định (Timeout 5s)");
        } finally {
            pendingRequests.remove(deviceNum);
        }
    }

    public void handleHardwareAck(DeviceAckMessage ack) {
        int deviceNum = ack.getDeviceId();
        log.info("[2-PHASE] [Pha 1 ACK] Nhận ACK từ thiết bị {}. Gửi lệnh Confirm...", deviceNum);
        String confirmTopic = "device/confirm/" + deviceNum;
        mqttPublisher.publish(confirmTopic, "{\"action\":\"CONFIRM\"}");
    }

    public void handleHardwareStatus(DeviceStatusMessage statusMsg) {
        int deviceNum = statusMsg.getDeviceId();
        log.info("[2-PHASE] [Pha 2 STATUS] Nhận trạng thái từ thiết bị {}: {}", deviceNum, statusMsg.getStatus());
        CompletableFuture<DeviceStatusMessage> future = pendingRequests.get(deviceNum);
        if (future != null) {
            future.complete(statusMsg);
        }
    }

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

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize, sort);
        return deviceHistoryRepository.findAll(spec, pageable);
    }
}