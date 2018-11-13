FROM ubuntu:18.04

RUN apt-get update && \
    apt-get install -y software-properties-common git
RUN apt-get update && \
    apt-get install -y apt-utils
RUN apt-get update && \
    apt-get install -y wget supervisor dnsutils curl jq net-tools
RUN apt-get update && \
    apt-get install -y openjdk-8-jdk
RUN apt-get update && \
    apt-get install -y vim
RUN apt-get update && \
    apt-get install -y nano
RUN apt-get update && \
   apt-cache search maven && apt-get install -y maven
	
RUN apt-get clean

ADD . /home/jpo-security-svcs
RUN cd /home/jpo-security-svcs && mvn clean install

CMD java -jar /home/jpo-security-svcs/target/jpo-security-svcs-0.0.1-SNAPSHOT.jar
