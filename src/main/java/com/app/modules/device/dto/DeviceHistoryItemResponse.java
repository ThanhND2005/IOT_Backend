package com.app.modules.device.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceHistoryItemResponse {
    private UUID id;
    private UUID deviceId;
    private String deviceName;
    private String action;
    private String status;
    private String source;
    private Integer executionTimeMs;
    private String errorMessage;
    private Long userId;
    private String fullName;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}