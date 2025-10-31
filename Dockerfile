# ============================
# 1️⃣ Build stage (Tạo file .war)
# ============================
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml để tải dependency (tăng tốc build)
COPY pom.xml .

# Tải trước dependency. Dùng '|| true' để build không bị lỗi nếu có dependency gặp vấn đề
# Dùng -B (non-interactive) và -DskipTests
RUN mvn -B -DskipTests dependency:go-offline || true

# Copy toàn bộ source code
COPY src ./src

# Build project, tạo ra file .war
# Dùng -B (non-interactive) và -DskipTests
RUN mvn -B -DskipTests clean package

# ============================
# 2️⃣ Runtime stage (Tomcat)
# ============================
FROM tomcat:10.1-jdk21-temurin
WORKDIR /usr/local/tomcat

# Xoá các ứng dụng mặc định của Tomcat
RUN rm -rf webapps/*

# Copy file .war đã build (từ giai đoạn 'build') vào thư mục webapps của Tomcat
# Đổi tên thành ROOT.war để ứng dụng chạy ở context root (ví dụ: http://localhost:8080/)
COPY --from=build /app/target/*.war webapps/ROOT.war

# Cấu hình biến môi trường (Render, các dịch vụ cloud khác thường override)
ENV PORT=8080
EXPOSE 8080

# Lệnh chạy Tomcat
# CMD ["catalina.sh","run"] là cách phổ biến để giữ container chạy.
CMD ["catalina.sh", "run"]