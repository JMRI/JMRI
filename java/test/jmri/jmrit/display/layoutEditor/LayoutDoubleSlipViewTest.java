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
 * Test simple functioning of LayoutDoubleSlipView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutDoubleSlipViewTest extends LayoutSlipViewTest {

    @Test
    public void testCtor() {
        new LayoutDoubleSlipView(dslip, layoutEditor);
    }

    LayoutEditor layoutEditor;
    LayoutDoubleSlip dslip;
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();

            layoutEditor = new LayoutEditor();
            
            Point2D point = new Point2D.Double(150.0, 100.0);
 
            dslip = new LayoutDoubleSlip("DoubleSlip", point, 0.0, layoutEditor);

        }
    }

    @After
    public void tearDown() {
        if (layoutEditor != null) {
            JUnitUtil.dispose(layoutEditor);
        }
        layoutEditor = null;
        dslip = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
