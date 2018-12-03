package jmri.jmrit.sensorgroup;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author	Bob Jacobsen Copyright 2003, 2007
 * @author Paul Bender Copyright (C) 2017
 */
public class SensorGroupFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SensorGroupFrame t = new SensorGroupFrame();
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SensorGroupFrameTest.class);
}
