# Build Spring Boot Backend (with embedded static React frontend)
FROM maven:3.9.6-eclipse-temurin-17 AS backend-builder
WORKDIR /app
COPY slides-api/pom.xml .
RUN mvn dependency:go-offline -B
COPY slides-api/src ./src
RUN mvn clean package -DskipTests -B

# Production Runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y fontconfig fonts-dejavu-core && rm -rf /var/lib/apt/lists/*
COPY --from=backend-builder /app/target/*.jar app.jar
EXPOSE 8080
ENV PORT=8080
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Djava.awt.headless=true", "-jar", "app.jar"]
