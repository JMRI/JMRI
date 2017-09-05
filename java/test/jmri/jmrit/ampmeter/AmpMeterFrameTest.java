package jmri.jmrit.ampmeter;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AmpMeterFrameTest {

    @Test
    @Ignore("need to create default jmri.MultiMeter object")
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AmpMeterFrame t = new AmpMeterFrame();
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AmpMeterFrameTest.class);

}
