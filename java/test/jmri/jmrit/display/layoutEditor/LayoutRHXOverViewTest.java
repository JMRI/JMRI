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
 * Test simple functioning of LayoutRHXOverView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class  LayoutRHXOverViewTest extends  LayoutXOverViewTest {

    @Test
    public void testCtor() {
        Point2D point = new Point2D.Double(150.0, 100.0);
        
        new LayoutRHXOverView(xover, point, 99.0, 1.5, 1.6, layoutEditor);
    }


    LayoutEditor layoutEditor;
    LayoutRHXOver xover;
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();

            layoutEditor = new LayoutEditor();
             
            xover = new LayoutRHXOver("Wye", layoutEditor);

        }
    }

    @After
    public void tearDown() {
        if (layoutEditor != null) {
            JUnitUtil.dispose(layoutEditor);
        }
        layoutEditor = null;
        xover = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
