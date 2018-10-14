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
 * Test simple functioning of LevelXing
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LevelXingTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        LevelXing t = new LevelXing("test", new Point2D.Double(0.0, 0.0),e);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(e);
    }

    // from here down is testing infrastructure
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
