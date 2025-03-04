package us.dot.its.jpo.sec.models;

import org.junit.Test;

public class MessageTest {

    @Test
    public void testInitialization() {
        // execute
        Message testMessage = new Message();

        // verify
        assert(testMessage.getMsg() == null);
        assert(testMessage.getSigValidityOverride() == 0);
    }

    @Test
    public void testSettersAndGetters() {
        // prepare
        Message testMessage = new Message();
        String testString = "test string";
        int testInt = 42;

        // execute
        testMessage.setMsg(testString);
        testMessage.setSigValidityOverride(testInt);

        // verify
        assert(testMessage.getMsg().equals(testString));
        assert(testMessage.getSigValidityOverride() == testInt);
    }
    
}
