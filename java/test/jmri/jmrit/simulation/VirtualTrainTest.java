package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.DccLocoAddress;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link VirtualTrain}.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class VirtualTrainTest {

    private static SimulatedLoco loco(double length) {
        return SimulatedLoco.builder(new DccLocoAddress(3, false)).length(length).fullSpeed(100).build();
    }

    @Test
    public void testBuild() {
        VirtualTrain t = VirtualTrain.builder("T").loco(loco(100)).length(200).lengthOffsetMm(30)
                .speedFactorPercent(-5).appliesAdjustmentOutsideConsist(true).build();
        assertEquals("T", t.getName());
        assertEquals("T", t.toString());
        assertEquals(1, t.getLocos().size());
        assertEquals(200, t.getLengthMm(), 1e-9, "what JMRI is told");
        assertEquals(230, t.getTrueLengthMm(), 1e-9, "the truth");
        assertEquals(30, t.getLengthOffsetMm(), 1e-9);
        assertEquals(-5, t.getSpeedFactorPercent(), 1e-9);
        assertTrue(t.isAdjustmentAppliedOutsideConsist());
        assertEquals(Detection.Mode.ENTIRE_TRAIN, t.getDetection().getMode(), "the default");
        assertFalse(VirtualTrain.builder("U").loco(loco(100)).length(200).build().isAdjustmentAppliedOutsideConsist(),
                "off unless set");
    }

    @Test
    public void testRefused() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> VirtualTrain.builder("T").length(200).build());
        assertEquals("Train \"T\" needs at least one loco.", e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> VirtualTrain.builder("T").loco(loco(100)).build());
        assertThrows(IllegalArgumentException.class,
                () -> VirtualTrain.builder("T").loco(loco(100)).length(200).lengthOffsetMm(-200).build());
        assertThrows(IllegalArgumentException.class,
                () -> VirtualTrain.builder("T").loco(loco(0)).length(200).detection(Detection.headOnly()).build(),
                "Head Only needs the loco's length");
        assertThrows(IllegalArgumentException.class,
                () -> VirtualTrain.builder("T").loco(loco(100)).length(200).speedFactorPercent(-100).build());
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
