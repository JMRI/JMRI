package jmri.jmrit.display.controlPanelEditor.shape;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GraphicsEnvironment;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class DrawRectangleTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanelEditor frame = new ControlPanelEditor();
        ShapeDrawer s = new ShapeDrawer(frame);
        DrawRectangle t = new DrawRectangle("newShape","Rectangle",s);
        Assert.assertNotNull("exists",t);
        t.dispose();
        frame.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.resetWindows(true);  // log existing windows in setup
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetWindows(false);  // don't log here.  should be from this class.
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(DrawRectangleTest.class.getName());

}
