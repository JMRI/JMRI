package jmri.jmrix.loconet;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LnSensorTest.class);
    
}
