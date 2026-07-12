
# Use Java 21 JRE-only runtime image — smaller, since we already built the JAR in CI
FROM eclipse-temurin:21-jre-alpine

# Set working directory inside the container
WORKDIR /app

# Copy the generated JAR file into the container
COPY target/ElectronicStore-0.0.1-SNAPSHOT.jar app.jar

# Expose Spring Boot port
EXPOSE 9090

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]