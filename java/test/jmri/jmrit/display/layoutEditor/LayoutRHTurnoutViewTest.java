package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.*;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutRHTurnoutView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutRHTurnoutViewTest extends LayoutTurnoutViewTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Point2D point = new Point2D.Double(150.0, 100.0);
        
        new LayoutRHTurnoutView(turnout, point, 99.0, 1.5, 1.6, layoutEditor);
    }

    LayoutRHTurnout turnout;
    
    @BeforeEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
             
            turnout = new LayoutRHTurnout("Wye", layoutEditor);
        }
    }

    @AfterEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() {
        turnout = null;
        super.tearDown();
    }
}
