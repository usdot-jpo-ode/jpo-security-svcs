package controllers;

import org.apache.tomcat.util.buf.HexUtils;
import org.apache.tomcat.util.codec.binary.Base64;
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
   
   private static final String MOCK_MESSAGE = "03810040038081a3d34d45e80ef4db807dd35102f42db7e81d34d34d34d34d34d05efbe43f41d37d35100ef7138e760f5e77f7bd7a0bdf44f43e79e75d34d34dc5d00e7d074f34d35d37d3b17c000f7defd1780f7041dc0d00f76eba0b4d34d34d3ce781370760b8ef6f75176d44f39104134e7bf7cd34dbce36d02e45dbad7bd79e3617c14407c13cd7b0bcdc30ba17c044e35d84dfc0b9176d03ef50b8db5f34d34db4d770c30fbeba0b60018300019924e7b3b2720001992842024272810101000301801631afb5fc255d0f508208f49317071422d1925e6f5b00031acb5dbc8400a983010180034801010001838182792f4e20404c92bf0707999b338ef65e6d6f110bfbf1b67a360ed8a8e412bfa88083a83da9c99739b68f2eff338bbb4b9af2982fe50d843f0f896b9cf291e5d39d1417be0d856eaaea639de2f6ff2d42928e0e2374cbe1ac5dc0d065b0a36ecdfac6";

   @Value("${destIp}")
   public String destIp;
   @Value("${destPort}")
   public String destPort;
   @Value("${mockResponse}")
   public boolean mockResponse;
   @Value("${useHsm}")
   public boolean useHsm;

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
      
      logger.info("mockResponse == {}", mockResponse);

      if (mockResponse) {
         logger.info("Returning mock response");
         response = ResponseEntity.status(HttpStatus.OK).body(jsonPair("result", MOCK_MESSAGE));
      } else if (useHsm) {
         logger.info("Signing using HSM");
         response = signWithHsm(message);
      } else {
         
         logger.error("Sending signature request to external service");
         ResponseEntity<String> result = forwardMessageToExternalService(message);

         logger.info("Received response: {}", result);
         
         byte[] decoded = Base64.decodeBase64(result.getBody());
         String hexString = HexUtils.toHexString(decoded);

         response = ResponseEntity.status(HttpStatus.OK).body(jsonPair("result", hexString));
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

      ResponseEntity<String> respEntity = template.postForEntity(uri,entity, String.class);

      logger.info("Rest response: {}", respEntity);

      return respEntity;
   }

   private ResponseEntity<String> signWithHsm(Message message) {
      return ResponseEntity.status(HttpStatus.OK).body(jsonPair("result", message + "_signature"));
   }

}
