package com.app.modules.sensor.dto;


import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorResponse {
    private UUID id;
    private String sensorName;
    private String sensorType;
    private String pinGpio;
    private String unit;
    private BigDecimal minThreshold;
    private BigDecimal maxThreshold;
    private String status;
}
