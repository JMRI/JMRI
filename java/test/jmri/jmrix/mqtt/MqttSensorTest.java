package jmri.jmrix.mqtt;

import jmri.util.*;

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

    private MqttAdapterScaffold a = null;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        // prepare an interface
        a = new MqttAdapterScaffold(true);
        t = new MqttSensor(a, "MS1", "track/sensor/1", "track/sensor/1");
        Assertions.assertEquals( 1, a.getPublishCount());
        Assertions.assertNotEquals( "track/sensor/1", a.getLastTopic(),"topic");
    }

    @Override
    @AfterEach
    public void tearDown() {
        t.dispose();
        a.dispose();
        JUnitUtil.tearDown();
    }

    @Override
    public void checkActiveMsgSent() {
        Assertions.assertEquals( "track/sensor/1", a.getLastTopic(), "topic");
        Assertions.assertEquals("ACTIVE", new String(a.getLastPayload()), "payload");
    }

    @Override
    public void checkInactiveMsgSent() {
        Assertions.assertEquals( "track/sensor/1", a.getLastTopic(),"topic");
        Assertions.assertEquals("INACTIVE", new String(a.getLastPayload()), "payload");
    }

    // private final static Logger log = LoggerFactory.getLogger(MqttSensorTest.class);

}
