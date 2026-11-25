package jmri.jmrix.mrc.simulator.simulatedtrains;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jmri.DccLocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrit.simulation.SimulatedSpeedSource.Command;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link MrcSimulatedSpeedSource}: the throttle packets a simulated
 * MRC command station is sent, written out here byte by byte, read as a
 * decoder receives their speed.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class MrcSimulatedSpeedSourceTest {

    private final DccLocoAddress three = new DccLocoAddress(3, false);
    private final DccLocoAddress longOne = new DccLocoAddress(1234, true);
    private MrcSimulatedSpeedSource s;
    private final List<DccLocoAddress> told = new ArrayList<>();

    @Test
    public void test128Steps() {
        assertNull(s.getCommand(three), "nothing sent yet");
        s.messageReceived("25 00 25 00 00 00 03 00 C0 00 02 00 C1 00", 1); // short 3, forward, 0x40: step 63
        Command c = s.getCommand(three);
        assertEquals(63, c.getSpeedStep());
        assertEquals(SpeedStepMode.NMRA_DCC_128, c.getMode());
        assertTrue(c.isForward());
        s.messageReceived("25 00 25 00 C4 00 D2 00 05 00 02 00 13 00", 2); // long 1234, reverse, step 4
        assertEquals(4, s.getCommand(longOne).getSpeedStep());
        assertFalse(s.getCommand(longOne).isForward());
        s.messageReceived("25 00 25 00 00 00 03 00 81 00 02 00 80 00", 3); // emergency stop
        assertTrue(s.getCommand(three).isEmergencyStop());
        assertEquals(List.of(three, longOne, three), told);
    }

    @Test
    public void test28Steps() {
        s.messageReceived("25 00 25 00 00 00 03 00 74 00 00 00 77 00", 1); // 011 1 0100: forward, value 9, step 6
        Command c = s.getCommand(three);
        assertEquals(SpeedStepMode.NMRA_DCC_28, c.getMode());
        assertEquals(6, c.getSpeedStep());
        assertTrue(c.isForward());
        s.messageReceived("25 00 25 00 00 00 03 00 41 00 00 00 42 00", 2); // reverse, value 2: emergency stop
        assertTrue(s.getCommand(three).isEmergencyStop());
    }

    @Test
    public void testOtherMessagesIgnored() {
        s.messageReceived("26 00 26 00 00 00 03 00 01 00", 1); // not a throttle packet
        s.messageReceived("25 00 25 00", 1); // too short
        s.messageReceived("", 1);
        assertTrue(told.isEmpty());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        s = new MrcSimulatedSpeedSource();
        s.addCommandListener(told::add);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
