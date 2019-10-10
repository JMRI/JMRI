package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * DCCppPowerManagerTest.java
 *
 * Description: tests for the jmri.jmrix.dccpp.DCCppPowerManager class
 *
 * @author Paul Bender
 * @author Mark Underwood (C) 2015
 *
 *         Based on XNetPowerManagerTest
 */
public class DCCppPowerManagerTest {

    @Test
    public void testCtor() {
        // infrastructure objects
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
        tc.getCommandStation().setCommandStationInfo(DCCppReply.parseDCCppReply(
                "iDCC++BASE STATION FOR ARDUINO MEGA / ARDUINO MOTOR SHIELD: BUILD 24 Nov 2015 23:59:59"));

        DCCppPowerManager c = new DCCppPowerManager(new DCCppSystemConnectionMemo(tc));

        Assert.assertNotNull(c);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
