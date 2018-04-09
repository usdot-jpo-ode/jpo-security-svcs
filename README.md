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
| sec.destIp | IP address of external signature service | 34.210.239.189 |
| sec.destPort | Destination port of external signature service | 55443 |
| sec.mockResponse | If no real signing is desired, the service will return a mock message | true |
| sec.useHsm | If true, will use internal HSM signing routine. If false, will use external service | false |
| sec.signPath | The REST endpoint path of the xternal service if `sec.useHsm=false` | /tmc/signtim |

