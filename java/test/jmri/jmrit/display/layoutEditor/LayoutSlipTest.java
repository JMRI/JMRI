package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of LayoutSlip
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LayoutSlipTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutSlip t = new LayoutSlip("test", new Point2D.Double(0.0, 0.0), 0.0, new LayoutEditor(), 0);
        Assert.assertNotNull("exists", t);
    }

    // from here down is testing infrastructure
    @Before
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        // reset the instance manager.
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
