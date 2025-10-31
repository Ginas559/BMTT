# ============================
# 1️⃣ Build stage
# ============================
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# copy pom.xml và tải dependency (tăng tốc build)
COPY pom.xml .
RUN mvn dependency:go-offline

# copy toàn bộ source code
COPY src ./src

# build project
RUN mvn clean package -DskipTests

# ============================
# 2️⃣ Runtime stage
# ============================
FROM eclipse-temurin:21-jre
WORKDIR /app

# copy file JAR đã build sang container
COPY --from=build /app/target/*.jar app.jar

# cấu hình biến môi trường (Render sẽ override)
ENV PORT=8080
EXPOSE 8080

# run app
ENTRYPOINT ["java","-jar","app.jar"]
