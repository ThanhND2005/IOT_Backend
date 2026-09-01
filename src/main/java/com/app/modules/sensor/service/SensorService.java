package com.app.modules.sensor.service;

import com.app.common.base.search.dto.DynamicSearchRequest;
import com.app.common.base.search.dto.SearchParam;
import com.app.common.base.search.service.GenericSpecification;
import com.app.common.enums.SensorType;
import com.app.modules.sensor.dto.ChartHistoryResponse;
import com.app.modules.sensor.dto.SensorLatestSnapshotResponse;
import com.app.modules.sensor.dto.SensorResponse;
import com.app.modules.sensor.dto.TelemetryMessage;
import com.app.modules.sensor.entity.SensorLog;
import com.app.modules.sensor.repository.SensorLogRepository;
import com.app.modules.sensor.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorService {
    private final SensorRepository sensorRepository;
    private final SensorLogRepository sensorLogRepository;
    private final SseEmitterService sseEmitterService;

    @Transactional(readOnly = true)
    public List<SensorResponse> getAllSensors() {
        return sensorRepository.findAll().stream().map(s -> SensorResponse.builder()
                .id(s.getId())
                .sensorName(s.getSensorName())
                .sensorType(s.getSensorType() != null ? s.getSensorType().name() : null)
                .pinGpio(s.getPinGpio())
                .unit(s.getUnit())
                .minThreshold(s.getMinThreshold())
                .maxThreshold(s.getMaxThreshold())
                .status(s.getStatus())
                .build()).toList();
    }

    @Transactional
    public void processIncomingTelemetry(TelemetryMessage telemetry) {
        if (telemetry == null) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();

        // 1. Lưu Temperature
        if (telemetry.getTemperature() != null) {
            saveMetric(SensorType.TEMPERATURE, telemetry.getTemperature(), "°C", now);
        }
        // 2. Lưu Humidity
        if (telemetry.getHumidity() != null) {
            saveMetric(SensorType.HUMIDITY, telemetry.getHumidity(), "%", now);
        }
        // 3. Lưu Light
        if (telemetry.getLight() != null) {
            saveMetric(SensorType.LIGHT, telemetry.getLight(), "lux", now);
        }

        // 4. Broadcast SSE SENSOR_METRICS_UPDATE
        Map<String, Object> ssePayload = new HashMap<>();
        if (telemetry.getTemperature() != null) {
            ssePayload.put("temperature", Map.of("value", telemetry.getTemperature(), "unit", "°C"));
        }
        if (telemetry.getHumidity() != null) {
            ssePayload.put("humidity", Map.of("value", telemetry.getHumidity(), "unit", "%"));
        }
        if (telemetry.getLight() != null) {
            ssePayload.put("light", Map.of("value", telemetry.getLight(), "unit", "lux"));
        }
        ssePayload.put("timestamp", now.toString());

        sseEmitterService.broadcastEvent("SENSOR_METRICS_UPDATE", ssePayload);
    }

    private void saveMetric(SensorType type, BigDecimal value, String unit, OffsetDateTime now) {
        if (value == null) {
            return;
        }
        sensorRepository.findBySensorType(type).ifPresent(sensor -> {
            SensorLog logItem = SensorLog.builder()
                    .sensor(sensor)
                    .value(value)
                    .unit(unit)
                    .recordedAt(now)
                    .build();
            sensorLogRepository.save(logItem);
        });
    }

    @Transactional(readOnly = true)
    public SensorLatestSnapshotResponse getLatestSnapshot() {
        Map<String, Object> snapshotMap = new HashMap<>();
        for (SensorType type : SensorType.values()) {
            sensorRepository.findBySensorType(type).ifPresent(s -> {
                sensorLogRepository.findFirstBySensorIdOrderByRecordedAtDesc(s.getId()).ifPresent(log -> {
                    Map<String, Object> metricData = new HashMap<>();
                    metricData.put("sensorId", s.getId());
                    metricData.put("sensorName", s.getSensorName());
                    metricData.put("value", log.getValue());
                    metricData.put("unit", log.getUnit());
                    metricData.put("status", s.getStatus());
                    metricData.put("minThreshold", s.getMinThreshold());
                    metricData.put("maxThreshold", s.getMaxThreshold());
                    metricData.put("recordedAt", log.getRecordedAt());
                    snapshotMap.put(type.name().toLowerCase(), metricData);
                });
            });
        }
        return new SensorLatestSnapshotResponse(snapshotMap);
    }

    @Transactional(readOnly = true)
    public ChartHistoryResponse getChartHistory(int limit) {
        List<ChartHistoryResponse.SeriesItem> seriesList = new ArrayList<>();
        List<String> timestamps = new ArrayList<>();
        int safeLimit = limit <= 0 ? 20 : limit;

        for (SensorType type : SensorType.values()) {
            sensorRepository.findBySensorType(type).ifPresent(s -> {
                List<SensorLog> logs = sensorLogRepository.findBySensorIdOrderByRecordedAtDesc(s.getId(), PageRequest.of(0, safeLimit));
                List<SensorLog> sortedLogs = new ArrayList<>(logs);
                Collections.reverse(sortedLogs);

                List<BigDecimal> dataPoints = sortedLogs.stream().map(SensorLog::getValue).toList();
                seriesList.add(ChartHistoryResponse.SeriesItem.builder()
                        .name(s.getSensorName())
                        .sensorId(s.getId())
                        .unit(s.getUnit())
                        .data(dataPoints)
                        .build());

                if (timestamps.isEmpty() && !sortedLogs.isEmpty()) {
                    timestamps.addAll(sortedLogs.stream().map(l -> l.getRecordedAt().toString()).toList());
                }
            });
        }

        return ChartHistoryResponse.builder()
                .timestamps(timestamps)
                .series(seriesList)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<SensorLog> searchSensorLogs(int page, int pageSize, DynamicSearchRequest request) {
        List<SearchParam> params = (request != null && request.getFilters() != null) ? request.getFilters() : Collections.emptyList();
        GenericSpecification<SensorLog> spec = new GenericSpecification<>(params);

        Sort sort = Sort.by(Sort.Direction.DESC, "recordedAt");
        if (request != null && request.getSortBy() != null && !request.getSortBy().isBlank()) {
            Sort.Direction direction = "ASC".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, request.getSortBy());
        }

        int safePage = Math.max(0, page - 1);
        int safePageSize = pageSize <= 0 ? 10 : pageSize;
        Pageable pageable = PageRequest.of(safePage, safePageSize, sort);
        return sensorLogRepository.findAll(spec, pageable);
    }
}
