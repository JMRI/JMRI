package jmri.jmrit.display.layoutEditor;

import java.awt.geom.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of LayoutRHTurnoutView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class LayoutRHTurnoutViewTest extends LayoutTurnoutViewTest {

    @Test
    public void testCtor() {

        Point2D point = new Point2D.Double(150.0, 100.0);
        
        var lrhtv = new LayoutRHTurnoutView(turnout, point, 99.0, 1.5, 1.6, layoutEditor);
        Assertions.assertNotNull(lrhtv);
    }

    LayoutRHTurnout turnout;

    @Override
    @BeforeEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        super.setUp();
        turnout = new LayoutRHTurnout("Wye", layoutEditor);

    }

    @Override
    @AfterEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() {
        turnout = null;
        super.tearDown();
    }
}
