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
 * Test simple functioning of LayoutDoubleXOverView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class  LayoutDoubleXOverViewTest extends  LayoutXOverViewTest {

    @Test
    public void testCtor() {
        new LayoutDoubleXOverView(xover, 
            new Point2D.Double(150.0, 100.0), 
            0., 100., 100., 
            layoutEditor);
    }

    LayoutEditor layoutEditor;
    LayoutDoubleXOver xover;
    LayoutDoubleXOverView xoverC;
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();

            layoutEditor = new LayoutEditor();
            
            xover = new LayoutDoubleXOver("XO", layoutEditor);

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
