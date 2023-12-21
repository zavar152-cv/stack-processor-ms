FROM openjdk:17-jdk
VOLUME /tmp
ADD target/*.jar app.jar
RUN microdnf install curl
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app.jar" ]