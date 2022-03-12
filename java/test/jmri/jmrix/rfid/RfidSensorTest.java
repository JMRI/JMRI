package jmri.jmrix.rfid;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * RfidSensorTest.java
 *
 * Test for the jmri.jmrix.rfid.RfidSensor class
 *
 * @author Paul Bender
 */
public class RfidSensorTest extends jmri.implementation.AbstractSensorTestBase {

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
       RfidSensor s = new RfidSensor("FSA", "Test");
       Assert.assertNotNull("exists", s);
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        t = new RfidSensor("FSA");
    }

    @Override
    @AfterEach
    public void tearDown() {
        t.dispose();
        JUnitUtil.tearDown();
    }

}
