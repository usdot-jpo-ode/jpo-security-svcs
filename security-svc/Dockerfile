FROM maven:3.8-eclipse-temurin-21-alpine as builder

WORKDIR /home
COPY ./pom.xml .
COPY ./src ./src

RUN mvn clean package

FROM eclipse-temurin:21-jre-alpine

RUN apk update
RUN apk add ca-certificates

RUN apk add java-cacerts
RUN rm $JAVA_HOME/lib/security/cacerts
RUN ln -sf /etc/ssl/certs/java/cacerts $JAVA_HOME/lib/security/cacerts

RUN apk add openssl

COPY --from=builder /home/src/main/resources/logback.xml /home
COPY --from=builder /home/target/jpo-security-svcs.jar /home

CMD ["java", "-Dlogback.configurationFile=/home/logback.xml", "-jar", "/home/jpo-security-svcs.jar"]
