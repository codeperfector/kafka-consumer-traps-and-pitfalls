FROM azul/zulu-openjdk-alpine:11-jre-headless

COPY build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java" , "-jar",  "/app/app.jar"]