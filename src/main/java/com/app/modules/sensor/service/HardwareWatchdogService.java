package com.app.modules.sensor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class HardwareWatchdogService {
    private final SseEmitterService sseEmitterService;
    private final AtomicLong lastHeartbeatTime = new AtomicLong(System.currentTimeMillis());
    private final AtomicBoolean isHardwareConnected = new AtomicBoolean(true);

    public void recordHeartbeat() {
        lastHeartbeatTime.set(System.currentTimeMillis());
        if (!isHardwareConnected.get()) {
            isHardwareConnected.set(true);
            log.info("[WATCHDOG] Thiết bị phần cứng đã KẾT NỐI LẠI!");
            sseEmitterService.broadcastEvent("HARDWARE_STATUS", Map.of(
                    "status", "connected",
                    "message", "Thiết bị phần cứng đã kết nối lại",
                    "last_seen", OffsetDateTime.now().toString()
            ));
        }
    }
    @Scheduled(fixedRate = 2000)
    public void checkHardwareConnection() {
        long elapsed = System.currentTimeMillis() - lastHeartbeatTime.get();
        if (elapsed > 10000 && isHardwareConnected.get()) {
            isHardwareConnected.set(false);
            log.warn("[WATCHDOG] MẤT KẾT NỐI PHẦN CỨNG! Không nhận được dữ liệu trong {} ms", elapsed);
            sseEmitterService.broadcastEvent("HARDWARE_STATUS", Map.of(
                    "status", "disconnected",
                    "message", "Không nhận được dữ liệu từ thiết bị trong 10 giây",
                    "last_seen", OffsetDateTime.now().minusSeconds(elapsed / 1000).toString()
            ));
        }
    }
}
