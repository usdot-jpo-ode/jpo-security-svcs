# jpo-security-svcs

## Install

`mvn clean install`

## Run

`java -jar target/jpo-security-svcs-0.1.0.jar`

## Test

`curl -H "Content-Type: application/json" -X POST -d '{"message":"hello_world"}' http://localhost:8080/sign`

Expected output:

```{"message+signature":"hello_world_signature"}```
