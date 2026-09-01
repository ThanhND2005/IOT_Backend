package com.app.modules.device.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceControlResponse {
    private UUID actionId;
    private UUID deviceId;
    private String deviceName;
    private String action;
    private String status;
    private Integer executionTimeMs;
    private OffsetDateTime confirmedAt;
}