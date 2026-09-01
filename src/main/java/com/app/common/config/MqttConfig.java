package com.app.common.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MqttConfig {
    @Value("${mqtt.broker-url:tcp://localhost:1883}")
    private String brokerUrl;
    @Value("${mqtt.client:Backend_Server}")
    private String clientId;
    @Value("${mqtt.topics.sensor-data:sensor/data}")
    private String sensorDataTopic;
    @Value("${mqtt.topics.device-ack:device/ack/+}")
    private String deviceAckTopic;
    @Value("${mqtt.topics.device-status:device/status}")
    private String deviceStatusTopic;

    private final MqttCallbackHandler mqttCallbackHandler;

    @Bean
    public MqttClient mqttClient() throws MqttException {
        MqttClient client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(60);

        client.setCallback(mqttCallbackHandler);
        try {
            client.connect(options);
            log.info(">>> [MQTT] Kết nối Mosquitto Broker THÀNH CÔNG: {}", brokerUrl);
            client.subscribe(sensorDataTopic, 1);
            client.subscribe(deviceAckTopic, 1);
            client.subscribe(deviceStatusTopic, 1);
            log.info(">>> [MQTT] Đã Subscribe: [{}], [{}], [{}]", sensorDataTopic, deviceAckTopic, deviceStatusTopic);
        } catch (MqttException e) {
            log.error(">>> [MQTT] Lỗi kết nối Broker: {}", e.getMessage());
        }
        return client;
    }
}
