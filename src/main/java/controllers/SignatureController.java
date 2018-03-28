package controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
public class SignatureController {

   public static class Message {
      @JsonProperty("message")
      public String message;
      @JsonProperty("ip")
      public String ip;
      @JsonProperty("port")
      public String port;
   }

   private static final Logger logger = LoggerFactory.getLogger(SignatureController.class);

   @RequestMapping(value = "/sign", method = RequestMethod.POST, produces = "application/json")
   @ResponseBody
   public ResponseEntity<String> sign(@RequestBody Message message) {

      logger.info("Received message: {}", message.message);
      logger.info("Destination ip:port  {}:{}", message.ip, message.port);

      ResponseEntity<String> result = forwardMessageToGreenHills(message);

      logger.info("Received response: {}", result);

      return ResponseEntity.status(HttpStatus.OK).body(jsonPair("result", result.getBody()));

   }

   public String jsonPair(String key, String value) {
      return "{\"" + key + "\":\"" + value + "\"}";
   }

   private ResponseEntity<String> forwardMessageToGreenHills(Message message) {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<String> entity = new HttpEntity<String>(jsonPair("message", message.message), headers);

      RestTemplate template = new RestTemplate();

      logger.info("Rest request: {}", entity);

      String uri = "http://" + message.ip + ":" + message.port + "/tmc/signtim";
      logger.info("URI: {}", uri);

      ResponseEntity<String> respEntity = template.postForEntity(uri, jsonPair("message", message.message),
            String.class);

      logger.info("Rest response: {}", respEntity);

      return respEntity;
   }

}
