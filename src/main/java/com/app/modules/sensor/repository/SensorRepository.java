package com.app.modules.sensor.repository;

import com.app.common.enums.SensorType;
import com.app.modules.sensor.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, UUID> {
    Optional<Sensor> findFirstBySensorType(SensorType sensorType);
    Optional<Sensor> findBySensorType(SensorType sensorType);
}
