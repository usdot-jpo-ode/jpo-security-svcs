FROM maven:3.5.4-jdk-8-alpine as builder
MAINTAINER 583114@bah.com

WORKDIR /home
COPY . .

RUN mvn clean package

FROM openjdk:8u171-jre-alpine

COPY --from=builder /home/target/jpo-security-svcs-0.0.1-SNAPSHOT.jar /home

CMD ["java", "-jar", "/home/jpo-security-svcs-0.0.1-SNAPSHOT.jar"]
