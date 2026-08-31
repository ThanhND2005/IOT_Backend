# 🚀 Base Spring Boot - Modular Monolith Architecture with JWT

Dự án mẫu Spring Boot 3.4+ thiết kế theo kiến trúc **Modular Monolith**, tích hợp sẵn xác thực **JWT (JSON Web Token)**, chuẩn hóa **BaseResponse**, **BaseEntity**, JPA Auditing, Swagger OpenAPI 3 UI, xử lý ngoại lệ tập trung (Global Exception Handling) và cấu hình sẵn prefix `/api/v1`.

---

## 🏗️ Cấu Trúc Dự Án (Modular Monolith)

```
com.app
│
├── BaseSpringApplication.java              # Main Spring Boot Application
│
├── shared/                                 # Shared Kernel (Core dùng chung toàn hệ thống)
│   ├── base/
│   │   ├── BaseEntity.java                 # Entity cha: id, createdAt, updatedAt, createdBy, updatedBy, isDeleted
│   │   ├── BaseResponse.java               # Chuẩn hóa payload trả về (success, code, message, data, errors, timestamp)
│   │   ├── PageResponse.java               # Chuẩn hóa phản hồi phân trang (Pageable)
│   │   └── AuditorAwareImpl.java           # Lấy username người dùng hiện tại cho JPA Auditing
│   │
│   ├── config/
│   │   ├── AppConfig.java                  # PasswordEncoder (BCrypt), AuditorAware bean
│   │   ├── WebMvcConfig.java               # Cấu hình tiền tố toàn cục /api/v1 & CORS
│   │   ├── OpenApiConfig.java              # Swagger / OpenAPI 3 + cấu hình Bearer Token
│   │   └── JpaAuditingConfig.java          # Kích hoạt JPA Auditing tự động
│   │
│   ├── exception/
│   │   ├── ErrorCode.java                  # Quản lý mã lỗi nghiệp vụ tập trung
│   │   ├── AppException.java               # Base runtime exception
│   │   ├── ResourceNotFoundException.java  # Lỗi không tìm thấy dữ liệu (404)
│   │   ├── UnauthorizedException.java      # Lỗi chưa xác thực / Token hết hạn (401)
│   │   └── GlobalExceptionHandler.java     # @RestControllerAdvice xử lý lỗi tập trung
│   │
│   └── security/
│       ├── SecurityConfig.java             # Spring Security 6+ cấu hình Stateless, CSRF disable, Filter chain
│       ├── JwtService.java                 # Sinh & xác thực Access Token, Refresh Token (JJWT 0.12.x)
│       ├── JwtAuthenticationFilter.java    # OncePerRequestFilter bắt Header "Authorization: Bearer <token>"
│       ├── JwtAuthenticationEntryPoint.java# Trả về JSON 401 theo chuẩn BaseResponse
│       ├── JwtAccessDeniedHandler.java     # Trả về JSON 403 theo chuẩn BaseResponse
│       ├── CustomUserDetailsService.java   # Load UserDetails từ Database
│       ├── UserPrincipal.java              # Custom UserDetails chứa thông tin context người dùng
│       └── SecurityUtils.java              # Tiện ích lấy User/UserId/Username hiện tại trong Security Context
│
└── modules/                                # Các Module nghiệp vụ độc lập
    ├── auth/                               # Module Xác thực (Authentication & Authorization)
    │   ├── controller/AuthController.java  # /api/v1/auth/login, /register, /refresh, /me
    │   ├── dto/
    │   │   ├── LoginRequest.java
    │   │   ├── RegisterRequest.java
    │   │   ├── RefreshTokenRequest.java
    │   │   └── AuthResponse.java
    │   └── service/
    │       ├── AuthService.java
    │       └── impl/AuthServiceImpl.java
    │
    └── user/                               # Module Quản lý người dùng
        ├── entity/
        │   ├── User.java                   # User entity kế thừa BaseEntity
        │   ├── Role.java                   # ROLE_USER, ROLE_ADMIN, ROLE_MANAGER
        │   └── UserStatus.java             # ACTIVE, INACTIVE, BLOCKED
        ├── repository/UserRepository.java
        ├── dto/UserResponse.java
        ├── service/
        │   ├── UserService.java
        │   └── impl/UserServiceImpl.java
        ├── controller/UserController.java  # /api/v1/users (Chỉ ADMIN), /api/v1/users/{id}
        └── init/UserDataInitializer.java   # Khởi tạo sẵn tài khoản mẫu (admin & user)
```

