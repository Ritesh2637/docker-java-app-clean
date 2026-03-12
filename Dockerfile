# Stage 1: Build with Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src ./src

# Build the shaded JAR
RUN mvn clean package -DskipTests

# Stage 2: Runtime with JDK only
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/docker-java-app-1.0.0-SNAPSHOT-shaded.jar app.jar

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]
