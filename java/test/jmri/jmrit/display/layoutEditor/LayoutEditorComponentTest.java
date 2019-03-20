package jmri.jmrit.display.layoutEditor;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import jmri.util.JUnitUtil;

import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LayoutEditorComponentTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor le = new LayoutEditor("Layout Editor Component Test Layout");
        LayoutEditorComponent t = new LayoutEditorComponent(le);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(le);
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

}
