package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RouteFinderTest.class.getName());
}
