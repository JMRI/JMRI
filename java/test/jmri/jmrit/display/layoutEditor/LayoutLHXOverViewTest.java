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
 * Test simple functioning of LayoutLHXOverView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class  LayoutLHXOverViewTest extends  LayoutXOverViewTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Point2D point = new Point2D.Double(150.0, 100.0);
        
        new LayoutLHXOverView(xover, point, 99.0, 1.5, 1.6, layoutEditor);
    }

    LayoutLHXOver xover;
    
    @Before
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            xover = new LayoutLHXOver("Wye", layoutEditor);

        }
    }

    @After
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() {
        xover = null;
        super.tearDown();
    }
}
