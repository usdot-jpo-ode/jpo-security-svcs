
# jpo-security-svcs
This module expopsed a RESTful API for performing cryptographic functions. The following paths identify the functions:
|Verb|path|Content Type|Functionality|Request Body Format|Response Body Format|
|--|--|--|--|--|--|
|POST|/sign|application/json|signs data provided in the body of the request|{"message":"Base64 encoded unsigned data"}|{"result": "Base64 Encoded Signed Data"}

## Install

`mvn clean install`

## Run

### Java JAR:

`java -jar target/jpo-security-svcs-0.0.1-SNAPSHOT.jar`

### Docker:

`docker build .`

(Take note of image reported by docker build)

`docker run -p 8090:8090 <image>`

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

In `./src/main/resources/application.properties` you'll find the following properties which can be defined wither on the command line or by environment variable. To define the property on the command line, insert `--` to the front of the Property name, for example, `--server.port=8091`:

| Property | Meaning | Default Value | Environment Variable Substitute |
| -----------|------------|-----------------|-----------|
| server.port | The port number to which this service will be listening.| 8090 |SERVER_PORT|
| sec.cryptoServiceBaseUri | Cryptographic service endpoint URI excluding path. For example, `http://<ip>:<port>` OR `http://server.dns.name` including the port number, if any. | - |SEC_CRYPTO_SERVICE_BASE_URI|
| sec.cryptoServiceEndpointSignPath | The REST endpoint path of the external service. | /tmc/signtim |SEC_CRYPTO_SERVICE_ENDPOINT_SIGN_PATH|
