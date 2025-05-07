package us.dot.its.jpo.sec.helpers;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import mockit.Tested;

@ExtendWith(MockitoExtension.class)
public class RestTemplateFactoryTest {

    @Tested
    RestTemplateFactory restTemplateFactory = new RestTemplateFactory();
    
    @Test
    public void testGetRestTemplate() {
        // execute
        RestTemplate restTemplate = restTemplateFactory.getRestTemplate();

        // verify
        assert(restTemplate != null);
    }
}
