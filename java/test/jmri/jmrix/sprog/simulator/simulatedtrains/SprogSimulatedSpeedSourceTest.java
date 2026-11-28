package jmri.jmrix.sprog.simulator.simulatedtrains;

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
 * Tests for {@link SprogSimulatedSpeedSource}: the messages a simulated SPROG
 * receives, as a command station (DCC packets) and as a programmer with one
 * throttle, read as a decoder receives them. The packets here are written out
 * from the NMRA standard, not made by JMRI's code.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class SprogSimulatedSpeedSourceTest {

    private final DccLocoAddress three = new DccLocoAddress(3, false);
    private final DccLocoAddress longOne = new DccLocoAddress(1234, true);
    private SprogSimulatedSpeedSource s;
    private final List<DccLocoAddress> told = new ArrayList<>();

    @Test
    public void test128StepPackets() {
        assertNull(s.getCommand(three), "nothing sent yet");
        s.messageReceived("O 03 3F C0 FC", 1); // short 3, 128 steps, forward, 0x40: step 63
        Command c = s.getCommand(three);
        assertEquals(63, c.getSpeedStep());
        assertEquals(SpeedStepMode.NMRA_DCC_128, c.getMode());
        assertTrue(c.isForward());
        s.messageReceived("O C4 D2 3F 05 2C", 2); // long 1234 (0xC4 0xD2), reverse, 0x05: step 4
        assertEquals(4, s.getCommand(longOne).getSpeedStep());
        assertFalse(s.getCommand(longOne).isForward());
        s.messageReceived("O 03 3F 81 BD", 3); // emergency stop, forward
        assertTrue(s.getCommand(three).isEmergencyStop());
        assertEquals(List.of(three, longOne, three), told);
    }

    @Test
    public void test28StepPackets() {
        s.messageReceived("O 03 74 77", 1); // 011 1 0100: forward, C=1, SSSS=0100: value 9, step 6
        Command c = s.getCommand(three);
        assertEquals(SpeedStepMode.NMRA_DCC_28, c.getMode());
        assertEquals(6, c.getSpeedStep());
        assertTrue(c.isForward());
        s.messageReceived("O 03 41 42", 2); // reverse, value 2: emergency stop
        assertTrue(s.getCommand(three).isEmergencyStop());
        assertFalse(s.getCommand(three).isForward());
        s.messageReceived("O 03 40 43", 3); // reverse, value 0: stop
        assertEquals(0, s.getCommand(three).getSpeedStep());
        assertFalse(s.getCommand(three).isEmergencyStop());
    }

    @Test
    public void testOtherPacketsIgnored() {
        s.messageReceived("O FF 00 FF", 1); // idle
        s.messageReceived("O 00 3F 80 BF", 1); // broadcast
        s.messageReceived("O 81 F8 79", 1); // accessory
        s.messageReceived("O 03 80 83", 1); // function group 1
        s.messageReceived("I", 1);
        s.messageReceived("", 1);
        assertTrue(told.isEmpty());
    }

    @Test
    public void testProgrammerModeThrottle() {
        s.messageReceived(">20", 1);
        assertTrue(told.isEmpty(), "no address yet");
        s.messageReceived("A 3", 2);
        s.messageReceived(">20", 3); // 128 steps by default: value 20, step 19
        assertEquals(19, s.getCommand(three).getSpeedStep());
        assertTrue(s.getCommand(three).isForward());
        s.messageReceived("M h400", 4);
        s.messageReceived("<9", 5); // 28 steps: value 9, step 6, reverse
        assertEquals(6, s.getCommand(three).getSpeedStep());
        assertEquals(SpeedStepMode.NMRA_DCC_28, s.getCommand(three).getMode());
        assertFalse(s.getCommand(three).isForward());
        s.messageReceived("A 1234 0", 6);
        s.messageReceived("M h800", 7);
        s.messageReceived(">127", 8);
        assertEquals(126, s.getCommand(longOne).getSpeedStep(), "a long address");
    }

    @Test
    public void testSameCommandAgainChangesNothing() {
        s.messageReceived("O 03 3F C0 FC", 1);
        s.messageReceived("O 03 3F C0 FC", 2);
        assertEquals(1, told.size());
        assertEquals(1, s.getCommand(three).getTimeNanos());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        s = new SprogSimulatedSpeedSource();
        s.addCommandListener(told::add);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
