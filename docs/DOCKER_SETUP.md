# Spring Profiles & Docker Setup Guide

## 📋 Mục Lục

1. [Spring Profiles Explained](#spring-profiles-explained)
2. [Docker Setup - Step by Step](#docker-setup---step-by-step)
3. [Troubleshooting](#troubleshooting)

---

## 🔧 Spring Profiles Explained

### Application Profiles là gì?

Spring Boot cho phép bạn có **nhiều configurations** cho các môi trường khác nhau:
- **Development** (dev) - Khi code trên máy local
- **Production** (prod) - Khi deploy lên server thật

### Cấu Trúc Files

```
src/main/resources/
├── application.yml          # ❶ Base config (chung cho tất cả)
├── application-dev.yml      # ❷ Development config
└── application-prod.yml     # ❸ Production config
```

---

### ❶ application.yml - Base Configuration

**File này chứa config CHUNG cho tất cả environments:**

```yaml
spring:
  application:
    name: auth-template
  
  profiles:
    active: ${SPRING_PROFILE:dev}  # ← Mặc định dùng 'dev'

server:
  port: 8080  # Port chung

springdoc:
  swagger-ui:
    path: /swagger-ui.html  # Swagger UI path
```

**Giải thích:**
- `profiles.active: ${SPRING_PROFILE:dev}` 
  - Đọc từ environment variable `SPRING_PROFILE`
  - Nếu không có → dùng `dev` (default)
  - Có thể override: `SPRING_PROFILE=prod`

---

### ❷ application-dev.yml - Development Config

**File này chứa config CHỈ cho development:**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:auth_db_dev}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
  
  jpa:
    hibernate:
      ddl-auto: update  # ← Tự động tạo/update tables
    show-sql: true      # ← Show SQL queries trong log
  
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    # ... có default values

jwt:
  secret: ${JWT_SECRET:YourSecretKeyHere...}  # ← Có default (unsafe)

logging:
  level:
    com.auth.template: DEBUG  # ← Debug logging
```

**Đặc điểm Development:**
- ✅ **Default values** - Chạy được ngay không cần config
- ✅ **Auto-create tables** - `ddl-auto: update`
- ✅ **Show SQL** - Debug dễ hơn
- ✅ **Debug logging** - Xem chi tiết
- ✅ **Relaxed security** - JWT secret có default

---

### ❸ application-prod.yml - Production Config

**File này chứa config CHỈ cho production:**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}  # ← KHÔNG có default
    hikari:
      maximum-pool-size: 10   # ← Connection pooling
  
  jpa:
    hibernate:
      ddl-auto: validate  # ← KHÔNG tự động tạo tables
    show-sql: false       # ← Không show SQL
  
  mail:
    host: ${MAIL_HOST}  # ← PHẢI có environment variable

jwt:
  secret: ${JWT_SECRET}  # ← PHẢI có, không có default

logging:
  level:
    com.auth.template: INFO  # ← Chỉ INFO level
  file:
    name: /var/log/auth-template/application.log  # ← Log to file
```

**Đặc điểm Production:**
- ❌ **NO default values** - Bắt buộc phải config
- ❌ **NO auto-create** - `ddl-auto: validate`
- ❌ **NO SQL logging** - Performance
- ✅ **Connection pooling** - Scale tốt hơn
- ✅ **File logging** - Persistent logs
- ✅ **Strict security** - Phải có JWT_SECRET

---

### Environment Variables

**Cách Spring Boot đọc config:**

```yaml
url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:auth_db_dev}
```

**Syntax:** `${VARIABLE_NAME:default_value}`

**Ví dụ:**
- `${DB_HOST:localhost}` 
  - Đọc env var `DB_HOST`
  - Nếu không có → dùng `localhost`
- `${DB_HOST}` (không có `:default`)
  - Đọc env var `DB_HOST`
  - Nếu không có → **ERROR** (app không start)

---

## 🐳 Docker Setup - Step by Step

### Prerequisites

```bash
# Check Docker installed
docker --version
docker-compose --version

# Nếu chưa có, install Docker Desktop
```

---

### Step 1: Hiểu Docker Compose File

**File:** `docker-compose.yml`

```yaml
version: '3.8'

services:
  # ❶ PostgreSQL Database
  postgres:
    image: postgres:15-alpine
    container_name: auth-template-db
    environment:
      POSTGRES_DB: mydb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  # ❷ Spring Boot Application
  app:
    build: .
    container_name: spring-boot-auth-template
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      DB_HOST: postgres  # ← Tên service
      DB_PORT: 5432
      DB_NAME: mydb
      DB_USERNAME: postgres
      DB_PASSWORD: postgres
      JWT_SECRET: ${JWT_SECRET}
      FRONTEND_URL: ${FRONTEND_URL:-http://localhost:3000}
      MAIL_HOST: ${MAIL_HOST}
      MAIL_PORT: ${MAIL_PORT}
      MAIL_USERNAME: ${MAIL_USERNAME}
      MAIL_PASSWORD: ${MAIL_PASSWORD}
    depends_on:
      postgres:
        condition: service_healthy  # ← Đợi DB ready
    restart: unless-stopped

volumes:
  postgres_data:  # ← Persistent storage
```

**Giải thích:**

**❶ PostgreSQL Service:**
- `image: postgres:15-alpine` - Dùng PostgreSQL 15
- `ports: "5432:5432"` - Map port host:container
- `volumes` - Lưu data persistent (không mất khi restart)
- `healthcheck` - Check DB ready chưa

**❷ App Service:**
- `build: .` - Build từ Dockerfile
- `depends_on` - Đợi postgres ready
- `environment` - Pass env vars vào container
- `DB_HOST: postgres` - Dùng tên service (Docker DNS)

---

### Step 2: Tạo .env File

```bash
# Copy template
cp .env.example .env

# Edit .env
nano .env
```

**Nội dung .env:**

```env
# Spring Profile
SPRING_PROFILES_ACTIVE=dev

# Database (Docker sẽ tự tạo)
DB_HOST=postgres
DB_PORT=5432
DB_NAME=auth_db
DB_USERNAME=postgres
DB_PASSWORD=postgres

# JWT Secret (QUAN TRỌNG!)
JWT_SECRET=YourVerySecureSecretKeyHereShouldBeAtLeast256BitsLongChangeThisInProduction

# Frontend URL
FRONTEND_URL=http://localhost:3000

# Email (Optional cho dev)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200
```

**Lưu ý:**
- **JWT_SECRET**: Generate secure key (xem bước 3)
- **MAIL_***: Không bắt buộc cho dev, nhưng cần cho email verification
- **DB_HOST=postgres**: Dùng tên service trong docker-compose

---

### Step 3: Generate JWT Secret

```bash
# Option 1: OpenSSL
openssl rand -base64 64

# Option 2: UUID
uuidgen | base64

# Copy output và paste vào .env
```

**Output example:**
```
YourVeryLongRandomSecretKeyHere1234567890abcdefghijklmnopqrstuvwxyz
```

---

### Step 4: Build & Run với Docker Compose

```bash
# Bước 1: Build application JAR
./mvnw clean package -DskipTests

# Bước 2: Build Docker images
docker-compose build

# Bước 3: Start all services
docker-compose up -d

# -d = detached mode (chạy background)
```

**Output:**
```
Creating network "auth-template_default" ...
Creating auth-template-db ... done
Creating auth-template-app ... done
```

---

### Step 5: Verify Services Running

```bash
# Check containers
docker-compose ps

# Expected output:
NAME                  STATUS              PORTS
auth-template-db      Up (healthy)        0.0.0.0:5432->5432/tcp
auth-template-app     Up                  0.0.0.0:8080->8080/tcp
```

---

### Step 6: View Logs

```bash
# All services
docker-compose logs -f

# Only app
docker-compose logs -f app

# Only database
docker-compose logs -f postgres

# Last 100 lines
docker-compose logs --tail=100 app
```

**Look for:**
```
Started AuthTemplateApplication in 5.123 seconds
```

---

### Step 7: Test Application

```bash
# Health check
curl http://localhost:8080/actuator/health

# Expected:
{"status":"UP"}

# Swagger UI
open http://localhost:8080/swagger-ui.html

# Test registration
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test@123",
    "fullName": "Test User"
  }'
```

---

### Step 8: Access Database (Optional)

```bash
# Connect to PostgreSQL
docker exec -it auth-template-db psql -U postgres -d auth_db

# Inside psql:
\dt                    # List tables
SELECT * FROM users;   # View users
\q                     # Quit
```

---

### Step 9: Stop Services

```bash
# Stop (giữ data)
docker-compose stop

# Stop và remove containers (giữ data)
docker-compose down

# Stop, remove containers VÀ XÓA DATA
docker-compose down -v
```

---

## 🔄 Common Docker Commands

### Restart Services

```bash
# Restart all
docker-compose restart

# Restart only app
docker-compose restart app

# Rebuild and restart
docker-compose up -d --build
```

### View Container Info

```bash
# Container stats (CPU, Memory)
docker stats

# Inspect container
docker inspect auth-template-app

# Execute command in container
docker exec -it auth-template-app bash
```

### Clean Up

```bash
# Remove stopped containers
docker-compose rm

# Remove all unused images
docker image prune -a

# Remove all unused volumes
docker volume prune
```

---

## 🐛 Troubleshooting

### Issue 1: Port Already in Use

**Error:**
```
Error: bind: address already in use
```

**Solution:**
```bash
# Find process using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>

# Or change port in docker-compose.yml
ports:
  - "8081:8080"  # Use different host port
```

---

### Issue 2: Database Connection Failed

**Error:**
```
Connection to localhost:5432 refused
```

**Solution:**
```bash
# Check postgres running
docker-compose ps postgres

# Check logs
docker-compose logs postgres

# Restart postgres
docker-compose restart postgres

# Wait for healthcheck
docker-compose up -d
```

---

### Issue 3: App Keeps Restarting

**Check logs:**
```bash
docker-compose logs app
```

**Common causes:**
1. **Missing env vars** - Check .env file
2. **Database not ready** - Wait for healthcheck
3. **Port conflict** - Change port
4. **Invalid JWT_SECRET** - Check .env

**Solution:**
```bash
# Verify .env
cat .env

# Check all env vars passed
docker exec auth-template-app env | grep -E "DB_|JWT_"

# Restart with fresh build
docker-compose down
docker-compose up -d --build
```

---

### Issue 4: Cannot Access Swagger UI

**Check:**
```bash
# App running?
curl http://localhost:8080/actuator/health

# Swagger endpoint
curl http://localhost:8080/swagger-ui.html
```

**Solution:**
- Wait for app to fully start (check logs)
- Clear browser cache
- Try http://localhost:8080/swagger-ui/index.html

---

## 📝 Quick Reference

### Start Fresh

```bash
# Complete reset
docker-compose down -v
./mvnw clean package -DskipTests
docker-compose up -d --build

# Wait and check
docker-compose logs -f app
```

### Update Code

```bash
# After code changes
./mvnw clean package -DskipTests
docker-compose up -d --build app
```

### Switch Profiles

```bash
# Edit .env
SPRING_PROFILES_ACTIVE=prod

# Restart
docker-compose restart app
```

---

## 🎯 Summary

**Application Profiles:**
- `application.yml` - Base config
- `application-dev.yml` - Development (relaxed, defaults)
- `application-prod.yml` - Production (strict, no defaults)

**Docker Setup:**
1. Copy `.env.example` → `.env`
2. Generate JWT_SECRET
3. `./mvnw clean package -DskipTests`
4. `docker-compose up -d`
5. Check logs: `docker-compose logs -f`
6. Test: `curl http://localhost:8080/actuator/health`

**Key Points:**
- ✅ Dev profile có defaults → dễ start
- ✅ Prod profile strict → bảo mật
- ✅ Docker Compose quản lý cả DB + App
- ✅ Data persistent trong volumes
- ✅ Healthcheck đảm bảo DB ready

---

**Bây giờ bạn hiểu rõ profiles và có thể start project với Docker! 🚀**
