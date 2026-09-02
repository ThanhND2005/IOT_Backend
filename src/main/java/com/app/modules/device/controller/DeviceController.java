package com.app.modules.device.controller;

import com.app.common.base.BaseResponse;
import com.app.common.base.PageResponse;
import com.app.common.base.search.dto.DynamicSearchRequest;
import com.app.modules.device.dto.DeviceControlRequest;
import com.app.modules.device.dto.DeviceControlResponse;
import com.app.modules.device.dto.DeviceHistoryItemResponse;
import com.app.modules.device.dto.DeviceResponse;
import com.app.modules.device.entity.DeviceHistory;
import com.app.modules.device.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/devices", "/api/v1/devices"})
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    // 1. GET /api/v1/devices
    @GetMapping
    public ResponseEntity<BaseResponse<List<DeviceResponse>>> getAllDevices() {
        List<DeviceResponse> list = deviceService.getAllDevices();
        return ResponseEntity.ok(BaseResponse.ok("Lấy danh sách thiết bị thành công", list));
    }

    // 2. POST /api/v1/devices/control/{id}
    @PostMapping("/control/{id}")
    public ResponseEntity<BaseResponse<DeviceControlResponse>> controlDevice(
            @PathVariable("id") UUID deviceId,
            @Valid @RequestBody DeviceControlRequest request) {

        DeviceControlResponse response = deviceService.controlDevice(deviceId, request);
        return ResponseEntity.ok(BaseResponse.created("Điều khiển thiết bị thành công", response));
    }

    // 3. POST /api/v1/devices/history
    @PostMapping("/history")
    public ResponseEntity<BaseResponse<PageResponse<DeviceHistoryItemResponse>>> searchDeviceHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestBody(required = false) DynamicSearchRequest request) {

        Page<DeviceHistory> resultPage = deviceService.searchDeviceHistory(page, pageSize, request);

        PageResponse<DeviceHistoryItemResponse> pageResponse = PageResponse.from(resultPage, h ->
                DeviceHistoryItemResponse.builder()
                        .id(h.getId())
                        .deviceId(h.getDevice().getId())
                        .deviceName(h.getDevice().getDeviceName())
                        .action(h.getAction().name())
                        .status(h.getStatus().name())
                        .source(h.getSource())
                        .executionTimeMs(h.getExecutionTimeMs())
                        .errorMessage(h.getErrorMessage())
                        .userId(h.getDevice().getUser().getId())
                        .fullName(h.getDevice().getUser().getFullName())
                        .createdAt(h.getCreatedAt())
                        .updatedAt(h.getUpdatedAt())
                        .build()
        );

        return ResponseEntity.ok(BaseResponse.ok("Lấy lịch sử điều khiển thiết bị thành công", pageResponse));
    }
}

