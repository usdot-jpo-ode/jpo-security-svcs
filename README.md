# jpo-security-svcs

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

In `./src/main/resources/application.properties` there are 5 properties:

| Property        | Meaning           | Default Value  |
| ------------- |:-------------:| -----:|
| server.port | The port number to which this service will be listening | 8090 |
| sec.useHsm | If true, will use internal HSM signing routine. If false, will use external service. MUST be false at this time until the internal functions are implemented.
 | false |
| sec.cryptoServiceBaseUri | Cryptographic service endpoint URI excluding path. For example, `http://<ip>:<por>` OR `http://server.dns.name` including the port number, if any| - |
| sec.cryptoServiceEndpointSignPath | The REST endpoint path of the xternal service if `sec.useHsm=false` | /tmc/signtim |