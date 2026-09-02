package com.app.modules.user.init;

import com.app.common.enums.SensorType;
import com.app.modules.sensor.entity.Sensor;
import com.app.modules.sensor.repository.SensorRepository;
import com.app.modules.user.entity.Role;
import com.app.modules.user.entity.User;
import com.app.modules.user.entity.UserStatus;
import com.app.modules.user.repository.UserRepository;
import com.app.common.enums.DeviceStatus;
import com.app.common.enums.DeviceType;
import com.app.modules.device.entity.Device;
import com.app.modules.device.repository.DeviceRepository;
import com.app.modules.sensor.entity.SensorLog;
import com.app.modules.sensor.repository.SensorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Initializes default sample users, sensors, and devices on startup.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SensorRepository sensorRepository;
    private final SensorLogRepository sensorLogRepository;
    private final DeviceRepository deviceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        User admin = null;
        if (!userRepository.existsByUsername("admin")) {
            admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("Admin@123"))
                    .email("admin@example.com")
                    .fullName("Quản trị viên")
                    .phoneNumber("0901234567")
                    .studentCode("B21DCCN001")
                    .role(Role.ROLE_ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();

            admin = userRepository.save(admin);
            log.info(">>> Initialized default ADMIN account: username='admin', password='Admin@123'");
        } else {
            admin = userRepository.findByUsername("admin").orElse(null);
        }

        if (!userRepository.existsByUsername("user")) {
            User normalUser = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("User@123"))
                    .email("user@example.com")
                    .fullName("Người dùng mẫu")
                    .phoneNumber("0912345678")
                    .studentCode("B21DCCN002")
                    .role(Role.ROLE_USER)
                    .status(UserStatus.ACTIVE)
                    .build();

            userRepository.save(normalUser);
            log.info(">>> Initialized default USER account: username='user', password='User@123'");
        }

        if (admin != null) {
            boolean updated = false;
            if (admin.getAvatarUrl() == null) {
                admin.setAvatarUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=NguyenDanhThanh&backgroundColor=b6e3f4");
                updated = true;
            }
            if (admin.getGithubUrl() == null) {
                admin.setGithubUrl("https://github.com/your-github/iot-project");
                updated = true;
            }
            if (admin.getFigmaUrl() == null) {
                admin.setFigmaUrl("https://www.figma.com/your-figma-link");
                updated = true;
            }
            if (admin.getSystemDocUrl() == null) {
                admin.setSystemDocUrl("https://docs.google.com/document/your-system-doc");
                updated = true;
            }
            if (admin.getApiDocUrl() == null) {
                admin.setApiDocUrl("https://your-postman-api-doc");
                updated = true;
            }
            if (updated) {
                userRepository.save(admin);
            }
        }

        if (admin != null && sensorRepository.count() == 0) {
            Sensor tempSensor = Sensor.builder()
                    .sensorName("Cảm biến nhiệt độ DHT11")
                    .sensorType(SensorType.TEMPERATURE)
                    .pinGpio("D4")
                    .unit("°C")
                    .minThreshold(BigDecimal.valueOf(0))
                    .maxThreshold(BigDecimal.valueOf(50))
                    .status("ACTIVE")
                    .description("Cảm biến DHT11 đo nhiệt độ môi trường (Chân D4 trên ESP8266)")
                    .user(admin)
                    .build();

            Sensor humSensor = Sensor.builder()
                    .sensorName("Cảm biến độ ẩm DHT11")
                    .sensorType(SensorType.HUMIDITY)
                    .pinGpio("D4")
                    .unit("%")
                    .minThreshold(BigDecimal.valueOf(20))
                    .maxThreshold(BigDecimal.valueOf(90))
                    .status("ACTIVE")
                    .description("Cảm biến DHT11 đo độ ẩm không khí (Chân D4 trên ESP8266)")
                    .user(admin)
                    .build();

            Sensor lightSensor = Sensor.builder()
                    .sensorName("Cảm biến ánh sáng LDR")
                    .sensorType(SensorType.LIGHT)
                    .pinGpio("A0")
                    .unit("lux")
                    .minThreshold(BigDecimal.valueOf(0))
                    .maxThreshold(BigDecimal.valueOf(1000))
                    .status("ACTIVE")
                    .description("Cảm biến quang trở LDR đo độ sáng phòng (Chân Analog A0)")
                    .user(admin)
                    .build();

            sensorRepository.save(tempSensor);
            sensorRepository.save(humSensor);
            sensorRepository.save(lightSensor);
            log.info(">>> Initialized 3 default sensors: TEMPERATURE, HUMIDITY, LIGHT");
        }

        if (admin != null && deviceRepository.count() == 0) {
            Device led1 = Device.builder()
                    .deviceName("Đèn LED 1")
                    .deviceType(DeviceType.LED)
                    .pinGpio("D1")
                    .currentStatus(DeviceStatus.OFF)
                    .description("Đèn LED điều khiển thử nghiệm số 1")
                    .user(admin)
                    .build();

            Device led2 = Device.builder()
                    .deviceName("Đèn LED 2")
                    .deviceType(DeviceType.LED)
                    .pinGpio("D2")
                    .currentStatus(DeviceStatus.OFF)
                    .description("Đèn LED điều khiển thử nghiệm số 2")
                    .user(admin)
                    .build();

            deviceRepository.save(led1);
            deviceRepository.save(led2);
            log.info(">>> Initialized 2 default devices: Đèn LED 1, Đèn LED 2");
        }

        if (sensorRepository.count() > 0 && sensorLogRepository.count() == 0) {
            OffsetDateTime now = OffsetDateTime.now();
            sensorRepository.findFirstBySensorType(SensorType.TEMPERATURE).ifPresent(s -> {
                sensorLogRepository.save(SensorLog.builder().sensor(s).value(BigDecimal.valueOf(28.5)).unit("°C").recordedAt(now).build());
            });
            sensorRepository.findFirstBySensorType(SensorType.HUMIDITY).ifPresent(s -> {
                sensorLogRepository.save(SensorLog.builder().sensor(s).value(BigDecimal.valueOf(65.0)).unit("%").recordedAt(now).build());
            });
            sensorRepository.findFirstBySensorType(SensorType.LIGHT).ifPresent(s -> {
                sensorLogRepository.save(SensorLog.builder().sensor(s).value(BigDecimal.valueOf(320.0)).unit("lux").recordedAt(now).build());
            });
            log.info(">>> Initialized initial sensor logs for TEMPERATURE, HUMIDITY, LIGHT");
        }
    }
}
