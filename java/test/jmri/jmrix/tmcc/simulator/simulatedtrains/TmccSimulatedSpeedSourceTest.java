package jmri.jmrix.tmcc.simulator.simulatedtrains;

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
 * Tests for {@link TmccSimulatedSpeedSource}: the commands a simulated TMCC
 * command base is sent, written out here byte by byte from the TMCC 1 and
 * Legacy command formats, read as an engine obeys them.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class TmccSimulatedSpeedSourceTest {

    private final DccLocoAddress three = new DccLocoAddress(3, false);
    private final DccLocoAddress twelve = new DccLocoAddress(12, false);
    private TmccSimulatedSpeedSource s;
    private final List<DccLocoAddress> told = new ArrayList<>();

    @Test
    public void testTmcc1() {
        assertNull(s.getCommand(three), "nothing sent yet");
        s.messageReceived("FE 01 F2", 1); // 00 0000011 11 10010: engine 3, absolute speed 18
        Command c = s.getCommand(three);
        assertEquals(18, c.getSpeedStep());
        assertEquals(SpeedStepMode.TMCC1_32, c.getMode());
        assertTrue(c.isForward(), "an engine starts forward");
        s.messageReceived("FE 01 83", 2); // 00 0000011 00 00011: reverse
        assertFalse(s.getCommand(three).isForward());
        assertEquals(18, s.getCommand(three).getSpeedStep(), "speed kept");
        s.messageReceived("FE 01 81", 3); // toggle
        assertTrue(s.getCommand(three).isForward());
        s.messageReceived("FE 01 C6", 4); // 10 00110: relative +1
        assertEquals(19, s.getCommand(three).getSpeedStep());
        s.messageReceived("FE 01 C0", 5); // relative -5
        assertEquals(14, s.getCommand(three).getSpeedStep());
        s.messageReceived("FE FF FF", 6); // halt
        assertTrue(s.getCommand(three).isEmergencyStop());
        s.messageReceived("FE 01 C6", 7); // relative +1 from the halt
        assertEquals(1, s.getCommand(three).getSpeedStep());
        assertFalse(s.getCommand(three).isEmergencyStop());
        assertEquals(List.of(three, three, three, three, three, three, three), told);
    }

    @Test
    public void testFoundByNumberAlone() {
        s.messageReceived("FE 01 F2", 1);
        assertEquals(18, s.getCommand(new DccLocoAddress(3, true)).getSpeedStep(), "TMCC has no long addresses");
    }

    @Test
    public void testLegacy() {
        s.messageReceived("F8 18 96", 1); // 0001100 0 10010110: engine 12, absolute speed 150 of 200
        Command c = s.getCommand(twelve);
        assertEquals(150, c.getSpeedStep());
        assertEquals(SpeedStepMode.TMCC2_200, c.getMode());
        s.messageReceived("F8 19 72", 2); // 0001100 1 011 10010: absolute speed 18 of 32
        assertEquals(18, s.getCommand(twelve).getSpeedStep());
        assertEquals(SpeedStepMode.TMCC2_32, s.getCommand(twelve).getMode());
        s.messageReceived("F8 19 03", 3); // reverse
        assertFalse(s.getCommand(twelve).isForward());
        s.messageReceived("F8 FF 8B", 4); // halt
        assertTrue(s.getCommand(twelve).isEmergencyStop());
    }

    @Test
    public void testSeveralAndSplitCommands() {
        s.messageReceived("FE 01 F2 FE 01", 1); // a command, then the start of the next
        assertEquals(18, s.getCommand(three).getSpeedStep());
        s.messageReceived("83 F8 18", 2);
        assertFalse(s.getCommand(three).isForward(), "the split command completed");
        assertNull(s.getCommand(twelve), "not complete yet");
        s.messageReceived("96", 3);
        assertEquals(150, s.getCommand(twelve).getSpeedStep());
    }

    @Test
    public void testOtherCommandsIgnored() {
        s.messageReceived("FE C9 E5", 1); // 11001 0011 11 00101: train 3, absolute speed 5
        s.messageReceived("FE 40 9F", 1); // 01 ...: a switch
        s.messageReceived("F9 18 96", 1); // Legacy train 12
        s.messageReceived("FE 01 8D", 1); // engine 3 headlight: not speed or direction
        s.messageReceived("00 12", 1); // not commands
        s.messageReceived("", 1);
        s.messageReceived("XY", 1);
        assertNull(s.getCommand(three));
        assertTrue(told.isEmpty());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        s = new TmccSimulatedSpeedSource();
        s.addCommandListener(told::add);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
