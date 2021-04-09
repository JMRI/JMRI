package jmri.jmrix.lenz;

import jmri.Sensor;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.lenz.XNetSensor class.
 *
 * @author Paul Bender Copyright 2004
 */
public class XNetSensorTest extends jmri.implementation.AbstractSensorTestBase {

    private XNetInterfaceScaffold xnis = null;

    @Override
    public int numListeners() {
        return xnis.numListeners();
    }

    @Override
    public void checkActiveMsgSent() {
    }

    @Override
    public void checkInactiveMsgSent() {
    }

    @Override
    public void checkStatusRequestMsgSent() {
        Assert.assertEquals("Sensor Status Request Sent", "42 05 80 C7", xnis.outbound.elementAt(0).toString());
    }

    // XNetSensor test for incoming status message
    @Test
    public void testXNetSensorStatusMsg() {
        XNetReply m;

        // Verify this was created in UNKNOWN state
        Assert.assertEquals(t.getKnownState(), Sensor.UNKNOWN);

        // notify the Sensor that somebody else changed it...
        m = new XNetReply();
        m.setElement(0, 0x42);     // Opcode for feedback response
        m.setElement(1, 0x05);     // The feedback encoder address
        m.setElement(2, 0x42);     // A bit pattern telling which
        // bits of the lower nibble
        // are on in the message.
        m.setElement(3, 0x05);     // The XOR of everything above
        ((XNetSensor) t).message(m);
        jmri.util.JUnitUtil.waitFor(() -> t.getState() == t.getRawState(), "raw state = state");
        Assert.assertEquals("Known state after activate ", jmri.Sensor.ACTIVE, t.getKnownState());

        m = new XNetReply();
        m.setElement(0, 0x42);     // Opcode for feedback response
        m.setElement(1, 0x05);     // The feedback encoder address
        m.setElement(2, 0x40);     // A bit pattern telling which
        // bits of the lower nibble
        // are on in the message.
        m.setElement(3, 0x07);     // The XOR of everything above
        ((XNetSensor) t).message(m);

        Assert.assertEquals("Known state after inactivate ", jmri.Sensor.INACTIVE, t.getKnownState());

    }

    // XNetSensor test for setting state
    @Test
    public void testXNetSensorSetState() throws jmri.JmriException {
        t.setKnownState(jmri.Sensor.ACTIVE);
        Assert.assertEquals(t.getKnownState(), Sensor.ACTIVE);
        t.setKnownState(jmri.Sensor.INACTIVE);
        Assert.assertEquals(t.getKnownState(), Sensor.INACTIVE);
    }

    // XNetSensor test for outgoing status request
    @Test
    public void testXNetSensorStatusRequest2() {
        XNetInterfaceScaffold xnis2 = new XNetInterfaceScaffold(new LenzCommandStation());
        XNetSensor s = new XNetSensor("XS513", xnis2, "X");

        s.requestUpdateFromLayout();
        // check that the correct message was sent
        Assert.assertEquals("Sensor Status Request Sent", "42 40 80 82", xnis2.outbound.elementAt(0).toString());

    }

    @Override
    @Test
    public void testDispose() throws jmri.JmriException {
        t.setState(jmri.Sensor.ACTIVE); // in case registration with TrafficController is deferred to after first use
        Assert.assertEquals("controller listeners ", 1, numListeners());
        t.dispose();
        // XPressNet leaves one listener after the dispose, should it?
        Assert.assertEquals("controller listeners remaining", 1, numListeners());
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        xnis = new XNetInterfaceScaffold(new LenzCommandStation());
        t = new XNetSensor("XS042", xnis, "X");
    }

    @Override
    @AfterEach
    public void tearDown() {
        t.dispose();
        xnis = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
