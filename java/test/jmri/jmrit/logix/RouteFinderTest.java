package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RouteFinderTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NXFrame nxFrame = new NXFrame();
        BlockOrder orig = new BlockOrder(new OBlock("OB1", "Test1"));
        BlockOrder dest = new BlockOrder(new OBlock("OB2", "Test2"));
        BlockOrder via = new BlockOrder(new OBlock("OB3", "Test3"));
        BlockOrder avoid = new BlockOrder(new OBlock("OB4", "Test4"));
        RouteFinder t = new RouteFinder(nxFrame, orig, dest, via, avoid, 3);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(nxFrame);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RouteFinderTest.class.getName());
}
