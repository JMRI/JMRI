package jmri.jmrix.lenz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.Sensor;
import jmri.util.JUnitUtil;

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
        assertEquals( "42 05 80 C7", xnis.outbound.elementAt(0).toString(),
            "Sensor Status Request Sent");
    }

    // XNetSensor test for incoming status message
    @Test
    public void testXNetSensorStatusMsg() {
        XNetReply m;

        // Verify this was created in UNKNOWN state
        assertEquals(Sensor.UNKNOWN, t.getKnownState() );

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
        assertEquals( Sensor.ACTIVE, t.getKnownState(), "Known state after activate ");

        m = new XNetReply();
        m.setElement(0, 0x42);     // Opcode for feedback response
        m.setElement(1, 0x05);     // The feedback encoder address
        m.setElement(2, 0x40);     // A bit pattern telling which
        // bits of the lower nibble
        // are on in the message.
        m.setElement(3, 0x07);     // The XOR of everything above
        ((XNetSensor) t).message(m);

        assertEquals( Sensor.INACTIVE, t.getKnownState(), "Known state after inactivate ");

    }

    // XNetSensor test for setting state
    @Test
    public void testXNetSensorSetState() throws jmri.JmriException {
        t.setKnownState( Sensor.ACTIVE);
        assertEquals( Sensor.ACTIVE, t.getKnownState());
        t.setKnownState(jmri.Sensor.INACTIVE);
        assertEquals( Sensor.INACTIVE, t.getKnownState());
    }

    // XNetSensor test for outgoing status request
    @Test
    public void testXNetSensorStatusRequest2() {
        XNetInterfaceScaffold xnis2 = new XNetInterfaceScaffold(new LenzCommandStation());
        XNetSensor s = new XNetSensor("XS513", xnis2, "X");

        s.requestUpdateFromLayout();
        // check that the correct message was sent
        assertEquals( "42 40 80 82", xnis2.outbound.elementAt(0).toString(),
            "Sensor Status Request Sent");

        xnis2.terminateThreads();
    }

    @Override
    @Test
    public void testDispose() throws jmri.JmriException {
        t.setState( Sensor.ACTIVE); // in case registration with TrafficController is deferred to after first use
        assertEquals( 1, numListeners(), "controller listeners ");
        t.dispose();
        // XPressNet leaves one listener after the dispose, should it?
        assertEquals( 1, numListeners(), "controller listeners remaining");
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
        xnis.terminateThreads();
        xnis = null;
        JUnitUtil.tearDown();
    }

}
