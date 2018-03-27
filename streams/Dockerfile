FROM gradle:4.6-jdk8-alpine as build

USER root
ENV HOME /root

RUN mkdir -p /root/.gradle
COPY build.gradle /home/gradle/project/
COPY settings.gradle /home/gradle/project/
COPY src /home/gradle/project/src

VOLUME /root/.gradle

WORKDIR /home/gradle/project

RUN gradle build

FROM openjdk:8u151-jre-slim-stretch

COPY --from=build /home/gradle/project/build/libs/s* .
CMD ["java", "-jar", "streams-1.0.jar"]