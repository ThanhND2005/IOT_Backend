package com.app.modules.sensor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelemetryMessage {
    private BigDecimal temperature;
    private BigDecimal humidity;
    private BigDecimal light;
}
