# Spring Boot Authentication Template

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📋 Tổng Quan

Template authentication production-ready với Spring Boot, JWT, và OAuth2. Đây là một starting point hoàn chỉnh cho mọi dự án cần authentication.

### ✨ Features

- ✅ **JWT Authentication** - Access & Refresh tokens
- ✅ **Email Verification** - Xác thực email khi đăng ký
- ✅ **Password Reset** - Quên mật khẩu qua email
- ✅ **Account Lockout** - Khóa tài khoản sau 5 lần đăng nhập sai
- ✅ **User Management** - Quản lý profile, đổi mật khẩu
- ✅ **Swagger UI** - API documentation tự động
- ✅ **Exception Handling** - Global error handling
- ✅ **Email Templates** - Thymeleaf email templates
- ✅ **Multi-environment** - Dev & Production configs
- ✅ **Docker Support** - Docker & docker-compose ready
- ✅ **CORS Configured** - Cross-origin support
- ✅ **Logging** - SLF4J logging throughout

### 🏗️ Kiến Trúc

```
com.auth.template/
├── config/              # Configurations
│   ├── security/        # Security configs (JWT, Filter)
│   ├── CorsConfig       # CORS configuration
│   └── SwaggerConfig    # API documentation
├── auth/                # Authentication domain
│   ├── controller/      # Auth endpoints
│   ├── service/         # Auth business logic
│   ├── dto/             # Request/Response DTOs
│   └── entity/          # User, Tokens entities
├── user/                # User management domain
│   ├── controller/      # User endpoints
│   ├── service/         # User business logic
│   └── repository/      # Data access
└── common/              # Shared utilities
    ├── exception/       # Exception handling
    ├── constant/        # Constants
    └── util/            # Utility classes
```

---

## 🚀 Quick Start

### Prerequisites

- Java 21+
- PostgreSQL 15+
- Maven 3.8+
- (Optional) Docker & Docker Compose

### 1. Clone & Setup

```bash
# Clone repository
git clone <your-repo-url>
cd spring-boot-auth-template

# Copy environment variables
cp .env.example .env

# Edit .env với thông tin của bạn
nano .env
```

### 2. Configure Database

**Option A: Local PostgreSQL**

```sql
CREATE DATABASE mydb;
```

**Option B: Docker**

```bash
docker-compose up -d postgres
```

### 3. Configure JWT Secret

```bash
# Generate secure secret
openssl rand -base64 64

# Add to .env
JWT_SECRET=<generated-secret>
```

### 4. Run Application

**Option A: Maven**

```bash
./mvnw spring-boot:run
```

**Option B: Docker Compose**

```bash
# Build và run
./mvnw clean package -DskipTests
docker-compose up --build
```

Application sẽ chạy tại: `http://localhost:8080`

Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 📚 API Documentation

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Đăng ký user mới | No |
| POST | `/api/auth/login` | Đăng nhập | No |
| POST | `/api/auth/refresh` | Refresh access token | No |
| GET | `/api/auth/verify-email?token=xxx` | Xác thực email | No |
| POST | `/api/auth/forgot-password` | Yêu cầu reset password | No |
| POST | `/api/auth/reset-password` | Reset password với token | No |

### User Management Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/users/me` | Lấy thông tin user hiện tại | Yes |
| PUT | `/api/users/me` | Cập nhật profile | Yes |
| PUT | `/api/users/me/password` | Đổi mật khẩu | Yes |
| DELETE | `/api/users/me` | Xóa tài khoản | Yes |

### Example Requests

#### 1. Register

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "SecurePass123",
    "fullName": "John Doe"
  }'
```

#### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "SecurePass123"
  }'
```

Response:
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "username": "johndoe",
  "email": "john@example.com"
}
```

#### 3. Access Protected Endpoint

```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

---

## ⚙️ Configuration

### Environment Variables

Xem file `.env.example` để biết tất cả environment variables cần thiết.

**Quan trọng:**
- `JWT_SECRET` - PHẢI thay đổi trong production
- `DB_PASSWORD` - Sử dụng password mạnh
- `MAIL_*` - Configure email service (Gmail, SendGrid, etc.)

### Profiles

- **dev** - Development (default)
  - H2/Local PostgreSQL
  - Debug logging
  - Relaxed CORS

- **prod** - Production
  - Strict validation
  - Error logging only
  - Secure CORS

Chạy với profile:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

---

## 📧 Email Configuration

