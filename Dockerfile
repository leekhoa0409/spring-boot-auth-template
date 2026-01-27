FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="spring-boot-auth-template"
LABEL description="Production-ready Spring Boot Authentication Template"

WORKDIR /app

# Copy the JAR file
COPY target/*.jar app.jar

# Create a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
