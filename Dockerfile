FROM ubuntu:latest

RUN apt-get update && \
 apt-get install -y default-jdk

RUN mkdir -p /home/jpo-security-svcs/target
ADD ./target/. /home/jpo-security-svcs/target

CMD java -jar /home/jpo-security-svcs/target/jpo-security-svcs-0.0.1.jar
