FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/smartlibrary-1.0.0.jar app.jar
EXPOSE 10000
ENV PORT=10000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
