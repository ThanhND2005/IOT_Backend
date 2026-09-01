package com.app.modules.sensor.controller;

import com.app.common.base.BaseResponse;
import com.app.common.base.PageResponse;
import com.app.common.base.search.dto.DynamicSearchRequest;
import com.app.modules.sensor.dto.ChartHistoryResponse;
import com.app.modules.sensor.dto.SensorLatestSnapshotResponse;
import com.app.modules.sensor.dto.SensorLogItemResponse;
import com.app.modules.sensor.dto.SensorResponse;
import com.app.modules.sensor.entity.SensorLog;
import com.app.modules.sensor.service.SensorService;
import com.app.modules.sensor.service.SseEmitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Sensor Management", description = "APIs for sensor data, history, and real-time streaming")
public class SensorController {

    private final SensorService sensorService;
    private final SseEmitterService sseEmitterService;

    // 1. GET /api/v1/sensor & /api/v1/sensors
    @GetMapping({"/sensor", "/sensors"})
    @Operation(summary = "Lấy danh mục cảm biến")
    public ResponseEntity<BaseResponse<List<SensorResponse>>> getAllSensors() {
        List<SensorResponse> sensors = sensorService.getAllSensors();
        return ResponseEntity.ok(BaseResponse.ok("Lấy danh mục cảm biến thành công", sensors));
    }

    // 2. GET /api/v1/sensors/latest
    @GetMapping("/sensors/latest")
    @Operation(summary = "Lấy dữ liệu cảm biến mới nhất khởi tạo thẻ đo")
    public ResponseEntity<BaseResponse<SensorLatestSnapshotResponse>> getLatestSnapshot() {
        SensorLatestSnapshotResponse snapshot = sensorService.getLatestSnapshot();
        return ResponseEntity.ok(BaseResponse.ok("Lấy dữ liệu cảm biến mới nhất khởi tạo thẻ đo thành công", snapshot));
    }

    // 3. GET /api/v1/sensors/stream (SSE)
    @GetMapping(value = "/sensors/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Luồng dữ liệu cảm biến SSE theo thời gian thực")
    public SseEmitter streamSensorMetrics() {
        return sseEmitterService.createConnection();
    }

    // 4. GET /api/v1/sensors/chart-history
    @GetMapping("/sensors/chart-history")
    @Operation(summary = "Lấy dữ liệu khởi tạo biểu đồ")
    public ResponseEntity<BaseResponse<ChartHistoryResponse>> getChartHistory(
            @RequestParam(defaultValue = "20") int limit) {
        ChartHistoryResponse chartData = sensorService.getChartHistory(limit);
        return ResponseEntity.ok(BaseResponse.ok("Lấy dữ liệu khởi tạo biểu đồ thành công", chartData));
    }

    // 5. POST /api/v1/sensors/data (Dynamic Search & Pagination)
    @PostMapping("/sensors/data")
    @Operation(summary = "Truy vấn danh sách dữ liệu cảm biến phân trang và lọc động")
    public ResponseEntity<BaseResponse<PageResponse<SensorLogItemResponse>>> searchSensorLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestBody(required = false) DynamicSearchRequest request) {

        Page<SensorLog> resultPage = sensorService.searchSensorLogs(page, pageSize, request);

        PageResponse<SensorLogItemResponse> response = PageResponse.from(resultPage, log -> SensorLogItemResponse.builder()
                .id(log.getId())
                .sensorId(log.getSensor() != null ? log.getSensor().getId() : null)
                .sensorName(log.getSensor() != null ? log.getSensor().getSensorName() : null)
                .sensorType(log.getSensor() != null && log.getSensor().getSensorType() != null ? log.getSensor().getSensorType().name() : null)
                .value(log.getValue())
                .unit(log.getUnit())
                .recordedAt(log.getRecordedAt())
                .build());

        return ResponseEntity.ok(BaseResponse.ok("Truy vấn danh sách dữ liệu cảm biến thành công", response));
    }
}

