package us.dot.its.jpo.sec.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import org.springframework.stereotype.Component;

@Component
public class KeyStoreReader {
    
   public KeyStore readStore(String keyStorePath, String keyStorePassword) throws Exception {
      try (InputStream keyStoreStream = new FileInputStream(new File(keyStorePath))) {
         KeyStore keyStore = KeyStore.getInstance("JKS");
         keyStore.load(keyStoreStream, keyStorePassword.toCharArray());
         return keyStore;
      } catch (Exception e) {
         throw new Exception("Error reading keystore", e);
      }
   }

}