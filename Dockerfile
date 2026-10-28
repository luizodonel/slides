# Stage 1: Build Frontend (React + Vite)
FROM node:20-alpine AS frontend-builder
WORKDIR /web
COPY slides-web/package*.json ./
RUN npm install
COPY slides-web ./
RUN npm run build

# Stage 2: Build Backend (Java 17 + Spring Boot 3)
FROM maven:3.9.6-eclipse-temurin-17 AS backend-builder
WORKDIR /app
COPY slides-api/pom.xml .
RUN mvn dependency:go-offline -B
COPY slides-api/src ./src
COPY --from=frontend-builder /web/dist ./src/main/resources/static
RUN mvn clean package -DskipTests -B

# Stage 3: Production Runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y fontconfig fonts-dejavu-core && rm -rf /var/lib/apt/lists/*
COPY --from=backend-builder /app/target/*.jar app.jar
EXPOSE 8080
ENV PORT=8080
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Djava.awt.headless=true", "-jar", "app.jar"]
