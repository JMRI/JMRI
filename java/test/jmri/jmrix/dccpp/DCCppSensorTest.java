package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * DCCppSensorTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.DCCppSensor class
 *
 * @author	Bob Jacobsen
 * @author	Mark Underwood
 * @author      Paul Bender Copyright (C) 2018
 */
public class DCCppSensorTest extends jmri.implementation.AbstractSensorTestBase  {

    @Override
    public int numListeners() {return 0;}

    @Override
    public void checkOnMsgSent() {}

    @Override
    public void checkOffMsgSent() {}

    @Override
    public void checkStatusRequestMsgSent() {}


    private DCCppInterfaceScaffold xnis = null;

    // DCCppSensor test for incoming status message
    @Test
    public void testDCCppSensorStatusMsg() {
        DCCppReply m;

        // Verify this was created in UNKNOWN state
        Assert.assertTrue(t.getKnownState() == jmri.Sensor.UNKNOWN);

        // notify the Sensor that somebody else changed it...
        m = DCCppReply.parseDCCppReply("Q 4");
        ((DCCppSensor)t).message(m);
        Assert.assertEquals("Known state after activate ", jmri.Sensor.ACTIVE, t.getKnownState());

        m = DCCppReply.parseDCCppReply("q 4");
        ((DCCppSensor)t).message(m);

        Assert.assertEquals("Known state after inactivate ", jmri.Sensor.INACTIVE, t.getKnownState());

    }

    // DCCppSensor test for incoming status message
    @Test
    public void testDCCppSensorInvertStatusMsg() {
        DCCppReply m;

        // Verify this was created in UNKNOWN state
        Assert.assertTrue(t.getKnownState() == jmri.Sensor.UNKNOWN);
        
        // Set the inverted flag
        t.setInverted(true);

        // notify the Sensor that somebody else changed it...
        m = DCCppReply.parseDCCppReply("Q 4");
        ((DCCppSensor)t).message(m);
        Assert.assertEquals("Known state after activate ", jmri.Sensor.INACTIVE, t.getKnownState());

        m = DCCppReply.parseDCCppReply("q 4");
        ((DCCppSensor)t).message(m);

        Assert.assertEquals("Known state after inactivate ", jmri.Sensor.ACTIVE, t.getKnownState());

    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        xnis = new DCCppInterfaceScaffold(new DCCppCommandStation());
        t = new DCCppSensor("DCCPPS04", xnis);
    }

    @Override
    @After
    public void tearDown() {
	t.dispose();
	xnis=null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
