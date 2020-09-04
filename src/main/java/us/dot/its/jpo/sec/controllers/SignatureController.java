/*******************************************************************************
 * Copyright 2018 572682
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package us.dot.its.jpo.sec.controllers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;

@Configuration
@ConfigurationProperties("sec")
@PropertySource("classpath:application.properties")
@RestController
public class SignatureController implements EnvironmentAware {

   @Autowired
   private Environment env;

//   private static final String MOCK_MESSAGE = "03810040038081a3d34d45e80ef4db807dd35102f42db7e81d34d34d34d34d34d05efbe43f41d37d35100ef7138e760f5e77f7bd7a0bdf44f43e79e75d34d34dc5d00e7d074f34d35d37d3b17c000f7defd1780f7041dc0d00f76eba0b4d34d34d3ce781370760b8ef6f75176d44f39104134e7bf7cd34dbce36d02e45dbad7bd79e3617c14407c13cd7b0bcdc30ba17c044e35d84dfc0b9176d03ef50b8db5f34d34db4d770c30fbeba0b60018300019924e7b3b2720001992842024272810101000301801631afb5fc255d0f508208f49317071422d1925e6f5b00031acb5dbc8400a983010180034801010001838182792f4e20404c92bf0707999b338ef65e6d6f110bfbf1b67a360ed8a8e412bfa88083a83da9c99739b68f2eff338bbb4b9af2982fe50d843f0f896b9cf291e5d39d1417be0d856eaaea639de2f6ff2d42928e0e2374cbe1ac5dc0d065b0a36ecdfac6";

   public String cryptoServiceBaseUri;
   private String cryptoServiceEndpointSignPath;
//   public boolean mockResponse;
   public boolean useHsm;

   public static class Message{
      @JsonProperty("message")
      public String msg;
      
      @JsonProperty("sigValidityOverride")
      public int sigValidityOverride = 0;
   }

   private static final Logger logger = LoggerFactory.getLogger(SignatureController.class);

   @RequestMapping(value = "/sign", method = RequestMethod.POST, produces = "application/json")
   @ResponseBody
   public ResponseEntity<Map<String,String>> sign(@RequestBody Message message) throws URISyntaxException {

      logger.info("Received message: {}", message.msg);
      logger.info("Received sigValidityOverride: {}", message.sigValidityOverride);

      ResponseEntity<Map<String,String>> response;
      
//      logger.info("mockResponse == {}", mockResponse);
//
//      if (mockResponse) {
//         logger.info("Returning mock response");
//         response = ResponseEntity.status(HttpStatus.OK).body(
//            Collections.singletonMap("result", MOCK_MESSAGE));
//      } else if (useHsm) {
      if (useHsm) {
         logger.info("Signing using HSM");
         response = signWithHsm(message);
      } else {
         logger.debug("Before trimming: cryptoServiceBaseUri={}, cryptoServiceEndpointSignPath={}", cryptoServiceBaseUri, cryptoServiceEndpointSignPath);
         //Remove all slashes from the end of the URI, if any
         while (cryptoServiceBaseUri != null && cryptoServiceBaseUri.endsWith("/")) {
            cryptoServiceBaseUri = cryptoServiceBaseUri.substring(0, cryptoServiceBaseUri.lastIndexOf('/'));
         }

         //Remove all slashes from the beginning of the path string, if any
         while (cryptoServiceEndpointSignPath != null && cryptoServiceEndpointSignPath.startsWith("/")) {
            cryptoServiceEndpointSignPath = cryptoServiceEndpointSignPath.substring(1);
         }

         logger.debug("After Trimming: cryptoServiceBaseUri={}, cryptoServiceEndpointSignPath={}", cryptoServiceBaseUri, cryptoServiceEndpointSignPath);
       
         String resultString = message.msg;
         if (!StringUtils.isEmpty(cryptoServiceBaseUri) && !StringUtils.isEmpty(cryptoServiceEndpointSignPath)) {
            logger.info("Sending signature request to external service");
            ResponseEntity<String> result = forwardMessageToExternalService(message);
   
            JSONObject json = new JSONObject(result.getBody());
            
            resultString = json.getString("message-signed");
            Map<String, String> mapResult = new HashMap<>();
            try 
            {
            
               mapResult.put("message-expiry", String.valueOf(json.getLong("message-expiry")));
               
            }
            catch(Exception e)
            {
               mapResult.put("message-expiry", "null");
            }
				mapResult.put("message-signed", resultString);
            response = ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("result", new JSONObject(mapResult).toString()));
         } else {
            String msg = "Properties sec.cryptoServiceBaseUri=" + cryptoServiceBaseUri
                  + ", sec.cryptoServiceEndpointSignPath=" + cryptoServiceEndpointSignPath
                  + " Not defined. Returning the message unchanged.";
            logger.warn(msg);
            Map<String, String> result = new HashMap<String, String>();
            result.put("result", resultString);
            result.put("warn", msg);
            response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
         }
         
         
      }

      return response;

   }

   private ResponseEntity<String> forwardMessageToExternalService(Message message) throws URISyntaxException {

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      Map<String,String> map;
      
      if(message.sigValidityOverride > 0) 
      {
    	  map = new HashMap<>();
    	  map.put("message",message.msg);
    	  map.put("sigValidityOverride", Integer.toString(message.sigValidityOverride));
      }
      else 
      {
    	 map = Collections.singletonMap("message", message.msg); 
      }     
      HttpEntity<Map<String, String>> entity = new HttpEntity<>(map, headers); 
      RestTemplate template = new RestTemplate();

      logger.debug("Received request: {}", entity);

      URI uri = new URI(cryptoServiceBaseUri + "/" + cryptoServiceEndpointSignPath);
      
      logger.debug("Sending request to: {}", uri);

      ResponseEntity<String> respEntity = template.postForEntity(uri, entity, String.class);

      logger.debug("Received response: {}", respEntity);

      return respEntity;
   }

   private ResponseEntity<Map<String, String>> signWithHsm(Message message) {
      return ResponseEntity.status(HttpStatus.OK).body(
         Collections.singletonMap("result", message + "NOT IMPLEMENTED"));
   }

   @Override
   public void setEnvironment(Environment env) {
      this.env = env;
   }

   public Environment getEnv() {
      return env;
   }

   public void setEnv(Environment env) {
      this.env = env;
   }

   public String getCryptoServiceBaseUri() {
      return cryptoServiceBaseUri;
   }

   public void setCryptoServiceBaseUri(String cryptoServiceBaseUri) {
      this.cryptoServiceBaseUri = cryptoServiceBaseUri;
   }

   public String getCryptoServiceEndpointSignPath() {
      return cryptoServiceEndpointSignPath;
   }

   public void setCryptoServiceEndpointSignPath(String cryptoServiceEndpointSignPath) {
      this.cryptoServiceEndpointSignPath = cryptoServiceEndpointSignPath;
   }

   public boolean isUseHsm() {
      return useHsm;
   }

   public void setUseHsm(boolean useHsm) {
      this.useHsm = useHsm;
   }

}
