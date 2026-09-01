package com.app.modules.device.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceResponse {
    private UUID id;
    private String deviceName;
    private String deviceType;
    private String pinGpio;
    private String currentStatus;
    private OffsetDateTime lastActiveAt;
}