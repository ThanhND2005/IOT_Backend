package com.app.modules.sensor.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SensorLatestSnapshotResponse {
    @Builder.Default
    private Map<String, Object> metrics = new HashMap<>();

    public SensorLatestSnapshotResponse(Map<String, Object> metrics) {
        this.metrics = metrics != null ? metrics : new HashMap<>();
    }

    @JsonAnyGetter
    public Map<String, Object> getMetrics() {
        return metrics;
    }

    @JsonAnySetter
    public void setMetric(String name, Object value) {
        if (this.metrics == null) {
            this.metrics = new HashMap<>();
        }
        this.metrics.put(name, value);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MetricSnapshot {
        private UUID sensorId;
        private String sensorName;
        private BigDecimal value;
        private String unit;
        private String status;
        private BigDecimal minThreshold;
        private BigDecimal maxThreshold;
        private OffsetDateTime recordedAt;
    }
}
