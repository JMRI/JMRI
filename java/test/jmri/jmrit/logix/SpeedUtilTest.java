package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;

import org.junit.jupiter.api.*;
import org.junit.Assume;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SpeedUtilTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SpeedUtil t = new SpeedUtil();
        assertThat(t).withFailMessage("exists").isNotNull();
    }

    @Test
    public void testMakeRamp() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SpeedUtil su = new SpeedUtil();
        assertThat(su).withFailMessage("exists").isNotNull();
        RampData ramp = su.getRampForSpeedChange(.1f, .8f);
        assertThat(ramp).withFailMessage("exists").isNotNull();
        assertThat(ramp.isUpRamp()).withFailMessage("upRamp").isTrue();
    }

    @Test
    public void testMomentumTimePerStep() {
        float steps126 = 1.0f / 126.0f;
        float steps28 = 1.0f / 28.0f;
        // NMRA example: CV#3 = 2 with 28 speed steps is 0.064 s per step
        assertThat(SpeedUtil.getMomentumTimePerStep(2, 0, steps28)).isEqualTo(64);
        // no momentum
        assertThat(SpeedUtil.getMomentumTimePerStep(0, 0, steps126)).isZero();
        // CV#4 = 20 with 126 steps: 20 * 896 / 126 = 142.2 ms
        assertThat(SpeedUtil.getMomentumTimePerStep(20, 0, steps126)).isEqualTo(142);
        // CV#24 adds to the CV value before the formula, not to the milliseconds
        assertThat(SpeedUtil.getMomentumTimePerStep(20, 10, steps126))
                .isEqualTo(SpeedUtil.getMomentumTimePerStep(30, 0, steps126));
        // bit 7 is the sign, and bits 0-6 the value: 0x8A is -10
        assertThat(SpeedUtil.getMomentumTimePerStep(20, 0x8A, steps126))
                .isEqualTo(SpeedUtil.getMomentumTimePerStep(10, 0, steps126));
        // bit 6 is part of the value: 0x4A is +74
        assertThat(SpeedUtil.getMomentumTimePerStep(20, 0x4A, steps126))
                .isEqualTo(SpeedUtil.getMomentumTimePerStep(94, 0, steps126));
        // underflow: no momentum
        assertThat(SpeedUtil.getMomentumTimePerStep(5, 0x8A, steps126)).isZero();
        // overflow: the maximum rate
        assertThat(SpeedUtil.getMomentumTimePerStep(250, 0x7F, steps126))
                .isEqualTo(SpeedUtil.getMomentumTimePerStep(255, 0, steps126));
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
     }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(SpeedUtilTest.class);

}
