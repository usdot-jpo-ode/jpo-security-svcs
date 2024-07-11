package us.dot.its.jpo.sec.controllers;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.Map;

import org.json.JSONException;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;

import mockit.Injectable;
import mockit.Tested;
import us.dot.its.jpo.sec.models.Message;

public class SignatureControllerTest {
    @Tested
    SignatureController testSignatureController;

    @Injectable
    Environment environment;

    @BeforeEach
    public void setUp() {
        testSignatureController = new SignatureController();
    }

    @Test
    public void testSign_useHsm() throws URISyntaxException, JSONException {
        // prepare
        setUp();
        testSignatureController.setUseHsm(true);
        Message message = new Message();
        message.setMsg("test message");    

        // execute
        ResponseEntity<Map<String, String>> response = testSignatureController.sign(message);

        // verify
        assertEquals(1, response.getBody().size());
        assertEquals("test messageNOT IMPLEMENTED", response.getBody().get("result"));
    }

    @Test
    public void testTrimBaseUriAndEndpointPath_TrailingSlashInUri() {
        // prepare
        setUp();
        String baseUri = "http://example.com/";
        String endpointPath = "endpoint";
        String expected = "http://example.com/endpoint";
        
        testSignatureController.setCryptoServiceBaseUri(baseUri);
        testSignatureController.setCryptoServiceEndpointSignPath(endpointPath);

        // execute
        testSignatureController.trimBaseUriAndEndpointPath();

        // verify
        assertEquals(expected, testSignatureController.getCryptoServiceBaseUri() + "/" + testSignatureController.getCryptoServiceEndpointSignPath());
    }

    @Test
    public void testTrimBaseUriAndEndpointPath_PrecedingSlashInPath() {
        // prepare
        setUp();
        String baseUri = "http://example.com";
        String endpointPath = "/endpoint";
        String expected = "http://example.com/endpoint";
        
        testSignatureController.setCryptoServiceBaseUri(baseUri);
        testSignatureController.setCryptoServiceEndpointSignPath(endpointPath);

        // execute
        testSignatureController.trimBaseUriAndEndpointPath();

        // verify
        assertEquals(expected, testSignatureController.getCryptoServiceBaseUri() + "/" + testSignatureController.getCryptoServiceEndpointSignPath());
    }

}
