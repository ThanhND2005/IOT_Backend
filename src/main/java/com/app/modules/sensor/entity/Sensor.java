package com.app.modules.sensor.entity;

import com.app.common.enums.SensorType;
import com.app.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "sensors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sensor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "sensor_name", nullable = false, length = 100)
    private String sensorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_type", nullable = false, length = 30)
    private SensorType sensorType; // TEMPERATURE, HUMIDITY, LIGHT

    @Column(name = "pin_gpio", length = 20)
    private String pinGpio;

    @Column(nullable = false, length = 20)
    private String unit; // °C, %, lux

    @Column(name = "min_threshold", precision = 8, scale = 2)
    private BigDecimal minThreshold;

    @Column(name = "max_threshold", precision = 8, scale = 2)
    private BigDecimal maxThreshold;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
