package com.app.common.config;

import com.app.modules.device.dto.DeviceAckMessage;
import com.app.modules.device.dto.DeviceStatusMessage;
import com.app.modules.device.service.DeviceService;
import com.app.modules.sensor.dto.TelemetryMessage;
import com.app.modules.sensor.service.HardwareWatchdogService;
import com.app.modules.sensor.service.SensorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
public class MqttCallbackHandler implements MqttCallbackExtended {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SensorService sensorService;
    private final HardwareWatchdogService hardwareWatchdogService;
    private final DeviceService deviceService;

    @Setter
    private Consumer<Boolean> onConnectCompleteCallback;

    public MqttCallbackHandler(
            SensorService sensorService,
            HardwareWatchdogService hardwareWatchdogService,
            @Lazy DeviceService deviceService) {
        this.sensorService = sensorService;
        this.hardwareWatchdogService = hardwareWatchdogService;
        this.deviceService = deviceService;
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        log.info("[MQTT] Kết nối Mosquitto Broker THÀNH CÔNG (reconnect={}): {}", reconnect, serverURI);
        if (onConnectCompleteCallback != null) {
            try {
                onConnectCompleteCallback.accept(reconnect);
            } catch (Exception e) {
                log.error("[MQTT] Lỗi callback connectComplete: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("[MQTT] Mất kết nối tới Mosquitto Broker: {}", cause != null ? cause.getMessage() : "Unknown reason");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        log.info("[MQTT INBOUND] Topic: [{}] | Payload: {}", topic, payload);

        try {
            hardwareWatchdogService.recordHeartbeat();

            if ("sensor/data".equals(topic)) {
                TelemetryMessage telemetry = objectMapper.readValue(payload, TelemetryMessage.class);
                sensorService.processIncomingTelemetry(telemetry);
            } else if (topic.startsWith("device/ack/")) {
                log.info("[MQTT DEVICE ACK] Topic: {} | Payload: {}", topic, payload);
                DeviceAckMessage ack = objectMapper.readValue(payload, DeviceAckMessage.class);
                deviceService.handleHardwareAck(ack);
            } else if ("device/status".equals(topic) || topic.startsWith("device/status/")) {
                log.info("[MQTT DEVICE STATUS] Topic: {} | Payload: {}", topic, payload);
                DeviceStatusMessage statusMsg;
                try {
                    statusMsg = objectMapper.readValue(payload, DeviceStatusMessage.class);
                } catch (Exception e) {
                    statusMsg = new DeviceStatusMessage();
                    statusMsg.setStatus(payload.trim());
                }

                if (statusMsg.getDeviceId() == 0 && topic.startsWith("device/status/")) {
                    try {
                        String idStr = topic.substring("device/status/".length()).trim();
                        statusMsg.setDeviceId(Integer.parseInt(idStr));
                    } catch (Exception ignored) {}
                }

                deviceService.handleHardwareStatus(statusMsg);
            }
        } catch (Exception e) {
            log.error("[MQTT HANDLER ERROR] Lỗi phân tích payload topic [{}]: {}", topic, e.getMessage());
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {}
}