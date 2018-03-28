package controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
public class SignatureController {
   
   public static class Message {
      @JsonProperty("message")
      public String message;
   }

   private static final Logger logger = LoggerFactory.getLogger(SignatureController.class);

   @RequestMapping(value = "/sign", method = RequestMethod.POST, produces = "application/json")
   @ResponseBody
   public ResponseEntity<String> sign(@RequestBody Message message) {
      
      logger.info("Received message: {}", message.message);
      
      String signedMessage = message.message + "_signature";
      
      return ResponseEntity.status(HttpStatus.OK).body(jsonPair("message+signature", signedMessage));

   }

   public String jsonPair(String key, String value) {
      return "{\"" + key + "\":\"" + value + "\"}";
   }

}
