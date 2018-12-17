FROM maven:3.5.4-jdk-8-alpine as builder
MAINTAINER 583114@bah.com

WORKDIR /home
COPY ./pom.xml .
COPY ./src ./src

RUN mvn clean package

FROM openjdk:8u171-jre-alpine

COPY --from=builder /home/src/main/resources/logback.xml /home
COPY --from=builder /home/target/jpo-security-svcs.jar /home

CMD ["java", "-Dlogback.configurationFile=/home/logback.xml", "-jar", "/home/jpo-security-svcs.jar"]