### Gmail Setup

1. Enable 2-factor authentication
2. Generate App Password: https://myaccount.google.com/apppasswords
3. Add to `.env`:

```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

### Email Templates

Templates nằm trong `src/main/resources/templates/email/`:
- `verification-email.html` - Email xác thực
- `password-reset-email.html` - Email reset password
- `welcome-email.html` - Email chào mừng

Customize templates theo brand của bạn.

---

## 🔒 Security Features

### Password Requirements

- Minimum 8 characters
- At least 1 uppercase letter
- At least 1 lowercase letter
- At least 1 number

### Account Lockout

- Tài khoản bị khóa sau 5 lần đăng nhập sai
- Tự động unlock sau 30 phút
- Email notification (optional)

### JWT Tokens

- **Access Token**: 1 hour expiration
- **Refresh Token**: 24 hours expiration
- Signed with HMAC-SHA256
- Issuer & Audience validation

---

## 🐳 Docker Deployment

### Build Image

```bash
./mvnw clean package -DskipTests
docker build -t auth-template:latest .
```

### Run with Docker Compose

```bash
docker-compose up -d
```

Services:
- **app**: http://localhost:8080
- **postgres**: localhost:5432

### Production Deployment

```bash
# Set production environment variables
export SPRING_PROFILES_ACTIVE=prod
export JWT_SECRET=<your-secure-secret>
export DB_PASSWORD=<your-db-password>

# Run
docker-compose -f docker-compose.prod.yml up -d
```

---

## 📖 Hướng Dẫn Sử Dụng

### 1. Customize Package Name

Đổi `com.auth.template` thành package name của bạn:

```bash
# Find and replace
find src -type f -name "*.java" -exec sed -i 's/com.auth.template/com.yourcompany.yourapp/g' {} +

# Rename directories
mv src/main/java/com/auth/template src/main/java/com/yourcompany/yourapp
```

### 2. Add New Endpoints

**Example: Add Admin Controller**

```java
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Admin endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        // Implementation
    }
}
```

### 3. Add OAuth2 Provider

Xem `implementation_plan.md` trong artifacts để biết cách thêm OAuth2 (Google, GitHub, Facebook).

### 4. Customize Email Templates

Edit files trong `src/main/resources/templates/email/` với HTML/CSS của bạn.

---

## 🧪 Testing

### Run Tests

```bash
./mvnw test
```

### Integration Testing

```bash
# Start test database
docker-compose -f docker-compose.test.yml up -d

# Run integration tests
./mvnw verify
```

---

## 📊 Monitoring

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

### Metrics

Add Spring Boot Actuator endpoints trong `application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

---

## 🔧 Troubleshooting

### Common Issues

**1. Build Failure**
```bash
# Clean và rebuild
./mvnw clean install -DskipTests
```

**2. Database Connection Error**
- Check PostgreSQL đang chạy
- Verify credentials trong `.env`
- Check port 5432 không bị conflict

**3. Email Not Sending**
- Verify SMTP credentials
- Check firewall/security groups
- Enable "Less secure app access" (Gmail)

**4. JWT Token Invalid**
- Verify JWT_SECRET match giữa services
- Check token expiration
- Ensure clock sync

---

## 📝 Best Practices

### Production Checklist

- [ ] Change JWT_SECRET to secure random value
- [ ] Use environment variables for all secrets
- [ ] Enable HTTPS/TLS
- [ ] Configure proper CORS origins
- [ ] Set up database connection pooling
- [ ] Configure logging levels
- [ ] Add rate limiting
- [ ] Set up monitoring & alerts
- [ ] Configure backup strategy
- [ ] Review security configurations
- [ ] Add health checks
- [ ] Configure reverse proxy (Nginx)

### Code Quality

- [ ] Write unit tests (target: 80%+ coverage)
- [ ] Write integration tests
- [ ] Use SonarQube for code analysis
- [ ] Follow SOLID principles
- [ ] Document complex logic
- [ ] Use meaningful variable names
- [ ] Keep methods small and focused

---

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- Spring Boot Team
- JWT.io
- Thymeleaf Team
- PostgreSQL Community

---

## 📞 Support

- 📧 Email: support@authtemplate.com
- 📖 Documentation: [Full Docs](docs/)
- 🐛 Issues: [GitHub Issues](https://github.com/yourrepo/issues)

---

**Made with ❤️ for the Spring Boot community**
