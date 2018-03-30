package controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
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

@Configuration
@PropertySource("classpath:application.properties")
@RestController
public class SignatureController {

   @Value("${destIp}")
   private static String destIp;
   @Value("${destPort}")
   private static String destPort;
   @Value("${mockResponse}")
   private static boolean mockResponse;
   @Value("${useHsm}")
   private static boolean useHsm;

   public static class Message {
      @JsonProperty("message")
      public String msg;
   }

   private static final Logger logger = LoggerFactory.getLogger(SignatureController.class);

   @RequestMapping(value = "/sign", method = RequestMethod.POST, produces = "application/json")
   @ResponseBody
   public ResponseEntity<String> sign(@RequestBody Message message) {

      logger.info("Received message: {}", message.msg);

      ResponseEntity<String> response = null;

      if (mockResponse) {
         logger.info("Returning mock response");
         response = ResponseEntity.status(HttpStatus.OK).body(jsonPair("result", message.msg + "_signature"));
      } else if (useHsm) {
         logger.info("Signing using HSM");
         response = signWithHsm(message);
      } else {
         logger.error("Sending signature request to external service");
         ResponseEntity<String> result = forwardMessageToExternalService(message);

         logger.info("Received response: {}", result);

         response = ResponseEntity.status(HttpStatus.OK).body(jsonPair("result", result.getBody()));
      }

      return response;

   }

   public String jsonPair(String key, String value) {
      return "{\"" + key + "\":\"" + value + "\"}";
   }

   private ResponseEntity<String> forwardMessageToExternalService(Message message) {

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<String> entity = new HttpEntity<>(jsonPair("message", message.msg), headers);

      RestTemplate template = new RestTemplate();

      logger.info("Rest request: {}", entity);

      String uri = "http://" + destIp + ":" + destPort + "/tmc/signtim";
      logger.info("Destination ip:port  {}:{}", destIp, destPort);
      logger.info("URI: {}", uri);

      ResponseEntity<String> respEntity = template.postForEntity(uri, jsonPair("message", message.msg), String.class);

      logger.info("Rest response: {}", respEntity);

      return respEntity;
   }

   private ResponseEntity<String> signWithHsm(Message message) {
      return ResponseEntity.status(HttpStatus.OK).body(jsonPair("result", message + "_signature"));
   }

}
