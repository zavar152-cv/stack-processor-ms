FROM openjdk:17-jdk
VOLUME /tmp
ADD target/*.jar app.jar
ENV JAVA_OPTS=""
RUN microdnf install curl
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app.jar" ]