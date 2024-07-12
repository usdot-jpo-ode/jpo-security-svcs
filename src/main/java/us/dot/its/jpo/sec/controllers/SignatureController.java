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

import us.dot.its.jpo.sec.helpers.RestTemplateFactory;
import us.dot.its.jpo.sec.models.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConfigurationProperties("sec")
@PropertySource("classpath:application.properties")
@RestController
public class SignatureController implements EnvironmentAware {

   @Autowired
   private Environment env;

   @Autowired
   private RestTemplateFactory restTemplateFactory;

   public String cryptoServiceBaseUri;
   private String cryptoServiceEndpointSignPath;

   private boolean useCertificates;
   private String keyStorePath;
   private String keyStorePassword;

   private static final Logger logger = LoggerFactory.getLogger(SignatureController.class);

   @Autowired
   public void injectBaseDependencies(Environment env, RestTemplateFactory restTemplateFactory) {
      this.env = env;
      this.restTemplateFactory = restTemplateFactory;
   }

   @RequestMapping(value = "/sign", method = RequestMethod.POST, produces = "application/json")
   @ResponseBody
   public ResponseEntity<Map<String, String>> sign(@RequestBody Message message) throws URISyntaxException {
      logger.info("Received message: {}", message.getMsg());
      logger.info("Received sigValidityOverride: {}", message.getSigValidityOverride());

      ResponseEntity<Map<String, String>> response;

      trimBaseUriAndEndpointPath();

      String resultString = message.getMsg();
      if ((cryptoServiceBaseUri == null || cryptoServiceBaseUri.length() == 0) || (cryptoServiceEndpointSignPath == null || cryptoServiceEndpointSignPath.length() == 0)) {
         // base URI or endpoint path not set, return the message unchanged
         String msg = "Properties sec.cryptoServiceBaseUri=" + cryptoServiceBaseUri
               + ", sec.cryptoServiceEndpointSignPath=" + cryptoServiceEndpointSignPath
               + " Not defined. Returning the message unchanged.";
         logger.warn(msg);
         Map<String, String> result = new HashMap<String, String>();
         result.put("result", resultString);
         result.put("warn", msg);
         response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
         return response;
      }

      logger.info("Sending signature request to external service");
      JSONObject json = forwardMessageToExternalService(message);

      if (json != null) {
         resultString = json.getString("message-signed");
         Map<String, String> mapResult = new HashMap<>();
         try {

            mapResult.put("message-expiry", String.valueOf(json.getLong("message-expiry")));

         } catch (Exception e) {
            mapResult.put("message-expiry", "null");
         }
         mapResult.put("message-signed", resultString);
         response = ResponseEntity.status(HttpStatus.OK)
               .body(Collections.singletonMap("result", new JSONObject(mapResult).toString()));
      } else {
         // no response from external service
         response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
               .body(Collections.singletonMap("error", "Error communicating with external service"));
      }

      return response;

   }

   protected void trimBaseUriAndEndpointPath() {
      logger.debug("Before trimming: cryptoServiceBaseUri={}, cryptoServiceEndpointSignPath={}", cryptoServiceBaseUri, cryptoServiceEndpointSignPath);
      // Remove all slashes from the end of the URI, if any
      while (cryptoServiceBaseUri != null && cryptoServiceBaseUri.endsWith("/")) {
         cryptoServiceBaseUri = cryptoServiceBaseUri.substring(0, cryptoServiceBaseUri.lastIndexOf('/'));
      }
      // Remove all slashes from the beginning of the path string, if any
      while (cryptoServiceEndpointSignPath != null && cryptoServiceEndpointSignPath.startsWith("/")) {
         cryptoServiceEndpointSignPath = cryptoServiceEndpointSignPath.substring(1);
      }
      logger.debug("After Trimming: cryptoServiceBaseUri={}, cryptoServiceEndpointSignPath={}", cryptoServiceBaseUri, cryptoServiceEndpointSignPath);
   }

   protected JSONObject forwardMessageToExternalService(Message message) throws URISyntaxException {

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      Map<String, String> map;

      if (message.getSigValidityOverride() > 0) {
         map = new HashMap<>();
         map.put("message", message.getMsg());
         map.put("sigValidityOverride", Integer.toString(message.getSigValidityOverride()));
      } else {
         map = Collections.singletonMap("message", message.getMsg());
      }
      HttpEntity<Map<String, String>> entity = new HttpEntity<>(map, headers);
      RestTemplate template = restTemplateFactory.getRestTemplate();

      logger.debug("Received request: {}", entity);

      URI uri = new URI(cryptoServiceBaseUri + "/" + cryptoServiceEndpointSignPath);

      logger.debug("Sending request to: {}", uri);

      if (useCertificates) {
         try {
            SSLContext sslContext = SSLContexts.custom()
                  .loadKeyMaterial(readStore(), keyStorePassword.toCharArray())
                  .build();

            HttpClient httpClient = HttpClients.custom()
                  .setSSLContext(sslContext)
                  .build();

            HttpPost httpPost = new HttpPost(uri);
            httpPost.setHeader("Content-Type", "application/json");
            org.apache.http.HttpEntity entity2 = new org.apache.http.entity.StringEntity(
                  new JSONObject(map).toString());
            httpPost.setEntity(entity2);

            HttpResponse response = httpClient
                  .execute(httpPost);
            org.apache.http.HttpEntity apache_entity = response.getEntity();
            String result = EntityUtils.toString(apache_entity);
            logger.debug("Returned signature object: {}", result);
            JSONObject jObj = new JSONObject(result);
            EntityUtils.consume(apache_entity);
            return jObj;

         } catch (Exception e) {
            logger.error("Error creating SSLContext", e);
            return null;
         }
      } else {

         ResponseEntity<String> respEntity = template.postForEntity(uri, entity, String.class);
         logger.debug("Received response: {}", respEntity);

         return new JSONObject(respEntity.getBody());
      }
   }

   protected KeyStore readStore() throws Exception {
      try (InputStream keyStoreStream = new FileInputStream(new File(keyStorePath))) {
         KeyStore keyStore = KeyStore.getInstance("JKS");
         keyStore.load(keyStoreStream, keyStorePassword.toCharArray());
         return keyStore;
      } catch (Exception e) {
         throw new Exception("Error reading keystore", e);
      }
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

   public boolean isUseCertificates() {
      return useCertificates;
   }

   public void setUseCertificates(boolean useCertficates) {
      this.useCertificates = useCertficates;
   }

   public String getKeyStorePath() {
      return keyStorePath;
   }

   public void setKeyStorePath(String keyStorePath) {
      this.keyStorePath = keyStorePath;
   }

   public String getKeyStorePassword() {
      return keyStorePassword;
   }

   public void setKeyStorePassword(String keyStorePassword) {
      this.keyStorePassword = keyStorePassword;
   }
}
