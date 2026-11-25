package jmri.jmrix.easydcc.simulator.simulatedtrains;

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
 * Tests for {@link EasyDccSimulatedSpeedSource}: the DCC packets a simulated
 * EasyDCC command station is sent, queued or sent at once, read as a decoder
 * receives them. The packets here are written out
 * from the NMRA standard, not made by JMRI's code.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class EasyDccSimulatedSpeedSourceTest {

    private final DccLocoAddress three = new DccLocoAddress(3, false);
    private final DccLocoAddress longOne = new DccLocoAddress(1234, true);
    private EasyDccSimulatedSpeedSource s;
    private final List<DccLocoAddress> told = new ArrayList<>();

    @Test
    public void test128StepPackets() {
        assertNull(s.getCommand(three), "nothing sent yet");
        s.messageReceived("Q 03 3F C0 FC", 1); // short 3, 128 steps, forward, 0x40: step 63
        Command c = s.getCommand(three);
        assertEquals(63, c.getSpeedStep());
        assertEquals(SpeedStepMode.NMRA_DCC_128, c.getMode());
        assertTrue(c.isForward());
        s.messageReceived("S 02 C4 D2 3F 05 2C", 2); // sent twice // long 1234 (0xC4 0xD2), reverse, 0x05: step 4
        assertEquals(4, s.getCommand(longOne).getSpeedStep());
        assertFalse(s.getCommand(longOne).isForward());
        s.messageReceived("Q 03 3F 81 BD", 3); // emergency stop, forward
        assertTrue(s.getCommand(three).isEmergencyStop());
        assertEquals(List.of(three, longOne, three), told);
    }

    @Test
    public void test28StepPackets() {
        s.messageReceived("Q 03 74 77", 1); // 011 1 0100: forward, C=1, SSSS=0100: value 9, step 6
        Command c = s.getCommand(three);
        assertEquals(SpeedStepMode.NMRA_DCC_28, c.getMode());
        assertEquals(6, c.getSpeedStep());
        assertTrue(c.isForward());
        s.messageReceived("Q 03 41 42", 2); // reverse, value 2: emergency stop
        assertTrue(s.getCommand(three).isEmergencyStop());
        assertFalse(s.getCommand(three).isForward());
        s.messageReceived("Q 03 40 43", 3); // reverse, value 0: stop
        assertEquals(0, s.getCommand(three).getSpeedStep());
        assertFalse(s.getCommand(three).isEmergencyStop());
    }

    @Test
    public void testOtherPacketsIgnored() {
        s.messageReceived("Q FF 00 FF", 1); // idle
        s.messageReceived("Q 00 3F 80 BF", 1); // broadcast
        s.messageReceived("Q 81 F8 79", 1); // accessory
        s.messageReceived("Q 03 80 83", 1); // function group 1
        s.messageReceived("", 1);
        assertTrue(told.isEmpty());
    }

    @Test
    public void testProgrammingMessagesIgnored() {
        s.messageReceived("S1 05", 1); // write register 1
        s.messageReceived("X", 1);
        assertTrue(told.isEmpty());
    }

    @Test
    public void testSameCommandAgainChangesNothing() {
        s.messageReceived("Q 03 3F C0 FC", 1);
        s.messageReceived("Q 03 3F C0 FC", 2);
        assertEquals(1, told.size());
        assertEquals(1, s.getCommand(three).getTimeNanos());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        s = new EasyDccSimulatedSpeedSource();
        s.addCommandListener(told::add);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
