package com.app.modules.device.service;

import com.app.common.enums.ActionStatus;
import com.app.common.enums.ActionType;
import com.app.common.enums.DeviceStatus;
import com.app.modules.device.entity.Device;
import com.app.modules.device.entity.DeviceHistory;
import com.app.modules.device.repository.DeviceHistoryRepository;
import com.app.modules.device.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceHistoryTxService {

    private final DeviceRepository deviceRepository;
    private final DeviceHistoryRepository deviceHistoryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DeviceHistory createPendingHistory(Device device, ActionType action, String source) {
        DeviceHistory history = DeviceHistory.builder()
                .device(device)
                .action(action)
                .status(ActionStatus.PENDING)
                .source(source != null ? source : "WEB_DASHBOARD")
                .build();
        DeviceHistory saved = deviceHistoryRepository.saveAndFlush(history);
        log.info("[TX-PENDING] Đã lưu và commit ngay trạng thái PENDING cho thiết bị {}: historyId={}",
                device.getDeviceName(), saved.getId());
        return saved;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<DeviceHistory> createPendingHistories(List<Device> devices, ActionType action, String source) {
        List<DeviceHistory> histories = new ArrayList<>();
        for (Device device : devices) {
            histories.add(DeviceHistory.builder()
                    .device(device)
                    .action(action)
                    .status(ActionStatus.PENDING)
                    .source(source != null ? source : "WEB_DASHBOARD")
                    .build());
        }
        List<DeviceHistory> savedList = deviceHistoryRepository.saveAllAndFlush(histories);
        log.info("[TX-PENDING] Đã lưu và commit ngay {} bản ghi PENDING cho tất cả thiết bị", savedList.size());
        return savedList;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccess(UUID historyId, UUID deviceId, String resolvedStatus, int executionTimeMs) {
        deviceRepository.findById(deviceId).ifPresent(dev -> {
            try {
                dev.setCurrentStatus(DeviceStatus.valueOf(resolvedStatus));
            } catch (Exception ignored) {}
            dev.setLastActiveAt(OffsetDateTime.now());
            deviceRepository.saveAndFlush(dev);
        });

        deviceHistoryRepository.findById(historyId).ifPresent(history -> {
            history.setStatus(ActionStatus.SUCCESS);
            history.setExecutionTimeMs(executionTimeMs);
            history.setErrorMessage(null);
            deviceHistoryRepository.saveAndFlush(history);
        });
        log.info("[TX-SUCCESS] Đã cập nhật trạng thái SUCCESS cho historyId={}", historyId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markError(UUID historyId, String errorMessage) {
        deviceHistoryRepository.findById(historyId).ifPresent(history -> {
            history.setStatus(ActionStatus.ERROR);
            history.setErrorMessage(errorMessage);
            deviceHistoryRepository.saveAndFlush(history);
        });
        log.info("[TX-ERROR] Đã cập nhật trạng thái ERROR cho historyId={}", historyId);
    }
}
