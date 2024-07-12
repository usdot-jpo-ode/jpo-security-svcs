package us.dot.its.jpo.sec.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.net.URISyntaxException;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import mockit.Injectable;
import us.dot.its.jpo.sec.helpers.RestTemplateFactory;
import us.dot.its.jpo.sec.models.Message;

@ExtendWith(MockitoExtension.class)
public class SignatureControllerTest {
    @Mock
    protected RestTemplate mockRestTemplate;

    @Mock
    protected RestTemplateFactory mockRestTemplateFactory;

    @Injectable
    Environment environment;

    @InjectMocks
    SignatureController uut = new SignatureController();

    @BeforeEach
    public void setUp() {
        mockRestTemplateFactory = mock(RestTemplateFactory.class);
        mockRestTemplate = mock(RestTemplate.class);
        doReturn(mockRestTemplate).when(mockRestTemplateFactory).getRestTemplate();
        uut.injectBaseDependencies(environment, mockRestTemplateFactory);
    }

    // @Test
    // public void testSign_SUCCESS() throws URISyntaxException {
    //     // TODO: implement
    // }

    @Test
    public void testSign_CryptoServiceBaseUriNotSet() throws URISyntaxException {
        // prepare
        setUp();
        uut.setCryptoServiceBaseUri(null);
        uut.setCryptoServiceEndpointSignPath("endpoint");
        Message message = new Message();
        message.setMsg("test");
        String expectedWarnString = "Properties sec.cryptoServiceBaseUri=null, sec.cryptoServiceEndpointSignPath=endpoint Not defined. Returning the message unchanged.";

        // execute
        ResponseEntity<Map<String, String>> response = uut.sign(message);

        // verify
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(message.getMsg(), response.getBody().get("result"));
        assertEquals(expectedWarnString, response.getBody().get("warn"));
    }

    @Test
    public void testSign_CryptoServiceEndpointSignPathNotSet() throws URISyntaxException {
        // prepare
        setUp();
        uut.setCryptoServiceBaseUri("http://example.com/");
        uut.setCryptoServiceEndpointSignPath(null);
        Message message = new Message();
        message.setMsg("test");
        String expectedWarnString = "Properties sec.cryptoServiceBaseUri=http://example.com, sec.cryptoServiceEndpointSignPath=null Not defined. Returning the message unchanged.";

        // execute
        ResponseEntity<Map<String, String>> response = uut.sign(message);

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
        
        uut.setCryptoServiceBaseUri(baseUri);
        uut.setCryptoServiceEndpointSignPath(endpointPath);

        // execute
        uut.trimBaseUriAndEndpointPath();

        // verify
        assertEquals(expected, uut.getCryptoServiceBaseUri() + "/" + uut.getCryptoServiceEndpointSignPath());
    }

    @Test
    public void testTrimBaseUriAndEndpointPath_PrecedingSlashInPath() {
        // prepare
        setUp();
        String baseUri = "http://example.com";
        String endpointPath = "/endpoint";
        String expected = "http://example.com/endpoint";
        
        uut.setCryptoServiceBaseUri(baseUri);
        uut.setCryptoServiceEndpointSignPath(endpointPath);

        // execute
        uut.trimBaseUriAndEndpointPath();

        // verify
        assertEquals(expected, uut.getCryptoServiceBaseUri() + "/" + uut.getCryptoServiceEndpointSignPath());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testForwardMessageToExternalService_useCertificates_False() throws URISyntaxException, JSONException {
        // prepare
        setUp();
        uut.setUseCertificates(false);
        uut.setCryptoServiceBaseUri("http://example.com/");
        uut.setCryptoServiceEndpointSignPath("endpoint");
        ResponseEntity<Map<String, String>> mockResponseEntity = mock(ResponseEntity.class);
        doReturn("{\"result\":\"test\"}").when(mockResponseEntity).getBody();
        doReturn(mockResponseEntity).when(mockRestTemplate).postForEntity(any(), any(), any());
        Message message = new Message();
        message.setMsg("test");

        // execute
        JSONObject response = uut.forwardMessageToExternalService(message);

        // verify
        assertEquals("test", response.get("result"));
    }

    // @Test
    // public void testForwardMessageToExternalService_useCertificates_True_SUCCESS() {
    //     // TODO: implement
    // }

    // @Test
    // public void testForwardMessageToExternalService_useCertificates_True_ERROR() {
    //     // TODO: implement
    // }

    // @Test
    // public void testReadStore_SUCCESS() {
    //     // TODO: implement
    // }

    // @Test
    // public void testReadStore_ERROR() {
    //     // TODO: implement
    // }

}
