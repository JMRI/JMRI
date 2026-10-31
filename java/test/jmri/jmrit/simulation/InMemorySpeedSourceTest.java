package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jmri.DccLocoAddress;
import jmri.SpeedStepMode;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link InMemorySpeedSource} and its commands.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class InMemorySpeedSourceTest {

    private static final DccLocoAddress THREE = new DccLocoAddress(3, false);

    @Test
    public void testCommandsAndNotice() {
        InMemorySpeedSource source = new InMemorySpeedSource();
        List<DccLocoAddress> told = new ArrayList<>();
        SimulatedSpeedSource.CommandListener l = told::add;
        source.addCommandListener(l);
        assertNull(source.getCommand(THREE));
        source.setSpeed(THREE, 63, SpeedStepMode.NMRA_DCC_128, false, 1000L);
        SimulatedSpeedSource.Command c = Objects.requireNonNull(source.getCommand(THREE), "a command for 3");
        assertEquals(63, c.getSpeedStep());
        assertEquals(SpeedStepMode.NMRA_DCC_128, c.getMode());
        assertFalse(c.isForward());
        assertFalse(c.isEmergencyStop());
        assertEquals(1000L, c.getTimeNanos());
        assertEquals(0.5, c.getSetting(), 1e-9, "63 of 126 steps");
        assertEquals(List.of(THREE), told);
        source.emergencyStop(THREE, 2000L);
        c = Objects.requireNonNull(source.getCommand(THREE), "a command for 3");
        assertTrue(c.isEmergencyStop());
        assertEquals(0.0, c.getSetting(), 1e-9);
        assertFalse(c.isForward(), "direction kept");
        assertEquals(2, told.size());
        source.removeCommandListener(l);
        source.setSpeed(THREE, 10, SpeedStepMode.NMRA_DCC_28, true);
        assertEquals(2, told.size(), "not told after removal");
    }

    @Test
    public void testCommandValues() {
        assertThrows(IllegalArgumentException.class,
                () -> new SimulatedSpeedSource.Command(127, SpeedStepMode.NMRA_DCC_128, true, false, 0L));
        assertThrows(IllegalArgumentException.class,
                () -> new SimulatedSpeedSource.Command(-1, SpeedStepMode.NMRA_DCC_28, true, false, 0L));
        SimulatedSpeedSource.Command a = new SimulatedSpeedSource.Command(14, SpeedStepMode.NMRA_DCC_28, true, false, 5L);
        assertEquals(a, new SimulatedSpeedSource.Command(14, SpeedStepMode.NMRA_DCC_28, true, false, 5L));
        assertEquals(a.hashCode(), new SimulatedSpeedSource.Command(14, SpeedStepMode.NMRA_DCC_28, true, false, 5L).hashCode());
        assertEquals("14/28 forward", a.toString());
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
