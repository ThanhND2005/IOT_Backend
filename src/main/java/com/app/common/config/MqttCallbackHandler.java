package com.app.common.config;

import com.app.modules.sensor.dto.TelemetryMessage;
import com.app.modules.sensor.service.HardwareWatchdogService;
import com.app.modules.sensor.service.SensorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqttCallbackHandler implements MqttCallback {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SensorService sensorService;
    private final HardwareWatchdogService hardwareWatchdogService;

    public MqttCallbackHandler(
            SensorService sensorService,
            HardwareWatchdogService hardwareWatchdogService) {
        this.sensorService = sensorService;
        this.hardwareWatchdogService = hardwareWatchdogService;
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
            } else if ("device/status".equals(topic)) {
                log.info("[MQTT DEVICE STATUS] Topic: {} | Payload: {}", topic, payload);
            }
        } catch (Exception e) {
            log.error("[MQTT HANDLER ERROR] Lỗi phân tích payload topic [{}]: {}", topic, e.getMessage());
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {}
}