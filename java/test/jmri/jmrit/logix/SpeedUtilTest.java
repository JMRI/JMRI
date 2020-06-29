package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SpeedUtilTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SpeedUtil t = new SpeedUtil();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testMakeRamp() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SpeedUtil su = new SpeedUtil();
        Assert.assertNotNull("exists", su);
        RampData ramp = su.getRampForSpeedChange(.1f, .8f);
        Assert.assertNotNull("exists",ramp);
        Assert.assertTrue("upRamp",ramp.isUpRamp());
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(SpeedUtilTest.class);

}
