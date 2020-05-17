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
 * Test simple functioning of LayoutSingleSlipView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutSingleSlipViewTest extends LayoutSlipViewTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Point2D point = new Point2D.Double(150.0, 100.0);

        new LayoutSingleSlipView(slip, point, 0.0, layoutEditor);
    }

    LayoutSingleSlip slip;
    
    @Before
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            slip = new LayoutSingleSlip("Slip", layoutEditor);

        }
    }

    @After
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() {
        slip = null;
        super.tearDown();
    }
}
