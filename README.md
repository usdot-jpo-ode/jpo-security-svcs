# jpo-security-svcs

## Install

`mvn clean install`

## Run

### Java JAR:

`java -jar target/jpo-security-svcs-0.1.0.jar`

### Docker:

`docker build .`

(Take note of image reported by docker build)

`docker run -p 8090:8090 <image>`

## Test

Send a POST request to `localhost:8080/sign` with a body of the form:

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
| server.port | The port to which messages should be sent | 8090 |
| destIp | IP address of external signature service | (none) |
| destPort | Destination port of external signature service | (none) |
| mockResponse | If no real signing is desired, the service will return a mock message | true |
| useHsm | If true, will use internal HSM signing routine. If false, will use external service | false |



