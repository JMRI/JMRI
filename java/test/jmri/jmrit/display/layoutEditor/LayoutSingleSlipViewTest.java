package jmri.jmrit.display.layoutEditor;

import java.awt.geom.Point2D;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of LayoutSingleSlipView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class LayoutSingleSlipViewTest extends LayoutSlipViewTest {

    @Test
    public void testCtor() {

        Point2D point = new Point2D.Double(150.0, 100.0);

        var lssv = new LayoutSingleSlipView(slip, point, 0.0, layoutEditor);
        Assertions.assertNotNull(lssv);
    }

    LayoutSingleSlip slip;

    @Override
    @BeforeEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        super.setUp();
        slip = new LayoutSingleSlip("Slip", layoutEditor);

    }

    @Override
    @AfterEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() {
        slip = null;
        super.tearDown();
    }
}
