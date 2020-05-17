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
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        new LayoutDoubleXOverView(xover, 
            new Point2D.Double(150.0, 100.0), 
            0., 100., 100., 
            layoutEditor);
    }

    LayoutDoubleXOver xover;
    LayoutDoubleXOverView xoverC;
    
    @Before
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            xover = new LayoutDoubleXOver("XO", layoutEditor);
        }
    }

    @After
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() {
        xover = null;
        super.tearDown();
    }
}
