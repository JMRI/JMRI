// LnSensorTest.java
package jmri.jmrix.loconet;
import junit.framework.*;

/**
 * Tests for the jmri.jmrix.loconet.LnSensor class
 * @author			Bob Jacobsen  Copyright 2001, 2002
 * @version         $Revision: 1.4 $
 */
public class LnSensorTest extends TestCase {

    public void testLnSensorCreate() {
        // prepare an interface
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

        LnSensor t = new LnSensor("LS042");

        // created in UNKNOWN state
        Assert.assertTrue(t.getKnownState() == jmri.Sensor.UNKNOWN);
    }

    // LnSensor test for incoming status message
    public void testLnSensorStatusMsg() {
        // prepare an interface
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

        LnSensor t = new LnSensor("LS043");
        LocoNetMessage m;

        // notify the Ln that somebody else changed it...

        m = new LocoNetMessage(4);
        m.setOpCode(0xb2);         // OPC_INPUT_REP
        m.setElement(1, 0x15);     // all but lowest bit of address
        m.setElement(2, 0x60);     // Aux (low addr bit high), sensor low
        m.setElement(3, 0x38);
        lnis.sendTestMessage(m);
        Assert.assertEquals("Known state after inactivate ", jmri.Sensor.INACTIVE, t.getKnownState());

        m = new LocoNetMessage(4);
        m.setOpCode(0xb2);         // OPC_INPUT_REP
        m.setElement(1, 0x15);     // all but lowest bit of address
        m.setElement(2, 0x70);     // Aux (low addr bit high), sensor high
        m.setElement(3, 0x78);
        lnis.sendTestMessage(m);
        Assert.assertEquals("Known state after activate ", jmri.Sensor.ACTIVE, t.getKnownState());
    }


    // LnSensor test for setting state
    public void testLnSensorSetState() throws jmri.JmriException {
        // prepare an interface
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

        LnSensor t = new LnSensor("LS043");

        t.setKnownState(jmri.Sensor.ACTIVE);
        t.setKnownState(jmri.Sensor.INACTIVE);
    }

    // LnSensor test for outgoing status request
    public void testLnSensorStatusRequest() {
        // prepare an interface
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

        LnSensor t = new LnSensor("LS042");

        t.requestUpdateFromLayout();
        // doesn't send a message right now, pending figuring out what
        // to send.
    }

    // from here down is testing infrastructure

    public LnSensorTest(String s) {
    	super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LnSensor.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LnSensorTest.class);
        return suite;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnSensorTest.class.getName());

    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

}
