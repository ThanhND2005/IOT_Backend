package com.app.modules.sensor.controller;

import com.app.common.base.search.dto.DynamicSearchRequest;
import com.app.common.base.search.dto.SearchParam;
import com.app.common.base.search.enums.SearchDataType;
import com.app.common.base.search.enums.SearchOperation;
import com.app.modules.sensor.dto.TelemetryMessage;
import com.app.modules.sensor.service.SensorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SensorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SensorService sensorService;

    @Test
    @DisplayName("GET /api/v1/sensor - Lấy danh mục cảm biến mặc định")
    void testGetAllSensors() throws Exception {
        mockMvc.perform(get("/api/v1/sensor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    @DisplayName("GET /api/v1/sensors/latest & GET /api/v1/sensors/chart-history sau khi ingest telemetry")
    void testTelemetryIngestionAndQueries() throws Exception {
        // 1. Giả lập đẩy dữ liệu đo telemetry qua MQTT handler / service
        TelemetryMessage msg = TelemetryMessage.builder()
                .temperature(BigDecimal.valueOf(28.5))
                .humidity(BigDecimal.valueOf(65.0))
                .light(BigDecimal.valueOf(420.0))
                .build();
        sensorService.processIncomingTelemetry(msg);

        // 2. Kiểm tra snapshot mới nhất
        mockMvc.perform(get("/api/v1/sensors/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.temperature.value", is(28.5)))
                .andExpect(jsonPath("$.data.humidity.value", is(65.0)))
                .andExpect(jsonPath("$.data.light.value", is(420.0)));

        // 3. Kiểm tra biểu đồ lịch sử
        mockMvc.perform(get("/api/v1/sensors/chart-history").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.series", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    @DisplayName("POST /api/v1/sensors/data - Tìm kiếm dữ liệu cảm biến phân trang")
    void testSearchSensorLogs() throws Exception {
        TelemetryMessage msg = TelemetryMessage.builder()
                .temperature(BigDecimal.valueOf(31.2))
                .humidity(BigDecimal.valueOf(70.5))
                .light(BigDecimal.valueOf(500.0))
                .build();
        sensorService.processIncomingTelemetry(msg);

        DynamicSearchRequest request = DynamicSearchRequest.builder()
                .filters(List.of(
                        SearchParam.builder()
                                .field("unit")
                                .value("°C")
                                .operate(SearchOperation.EQUAL)
                                .type(SearchDataType.STRING)
                                .build()
                ))
                .sortBy("recordedAt")
                .sortDirection("DESC")
                .build();

        mockMvc.perform(post("/api/v1/sensors/data")
                        .param("page", "1")
                        .param("pageSize", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.items", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.items[0].unit", is("°C")));
    }
}