package us.dot.its.jpo.sec.helpers;

import org.springframework.stereotype.Component;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;
import org.apache.http.ssl.SSLContexts;

@Component
public class SSLContextFactory {
    
    public SSLContext getSSLContext(KeyStore keyStore, String keyStorePassword) throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        return SSLContexts.custom()
                .loadKeyMaterial(keyStore, keyStorePassword.toCharArray())
                .build();
    }
}
