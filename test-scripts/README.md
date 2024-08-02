# Test Scripts

## hit_endpoint.sh
The `hit_endpoint.sh` script is a simple bash script that sends a GET request to localhost:8090/sign and prints the response. It is used to test the `/sign` endpoint of the server, the code for which can be found in [SignatureController.java](../src/main/java/us/dot/its/jpo/sec/controllers/SignatureController.java).

To run the script, simply execute the following command in the terminal:
```bash
./hit_endpoint.sh
```
