# jpo-security-svcs

## Install

`mvn clean install`

## Run

### Java JAR:

`java -jar target/jpo-security-svcs-0.1.0.jar`

### Docker:

`docker build .`

(Take note of image reported by docker build)

`docker run -p 8080:8080 <image>`

### Docker Compose:

`docker-compose up --build`

## Test

Send a POST request to `localhost:8080/sign` with a body of the form:

```
{
	"message": "<base64 encoded data>",
	"ip": "192.168.1.1",
	"port": "1234"
}
```

Expected output:

```
{
   "message-signed": "<base64 encoded data + signature>"
}
```
