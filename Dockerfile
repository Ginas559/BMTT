FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline || true
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM tomcat:10.1-jdk21-temurin
RUN rm -rf /usr/local/tomcat/webapps/*

COPY docker/server.xml /usr/local/tomcat/conf/server.xml
COPY docker/start.sh /start.sh
RUN chmod +x /start.sh # Cấp quyền thực thi cho start script

COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080 
CMD /start.sh