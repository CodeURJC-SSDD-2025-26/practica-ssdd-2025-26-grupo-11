FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY utility-service/pom.xml .
COPY utility-service/src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]