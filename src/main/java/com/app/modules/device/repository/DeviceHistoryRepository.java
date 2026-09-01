package com.app.modules.device.repository;

import com.app.modules.device.entity.DeviceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeviceHistoryRepository extends JpaRepository<DeviceHistory, UUID>, JpaSpecificationExecutor<DeviceHistory> {}