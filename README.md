
# jpo-security-svcs
This module exposes a RESTful API for performing cryptographic functions. The following paths identify the functions:
|Verb|path|Content Type|Functionality|Request Body Format|Response Body Format|
|--|--|--|--|--|--|
|POST|/sign|application/json|signs data provided in the body of the request|{"message":"Base64 encoded unsigned data"}|{"result": "Base64 Encoded Signed Data"}


Note that the cryptographic functions may be carried out against an on-prem instance, or against a remote instance. A local instance is a simplified case and does not require mutual TLS authentication. For the remote signing use case, MTLS will be used to secure the communications. This requires some additional work on the part of configuration, and uses private and public keys to secure the communications between this instance and the remote signing instance.
## Mutual TLS Authentication
For enhanced security, MTLS is used to communicate with a remote signing system. This requires properly configured certificates from the remote system to property perform - work with your remote signing authority to obtain necessary certificates. 

### Certificate Conversion
Certificates can be in several different formats, including the widely used PEM format. Because this is a Java application, the signing certificates must be in the Java Keystore format to be used. The following commands using [OpenSSL](https://www.openssl.org/) and the Java keytool will convert a PEM file to a Java Keystore file.

1. Convert the PEM file to a PKCS12 file:
```
openssl pkcs12 -export -in <cert.pem> -inkey <key.pem> -out <certificate.p12> -name <alias>
```
2. Convert the PKCS12 file to a Java Keystore file:
```
keytool -importkeystore -srckeystore <certificate.p12> -srcstoretype pkcs12 -destkeystore <cert.jks>
```

### Certificate Authority Issues
If your remote signing authority is using their own certificates as a CA, you may need to import those certificates into your Java truststore to allow the handshake to go through. Note that when running the dockerized application, these CA certificates are to be one certificate per file (chain certificates are not supported).

## Install

`mvn clean install`


## Debug
If running in VS Code, a launch.json has been included to allow for ease of debugging. A .env file can be created using the sample.env file as a starting point. Once this settings file is in place, simply click the green arrow in the debug tab to run the application. At this point all breakpoints will function as expected.

## Run

### Java JAR:

`java -jar target/jpo-security-svcs-0.0.1-SNAPSHOT.jar`

### Docker:

`docker build .`

(Take note of image reported by docker build)

`docker run -p 8090:8090 <image>`

### Docker Compose
A docker-compose.yml file has been included as an example for running the application under Docker Compose. Additionally a sample.env has been included to show which values are expected. 

The docker-compose.yml file is configured for using certificates, and mounts a volume to the container pointing to the local `./src/main/resources/creds` directory. This directory (or another you chose to point to) should contain your own JKS used to sign messages for MTLS. It should also contain a subdirectory, "caCerts", containing any CA certificates (one per file) from the remote system that need to be installed in the Java truststore on boot. 

To use, copy the sample.env file to a new '.env' file and replace with your settings. Then, simply run the following command:
```
docker-compose up --build -d
```
This will spin up a new container and run it listening on port 8090. To stop the container, run the following command:
```
docker-compose down
```

## Test

Send a POST request to `localhost:8090/sign` with a body of the form:

```
{
	"message": "<hex encoded data>"
}
```

Expected output:

```
{
	"result": "<hex encoded data + signature>"
}
```

## Configuration

In `./src/main/resources/application.properties` you'll find the following properties which can be defined whether on the command line or by environment variable. To define the property on the command line, insert `--` to the front of the Property name, for example, `--server.port=8091`:

| Property | Meaning | Default Value | Environment Variable Substitute |
| -----------|------------|-----------------|-----------|
| server.port | The port number to which this service will be listening.| 8090 |SERVER_PORT|
| sec.useHsm | Whether to use an HSM or not. | false | SEC_USE_HSM |
| sec.cryptoServiceBaseUri | Cryptographic service endpoint URI excluding path. For example, `http://<ip>:<port>` OR `http://server.dns.name` including the port number, if any. | - |SEC_CRYPTO_SERVICE_BASE_URI|
| sec.cryptoServiceEndpointSignPath | The REST endpoint path of the external service. | /tmc/signtim |SEC_CRYPTO_SERVICE_ENDPOINT_SIGN_PATH|
| sec.useCertficates | Whether to use certificates or not. | true | SEC_USE_CERTIFICATES |
| sec.keyStorePath | The path to the keystore file. | /home/cert.jks | SEC_KEY_STORE_PATH |
| sec.keyStorePassword | The password for the keystore file. | password | SEC_KEY_STORE_PASSWORD |