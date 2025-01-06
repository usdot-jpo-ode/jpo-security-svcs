package us.dot.its.jpo.sec.helpers;

import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.springframework.stereotype.Component;

@Component
public class KeyStoreReader {
    
   public KeyStore readStore(String keyStorePath, String keyStorePassword)
           throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
      try (InputStream keyStoreStream = new FileInputStream(new File(keyStorePath))) {
         KeyStore keyStore = KeyStore.getInstance("JKS");
         keyStore.load(keyStoreStream, keyStorePassword.toCharArray());
         return keyStore;
      }
   }

}