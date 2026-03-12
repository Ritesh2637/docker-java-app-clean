# Stage 1: Build with Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime with JDK only
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/docker-java-app-1.0.0-SNAPSHOT-shaded.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
