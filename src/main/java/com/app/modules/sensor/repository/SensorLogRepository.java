package com.app.modules.sensor.repository;

import com.app.modules.sensor.entity.SensorLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SensorLogRepository extends JpaRepository<SensorLog, UUID>, JpaSpecificationExecutor<SensorLog> {
    Optional<SensorLog> findFirstBySensorIdOrderByRecordedAtDesc(UUID sensorId);

    List<SensorLog> findBySensorIdOrderByRecordedAtDesc(UUID sensorId, Pageable pageable);

    @Query(value = "SELECT * FROM sensor_logs WHERE sensor_id = :sensorId ORDER BY recorded_at DESC LIMIT :limit", nativeQuery = true)
    List<SensorLog> findRecentLogsBySensorId(@Param("sensorId") UUID sensorId, @Param("limit") int limit);
}
