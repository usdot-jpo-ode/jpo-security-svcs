package us.dot.its.jpo.sec.helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

import org.apache.http.ParseException;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import mockit.Tested;

@ExtendWith(MockitoExtension.class)
public class SSLContextFactoryTest {
    
    @Tested
    SSLContextFactory sslContextFactory = new SSLContextFactory();

    @Test
    public void testGetSSLContext() throws ParseException, IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, KeyManagementException, UnrecoverableKeyException {
        // prepare
        KeyStore testKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        char[] password = "password".toCharArray();
        testKeyStore.load(null, password);
        
        // execute
        SSLContext sslContext = sslContextFactory.getSSLContext(testKeyStore, "password");

        // verify
        assert(sslContext != null);
    }
}
