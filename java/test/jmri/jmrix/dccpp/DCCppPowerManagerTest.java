package jmri.jmrix.dccpp;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * DCCppPowerManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.DCCppPowerManager class
 *
 * @author	Paul Bender
 * @author	Mark Underwood (C) 2015
 * @version $Revision$
 *
 * Based on XNetPowerManagerTest
 */
public class DCCppPowerManagerTest extends TestCase {

    public void testCtor() {
        // infrastructure objects
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
	tc.getCommandStation().setCommandStationInfo(DCCppReply.parseDCCppReply("iDCC++BASE STATION FOR ARDUINO MEGA / ARDUINO MOTOR SHIELD: BUILD 24 Nov 2015 23:59:59"));

        DCCppPowerManager c = new DCCppPowerManager(new DCCppSystemConnectionMemo(tc));

        Assert.assertNotNull(c);
    }

    // from here down is testing infrastructure
    public DCCppPowerManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DCCppPowerManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DCCppPowerManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
