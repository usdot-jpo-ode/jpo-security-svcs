package us.dot.its.jpo.sec.helpers;

import static org.junit.Assert.assertThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import mockit.Tested;

@ExtendWith(MockitoExtension.class)
public class KeyStoreReaderTest {

    @Tested
    KeyStoreReader keyStoreReader = new KeyStoreReader();

    private void createKeyStoreForTesting() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore testKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        char[] password = "password".toCharArray();
        testKeyStore.load(null, password);
        testKeyStore.store(new FileOutputStream("src/test/resources/test.jks"), password);
    }

    private void setUp() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        // create resources directory if it doesn't exist
        File resourcesDir = new File("src/test/resources");
        if (!resourcesDir.exists()) {
            resourcesDir.mkdir();
        }
        File file = new File("src/test/resources/test.jks");
        if (file.exists()) {
            file.delete();
        }
        createKeyStoreForTesting();
    }
    
    @Test
    public void testReadStore_Success() throws Exception {
        // prepare
        setUp();
        String keyStorePath = "src/test/resources/test.jks";
        String keyStorePassword = "password";
        
        // execute
        KeyStore keyStore = keyStoreReader.readStore(keyStorePath, keyStorePassword);

        // verify
        assert(keyStore != null);
    }

    @Test
    public void testReadStore_Failure_WrongPassword() throws Exception {
        // prepare
        setUp();
        String keyStorePath = "src/test/resources/test.jks";
        String keyStorePassword = "wrongpassword";
        
        // execute
        assertThrows(Exception.class, () -> {
            KeyStore keyStore = keyStoreReader.readStore(keyStorePath, keyStorePassword);
        });
    }

    @Test
    public void testReadStore_Failure_WrongPath() throws Exception {
        // prepare
        setUp();
        String keyStorePath = "src/test/resources/wrong.jks";
        String keyStorePassword = "password";
        
        // execute
        assertThrows(Exception.class, () -> {
            KeyStore keyStore = keyStoreReader.readStore(keyStorePath, keyStorePassword);
        });
    }
}
