package com.app.modules.user.init;

import com.app.common.enums.SensorType;
import com.app.modules.sensor.entity.Sensor;
import com.app.modules.sensor.repository.SensorRepository;
import com.app.modules.user.entity.Role;
import com.app.modules.user.entity.User;
import com.app.modules.user.entity.UserStatus;
import com.app.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Initializes default sample users (Admin and Regular User) and default sensors on startup.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SensorRepository sensorRepository;
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

        if (admin != null && sensorRepository.count() == 0) {
            Sensor tempSensor = Sensor.builder()
                    .sensorName("Cảm biến nhiệt độ DHT22")
                    .sensorType(SensorType.TEMPERATURE)
                    .pinGpio("D4")
                    .unit("°C")
                    .minThreshold(BigDecimal.valueOf(0))
                    .maxThreshold(BigDecimal.valueOf(100))
                    .status("ACTIVE")
                    .description("Cảm biến giám sát nhiệt độ phòng")
                    .user(admin)
                    .build();

            Sensor humSensor = Sensor.builder()
                    .sensorName("Cảm biến độ ẩm DHT22")
                    .sensorType(SensorType.HUMIDITY)
                    .pinGpio("D4")
                    .unit("%")
                    .minThreshold(BigDecimal.valueOf(0))
                    .maxThreshold(BigDecimal.valueOf(100))
                    .status("ACTIVE")
                    .description("Cảm biến giám sát độ ẩm không khí")
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
                    .description("Cảm biến đo cường độ ánh sáng môi trường")
                    .user(admin)
                    .build();

            sensorRepository.save(tempSensor);
            sensorRepository.save(humSensor);
            sensorRepository.save(lightSensor);
            log.info(">>> Initialized 3 default sensors: TEMPERATURE, HUMIDITY, LIGHT");
        }
    }
}
