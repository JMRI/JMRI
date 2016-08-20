package jmri.jmrix.dccpp;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * DCCppSensorTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.DCCppSensor class
 *
 * @author	Bob Jacobsen
 * @author	Mark Underwood
 */
public class DCCppSensorTest extends TestCase {

    public void testDCCppSensorCreate() {
        DCCppInterfaceScaffold xnis = new DCCppInterfaceScaffold(new DCCppCommandStation());
        DCCppSensor t = new DCCppSensor("DCCPPS042", xnis);

        // created in UNKNOWN state
        Assert.assertTrue(t.getKnownState() == jmri.Sensor.UNKNOWN);
    }

    // DCCppSensor test for incoming status message
    public void testDCCppSensorStatusMsg() {
        DCCppInterfaceScaffold xnis = new DCCppInterfaceScaffold(new DCCppCommandStation());
        Assert.assertNotNull("exists", xnis);

        DCCppSensor t = new DCCppSensor("DCCPPS04", xnis);
        DCCppReply m;

        // Verify this was created in UNKNOWN state
        Assert.assertTrue(t.getKnownState() == jmri.Sensor.UNKNOWN);

        // notify the Sensor that somebody else changed it...
        m = DCCppReply.parseDCCppReply("Q 4");
        t.message(m);
        Assert.assertEquals("Known state after activate ", jmri.Sensor.ACTIVE, t.getKnownState());

        m = DCCppReply.parseDCCppReply("q 4");
        t.message(m);

        Assert.assertEquals("Known state after inactivate ", jmri.Sensor.INACTIVE, t.getKnownState());

    }

    // DCCppSensor test for setting state
    public void testDCCppSensorSetState() throws jmri.JmriException {
        DCCppInterfaceScaffold xnis = new DCCppInterfaceScaffold(new DCCppCommandStation());
        DCCppSensor t = new DCCppSensor("DCCPPS043", xnis);

        t.setKnownState(jmri.Sensor.ACTIVE);
        Assert.assertTrue(t.getKnownState() == jmri.Sensor.ACTIVE);
        t.setKnownState(jmri.Sensor.INACTIVE);
        Assert.assertTrue(t.getKnownState() == jmri.Sensor.INACTIVE);
    }

    /* Functions not supported

    // DCCppSensor test for outgoing status request
    public void testDCCppSensorStatusRequest() {
        DCCppInterfaceScaffold xnis = new DCCppInterfaceScaffold(new DCCppCommandStation());

        DCCppSensor t = new DCCppSensor("XS042", xnis);

        t.requestUpdateFromLayout();
        // check that the correct message was sent
        Assert.assertEquals("Sensor Status Request Sent", "42 05 80 C7", xnis.outbound.elementAt(0).toString());
    }

    // DCCppSensor test for outgoing status request
    public void testDCCppSensorStatusRequest2() {
        DCCppInterfaceScaffold xnis = new DCCppInterfaceScaffold(new DCCppCommandStation());

        DCCppSensor t = new DCCppSensor("XS513", xnis);

        t.requestUpdateFromLayout();
        // check that the correct message was sent
        Assert.assertEquals("Sensor Status Request Sent", "42 40 80 82", xnis.outbound.elementAt(0).toString());
    }
    */

    // from here down is testing infrastructure
    public DCCppSensorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DCCppSensorTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DCCppSensorTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

}
