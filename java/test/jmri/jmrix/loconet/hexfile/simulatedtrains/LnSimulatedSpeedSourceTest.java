package jmri.jmrix.loconet.hexfile.simulatedtrains;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.SpeedStepMode;
import jmri.jmrit.simulation.SimulatedSpeedSource.Command;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link LnSimulatedSpeedSource}'s reading of a slot's speed.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class LnSimulatedSpeedSourceTest {

    @Test
    public void test128Steps() {
        Command c = LnSimulatedSpeedSource.command(64, true, 3, 1);
        assertEquals(SpeedStepMode.NMRA_DCC_128, c.getMode());
        assertEquals(63, c.getSpeedStep());
        assertTrue(c.isForward());
        assertEquals(126, LnSimulatedSpeedSource.command(127, true, 3, 1).getSpeedStep());
        assertTrue(LnSimulatedSpeedSource.command(1, false, 3, 1).isEmergencyStop());
        assertEquals(0, LnSimulatedSpeedSource.command(0, false, 3, 1).getSpeedStep());
    }

    @Test
    public void test28And14Steps() {
        Command c = LnSimulatedSpeedSource.command(127, false, 0, 1);
        assertEquals(SpeedStepMode.NMRA_DCC_28, c.getMode());
        assertEquals(28, c.getSpeedStep());
        assertFalse(c.isForward());
        assertEquals(1, LnSimulatedSpeedSource.command(2, true, 0, 1).getSpeedStep(), "any speed moves the loco");
        assertEquals(SpeedStepMode.NMRA_DCC_14, LnSimulatedSpeedSource.command(64, true, 2, 1).getMode());
        assertEquals(7, LnSimulatedSpeedSource.command(64, true, 2, 1).getSpeedStep());
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
