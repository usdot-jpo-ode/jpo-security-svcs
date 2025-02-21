# jpo-security-svcs

## GitHub Repository Link
https://github.com/usdot-jpo-ode/jpo-security-svcs

## Purpose
The purpose of the jpo-security-svcs program is to provide a REST endpoint for signing TIMs.

## How to pull the latest image
The latest image can be pulled using the following command:
> docker pull usdotjpoode/jpo-security-svcs:latest

## Required environment variables
- SEC_USE_CERTIFICATES

## Expected files/directories
The following should get mounted when running the container:
- creds/
- cert.jks

## Direct Dependencies
None

## Indirect Dependencies
The SEC will not receive messages to process if the ODE is not running.

## Example docker-compose.yml with direct dependencies:
```
version: '3'
services:
  sec:
    image: usdotjpoode/jpo-security-svcs:release_q3
    ports:
     - "8090:8090"
    environment:
      SEC_CRYPTO_SERVICE_BASE_URI: ${SEC_CRYPTO_SERVICE_BASE_URI}
      SEC_CRYPTO_SERVICE_ENDPOINT_SIGN_PATH: ${SEC_CRYPTO_SERVICE_ENDPOINT_SIGN_PATH}
      SEC_USE_CERTIFICATES: ${SEC_USE_CERTIFICATES}
      SEC_KEY_STORE_PASSWORD: ${SEC_KEY_STORE_PASSWORD}
    volumes: 
      - ./creds:/usr/local/share/ca-certificates
      - ./cert.jks:/home/cert.jks
    command: sh -c "update-ca-certificates && java -jar /home/jpo-security-svcs.jar"
    logging:
      options:
        max-size: "10m"  
        max-file: "5"
```

## Expected startup output
The latest line in the logs should look like this:
> jpo-security-svcs-sec-1  | 2023-11-09 18:09:35 [main] INFO  Application - Started Application in 2.569 seconds (JVM running for 3.274)
