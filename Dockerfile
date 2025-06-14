FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy the built JAR
COPY target/connect-auth-*.jar app.jar

# Expose the app port
EXPOSE 8080

# Launch Spring Boot using .env-injected properties
ENTRYPOINT ["java", "-jar", "app.jar"]
