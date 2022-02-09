package jmri.jmrit.logix;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;
/*
 * @author Pete Cressman Copyright (C) 2019
 */
public class RampDataTest {

    @Test
    public void testCTor() {
        RampData ramp = new RampData(new SpeedUtil(), .005f, 1000, 0.0f, 1.0f);
        assertThat(ramp).withFailMessage("exists").isNotNull();
        assertThat(ramp.isUpRamp()).withFailMessage("upRamp").isTrue();
    }

    @Test
    public void testCTor2() {
        RampData ramp = new RampData(new SpeedUtil(), .005f, 1000, 0.8f, 0.1f);
        assertThat(ramp).withFailMessage("exists").isNotNull();
        assertThat(ramp.isUpRamp()).withFailMessage("downRamp").isFalse();
    }

    @Test
    public void testGets() {
        RampData ramp = new RampData(new SpeedUtil(), .011f, 1500, 0.91f, 0.15f);
        assertThat(ramp).withFailMessage("exists").isNotNull();
        assertThat(ramp.isUpRamp()).withFailMessage("downRamp").isFalse();
        assertThat(ramp.getNumSteps()).withFailMessage("NumRampSteps").isEqualTo(23);
        assertThat(ramp.getRamptime()).withFailMessage("Ramptime").isEqualTo(33000);
        assertThat(ramp.getMaxSpeed()).withFailMessage("MaxSpeed").isEqualTo(0.91f);
        assertThat(ramp.getRampLength()).withFailMessage("RampLength").isEqualTo(5083.989f);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.resetProfileManager();
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
