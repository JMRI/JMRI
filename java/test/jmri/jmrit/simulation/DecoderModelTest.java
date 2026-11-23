package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the simulator's own decoder model, against values worked by hand
 * from NMRA S-9.2.2. These check the simulator's code only.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DecoderModelTest {

    private static final double EPS = 1e-9;

    @Test
    public void testStandardExample() {
        // S-9.2.2: CV3 = 2 at 28 speed steps gives 0.064 s per step
        assertEquals(0.064, DecoderModel.secondsPerStep(2, 28), EPS);
        // the same CV at 128 steps (126 moving steps): 2 x 0.896 / 126
        assertEquals(0.014222222222, DecoderModel.secondsPerStep(2, 126), 1e-9);
    }

    @Test
    public void testAccelerationTakesCvTimesPoint896ForFullRange() {
        // CV3 = 10: stop to full in 8.96 s, so half way after 4.48 s
        DecoderModel d = new DecoderModel(10, 0);
        List<DecoderModel.Ramp> ramps = d.advance(1.0, 0.0, 4.48);
        assertEquals(1, ramps.size());
        assertEquals(0.5, d.getSetting(), EPS);
        // a 28-step decoder at 0.32 s per step covers 14 steps in 4.48 s: half
        assertEquals(0.32, DecoderModel.secondsPerStep(10, 28), EPS);
        d.advance(1.0, 4.48, 10.0);
        assertEquals(1.0, d.getSetting(), EPS);
    }

    @Test
    public void testDecelerationUsesCv4() {
        DecoderModel d = new DecoderModel(0, 5);
        d.advance(1.0, 0.0, 0.05);
        assertEquals(1.0, d.getSetting(), EPS, "no acceleration momentum");
        // CV4 = 5: full to stop in 4.48 s
        d.advance(0.0, 0.0, 2.24);
        assertEquals(0.5, d.getSetting(), EPS);
        List<DecoderModel.Ramp> ramps = d.advance(0.0, 2.24, 5.0);
        assertEquals(2, ramps.size(), "slows to a stand, then stands");
        assertEquals(4.48, ramps.get(0).t1, EPS);
        assertEquals(0.0, d.getSetting(), EPS);
    }

    @Test
    public void testReversingSlowsWithCv4ThenAcceleratesWithCv3() {
        DecoderModel d = new DecoderModel(10, 5);
        d.advance(0.5, 0.0, 10.0);
        assertEquals(0.5, d.getSetting(), EPS);
        List<DecoderModel.Ramp> ramps = d.advance(-0.5, 0.0, 10.0);
        // 0.5 to 0 at 1/4.48 per second: 2.24 s; 0 to -0.5 at 1/8.96: 4.48 s
        assertEquals(3, ramps.size());
        assertEquals(2.24, ramps.get(0).t1, EPS);
        assertEquals(0.0, ramps.get(0).u1, EPS);
        assertEquals(6.72, ramps.get(1).t1, EPS);
        assertEquals(-0.5, ramps.get(1).u1, EPS);
    }

    @Test
    public void testEmergencyStop() {
        DecoderModel d = new DecoderModel(10, 10);
        d.advance(1.0, 0.0, 20.0);
        d.stopNow();
        assertEquals(0.0, d.getSetting(), EPS);
    }

    private static SimulatedLoco loco(int cv3, int cv4, int cv19, int cv23, int cv24) {
        return SimulatedLoco.builder(new jmri.DccLocoAddress(3, false)).momentum(cv3, cv4)
                .consist(cv19, cv23, cv24).fullSpeed(100).build();
    }

    @Test
    public void testAdjustmentOnlyInConsist() {
        // CV3 = 20, CV23 = 10 at 126 steps, not in a consist: CV3 alone, 20 x 0.896 / 126 = 142 ms per step
        DecoderModel d = DecoderModel.of(loco(20, 0, 0, 10, 0), false);
        assertEquals(20, d.getAccelerationCv());
        assertEquals(0.142222222, DecoderModel.secondsPerStep(d.getAccelerationCv(), 126), 1e-9);
        // the same in a consist (CV19 = 5): (20 + 10) x 0.896 / 126 = 213 ms per step
        d = DecoderModel.of(loco(20, 0, 5, 10, 0), false);
        assertEquals(30, d.getAccelerationCv());
        assertEquals(0.213333333, DecoderModel.secondsPerStep(d.getAccelerationCv(), 126), 1e-9);
        // CV24 with bit 7 set subtracts: CV4 20 - 5
        d = DecoderModel.of(loco(20, 20, 5, 0, 0x85), false);
        assertEquals(15, d.getDecelerationCv());
        // truth option on, not in a consist: adjusted all the same
        d = DecoderModel.of(loco(20, 0, 0, 10, 0), true);
        assertEquals(30, d.getAccelerationCv());
    }

    @Test
    public void testAdjustmentCvs() {
        // bits 0-6 the value, bit 7 set to subtract
        assertEquals(15, DecoderModel.adjusted(10, 5));
        assertEquals(5, DecoderModel.adjusted(10, 0x85));
        assertEquals(137, DecoderModel.adjusted(10, 0x7F), "all seven bits are the value");
        assertEquals(255, DecoderModel.adjusted(250, 20), "overflow uses the maximum");
        assertEquals(0, DecoderModel.adjusted(3, 0x8A), "underflow means no momentum");
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
