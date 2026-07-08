FROM eclipse-temurin:17-jre

WORKDIR /app
ENV SERVER_PORT=8080

COPY app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
