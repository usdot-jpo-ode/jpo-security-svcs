FROM maven:3.8-eclipse-temurin-21-alpine AS builder

WORKDIR /home
COPY ./pom.xml .
COPY ./src ./src

RUN mvn clean package

FROM eclipse-temurin:21-jre-alpine

COPY --from=builder /home/target/mock-signing-svc.jar /home
ENTRYPOINT ["java","-jar","/home/mock-signing-svc.jar"]