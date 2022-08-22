package jmri.jmrit.display.layoutEditor;

import java.awt.geom.Point2D;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of LayoutWyeView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class LayoutWyeViewTest extends LayoutTurnoutViewTest {

    @Test
    public void testCtor() {

        Point2D point = new Point2D.Double(150.0, 100.0);
        
        var lwv = new LayoutWyeView(wye, point, 99.0, 1.5, 1.6, layoutEditor);
        Assertions.assertNotNull(lwv);
    }

    LayoutWye wye;

    @Override
    @BeforeEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        super.setUp();
        wye = new LayoutWye("Wye", layoutEditor);

    }

    @Override
    @AfterEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() {
        wye = null;
        super.tearDown();
    }
}
