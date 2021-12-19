FROM adoptopenjdk/openjdk15:jre-15.0.2_7-alpine
COPY build/libs/*.jar tg-bot.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "tg-bot.jar"]