package jmri.jmrix.dccpp.simulator.simulatedtrains;

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
 * Tests for {@link DCCppSimulatedSpeedSource}: DCC-EX speeds and directions
 * read as a decoder in 128 speed step mode receives them, and the throttle
 * commands the simulated command station receives passed on.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DCCppSimulatedSpeedSourceTest {

    @Test
    public void testCommand() {
        Command c = DCCppSimulatedSpeedSource.command(0, 1, 5);
        assertEquals(0, c.getSpeedStep());
        assertTrue(c.isForward());
        assertFalse(c.isEmergencyStop());
        assertEquals(SpeedStepMode.NMRA_DCC_128, c.getMode());
        assertEquals(5, c.getTimeNanos());

        c = DCCppSimulatedSpeedSource.command(63, 0, 0);
        assertEquals(63, c.getSpeedStep());
        assertFalse(c.isForward(), "direction 0 is reverse");
        assertEquals(0.5, c.getSetting(), 1e-9);

        c = DCCppSimulatedSpeedSource.command(126, 1, 0);
        assertEquals(1.0, c.getSetting(), 1e-9, "126 is full speed");

        c = DCCppSimulatedSpeedSource.command(-1, 1, 0);
        assertTrue(c.isEmergencyStop(), "-1 is an emergency stop");
        assertEquals(0, c.getSpeedStep());

        assertEquals(126, DCCppSimulatedSpeedSource.command(200, 1, 0).getSpeedStep(), "taken as the top step");
    }

    @Test
    public void testReceived() {
        DCCppSimulatedSpeedSource s = new DCCppSimulatedSpeedSource();
        List<DccLocoAddress> told = new ArrayList<>();
        s.addCommandListener(told::add);
        DccLocoAddress shortAddress = new DccLocoAddress(3, false);
        DccLocoAddress longAddress = new DccLocoAddress(4321, true);
        assertNull(s.getCommand(shortAddress), "nothing sent yet");

        s.throttleCommand(3, 40, 1, 1);
        s.throttleCommand(4321, 20, 0, 2);
        assertEquals(40, s.getCommand(shortAddress).getSpeedStep());
        assertEquals(20, s.getCommand(longAddress).getSpeedStep(), "a cab above 127 is a long address");
        assertEquals(List.of(shortAddress, longAddress), told);

        s.throttleCommand(3, 40, 1, 3);
        assertEquals(2, told.size(), "the same command again changes nothing");
        assertEquals(1, s.getCommand(shortAddress).getTimeNanos());

        s.throttleCommand(3, 40, 0, 4);
        assertEquals(3, told.size(), "a change of direction is a change");
        assertFalse(s.getCommand(shortAddress).isForward());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
