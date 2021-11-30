package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
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

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
     }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SpeedUtilTest.class);

}
