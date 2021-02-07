package jmri.jmrix.rfid;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * TimeoutRfidSensorTest.java
 *
 * Test for the jmri.jmrix.rfid.TimeoutRfidSensor class
 *
 * @author Paul Bender
 */
public class TimeoutRfidSensorTest extends jmri.implementation.AbstractSensorTestBase {

    @Override
    public int numListeners() {return 0;}

    @Override
    public void checkActiveMsgSent() {}

    @Override
    public void checkInactiveMsgSent() {}

    @Override
    public void checkStatusRequestMsgSent() {}

    @Test
    public void test2ParamCtor() {
       TimeoutRfidSensor s = new TimeoutRfidSensor("FSA", "Test");
       Assert.assertNotNull("exists", s);
    }

    @Override
    @BeforeEach
    public void setUp(){
        JUnitUtil.setUp();
        t = new TimeoutRfidSensor("FSA");
    }

    @Override
    @AfterEach
    public void tearDown() {
        t.dispose();
        JUnitUtil.tearDown();
    }

}
