# Troubleshooting Guide

## 📋 Common Issues & Solutions

### 🔐 Authentication Issues

#### Issue: "Invalid JWT signature"

**Symptoms:**
- 401 Unauthorized error
- Error message: "JWT signature validation failed"

**Causes:**
1. JWT_SECRET mismatch between services
2. Token was modified
3. Token generated with different secret

**Solutions:**
```bash
# 1. Verify JWT_SECRET is same everywhere
echo $JWT_SECRET

# 2. Generate new secret if needed
openssl rand -base64 64

# 3. Update .env and restart
docker-compose restart app

# 4. Clear old tokens and login again
```

---

#### Issue: "Token has expired"

**Symptoms:**
- 401 Unauthorized
- Error: "JWT token has expired"

**Solutions:**
```bash
# 1. Use refresh token to get new access token
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "your-refresh-token"}'

# 2. If refresh token also expired, login again
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "pass"}'
```

---

#### Issue: "Account is locked"

**Symptoms:**
- 403 Forbidden
- Error: "Account is locked due to multiple failed login attempts"

**Solutions:**
```sql
-- Option 1: Wait 30 minutes for auto-unlock

-- Option 2: Manually unlock in database
UPDATE users 
SET account_locked = false, 
    failed_login_attempts = 0, 
    lock_time = NULL 
WHERE username = 'locked_user';
```

---

### 💾 Database Issues

#### Issue: "Connection refused" to PostgreSQL

**Symptoms:**
- Application won't start
- Error: "Connection to localhost:5432 refused"

**Solutions:**
```bash
# 1. Check if PostgreSQL is running
docker ps | grep postgres
# or
sudo systemctl status postgresql

# 2. Start PostgreSQL
docker-compose up -d postgres
# or
sudo systemctl start postgresql

# 3. Verify connection
psql -h localhost -U postgres -d auth_db_dev

# 4. Check credentials in .env
cat .env | grep DB_
```

---

#### Issue: "Database does not exist"

**Symptoms:**
- Error: "database \"auth_db_dev\" does not exist"

**Solutions:**
```bash
# 1. Create database
docker exec -it auth-template-db psql -U postgres -c "CREATE DATABASE auth_db_dev;"

# Or connect and create manually
psql -h localhost -U postgres
CREATE DATABASE auth_db_dev;
\q

# 2. Restart application
docker-compose restart app
```

---

#### Issue: "Table doesn't exist"

**Symptoms:**
- Error: "relation \"users\" does not exist"

**Solutions:**
```yaml
# 1. Enable auto-create in application-dev.yml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # or create

# 2. Or run migration manually
# Create tables using schema.sql if provided

# 3. Restart application
```

---

### 📧 Email Issues

#### Issue: "Failed to send email"

**Symptoms:**
- Registration works but no verification email
- Logs show "Failed to send verification email"

**Solutions:**
```bash
# 1. Check SMTP credentials
echo $MAIL_USERNAME
echo $MAIL_PASSWORD

# 2. Test SMTP connection
telnet smtp.gmail.com 587

# 3. For Gmail, use App Password
# Go to: https://myaccount.google.com/apppasswords
# Generate new app password
# Update MAIL_PASSWORD in .env

# 4. Check firewall/security groups
# Ensure port 587 is open

# 5. Enable debug logging
# In application-dev.yml:
logging:
  level:
    org.springframework.mail: DEBUG
```

---

#### Issue: "Email sent but not received"

**Symptoms:**
- No errors in logs
- Email not in inbox or spam

**Solutions:**
```bash
# 1. Check email logs
docker-compose logs app | grep -i email

# 2. Verify email address
# Check if email is correct in database
SELECT email FROM users WHERE username = 'testuser';

# 3. Check spam folder

# 4. Use email testing service
# For development, use MailHog or Mailtrap
# Update MAIL_HOST to localhost:1025
```

---

### 🐳 Docker Issues

#### Issue: "Port already in use"

**Symptoms:**
- Error: "Bind for 0.0.0.0:8080 failed: port is already allocated"

**Solutions:**
```bash
# 1. Find process using port
lsof -i :8080
# or
netstat -tulpn | grep 8080

# 2. Kill process
kill -9 <PID>

# 3. Or change port in docker-compose.yml
ports:
  - "8081:8080"  # Use different host port
```

---

#### Issue: "Container keeps restarting"

**Symptoms:**
- `docker ps` shows container restarting
- Application logs show errors

**Solutions:**
```bash
# 1. View logs
docker logs auth-template

# 2. Check for common issues:
# - Database connection
# - Missing environment variables
# - Invalid configuration

# 3. Run container interactively
docker run -it --rm \
  --env-file .env \
  auth-template:1.0.0 \
  /bin/sh

# 4. Check inside container
java -jar app.jar --debug
```

---

### 🔧 Build Issues

#### Issue: "BUILD FAILURE" - Compilation errors

**Symptoms:**
- Maven build fails
- Compilation errors

