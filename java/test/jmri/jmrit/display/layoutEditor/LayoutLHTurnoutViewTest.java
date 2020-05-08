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
 * Test simple functioning of LayoutLHTurnoutView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutLHTurnoutViewTest extends LayoutTurnoutViewTest {

    @Test
    public void testCtor() {
        new LayoutLHTurnoutView(turnout, layoutEditor);
    }

    LayoutEditor layoutEditor;
    LayoutLHTurnout turnout;
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();

            layoutEditor = new LayoutEditor();
            
            Point2D point = new Point2D.Double(150.0, 100.0);
 
            turnout = new LayoutLHTurnout("Wye", point, 99.0, 1.5, 1.6, layoutEditor);

        }
    }

    @After
    public void tearDown() {
        if (layoutEditor != null) {
            JUnitUtil.dispose(layoutEditor);
        }
        layoutEditor = null;
        turnout = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
