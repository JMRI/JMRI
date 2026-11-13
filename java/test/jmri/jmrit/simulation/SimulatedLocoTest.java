package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link SimulatedLoco}. Building from a roster file is tested in
 * {@link RosterCvsTest}.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class SimulatedLocoTest {

    @Test
    public void testDefaults() {
        SimulatedLoco l = SimulatedLoco.builder(new DccLocoAddress(1234, true)).build();
        assertEquals(new DccLocoAddress(1234, true), l.getAddress());
        assertEquals(l.getAddress(), l.getRespondingAddress());
        assertEquals(null, l.getRosterId());
        assertEquals(0, l.getLengthMm(), 1e-9);
        assertFalse(l.isFacingRear());
        assertFalse(l.isInConsist());
        assertFalse(l.hasMeasuredSpeeds());
        // scale 100 mph (44704 mm/s full size) at the layout scale
        float scale = InstanceManager.getDefault(SignalSpeedMap.class).getLayoutScale();
        assertEquals(44704.0 / scale, l.forwardTable().speed(1.0), 1e-6);
    }

    @Test
    public void testConsistAndProfile() {
        RosterSpeedProfile p = new RosterSpeedProfile(new RosterEntry());
        p.setSpeed(500, 100f, 90f);
        SimulatedLoco l = SimulatedLoco.builder(new DccLocoAddress(3, false)).consist(0x80 | 12, 0, 0)
                .facingRear().speedProfile(p).momentum(4, 8).length(150).build();
        assertTrue(l.isInConsist());
        assertEquals(new DccLocoAddress(12, false), l.getRespondingAddress());
        assertTrue(l.isFacingRear());
        assertTrue(l.hasMeasuredSpeeds());
        assertEquals(4, l.getCv3());
        assertEquals(8, l.getCv4());
        assertEquals(150, l.getLengthMm(), 1e-9);
        assertEquals(90.0, l.reverseTable().speed(0.5), 1e-6);
    }

    @Test
    public void testCvRange() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> SimulatedLoco.builder(new DccLocoAddress(3, false)).momentum(256, 0));
        assertEquals("CV value 256 is outside 0 to 255.", e.getMessage());
        assertThrows(IllegalArgumentException.class,
                () -> SimulatedLoco.builder(new DccLocoAddress(3, false)).consist(0, -1, 0));
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
