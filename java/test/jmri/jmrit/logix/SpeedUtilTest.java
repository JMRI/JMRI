package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SpeedUtilTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SpeedUtil t = new SpeedUtil(null);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testMakeRamp() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SpeedUtil su = new SpeedUtil(null);
        Assert.assertNotNull("exists", su);
        RampData ramp = su.getRampForSpeedChange(.1f, .8f);
        Assert.assertNotNull("exists",ramp);
        Assert.assertTrue("upRamp",ramp.isUpRamp());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(SpeedUtilTest.class);

}
