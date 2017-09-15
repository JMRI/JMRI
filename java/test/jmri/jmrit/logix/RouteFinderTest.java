package jmri.jmrit.logix;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RouteFinderTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NXFrame nxFrame = NXFrame.getDefault();
        BlockOrder orig = new BlockOrder(new OBlock("OB1", "Test1"));
        BlockOrder dest = new BlockOrder(new OBlock("OB2", "Test2"));
        BlockOrder via = new BlockOrder(new OBlock("OB3", "Test3"));
        BlockOrder avoid = new BlockOrder(new OBlock("OB4", "Test4"));
        RouteFinder t = new RouteFinder(nxFrame,orig,dest,via,avoid,3);
        Assert.assertNotNull("exists",t);
        jmri.util.JUnitUtil.dispose(nxFrame);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(RouteFinderTest.class.getName());

}
