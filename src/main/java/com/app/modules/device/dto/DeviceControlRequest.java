package com.app.modules.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceControlRequest {
    @NotBlank(message = "Hành động điều khiển không được để trống")
    @Pattern(regexp = "(?i)^(ON|OFF)$", message = "Hành động chỉ có thể là ON hoặc OFF")
    private String action; // "ON" hoặc "OFF"
}