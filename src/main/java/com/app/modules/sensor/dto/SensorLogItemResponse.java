package com.app.modules.sensor.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorLogItemResponse {
    private UUID id;
    private UUID sensorId;
    private String sensorName;
    private String sensorType;
    private BigDecimal value;
    private String unit;
    private OffsetDateTime recordedAt;
}
