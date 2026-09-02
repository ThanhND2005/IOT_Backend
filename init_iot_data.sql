-- ====================================================================
-- KỊCH BẢN KHỞI TẠO DỮ LIỆU IOT CHO POSTGRESQL
-- Tương thích hoàn toàn với file: hardware.ino & Spring Boot Backend
-- ====================================================================

-- 1. Đảm bảo extension UUID có sẵn
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- --------------------------------------------------------------------
-- 2. TẠO HOẶC CẬP NHẬT TÀI KHOẢN QUẢN TRỊ (USER ADMIN)
-- Password mặc định: Admin@123 (đã mã hóa BCrypt)
-- --------------------------------------------------------------------
INSERT INTO users (
    id, username, email, password_hash, full_name, phone_number,
    student_code, avatar_url, github_url, figma_url, system_doc_url, api_doc_url,
    role, status, is_deleted, created_at, updated_at
) VALUES (
    1,
    'admin',
    'b23dccn772@ptit.edu.vn',
    '$2a$10$7R0Z4b0V78sZg7K8kX9h1u0.sU2p.bQ9O/Kz7uD1Kz7uD1Kz7uD1K', -- Admin@123
    'Nguyễn Danh Thành',
    '0901234567',
    'B23DCCN772',
    'https://api.dicebear.com/7.x/avataaars/svg?seed=NguyenDanhThanh&backgroundColor=b6e3f4',
    'https://github.com/your-github/iot-project',
    'https://www.figma.com/your-figma-link',
    'https://docs.google.com/document/your-system-doc',
    'http://localhost:8080/swagger-ui/index.html',
    'ROLE_ADMIN',
    'ACTIVE',
    false,
    NOW(),
    NOW()
) ON CONFLICT (id) DO UPDATE SET
    full_name = EXCLUDED.full_name,
    student_code = EXCLUDED.student_code,
    avatar_url = EXCLUDED.avatar_url,
    github_url = EXCLUDED.github_url,
    figma_url = EXCLUDED.figma_url,
    system_doc_url = EXCLUDED.system_doc_url,
    api_doc_url = EXCLUDED.api_doc_url,
    updated_at = NOW();

-- Cập nhật sequence cho bảng users nếu cần
SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE((SELECT MAX(id) FROM users), 1));


-- --------------------------------------------------------------------
-- 3. THÊM DANH MỤC THIẾT BỊ (DEVICES) DỰA THEO hardware.ino
-- hardware.ino:
--   Device 1: Pin D1 (LED1_PIN)
--   Device 2: Pin D2 (LED2_PIN)
-- Lưu ý: Backend mapDeviceUuidToNumber dùng đuôi '01' cho Device 1 và '02' cho Device 2
-- --------------------------------------------------------------------
INSERT INTO devices (
    id, device_name, device_type, pin_gpio, current_status, description, user_id, last_active_at, created_at, updated_at
) VALUES 
(
    'd1a2b3c4-0001-0001-0001-000000000001',
    'Đèn LED 1',
    'LED',
    'D1',
    'OFF',
    'Đèn LED điều khiển thử nghiệm số 1 (Pin D1 trên ESP8266)',
    1,
    NOW() - INTERVAL '1 hour',
    NOW() - INTERVAL '1 day',
    NOW()
),
(
    'd1a2b3c4-0002-0002-0002-000000000002',
    'Đèn LED 2',
    'LED',
    'D2',
    'OFF',
    'Đèn LED điều khiển thử nghiệm số 2 (Pin D2 trên ESP8266)',
    1,
    NOW() - INTERVAL '30 minutes',
    NOW() - INTERVAL '1 day',
    NOW()
)
ON CONFLICT (id) DO UPDATE SET
    device_name = EXCLUDED.device_name,
    pin_gpio = EXCLUDED.pin_gpio,
    description = EXCLUDED.description;


