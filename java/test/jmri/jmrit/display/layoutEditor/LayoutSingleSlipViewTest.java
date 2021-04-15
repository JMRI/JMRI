package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;

import org.junit.Assume;
import org.junit.jupiter.api.*;

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
    
    @BeforeEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            slip = new LayoutSingleSlip("Slip", layoutEditor);

        }
    }

    @AfterEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() {
        slip = null;
        super.tearDown();
    }
}
