package us.dot.its.jpo.sec.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.URISyntaxException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;

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
import us.dot.its.jpo.sec.helpers.HttpClientFactory;
import us.dot.its.jpo.sec.helpers.HttpEntityStringifier;
import us.dot.its.jpo.sec.helpers.KeyStoreReader;
import us.dot.its.jpo.sec.helpers.RestTemplateFactory;
import us.dot.its.jpo.sec.helpers.SSLContextFactory;
import us.dot.its.jpo.sec.models.Message;

@ExtendWith(MockitoExtension.class)
public class SignatureControllerTest {
    @Mock
    protected RestTemplate mockRestTemplate;

    @Mock
    protected RestTemplateFactory mockRestTemplateFactory;

    @Mock
    protected KeyStoreReader mockKeyStoreReader;

    @Mock
    protected SSLContextFactory mockSSLContextFactory;

    @Mock
    protected HttpClientFactory mockHttpClientFactory;

    @Mock
    HttpEntityStringifier mockHttpEntityStringifier;

    @Injectable
    Environment environment;

    @InjectMocks
    SignatureController uut;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        mockRestTemplate = mock(RestTemplate.class);
        mockRestTemplateFactory = mock(RestTemplateFactory.class);
        doReturn(mockRestTemplate).when(mockRestTemplateFactory).getRestTemplate();
        mockKeyStoreReader = mock(KeyStoreReader.class);
        mockSSLContextFactory = mock(SSLContextFactory.class);
        mockHttpClientFactory = mock(HttpClientFactory.class);
        mockHttpEntityStringifier = mock(HttpEntityStringifier.class);
        uut = new SignatureController(environment, mockRestTemplateFactory, mockKeyStoreReader,
                mockSSLContextFactory, mockHttpClientFactory, mockHttpEntityStringifier);
    }

    @Test
    public void testSign_SUCCESS() throws URISyntaxException, KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, ClientProtocolException, IOException {
        // prepare
        setUp();
        uut.setUseCertificates(true);
        uut.setCryptoServiceBaseUri("http://example.com/");
        uut.setCryptoServiceEndpointSignPath("endpoint");
        SSLContext mockSSLContext = mock(SSLContext.class);
        doReturn(mockSSLContext).when(mockSSLContextFactory).getSSLContext(any(), any());
        HttpClient mockHttpClient = mock(HttpClient.class);
        doReturn(mockHttpClient).when(mockHttpClientFactory).getHttpClient(mockSSLContext);
        HttpResponse mockHttpResponse = mock(HttpResponse.class);
        doReturn(mockHttpResponse).when(mockHttpClient).execute(any());
        org.apache.http.HttpEntity mockHttpEntity = mock(org.apache.http.HttpEntity.class);
        doReturn(mockHttpEntity).when(mockHttpResponse).getEntity();
        doReturn("{\"message-signed\":\"test12345\",\"message-expiry\":1}").when(mockHttpEntityStringifier).stringifyHttpEntity(mockHttpEntity);
        Message message = new Message();
        message.setMsg("test");

        // execute
        var response = uut.sign(message);

        // verify
        assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"messageExpiry\":\"1\",\"messageSigned\":\"test12345\"}", objectMapper.writeValueAsString(response.getBody()));
    }

    @Test
    public void testSign_ERROR_NoResponse() throws URISyntaxException, KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, JsonProcessingException {
        // prepare
        setUp();
        uut.setUseCertificates(true);
        uut.setCryptoServiceBaseUri("http://example.com/");
        uut.setCryptoServiceEndpointSignPath("endpoint");
        doThrow(new KeyManagementException()).when(mockSSLContextFactory).getSSLContext(any(), any());
        Message message = new Message();
        message.setMsg("test");

        // execute
        var response = uut.sign(message);

        // verify
        assertEquals(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error communicating with external service", objectMapper.writeValueAsString(response.getBody()));
    }

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
        var response = uut.sign(message);

        // verify
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(message.getMsg(), response.getBody().getMessageSigned());
        assertEquals(expectedWarnString, response.getBody().getMessageSigned());
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
        var response = uut.sign(message);

        // verify
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(message.getMsg(), response.getBody().getMessageSigned());
        assertEquals(expectedWarnString, response.getBody().getMessageSigned());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testForwardMessageToExternalService_useCertificates_False() throws URISyntaxException, JSONException {
        // prepare
        setUp();
        uut.setUseCertificates(false);
        uut.setCryptoServiceBaseUri("http://example.com/");
        uut.setCryptoServiceEndpointSignPath("endpoint");
        var mockResponseEntity = mock(ResponseEntity.class);
        doReturn("{\"result\":\"test\"}").when(mockResponseEntity).getBody();
        doReturn(mockResponseEntity).when(mockRestTemplate).postForEntity(any(), any(), any());
        Message message = new Message();
        message.setMsg("test");

        // execute
        JSONObject response = uut.forwardMessageToExternalService(message);

        // verify
        assertEquals("test", response.get("result"));
    }

    @Test
    public void testForwardMessageToExternalService_useCertificates_True_SUCCESS() throws URISyntaxException, KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, ClientProtocolException, IOException, JSONException {
        // prepare
        setUp();
        uut.setUseCertificates(true);
        uut.setCryptoServiceBaseUri("http://example.com/");
        uut.setCryptoServiceEndpointSignPath("endpoint");
        SSLContext mockSSLContext = mock(SSLContext.class);
        doReturn(mockSSLContext).when(mockSSLContextFactory).getSSLContext(any(), any());
        HttpClient mockHttpClient = mock(HttpClient.class);
        doReturn(mockHttpClient).when(mockHttpClientFactory).getHttpClient(mockSSLContext);
        HttpResponse mockHttpResponse = mock(HttpResponse.class);
        doReturn(mockHttpResponse).when(mockHttpClient).execute(any());
        org.apache.http.HttpEntity mockHttpEntity = mock(org.apache.http.HttpEntity.class);
        doReturn(mockHttpEntity).when(mockHttpResponse).getEntity();
        doReturn("{\"result\":\"test\"}").when(mockHttpEntityStringifier).stringifyHttpEntity(mockHttpEntity);
        Message message = new Message();
        message.setMsg("test");

        // execute
        JSONObject response = uut.forwardMessageToExternalService(message);

        // verify
        assertEquals("test", response.get("result"));
    }

    @Test
    public void testForwardMessageToExternalService_useCertificates_True_ERROR() throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, URISyntaxException {
        // prepare
        setUp();
        uut.setUseCertificates(true);
        uut.setCryptoServiceBaseUri("http://example.com/");
        uut.setCryptoServiceEndpointSignPath("endpoint");
        doThrow(new KeyManagementException()).when(mockSSLContextFactory).getSSLContext(any(), any());
        Message message = new Message();
        message.setMsg("test");

        // execute
        JSONObject response = uut.forwardMessageToExternalService(message);

        // verify
        assertNull(response);
    }

    @Test
    public void testForwardMessageToExternalService_useCertificates_True_FailureToCreateHttpContext() throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, ClientProtocolException, IOException, URISyntaxException {
        // prepare
        setUp();
        uut.setUseCertificates(true);
        uut.setCryptoServiceBaseUri("http://example.com/");
        uut.setCryptoServiceEndpointSignPath("endpoint");
        SSLContext mockSSLContext = mock(SSLContext.class);
        doReturn(mockSSLContext).when(mockSSLContextFactory).getSSLContext(any(), any());
        doReturn(null).when(mockHttpClientFactory).getHttpClient(mockSSLContext);
        Message message = new Message();
        message.setMsg("test");

        // execute
        JSONObject response = uut.forwardMessageToExternalService(message);

        // verify
        assertNull(response);
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

}
