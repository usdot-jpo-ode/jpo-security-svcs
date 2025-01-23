package us.dot.its.jpo.sec.helpers;

import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Component
public class KeyStoreReader {
    
   public KeyStore readStore(String keyStorePath, String keyStorePassword)
           throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
      try (InputStream keyStoreStream = new FileInputStream(keyStorePath)) {
         KeyStore keyStore = KeyStore.getInstance("JKS");
         keyStore.load(keyStoreStream, keyStorePassword.toCharArray());
         return keyStore;
      }
   }

}