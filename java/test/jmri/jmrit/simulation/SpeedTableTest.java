package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;

import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the simulator's own reading of a speed profile, against values
 * worked by hand. These check the simulator's code only.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class SpeedTableTest {

    private static final double EPS = 1e-6;

    private RosterSpeedProfile profile() {
        RosterSpeedProfile p = new RosterSpeedProfile(new RosterEntry());
        // throttle setting x 1000; forward and reverse in mm/s; 0 = not measured
        p.setSpeed(250, 50f, 40f);
        p.setSpeed(500, 120f, 0f);
        p.setSpeed(750, 210f, 200f);
        return p;
    }

    @Test
    public void testForwardBetweenMeasuredSteps() {
        SpeedTable t = Objects.requireNonNull(SpeedTable.fromProfile(profile(), true), "a table from a measured profile");
        assertTrue(t.isMeasured());
        assertEquals(0.0, t.speed(0.0), EPS);
        assertEquals(25.0, t.speed(0.125), EPS, "from a stand to the lowest point");
        assertEquals(50.0, t.speed(0.25), EPS);
        assertEquals(85.0, t.speed(0.375), EPS, "half way from 50 to 120");
        assertEquals(165.0, t.speed(0.625), EPS, "half way from 120 to 210");
        assertEquals(280.0, t.speed(1.0), EPS, "in proportion above the highest: 210 x 1.0 / 0.75");
    }

    @Test
    public void testReverseSkipsUnmeasuredStep() {
        SpeedTable t = Objects.requireNonNull(SpeedTable.fromProfile(profile(), false), "a table from a measured profile");
        // 0.5 is not measured in reverse: 40 at 0.25 to 200 at 0.75
        assertEquals(120.0, t.speed(0.5), EPS);
        assertEquals(2, t.breakpointsBetween(0.0, 1.0).size(), "0.25 and 0.75");
    }

    @Test
    public void testNoProfile() {
        assertNull(SpeedTable.fromProfile(null, true));
        assertNull(SpeedTable.fromProfile(new RosterSpeedProfile(new RosterEntry()), true));
        SpeedTable t = SpeedTable.proportional(500.0);
        assertEquals(250.0, t.speed(0.5), EPS);
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
