FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/web-0.0.1-SNAPSHOT.jar app.jar
RUN chmod 0444 /app/app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
