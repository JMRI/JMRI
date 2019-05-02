package jmri.jmrix.rfid;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * RfidSensorTest.java
 *
 * Description:	tests for the jmri.jmrix.rfid.RfidSensor class
 *
 * @author	Paul Bender
 */
public class RfidSensorTest extends jmri.implementation.AbstractSensorTestBase {

    @Override
    public int numListeners() {return 0;}

    @Override
    public void checkOnMsgSent() {}

    @Override
    public void checkOffMsgSent() {}

    @Override
    public void checkStatusRequestMsgSent() {}

    @Test
    public void test2ParamCtor() {
       RfidSensor s = new RfidSensor("FSA", "Test");
       Assert.assertNotNull("exists", s);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        t = new RfidSensor("FSA");
    }

    @Override
    @After
    public void tearDown() {
        t.dispose();
        JUnitUtil.tearDown();
    }

}
