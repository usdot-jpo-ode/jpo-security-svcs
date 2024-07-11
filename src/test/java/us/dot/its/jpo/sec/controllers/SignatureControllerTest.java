package us.dot.its.jpo.sec.controllers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.core.env.Environment;
import mockit.Injectable;
import mockit.Tested;

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
