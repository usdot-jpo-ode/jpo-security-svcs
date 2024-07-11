package us.dot.its.jpo.sec.controllers;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.Map;

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
    public void testSign_CryptoServiceBaseUriNotSet() throws URISyntaxException {
        // prepare
        setUp();
        testSignatureController.setCryptoServiceBaseUri(null);
        testSignatureController.setCryptoServiceEndpointSignPath("endpoint");
        Message message = new Message();
        message.setMsg("test");
        String expectedWarnString = "Properties sec.cryptoServiceBaseUri=null, sec.cryptoServiceEndpointSignPath=endpoint Not defined. Returning the message unchanged.";

        // execute
        ResponseEntity<Map<String, String>> response = testSignatureController.sign(message);

        // verify
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(message.getMsg(), response.getBody().get("result"));
        assertEquals(expectedWarnString, response.getBody().get("warn"));
    }

    @Test
    public void testSign_CryptoServiceEndpointSignPathNotSet() throws URISyntaxException {
        // prepare
        setUp();
        testSignatureController.setCryptoServiceBaseUri("http://example.com/");
        testSignatureController.setCryptoServiceEndpointSignPath(null);
        Message message = new Message();
        message.setMsg("test");
        String expectedWarnString = "Properties sec.cryptoServiceBaseUri=http://example.com, sec.cryptoServiceEndpointSignPath=null Not defined. Returning the message unchanged.";

        // execute
        ResponseEntity<Map<String, String>> response = testSignatureController.sign(message);

        // verify
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(message.getMsg(), response.getBody().get("result"));
        assertEquals(expectedWarnString, response.getBody().get("warn"));
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
