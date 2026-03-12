FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/docker-java-app-1.0.0-SNAPSHOT-shaded.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
