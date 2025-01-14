package us.dot.its.jpo.sec.helpers;

import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Component;

@Component
public class HttpClientFactory {
        
        public HttpClient getHttpClient(SSLContext sslContext) {
            HttpClientBuilder httpClientBuilder = HttpClients.custom();
            if (sslContext == null) {
                return null;
            }
            httpClientBuilder.setSSLContext(sslContext);
            return httpClientBuilder.build();
        }
}
