FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY app-service/pom.xml .
COPY app-service/src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8443
ENTRYPOINT ["java", "-jar", "app.jar"]