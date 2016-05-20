// LnSensorTest.java
package jmri.jmrix.loconet;
import org.apache.log4j.Logger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.loconet.LnSensor class.
 * @author			Bob Jacobsen  Copyright 2001, 2002
 * @version         $Revision$
 */
public class LnSensorTest extends TestCase {

    public void testLnSensorCreate() {
        // prepare an interface
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        Assert.assertNotNull("exists", lnis );

        LnSensor t = new LnSensor("LS042", lnis, "L");

        // created in UNKNOWN state
        Assert.assertTrue(t.getKnownState() == jmri.Sensor.UNKNOWN);
    }

    // LnSensor test for incoming status message
    public void testLnSensorStatusMsg() {
        // prepare an interface
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

        LnSensor t = new LnSensor("LS044", lnis, "L");
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
        Assert.assertNotNull("exists", lnis );
        

        LnSensor t = new LnSensor("LS043", lnis, "L");

        t.setKnownState(jmri.Sensor.ACTIVE);
        t.setKnownState(jmri.Sensor.INACTIVE);
    }

    // LnSensor test for outgoing status request
    public void testLnSensorStatusRequest() {
        // prepare an interface
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        Assert.assertNotNull("exists", lnis );
        
        LnSensor t = new LnSensor("LS042", lnis, "L");

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

    static Logger log = Logger.getLogger(LnSensorTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
