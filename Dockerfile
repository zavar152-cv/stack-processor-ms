FROM openjdk:17-alpine as build
WORKDIR /workspace/app

ARG SPRING_DATASOURCE_URL
ENV SPRING_DATASOURCE_URL ${SPRING_DATASOURCE_URL}
ARG SPRING_DATASOURCE_USERNAME
ENV SPRING_DATASOURCE_USERNAME ${SPRING_DATASOURCE_USERNAME}
ARG SPRING_DATASOURCE_PASSWORD
ENV SPRING_DATASOURCE_PASSWORD ${SPRING_DATASOURCE_PASSWORD}

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN sed -i 's/\r$//' mvnw
RUN echo $SPRING_DATASOURCE_URL
RUN ./mvnw clean install liquibase:diff -DskipTests=true
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM openjdk:17-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","ru.itmo.zavar.highloadproject.HighloadProjectApplication"]