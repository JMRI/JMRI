package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.loconet.LnSensor class.
 *
 * @author	Bob Jacobsen Copyright 2001, 2002
 */
public class LnSensorTest {

    @Test
    public void testLnSensorCreate() {
        // prepare an interface
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        Assert.assertNotNull("exists", lnis);

        LnSensor t = new LnSensor("LS042", lnis, "L");

        // created in UNKNOWN state
        Assert.assertTrue(t.getKnownState() == jmri.Sensor.UNKNOWN);
    }

    // LnSensor test for incoming status message
    @Test
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
    @Test
    public void testLnSensorSetState() throws jmri.JmriException {
        // prepare an interface
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        Assert.assertNotNull("exists", lnis);

        LnSensor t = new LnSensor("LS043", lnis, "L");

        t.setKnownState(jmri.Sensor.ACTIVE);
        t.setKnownState(jmri.Sensor.INACTIVE);
    }

    // LnSensor test for outgoing status request
    @Test
    public void testLnSensorStatusRequest() {
        // prepare an interface
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        Assert.assertNotNull("exists", lnis);

        LnSensor t = new LnSensor("LS042", lnis, "L");

        t.requestUpdateFromLayout();
        // doesn't send a message right now, pending figuring out what
        // to send.
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
