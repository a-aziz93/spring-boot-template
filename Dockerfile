FROM openjdk:11
MAINTAINER ai.tech
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY build/libs/*.jar spring-boot-template-1.0.0.jar
ENV PORT 5555
EXPOSE 5555
ENTRYPOINT ["java","-jar","spring-boot-template-1.0.0.jar"]