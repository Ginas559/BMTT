# -------- Build stage --------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline || true
COPY src ./src
RUN mvn -B -DskipTests clean package

# -------- Runtime: Tomcat --------
FROM tomcat:10.1-jdk21-temurin

# Xoá app mặc định
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy server.xml đã custom để dùng ${PORT} và tắt shutdown port
COPY docker/server.xml /usr/local/tomcat/conf/server.xml

# Deploy WAR của bạn làm ROOT.war
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

# Render sẽ cung cấp biến môi trường PORT; chuyển nó thành system property cho Tomcat
ENV CATALINA_OPTS="-DPORT=${PORT}"

EXPOSE 8080
CMD ["catalina.sh","run"]
