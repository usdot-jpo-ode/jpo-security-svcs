
# jpo-security-svcs
This module exposes a RESTful API for performing cryptographic signing of messages.

Cryptographic signing is carried out against a remote instance, which requires mutual TLS authentication to secure the 
communications. This requires using private and public keys to secure the communications between this instance and the remote signing instance.

## Table of Contents
- [Release notes](#release-notes)
- [Usage](#usage)
- [Installation](#installation)
- [Configuration](#configuration)
- [Mutual TLS Authentication](#mutual-tls-authentication)
- [Debugging](#debugging)
- [Testing](#testing)

## Release Notes
The current version and release history of the Jpo-security-svcs: [Jpo-security-svcs Release Notes](<docs/Release_notes.md>)

## Usage
### RESTful API
The following table depicts the RESTful API exposed by the jpo-security-svcs module:

| Verb | path  | Content Type     | Functionality                                  | Request Body Format                        | Response Body Format                     |
|------|-------|------------------|------------------------------------------------|--------------------------------------------|------------------------------------------|
| POST | /sign | application/json | signs data provided in the body of the request | {"message":"Base64 encoded unsigned data"} | {"result": "Base64 Encoded Signed Data"} |

### Example
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

## Installation
### Docker
1. Install Docker
2. Set up a directory to contain your own JKS file.
3. Set up a subdirectory in the above directory called "caCerts" to contain any CA certificates (one per file) from the 
   remote system that need to be installed in the Java truststore on boot.
4. Copy the sample.env file to a new '.env' file and populate with your settings. See [Configuration](#configuration) for more information.
5. Build & run the application using `docker compose up --build -d`
6. To spin down the container, run `docker compose down`

### Manual
1. Install Maven
2. Install Java
3. Configure the properties file as needed (see [Configuration](#configuration))
4. Package the application using `mvn clean install`
5. Run the application using `java -jar target/jpo-security-svcs-0.0.1-SNAPSHOT.jar`

## Configuration
In `./src/main/resources/application.properties` you'll find the following properties which can be defined whether on 
the command line or by environment variable. To define the property on the command line, insert `--` to the front of the Property name, 
for example, `--server.port=8091`:

| Property                          | Meaning                                                                                                                                             | Default Value  | Environment Variable Substitute       |
|-----------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|----------------|---------------------------------------|
| server.port                       | The port number to which this service will be listening.                                                                                            | 8090           | SERVER_PORT                           |
| sec.cryptoServiceBaseUri          | Cryptographic service endpoint URI excluding path. For example, `http://<ip>:<port>` OR `http://server.dns.name` including the port number, if any. | -              | SEC_CRYPTO_SERVICE_BASE_URI           |
| sec.cryptoServiceEndpointSignPath | The REST endpoint path of the external service.                                                                                                     | /tmc/signtim   | SEC_CRYPTO_SERVICE_ENDPOINT_SIGN_PATH |
| sec.useCertificates               | Whether to use certificates or not.                                                                                                                 | true           | SEC_USE_CERTIFICATES                  |
| sec.keyStorePath                  | The path to the keystore file.                                                                                                                      | /home/cert.jks | SEC_KEY_STORE_PATH                    |
| sec.keyStorePassword              | The password for the keystore file.                                                                                                                 | password       | SEC_KEY_STORE_PASSWORD                |


## Option 1: Use the ext-signature-svc-mock

If you're looking to quickly explore/test the behaviors of this service and its API, then you may want to use the ext-signature-svc-mock.
Usage and configuration information can be found in that [subproject's readme](../ext-signature-svc-mock/README.md)

## Option 2: Configure a local instance with MTLS authentication

### Mutual TLS Authentication
For enhanced security, MTLS is used to communicate with a remote signing system. This requires properly configured 
certificates from the remote system to properly perform - work with your remote signing authority to obtain necessary certificates. 

#### Certificate Conversion
Certificates can be in several different formats, including the widely used PEM format. 
Because this is a Java application, the signing certificates must be in the Java Keystore format to be used. 
The following commands using [OpenSSL](https://www.openssl.org/) and the Java keytool will convert a PEM file to a Java Keystore file.

1. Convert the PEM file to a PKCS12 file:
```
openssl pkcs12 -export -in <cert.pem> -inkey <key.pem> -out <certificate.p12> -name <alias>
```
2. Convert the PKCS12 file to a Java Keystore file:
```
keytool -importkeystore -srckeystore <certificate.p12> -srcstoretype pkcs12 -destkeystore <cert.jks>
```

#### Certificate Authority Issues
If your remote signing authority is using their own certificates as a CA, you may need to import those certificates 
into your Java truststore to allow the handshake to go through. Note that when running the dockerized application, 
these CA certificates are to be one certificate per file (chain certificates are not supported).

### Debugging
If running in VS Code, a launch.json has been included to allow for ease of debugging. A .env file can be created 
using the sample.env file as a starting point. Once this settings file is in place, simply click the green arrow 
in the debug tab to run the application. At this point all breakpoints will function as expected.

## Testing
### Unit Tests
To run the unit tests, reopen the project in the provided dev container and run the following command:
`mvn test`

### hit_endpoint.sh Script
A script has been provided to test the endpoint. To use this script, run the following command:
`./hit_endpoint.sh <data to sign>`

This script will send a POST request to the endpoint with the provided data to sign. The output will be the 
signed data returned from the endpoint. See [Usage](#usage) for more information on the expected input and output.