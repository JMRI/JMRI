package jmri.jmrix.mqtt;

import jmri.util.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Bob Jacobsen Coyright (C) 2020
 */
public class MqttSensorTest extends jmri.implementation.AbstractSensorTestBase {
    
    @Override
    public int numListeners() {return 0;}

    @Override
    public void checkStatusRequestMsgSent() {}


    MqttAdapter a;
    String saveTopic;
    byte[] savePayload;
    
    @Override
    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        // prepare an interface
        saveTopic = null;
        savePayload = null;
        a = new MqttAdapter(){
                @Override
                public void publish(String topic, byte[] payload) {
                    saveTopic = topic;
                    savePayload = payload;
                }
            };
        t = new MqttSensor(a, "MS1", "track/sensor/1", "track/sensor/1");
        JUnitAppender.assertWarnMessage("Trying to subscribe before connect/configure is done");
    }

    @Override
    @AfterEach
    public void tearDown() {
        t.dispose();
        JUnitUtil.tearDown();
    }

    @Override
    public void checkActiveMsgSent() {
        Assert.assertEquals("topic", "track/sensor/1", saveTopic);
        Assert.assertEquals("topic", "ACTIVE", new String(savePayload));
    }

    @Override
    public void checkInactiveMsgSent() {
        Assert.assertEquals("topic", "track/sensor/1", saveTopic);
        Assert.assertEquals("topic", "INACTIVE", new String(savePayload));
    }

    // private final static Logger log = LoggerFactory.getLogger(MqttSensorTest.class);

}
