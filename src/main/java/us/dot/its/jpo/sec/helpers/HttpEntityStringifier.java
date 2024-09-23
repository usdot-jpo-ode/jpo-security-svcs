package us.dot.its.jpo.sec.helpers;

import java.io.IOException;

import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

@Component
public class HttpEntityStringifier {
    
    public String stringifyHttpEntity(org.apache.http.HttpEntity apache_entity) throws ParseException, IOException {
        return EntityUtils.toString(apache_entity);
    }
}
