package com.app.common.config;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MqttConfig {

    @Value("${mqtt.broker-url:tcp://localhost:1883}")
    private String brokerUrl;
    @Value("${mqtt.client:Backend_Server}")
    private String clientId;
    @Value("${mqtt.username:}")
    private String username;
    @Value("${mqtt.password:}")
    private String password;
    @Value("${mqtt.keep-alive:60}")
    private int keepAliveInterval;
    @Value("${mqtt.connection-timeout:10}")
    private int connectionTimeout;
    @Value("${mqtt.clean-session:true}")
    private boolean cleanSession;

    @Value("${mqtt.topics.sensor-data:sensor/data}")
    private String sensorDataTopic;
    @Value("${mqtt.topics.device-ack:device/ack/+}")
    private String deviceAckTopic;
    @Value("${mqtt.topics.device-status:device/status}")
    private String deviceStatusTopic;

    private final MqttCallbackHandler mqttCallbackHandler;
    private MqttClient mqttClient;

    @Bean
    public MqttClient mqttClient() throws MqttException {
        this.mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
        return this.mqttClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startMqttClient() {
        if (mqttClient == null) {
            return;
        }

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(cleanSession);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(connectionTimeout);
        options.setKeepAliveInterval(keepAliveInterval);
        if (username != null && !username.isBlank()) {
            options.setUserName(username);
        }
        if (password != null && !password.isBlank()) {
            options.setPassword(password.toCharArray());
        }

        mqttClient.setCallback(mqttCallbackHandler);
        try {
            if (!mqttClient.isConnected()) {
                mqttClient.connect(options);
                log.info(">>> [MQTT] Kết nối Mosquitto Broker THÀNH CÔNG: {}", brokerUrl);
                mqttClient.subscribe(sensorDataTopic, 1);
                mqttClient.subscribe(deviceAckTopic, 1);
                mqttClient.subscribe(deviceStatusTopic, 1);
                log.info(">>> [MQTT] Đã Subscribe: [{}], [{}], [{}]", sensorDataTopic, deviceAckTopic, deviceStatusTopic);
            }
        } catch (MqttException e) {
            log.error(">>> [MQTT] Lỗi kết nối Broker: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
                log.info(">>> [MQTT] Đã ngắt kết nối Mosquitto Broker thành công.");
            }
        } catch (MqttException e) {
            log.error(">>> [MQTT] Lỗi khi ngắt kết nối Broker: {}", e.getMessage());
        }
    }
}
