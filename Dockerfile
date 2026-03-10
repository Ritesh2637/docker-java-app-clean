# Stage 1: Build the JAR using Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the JAR with a lightweight JDK
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy shaded JAR from build stage
COPY --from=build /app/target/docker-java-app-1.0.0-SNAPSHOT-shaded.jar app.jar

# Environment variable for BigQuery credentials
# Jenkins pipeline will inject key.json at runtime
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/key.json

# Run the app
CMD ["java", "-jar", "app.jar"]