package us.dot.its.jpo.sec.helpers;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateFactory {
    
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
