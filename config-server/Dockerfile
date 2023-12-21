FROM openjdk:17-jdk-alpine
VOLUME /tmp
ADD target/*.jar app.jar
RUN apk --no-cache add curl
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app.jar" ]