---

## 🛠️ Công Nghệ & Thư Viện Sử Dụng

- **Java**: 17+ (tương thích Java 21, 23)
- **Spring Boot**: 3.4.2
- **Spring Security 6+**: Cơ chế Stateless JWT Authentication & Role-based Authorization.
- **JJWT (io.jsonwebtoken)**: 0.12.6
- **Spring Data JPA & Hibernate**: Tích hợp Auditing tự động.
- **H2 In-memory Database**: Sẵn sàng chạy ngay không cần cài đặt database ngoài (dễ dàng đổi sang PostgreSQL / MySQL trong `application.yml`).
- **SpringDoc OpenAPI 3 / Swagger UI**: 2.8.5 (có nút Authorize nhập Bearer Token).
- **Lombok**: Giảm thiểu boilerplate code.
- **Jakarta Bean Validation**: Kiểm tra tính hợp lệ của Request payload.

---

## 🔑 Tài Khoản Khởi Tạo Mặc Định (Seed Data)

Khi khởi động ứng dụng lần đầu, hệ thống sẽ tự động tạo sẵn 2 tài khoản:

| Username | Password | Email | Vai trò (Role) | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| `admin` | `Admin@123` | `admin@example.com` | `ROLE_ADMIN` | `ACTIVE` |
| `user` | `User@123` | `user@example.com` | `ROLE_USER` | `ACTIVE` |

---

## 🔌 Danh Sách API Sẵn Có (Prefix `/api/v1`)

### 1. Module Auth (`/api/v1/auth`)

| Method | Endpoint | Quyền | Mô tả |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/login` | Public | Đăng nhập bằng username/email + mật khẩu để nhận Access Token & Refresh Token |
| `POST` | `/api/v1/auth/register` | Public | Đăng ký tài khoản người dùng mới |
| `POST` | `/api/v1/auth/refresh` | Public | Làm mới Access Token bằng Refresh Token |
| `GET` | `/api/v1/auth/me` | Bearer Token | Lấy thông tin tài khoản hiện tại |

### 2. Module User (`/api/v1/users`)

| Method | Endpoint | Quyền | Mô tả |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/users` | `ROLE_ADMIN` | Lấy danh sách người dùng phân trang (`page`, `size`, `sort`) |
| `GET` | `/api/v1/users/{id}` | Authenticated | Lấy chi tiết thông tin người dùng theo ID |

---

## 📦 Định Dạng Phản Hồi Chuẩn (`BaseResponse<T>`)

Mọi phản hồi từ API đều tuân theo cấu trúc JSON thống nhất:

### Phản hồi thành công:
```json
{
  "success": true,
  "code": 200,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "username": "admin",
      "email": "admin@example.com",
      "fullName": "Quản trị viên",
      "role": "ROLE_ADMIN",
      "status": "ACTIVE",
      "createdAt": "2026-08-15 16:30:00",
      "updatedAt": "2026-08-15 16:30:00"
    }
  },
  "timestamp": "2026-08-15 16:30:00"
}
```

### Phản hồi lỗi nghiệp vụ hoặc validation:
```json
{
  "success": false,
  "code": 400,
  "message": "Dữ liệu đầu vào không hợp lệ",
  "errors": {
    "password": "Mật khẩu phải từ 6 đến 100 ký tự",
    "email": "Email không đúng định dạng"
  },
  "timestamp": "2026-08-15 16:30:00"
}
```

---

## 🚀 Hướng Dẫn Chạy Ứng Dụng

### 1. Khởi chạy Server
Trong thư mục dự án, chạy lệnh:
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / MacOS
./mvnw spring-boot:run
```

Server sẽ khởi chạy tại cổng: `http://localhost:8080`

### 2. Swagger UI (OpenAPI 3)
Truy cập giao diện tài liệu API tương tác:
👉 **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

> Nhấn nút **Authorize** ở góc phải và nhập Token theo định dạng `{token}` sau khi gọi API `/api/v1/auth/login` để test các API yêu cầu xác thực.

### 3. H2 Database Console
Truy cập giao diện quản lý cơ sở dữ liệu in-memory:
👉 **[http://localhost:8080/h2-console](http://localhost:8080/h2-console)**
- **JDBC URL**: `jdbc:h2:mem:basedb`
- **User Name**: `sa`
- **Password**: *(để trống)*

### 4. Chạy Unit & Integration Test
```bash
.\mvnw.cmd test
```
