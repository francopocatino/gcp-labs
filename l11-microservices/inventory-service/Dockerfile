FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY app/pom.xml .
RUN mvn dependency:go-offline
COPY app/src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

USER nobody
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
