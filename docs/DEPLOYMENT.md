# Deployment Guide

## 📋 Table of Contents

1. [Local Deployment](#local-deployment)
2. [Docker Deployment](#docker-deployment)
3. [Production Deployment](#production-deployment)
4. [Cloud Deployment](#cloud-deployment)
5. [Monitoring & Maintenance](#monitoring--maintenance)

---

## 🏠 Local Deployment

### Option 1: Maven

```bash
# 1. Build
./mvnw clean package -DskipTests

# 2. Run
java -jar target/spring-boot-auth-template-1.0.0.jar

# Or with profile
java -jar target/spring-boot-auth-template-1.0.0.jar --spring.profiles.active=dev
```

### Option 2: Maven Spring Boot Plugin

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Option 3: IDE

**IntelliJ IDEA:**
1. Right-click on `AuthTemplateApplication.java`
2. Select "Run 'AuthTemplateApplication'"

**VS Code:**
1. Open Run and Debug (Ctrl+Shift+D)
2. Select "Spring Boot App"
3. Press F5

---

## 🐳 Docker Deployment

### Build Docker Image

```bash
# 1. Build JAR
./mvnw clean package -DskipTests

# 2. Build Docker image
docker build -t auth-template:1.0.0 .

# 3. Tag for registry (optional)
docker tag auth-template:1.0.0 your-registry/auth-template:1.0.0
```

### Run with Docker

```bash
# Run container
docker run -d \
  --name auth-template \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=your-db-host \
  -e DB_PASSWORD=your-db-password \
  -e JWT_SECRET=your-jwt-secret \
  auth-template:1.0.0

# View logs
docker logs -f auth-template

# Stop container
docker stop auth-template

# Remove container
docker rm auth-template
```

### Docker Compose (Recommended for Local)

```bash
# 1. Copy and configure .env
cp .env.example .env
nano .env

# 2. Start all services
docker-compose up -d

# 3. View logs
docker-compose logs -f

# 4. Stop all services
docker-compose down

# 5. Stop and remove volumes
docker-compose down -v
```

**docker-compose.yml** already includes:
- PostgreSQL database
- Application container
- Automatic health checks
- Volume persistence

---

## 🚀 Production Deployment

### Pre-Deployment Checklist

- [ ] Change JWT_SECRET to secure random value
- [ ] Use strong database password
- [ ] Configure proper CORS origins
- [ ] Set up SSL/TLS certificates
- [ ] Configure email service (SMTP)
- [ ] Set up database backups
- [ ] Configure logging
- [ ] Set up monitoring
- [ ] Review security settings
- [ ] Test all endpoints
- [ ] Prepare rollback plan

### Environment Variables (Production)

Create `.env.prod`:

```env
# Spring Profile
SPRING_PROFILES_ACTIVE=prod

# Database
DB_HOST=prod-db.example.com
DB_PORT=5432
DB_NAME=auth_db_prod
DB_USERNAME=auth_user
DB_PASSWORD=<strong-password>

# JWT (CRITICAL: Use secure random value)
JWT_SECRET=<generate-with-openssl-rand-base64-64>
JWT_ACCESS_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=86400000

# Email
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=<sendgrid-api-key>
EMAIL_FROM=noreply@yourcompany.com

# CORS
CORS_ALLOWED_ORIGINS=https://app.yourcompany.com,https://www.yourcompany.com

# Frontend
FRONTEND_URL=https://app.yourcompany.com
```

### Generate Secure JWT Secret

```bash
# Generate 512-bit secret
openssl rand -base64 64

# Or use UUID
uuidgen | base64
```

### Database Setup (Production)

```sql
-- 1. Create database
CREATE DATABASE auth_db_prod;

-- 2. Create user
CREATE USER auth_user WITH ENCRYPTED PASSWORD 'strong-password';

-- 3. Grant privileges
GRANT ALL PRIVILEGES ON DATABASE auth_db_prod TO auth_user;

-- 4. Connect to database
\c auth_db_prod

-- 5. Grant schema privileges
GRANT ALL ON SCHEMA public TO auth_user;
```

### Deploy with Docker (Production)

```bash
# 1. Build production image
./mvnw clean package -DskipTests
docker build -t auth-template:1.0.0 .

# 2. Push to registry
docker tag auth-template:1.0.0 your-registry.com/auth-template:1.0.0
docker push your-registry.com/auth-template:1.0.0

# 3. Pull on production server
docker pull your-registry.com/auth-template:1.0.0

# 4. Run with production config
docker run -d \
  --name auth-template-prod \
  --restart unless-stopped \
  -p 8080:8080 \
  --env-file .env.prod \
  your-registry.com/auth-template:1.0.0

# 5. Verify
curl http://localhost:8080/actuator/health
```

---

## ☁️ Cloud Deployment

### AWS Elastic Beanstalk

```bash
# 1. Install EB CLI
pip install awsebcli

# 2. Initialize
eb init -p docker auth-template

# 3. Create environment
eb create auth-template-prod

# 4. Deploy
eb deploy

# 5. Open in browser
eb open

# 6. View logs
eb logs

# 7. SSH into instance
eb ssh
```

**Dockerrun.aws.json:**
```json
{
  "AWSEBDockerrunVersion": "1",
  "Image": {
    "Name": "your-registry/auth-template:1.0.0",
    "Update": "true"
  },
  "Ports": [
    {
      "ContainerPort": 8080,
      "HostPort": 8080
    }
  ]
}
```

### Google Cloud Run

```bash
# 1. Build and push to GCR
gcloud builds submit --tag gcr.io/PROJECT_ID/auth-template

# 2. Deploy
gcloud run deploy auth-template \
  --image gcr.io/PROJECT_ID/auth-template \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars="SPRING_PROFILES_ACTIVE=prod" \
  --set-env-vars="DB_HOST=..." \
  --set-secrets="JWT_SECRET=jwt-secret:latest"

# 3. Get URL
gcloud run services describe auth-template --format='value(status.url)'
```

### Azure Container Instances

```bash
# 1. Login
az login

# 2. Create resource group
az group create --name auth-template-rg --location eastus

# 3. Create container registry
az acr create --resource-group auth-template-rg \
  --name authtemplate --sku Basic

# 4. Build and push
az acr build --registry authtemplate \
  --image auth-template:1.0.0 .

# 5. Deploy
az container create \
  --resource-group auth-template-rg \
  --name auth-template \
  --image authtemplate.azurecr.io/auth-template:1.0.0 \
  --dns-name-label auth-template \
  --ports 8080 \
  --environment-variables \
    SPRING_PROFILES_ACTIVE=prod \
    DB_HOST=... \
  --secure-environment-variables \
    JWT_SECRET=...
```

### Heroku

```bash
# 1. Login
heroku login

# 2. Create app
heroku create auth-template-prod

# 3. Add PostgreSQL
heroku addons:create heroku-postgresql:hobby-dev

# 4. Set config vars
heroku config:set JWT_SECRET=$(openssl rand -base64 64)
heroku config:set SPRING_PROFILES_ACTIVE=prod

# 5. Deploy
git push heroku main

# 6. Open app
heroku open

# 7. View logs
heroku logs --tail
```

---

## 🔧 Reverse Proxy Setup

### Nginx

```nginx
# /etc/nginx/sites-available/auth-template
server {
    listen 80;
    server_name api.yourcompany.com;

    # Redirect HTTP to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name api.yourcompany.com;

    # SSL certificates
    ssl_certificate /etc/letsencrypt/live/api.yourcompany.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.yourcompany.com/privkey.pem;

    # SSL configuration
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # Proxy to Spring Boot app
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket support (if needed)
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # Rate limiting
    limit_req_zone $binary_remote_addr zone=auth_limit:10m rate=10r/s;
    limit_req zone=auth_limit burst=20 nodelay;

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
}
```

Enable site:
```bash
sudo ln -s /etc/nginx/sites-available/auth-template /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### Apache

```apache
# /etc/apache2/sites-available/auth-template.conf
<VirtualHost *:80>
    ServerName api.yourcompany.com
    Redirect permanent / https://api.yourcompany.com/
</VirtualHost>

<VirtualHost *:443>
    ServerName api.yourcompany.com

    SSLEngine on
    SSLCertificateFile /etc/letsencrypt/live/api.yourcompany.com/fullchain.pem
    SSLCertificateKeyFile /etc/letsencrypt/live/api.yourcompany.com/privkey.pem

    ProxyPreserveHost On
    ProxyPass / http://localhost:8080/
    ProxyPassReverse / http://localhost:8080/

    # Security headers
    Header always set X-Frame-Options "SAMEORIGIN"
    Header always set X-Content-Type-Options "nosniff"
    Header always set X-XSS-Protection "1; mode=block"
</VirtualHost>
```

Enable site:
```bash
sudo a2enmod ssl proxy proxy_http headers
sudo a2ensite auth-template
sudo systemctl reload apache2
```

---

## 📊 Monitoring & Maintenance

### Health Checks

```bash
# Application health
curl http://localhost:8080/actuator/health

# Database connectivity
curl http://localhost:8080/actuator/health/db

# Disk space
curl http://localhost:8080/actuator/health/diskSpace
```

### Logging

**View logs:**
```bash
# Docker
docker logs -f auth-template

# Docker Compose
docker-compose logs -f app

# Systemd service
journalctl -u auth-template -f

# File-based
tail -f /var/log/auth-template/application.log
```

**Log rotation (logrotate):**
```
# /etc/logrotate.d/auth-template
/var/log/auth-template/*.log {
    daily
    rotate 30
    compress
    delaycompress
    notifempty
    create 0640 spring spring
    sharedscripts
    postrotate
        systemctl reload auth-template
    endscript
}
```

### Database Backups

```bash
# Backup script
#!/bin/bash
BACKUP_DIR="/backups/postgres"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/auth_db_$DATE.sql"

pg_dump -h localhost -U auth_user auth_db_prod > $BACKUP_FILE
gzip $BACKUP_FILE

# Keep only last 30 days
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete

# Cron job (daily at 2 AM)
# 0 2 * * * /usr/local/bin/backup-auth-db.sh
```

### Monitoring Tools

**Prometheus + Grafana:**

Add to `pom.xml`:
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

Enable in `application-prod.yml`:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

**ELK Stack (Elasticsearch, Logstash, Kibana):**

Configure logback:
```xml
<!-- logback-spring.xml -->
<appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
    <destination>logstash:5000</destination>
    <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
</appender>
```

---

## 🔄 Zero-Downtime Deployment

### Blue-Green Deployment

```bash
# 1. Deploy new version (green)
docker run -d --name auth-template-green \
  -p 8081:8080 \
  --env-file .env.prod \
  auth-template:2.0.0

# 2. Health check
curl http://localhost:8081/actuator/health

# 3. Switch Nginx to green
# Update nginx config to proxy_pass http://localhost:8081

# 4. Reload Nginx
sudo nginx -s reload

# 5. Stop blue
docker stop auth-template-blue

# 6. Rename green to blue for next deployment
docker rename auth-template-green auth-template-blue
```

### Rolling Update (Kubernetes)

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-template
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  template:
    spec:
      containers:
      - name: auth-template
        image: auth-template:2.0.0
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
```

---

## 🆘 Rollback Procedure

```bash
# Docker
docker stop auth-template
docker rm auth-template
docker run -d --name auth-template \
  --env-file .env.prod \
  auth-template:1.0.0  # Previous version

# Kubernetes
kubectl rollout undo deployment/auth-template

# Heroku
heroku releases:rollback v123
```

---

## 📝 Post-Deployment Checklist

- [ ] Verify application is running
- [ ] Check health endpoint
- [ ] Test authentication flow
- [ ] Verify email sending
- [ ] Check database connectivity
- [ ] Review logs for errors
- [ ] Test all critical endpoints
- [ ] Verify SSL certificate
- [ ] Check monitoring dashboards
- [ ] Update documentation
- [ ] Notify team

---

**For more information, see [README.md](../README.md)**
