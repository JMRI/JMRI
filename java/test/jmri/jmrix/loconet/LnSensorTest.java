package jmri.jmrix.loconet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.NotApplicable;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.loconet.LnSensor class.
 *
 * @author Bob Jacobsen Copyright 2001, 2002
 */
public class LnSensorTest extends jmri.implementation.AbstractSensorTestBase {

    private LocoNetInterfaceScaffold lnis = null;

    @Override
    public int numListeners() {return lnis.numListeners();}

    @Override
    public void checkActiveMsgSent() {}

    @Override
    public void checkInactiveMsgSent() {}

    @Override
    public void checkStatusRequestMsgSent() {
        // doesn't send a message right now, pending figuring out what
        // to send.
    }

    @Test
    @Override
    @NotApplicable("status not currently updated when set UNKNOWN / INCONSISTENT")
    public void testSensorSetKnownState() {
    }

    // LnSensor test for incoming status message
    @Test
    public void testLnSensorStatusMsg() {
        // create a new unregistered sensor.
        LnSensor s = new LnSensor("LS044", lnis, "L");
        LocoNetMessage m;

        // notify the Ln that somebody else changed it...
        m = new LocoNetMessage(4);
        m.setOpCode(0xb2);         // OPC_INPUT_REP
        m.setElement(1, 0x15);     // all but lowest bit of address
        m.setElement(2, 0x60);     // Aux (low addr bit high), sensor low
        m.setElement(3, 0x38);
        s.messageFromManager(m);
        assertEquals( jmri.Sensor.INACTIVE, s.getKnownState(), "Known state after inactivate ");

        m = new LocoNetMessage(4);
        m.setOpCode(0xb2);         // OPC_INPUT_REP
        m.setElement(1, 0x15);     // all but lowest bit of address
        m.setElement(2, 0x70);     // Aux (low addr bit high), sensor high
        m.setElement(3, 0x78);
        s.messageFromManager(m);
        assertEquals( jmri.Sensor.ACTIVE, s.getKnownState(), "Known state after activate ");
    }

    @Test
    public void testIncomingReportGivesItsReceivedTime() {
        LnSensor s = new LnSensor("LS044", lnis, "L");
        java.util.List<Long> times = new java.util.ArrayList<>();
        java.util.List<Integer> states = new java.util.ArrayList<>();
        s.addRawStateListener((sensor, oldState, newState, timeNanos) -> {
            states.add(newState);
            times.add(timeNanos);
        });

        LocoNetMessage high = new LocoNetMessage(new int[]{0xB2, 0x15, 0x70, 0x28});
        high.setReceivedTime(1234L);
        s.messageFromManager(high);
        assertEquals(jmri.Sensor.ACTIVE, s.getKnownState());

        long before = System.nanoTime();
        s.messageFromManager(new LocoNetMessage(new int[]{0xB2, 0x15, 0x60, 0x38})); // not stamped
        long after = System.nanoTime();

        assertEquals(java.util.List.of(jmri.Sensor.ACTIVE, jmri.Sensor.INACTIVE), states);
        assertEquals(1234L, times.get(0), "the time the report was received");
        assertTrue(times.get(1) - before >= 0 && after - times.get(1) >= 0, "not stamped: dated when set");
        s.dispose();
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface the sensor t, is unregistered so
        // we must feedback the message manually
        lnis = new LocoNetInterfaceScaffold() {
            @Override
            public void sendLocoNetMessage(LocoNetMessage m) {
                // super logs to debug and saves a copy to outbound.
                super.sendLocoNetMessage(m);
                ((LnSensor)t).messageFromManager(m);
            }
        };
        t = new LnSensor("LS042", lnis, "L");
    }

    @AfterEach
    @Override
    public void tearDown() {
        t.dispose();
        t = null;
        lnis = null;
        JUnitUtil.tearDown();
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LnSensorTest.class);
    
}
