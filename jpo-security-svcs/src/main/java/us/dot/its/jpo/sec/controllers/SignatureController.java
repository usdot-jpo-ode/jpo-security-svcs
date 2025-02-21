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

import joptsimple.internal.Strings;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import us.dot.its.jpo.sec.helpers.HttpClientFactory;
import us.dot.its.jpo.sec.helpers.HttpEntityStringifier;
import us.dot.its.jpo.sec.helpers.KeyStoreReader;
import us.dot.its.jpo.sec.helpers.RestTemplateFactory;
import us.dot.its.jpo.sec.helpers.SSLContextFactory;
import us.dot.its.jpo.sec.models.Message;
import us.dot.its.jpo.sec.models.SignatureResponse;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties("sec")
@PropertySource("classpath:application.properties")
@RestController
public class SignatureController implements EnvironmentAware {

    private Environment env;
    private final RestTemplateFactory restTemplateFactory;
    private final KeyStoreReader keyStoreReader;
    private final SSLContextFactory sslContextFactory;
    private final HttpClientFactory httpClientFactory;
    private final HttpEntityStringifier httpEntityStringifier;

    public String cryptoServiceBaseUri;
    private String cryptoServiceEndpointSignPath;

    private boolean useCertificates;
    private String keyStorePath;
    private String keyStorePassword;

    private static final Logger logger = LoggerFactory.getLogger(SignatureController.class);

    @Autowired
    public SignatureController(Environment env, RestTemplateFactory restTemplateFactory, KeyStoreReader keyStoreReader,
                                    SSLContextFactory sslContextFactory, HttpClientFactory httpClientFactory, HttpEntityStringifier httpEntityStringifier) {
        this.env = env;
        this.restTemplateFactory = restTemplateFactory;
        this.keyStoreReader = keyStoreReader;
        this.sslContextFactory = sslContextFactory;
        this.httpClientFactory = httpClientFactory;
        this.httpEntityStringifier = httpEntityStringifier;
    }

    @PostMapping(value = "/sign", produces = "application/json")
    public ResponseEntity<SignatureResponse> sign(@RequestBody Message message) throws SignatureControllerException {
        logger.info("Received message: {} with sigValidityOverride: {}", message.getMsg(), message.getSigValidityOverride());

        trimBaseUriAndEndpointPath();

        if (Strings.isNullOrEmpty(cryptoServiceBaseUri) || Strings.isNullOrEmpty(cryptoServiceEndpointSignPath)) {
            // base URI or endpoint path not set, return the message unchanged
            logger.warn("Properties sec.cryptoServiceBaseUri={}, sec.cryptoServiceEndpointSignPath={} not defined. Cannot sign message.",
                    cryptoServiceBaseUri, cryptoServiceEndpointSignPath);

            throw new SignatureControllerException("Cannot sign message - signing service not configured.", HttpStatus.NOT_FOUND);
        }

        logger.info("Sending signature request to external service");
        JSONObject json = forwardMessageToExternalService(message);

        ResponseEntity<SignatureResponse> response;
        var signatureResponse = new SignatureResponse();
        signatureResponse.setMessageSigned(json.getString("message-signed"));
        try {
            signatureResponse.setMessageExpiry(String.valueOf(json.getLong("message-expiry")));
        } catch (Exception e) {
            signatureResponse.setMessageExpiry("null");
        }
        response = ResponseEntity.status(HttpStatus.OK).body(signatureResponse);

        return response;

    }

    protected JSONObject forwardMessageToExternalService(Message message) throws SignatureControllerException {

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

        URI uri;
        try {
            uri = new URI("%s/%s".formatted(cryptoServiceBaseUri, cryptoServiceEndpointSignPath));
        } catch (URISyntaxException e) {
            throw new SignatureControllerException("Invalid signing service configuration - cannot sign message", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        logger.debug("Sending request to: {}", uri);

        if (useCertificates) {
            KeyStore keyStore;
            SSLContext sslContext;
            try {
                keyStore = keyStoreReader.readStore(keyStorePath, keyStorePassword);
                sslContext = sslContextFactory.getSSLContext(keyStore, keyStorePassword);
            } catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException |
                     IOException | CertificateException e) {
                logger.error("Unable to initialize ssl: {}", e.getMessage(), e);
                throw new SignatureControllerException("Unable to connect to external signing service", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            HttpClient httpClient = httpClientFactory.getHttpClient(sslContext);
            if (httpClient == null) {
                throw new SignatureControllerException("Unable to connect to external signing service", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            HttpPost httpPost = new HttpPost(uri);
            httpPost.setHeader("Content-Type", "application/json");
            org.apache.http.HttpEntity postEntity;
            try {
                postEntity = new org.apache.http.entity.StringEntity(new JSONObject(map).toString());
            } catch (UnsupportedEncodingException e) {
                throw new SignatureControllerException("Invalid request body.", HttpStatus.BAD_REQUEST);
            }
            httpPost.setEntity(postEntity);

            HttpResponse response;
            try {
                response = httpClient.execute(httpPost);
            } catch (IOException e) {
                logger.error("Unable to execute http request: {}", e.getMessage(), e);
                throw new SignatureControllerException("Unable to sign message.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            org.apache.http.HttpEntity responseEntity = response.getEntity();
            String result;
            try {
                result = httpEntityStringifier.stringifyHttpEntity(responseEntity);
            } catch (IOException e) {
                logger.error("Unable to read response from external signing service: {}", e.getMessage(), e);
                throw new SignatureControllerException("Unable to read response from external signing service", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            logger.debug("Raw response from the external service: >>>{}<<<", result);
            if (result == null || result.trim().isEmpty()) {
                logger.error("Empty or null response received from the external signing service.");
                throw new SignatureControllerException("External service returned empty or null response", HttpStatus.BAD_GATEWAY);
            }

            try {
                EntityUtils.consume(responseEntity);
            } catch (IOException e) {
                logger.error("Unable to consume response from external signing service: {}", e.getMessage(), e);
                throw new SignatureControllerException("Unable to consume response from external signing service", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            result = result.trim();
            try {
                return new JSONObject(result);
            } catch (JSONException e) {
                logger.error("Invalid JSON response: [{}]", result, e);
                throw new SignatureControllerException("External service returned invalid JSON", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } else {
            ResponseEntity<String> respEntity = template.postForEntity(uri, entity, String.class);
            logger.debug("Received response: {}", respEntity);

            return new JSONObject(respEntity.getBody());
        }
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

    public void setUseCertificates(boolean useCertificates) {
        this.useCertificates = useCertificates;
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
