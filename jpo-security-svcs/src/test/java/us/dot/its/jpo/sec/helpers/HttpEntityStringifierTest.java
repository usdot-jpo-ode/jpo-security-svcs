package us.dot.its.jpo.sec.helpers;

import java.io.IOException;

import org.apache.http.ParseException;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import mockit.Tested;

@ExtendWith(MockitoExtension.class)
public class HttpEntityStringifierTest {
    
    @Tested
    HttpEntityStringifier httpEntityStringifier = new HttpEntityStringifier();

    @Test
    public void testStringifyHttpEntity() throws ParseException, IOException {
        // prepare
        org.apache.http.HttpEntity apache_entity = new org.apache.http.entity.StringEntity("test");
        
        // execute
        String stringified = httpEntityStringifier.stringifyHttpEntity(apache_entity);

        // verify
        assert(stringified.equals("test"));
    }
}
