package jmri.jmrix.rfid;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * TimeoutRfidSensorTest.java
 *
 * Description:	tests for the jmri.jmrix.rfid.TimeoutRfidSensor class
 *
 * @author	Paul Bender
 */
public class TimeoutRfidSensorTest extends jmri.implementation.AbstractSensorTestBase {

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
       TimeoutRfidSensor s = new TimeoutRfidSensor("FSA", "Test");
       Assert.assertNotNull("exists", s);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp(){
        JUnitUtil.setUp();
        t = new TimeoutRfidSensor("FSA");
    }

    @Override
    @After
    public void tearDown() {
        t.dispose();
        JUnitUtil.tearDown();
    }

}