-- --------------------------------------------------------------------
-- 4. THÊM DANH MỤC CẢM BIẾN (SENSORS) DỰA THEO hardware.ino
-- hardware.ino:
--   Cảm biến DHT11 (Nhiệt độ & Độ ẩm) cắm ở chân D4
--   Cảm biến Quang trở LDR (Ánh sáng) cắm ở chân Analog A0
-- --------------------------------------------------------------------
INSERT INTO sensors (
    id, sensor_name, sensor_type, pin_gpio, unit, min_threshold, max_threshold, status, description, user_id, created_at
) VALUES 
(
    'a1b2c3d4-0001-0001-0001-000000000001',
    'Cảm biến nhiệt độ DHT11',
    'TEMPERATURE',
    'D4',
    '°C',
    0.00,
    50.00,
    'ACTIVE',
    'Cảm biến DHT11 đo nhiệt độ môi trường (Chân D4 trên ESP8266)',
    1,
    NOW() - INTERVAL '1 day'
),
(
    'a1b2c3d4-0002-0002-0002-000000000002',
    'Cảm biến độ ẩm DHT11',
    'HUMIDITY',
    'D4',
    '%',
    20.00,
    90.00,
    'ACTIVE',
    'Cảm biến DHT11 đo độ ẩm không khí (Chân D4 trên ESP8266)',
    1,
    NOW() - INTERVAL '1 day'
),
(
    'a1b2c3d4-0003-0003-0003-000000000003',
    'Cảm biến ánh sáng LDR',
    'LIGHT',
    'A0',
    'lux',
    0.00,
    1000.00,
    'ACTIVE',
    'Cảm biến quang trở LDR đo độ sáng phòng (Chân Analog A0)',
    1,
    NOW() - INTERVAL '1 day'
)
ON CONFLICT (sensor_type) DO UPDATE SET
    sensor_name = EXCLUDED.sensor_name,
    pin_gpio = EXCLUDED.pin_gpio,
    min_threshold = EXCLUDED.min_threshold,
    max_threshold = EXCLUDED.max_threshold,
    description = EXCLUDED.description;


-- --------------------------------------------------------------------
-- 5. THÊM NHẬT KÝ ĐO CẢM BIẾN MẪU (SENSOR LOGS)
-- Để Dashboard khởi tạo biểu đồ và SensorDataPage có dữ liệu sẵn
-- --------------------------------------------------------------------
INSERT INTO sensor_logs (id, sensor_id, "value", unit, recorded_at)
SELECT 
    gen_random_uuid(),
    'a1b2c3d4-0001-0001-0001-000000000001', -- Nhiệt độ
    ROUND((26.0 + (random() * 4.0))::numeric, 1),
    '°C',
    NOW() - (n * INTERVAL '10 seconds')
FROM generate_series(0, 30) AS n;

INSERT INTO sensor_logs (id, sensor_id, "value", unit, recorded_at)
SELECT 
    gen_random_uuid(),
    'a1b2c3d4-0002-0002-0002-000000000002', -- Độ ẩm
    ROUND((60.0 + (random() * 15.0))::numeric, 1),
    '%',
    NOW() - (n * INTERVAL '10 seconds')
FROM generate_series(0, 30) AS n;

INSERT INTO sensor_logs (id, sensor_id, "value", unit, recorded_at)
SELECT 
    gen_random_uuid(),
    'a1b2c3d4-0003-0003-0003-000000000003', -- Ánh sáng
    ROUND((300.0 + (random() * 250.0))::numeric, 0),
    'lux',
    NOW() - (n * INTERVAL '10 seconds')
FROM generate_series(0, 30) AS n;


-- --------------------------------------------------------------------
-- 6. THÊM LỊCH SỬ ĐIỀU KHIỂN THIẾT BỊ MẪU (DEVICE HISTORY)
-- Để DeviceHistoryPage có sẵn dữ liệu hiển thị trạng thái SUCCESS / ERROR
-- --------------------------------------------------------------------
INSERT INTO device_history (
    id, device_id, action, status, source, execution_time_ms, error_message, created_at, updated_at
) VALUES
(
    gen_random_uuid(),
    'd1a2b3c4-0001-0001-0001-000000000001',
    'ON',
    'SUCCESS',
    'WEB_DASHBOARD',
    145,
    NULL,
    NOW() - INTERVAL '45 minutes',
    NOW() - INTERVAL '45 minutes'
),
(
    gen_random_uuid(),
    'd1a2b3c4-0001-0001-0001-000000000001',
    'OFF',
    'SUCCESS',
    'WEB_DASHBOARD',
    182,
    NULL,
    NOW() - INTERVAL '30 minutes',
    NOW() - INTERVAL '30 minutes'
),
(
    gen_random_uuid(),
    'd1a2b3c4-0002-0002-0002-000000000002',
    'ON',
    'SUCCESS',
    'WEB_DASHBOARD',
    210,
    NULL,
    NOW() - INTERVAL '20 minutes',
    NOW() - INTERVAL '20 minutes'
),
(
    gen_random_uuid(),
    'd1a2b3c4-0002-0002-0002-000000000002',
    'OFF',
    'SUCCESS',
    'WEB_DASHBOARD',
    160,
    NULL,
    NOW() - INTERVAL '10 minutes',
    NOW() - INTERVAL '10 minutes'
),
(
    gen_random_uuid(),
    'd1a2b3c4-0001-0001-0001-000000000001',
    'ON',
    'ERROR',
    'WEB_DASHBOARD',
    5000,
    'Thiết bị không phản hồi trong 5000ms (Gateway Timeout)',
    NOW() - INTERVAL '5 minutes',
    NOW() - INTERVAL '5 minutes'
);
