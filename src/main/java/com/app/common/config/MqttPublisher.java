package com.app.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;


@Slf4j
@Component
@RequiredArgsConstructor
public class MqttPublisher {
    private final MqttClient mqttClient;
    public void publish(String topic, String payload) {
        try {
            if(!mqttClient.isConnected()){
                mqttClient.reconnect();
            }
            MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            message.setQos(1);
            mqttClient.publish(topic,message);
            log.info("[MQTT PUBLISH] Topic: {} | Payload: {}", topic, payload);
        }
        catch (Exception e){
            log.error("[MQTT PUBLISH ERROR] Lỗi gửi message tới {}: {}", topic, e.getMessage());
        }
    }
}