**Solutions:**
```bash
# 1. Clean and rebuild
./mvnw clean install -DskipTests

# 2. Update dependencies
./mvnw dependency:purge-local-repository

# 3. Check Java version
java -version  # Should be 21+

# 4. Clear Maven cache
rm -rf ~/.m2/repository

# 5. Rebuild
./mvnw clean install
```

---

#### Issue: "Tests failing"

**Symptoms:**
- Build fails during test phase
- Test errors in logs

**Solutions:**
```bash
# 1. Skip tests temporarily
./mvnw clean package -DskipTests

# 2. Run specific test
./mvnw test -Dtest=AuthServiceTest

# 3. Check test database
# Ensure H2 or test database is configured

# 4. View test logs
cat target/surefire-reports/*.txt
```

---

### 🌐 CORS Issues

#### Issue: "CORS policy blocked"

**Symptoms:**
- Browser console: "blocked by CORS policy"
- Frontend can't access API

**Solutions:**
```bash
# 1. Check allowed origins in .env
echo $CORS_ALLOWED_ORIGINS

# 2. Add frontend URL
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200

# 3. Restart application
docker-compose restart app

# 4. Verify CORS config
curl -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -X OPTIONS \
  http://localhost:8080/api/auth/login -v
```

---

### 🔍 Debugging Tips

#### Enable Debug Logging

```yaml
# application-dev.yml
logging:
  level:
    root: INFO
    com.auth.template: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

#### View SQL Queries

```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
```

#### Remote Debugging

```bash
# Run with debug port
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 \
  -jar target/spring-boot-auth-template-1.0.0.jar

# In IDE, attach debugger to localhost:5005
```

---

## 🔧 Diagnostic Commands

### Check Application Health

```bash
# Health endpoint
curl http://localhost:8080/actuator/health

# Detailed health
curl http://localhost:8080/actuator/health | jq

# Database health
curl http://localhost:8080/actuator/health/db
```

### Check Environment

```bash
# View environment variables
docker exec auth-template env | grep -E "DB_|JWT_|MAIL_"

# Check Java version
docker exec auth-template java -version

# Check application properties
docker exec auth-template cat /app/application.yml
```

### Database Diagnostics

```bash
# Connect to database
docker exec -it auth-template-db psql -U postgres -d auth_db_dev

# List tables
\dt

# Check users table
SELECT * FROM users LIMIT 5;

# Check failed login attempts
SELECT username, failed_login_attempts, account_locked, lock_time 
FROM users 
WHERE failed_login_attempts > 0;

# Check tokens
SELECT * FROM verification_tokens WHERE used = false;
```

---

## 📝 Logs Analysis

### Find Errors

```bash
# All errors
docker-compose logs app | grep -i error

# Authentication errors
docker-compose logs app | grep -i "authentication"

# Database errors
docker-compose logs app | grep -i "sql\|database"

# Email errors
docker-compose logs app | grep -i "email\|mail"
```

### Monitor Real-time

```bash
# Follow logs
docker-compose logs -f app

# Filter by level
docker-compose logs app | grep -E "ERROR|WARN"

# Last 100 lines
docker-compose logs --tail=100 app
```

---

## 🆘 Emergency Procedures

### Application Won't Start

```bash
# 1. Check logs
docker-compose logs app

# 2. Check database
docker-compose logs postgres

# 3. Verify environment variables
cat .env

# 4. Restart everything
docker-compose down
docker-compose up -d

# 5. If still failing, start fresh
docker-compose down -v
docker-compose up -d
```

### Database Corruption

```bash
# 1. Stop application
docker-compose stop app

# 2. Backup database
docker exec auth-template-db pg_dump -U postgres auth_db_dev > backup.sql

# 3. Drop and recreate
docker exec -it auth-template-db psql -U postgres
DROP DATABASE auth_db_dev;
CREATE DATABASE auth_db_dev;
\q

# 4. Restore from backup
docker exec -i auth-template-db psql -U postgres auth_db_dev < backup.sql

# 5. Restart application
docker-compose start app
```

### Reset Everything

```bash
# WARNING: This will delete all data!

# 1. Stop and remove containers
docker-compose down -v

# 2. Remove images
docker rmi auth-template:1.0.0

# 3. Clean build
./mvnw clean

# 4. Rebuild
./mvnw clean package -DskipTests
docker-compose build

# 5. Start fresh
docker-compose up -d
```

---

## 📞 Getting Help

### Check Documentation

1. [README.md](../README.md) - Main documentation
2. [API_EXAMPLES.md](API_EXAMPLES.md) - API usage examples
3. [DEVELOPMENT.md](DEVELOPMENT.md) - Development guide
4. [DEPLOYMENT.md](DEPLOYMENT.md) - Deployment guide

### Community Support

- Stack Overflow: Tag with `spring-boot`, `spring-security`, `jwt`
- Spring Community: https://spring.io/community
- GitHub Issues: Create issue with logs and error details

### Reporting Bugs

Include:
1. Error message and stack trace
2. Steps to reproduce
3. Environment (OS, Java version, Docker version)
4. Relevant configuration
5. Logs

---

**Still having issues? Check the logs first, then consult the documentation!**
