package us.dot.its.jpo.sec.helpers;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.http.client.HttpClient;

import mockit.Tested;

@ExtendWith(MockitoExtension.class)
public class HttpClientFactoryTest {

    @Tested
    HttpClientFactory httpClientFactory = new HttpClientFactory();
    
    @Test
    public void testGetHttpClient_nullSslContext() {
        // execute
        HttpClient httpClient = httpClientFactory.getHttpClient(null);

        // verify
        assert(httpClient == null);
    }
}
