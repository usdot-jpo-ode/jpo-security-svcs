FROM ubuntu:latest

RUN apt-get update && \
 apt-get install -y default-jdk

RUN mkdir -p /home/jpo-security-svcs/target
ADD ./target/jpo-security-svcs-0.0.1-SNAPSHOT.jar /home/jpo-security-svcs/target

CMD java -jar /home/jpo-security-svcs/target/jpo-security-svcs-0.0.1-SNAPSHOT.jar --sec.cryptoServiceBaseUri=${SEC_CRYPTO_SERVICE_BASE_URI}
