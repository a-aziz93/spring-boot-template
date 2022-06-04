FROM openjdk:11
RUN addgroup -S springboot && adduser -S sbuser -G springboot
USER sbuser
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
ENV PORT 5555
EXPOSE 5555
ENTRYPOINT ["java","-jar","/application.jar"]