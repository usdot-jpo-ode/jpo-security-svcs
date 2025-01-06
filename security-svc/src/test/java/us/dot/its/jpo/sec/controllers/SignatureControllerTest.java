package us.dot.its.jpo.sec.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import mockit.Injectable;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import us.dot.its.jpo.sec.helpers.*;
import us.dot.its.jpo.sec.models.Message;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignatureControllerTest {
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
        mockRestTemplateFactory = mock(RestTemplateFactory.class);
        mockKeyStoreReader = mock(KeyStoreReader.class);
        mockSSLContextFactory = mock(SSLContextFactory.class);
        mockHttpClientFactory = mock(HttpClientFactory.class);
        mockHttpEntityStringifier = mock(HttpEntityStringifier.class);
        uut = new SignatureController(environment, mockRestTemplateFactory, mockKeyStoreReader,
                mockSSLContextFactory, mockHttpClientFactory, mockHttpEntityStringifier);
    }

    @Test
    void testSign_SUCCESS() throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, IOException, SignatureControllerException {
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
    void testSign_ERROR_NoResponse() throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        // prepare
        setUp();
        uut.setUseCertificates(true);
        uut.setCryptoServiceBaseUri("http://example.com/");
        uut.setCryptoServiceEndpointSignPath("endpoint");
        doThrow(new KeyManagementException()).when(mockSSLContextFactory).getSSLContext(any(), any());
        Message message = new Message();
        message.setMsg("test");

        // execute
        assertThrows(SignatureControllerException.class , () -> uut.sign(message));
    }

    @Test
    void testSign_CryptoServiceBaseUriNotSet() {
        // prepare
        setUp();
        uut.setCryptoServiceBaseUri(null);
        uut.setCryptoServiceEndpointSignPath("endpoint");
        Message message = new Message();
        message.setMsg("test");

        assertThrows(SignatureControllerException.class , () -> uut.sign(message));
    }

    @Test
    void testSign_CryptoServiceEndpointSignPathNotSet() {
        // prepare
        setUp();
        uut.setCryptoServiceBaseUri("http://example.com/");
        uut.setCryptoServiceEndpointSignPath(null);
        Message message = new Message();
        message.setMsg("test");

        // execute
        assertThrows(SignatureControllerException.class, () -> uut.sign(message));
    }

    @Test
    void testForwardMessageToExternalService_useCertificates_False() throws JSONException, SignatureControllerException {
        // prepare
        setUp();
        uut.setUseCertificates(false);
        uut.setCryptoServiceBaseUri("http://example.com/");
        uut.setCryptoServiceEndpointSignPath("endpoint");
        var mockResponseEntity = mock(ResponseEntity.class);
        doReturn("{\"result\":\"test\"}").when(mockResponseEntity).getBody();
        var mockTemplate = mock(RestTemplate.class);
        doReturn(mockTemplate).when(mockRestTemplateFactory).getRestTemplate();
        doReturn(mockResponseEntity).when(mockTemplate).postForEntity(any(), any(), any());
        Message message = new Message();
        message.setMsg("test");

        // execute
        JSONObject response = uut.forwardMessageToExternalService(message);

        // verify
        assertEquals("test", response.get("result"));
    }

    @Test
    void testForwardMessageToExternalService_useCertificates_True_SUCCESS() throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, IOException, JSONException, SignatureControllerException {
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
    void testForwardMessageToExternalService_useCertificates_True_ERROR() throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        // prepare
        setUp();
        uut.setUseCertificates(true);
        uut.setCryptoServiceBaseUri("http://example.com/");
        uut.setCryptoServiceEndpointSignPath("endpoint");
        doThrow(new KeyManagementException()).when(mockSSLContextFactory).getSSLContext(any(), any());
        Message message = new Message();
        message.setMsg("test");

        // execute
        assertThrows(SignatureControllerException.class,  () -> uut.forwardMessageToExternalService(message));
    }

    @Test
    void testForwardMessageToExternalService_useCertificates_True_FailureToCreateHttpContext() throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
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
        assertThrows(SignatureControllerException.class,  () -> uut.forwardMessageToExternalService(message));
    }

    @Test
    void testTrimBaseUriAndEndpointPath_TrailingSlashInUri() {
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
    void testTrimBaseUriAndEndpointPath_PrecedingSlashInPath() {
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
