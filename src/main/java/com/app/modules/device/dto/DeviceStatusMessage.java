package com.app.modules.device.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceStatusMessage {
    @JsonAlias({"id", "device_id", "deviceId"})
    private int deviceId;

    @JsonAlias({"status", "action", "state"})
    private String status;
}
