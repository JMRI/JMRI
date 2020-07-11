package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutDoubleSlipView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutDoubleSlipViewTest extends LayoutSlipViewTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Point2D point = new Point2D.Double(150.0, 100.0);
        
        new LayoutDoubleSlipView(dslip, point, 0.0, layoutEditor);
    }

    LayoutDoubleSlip dslip;
    
    @BeforeEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
             
            dslip = new LayoutDoubleSlip("DoubleSlip", layoutEditor);

        }
    }

    @AfterEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() {
        dslip = null;
        super.tearDown();
    }
}
