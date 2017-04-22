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
 * Test simple functioning of LayoutTurntable
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LayoutTurntableTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutTurntable t = new LayoutTurntable("test", new Point2D.Double(0.0, 0.0), new LayoutEditor());
        Assert.assertNotNull("exists", t);
    }

    // from here down is testing infrastructure
    @Before
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        // dispose of the single PanelMenu instance
        jmri.jmrit.display.PanelMenu.dispose();
        // reset the instance manager.
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() throws Exception {
        // dispose of the single PanelMenu instance
        jmri.jmrit.display.PanelMenu.dispose();
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
