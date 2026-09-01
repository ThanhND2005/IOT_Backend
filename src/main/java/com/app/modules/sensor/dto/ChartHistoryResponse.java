package com.app.modules.sensor.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChartHistoryResponse {
    private List<String> timestamps;
    private List<SeriesItem> series;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeriesItem {
        private String name;
        private UUID sensorId;
        private String unit;
        private List<BigDecimal> data;
    }
}
