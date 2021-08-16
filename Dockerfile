FROM openjdk:11

EXPOSE 8080
RUN mkdir -p /apps
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /apps/app.jar
ENTRYPOINT ["java","-jar","/apps/app.jar"]