package jmri.jmrit.display.layoutEditor;

import java.awt.geom.Point2D;
import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutWyeView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutWyeViewTest extends LayoutTurnoutViewTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Point2D point = new Point2D.Double(150.0, 100.0);
        
        new LayoutWyeView(wye, point, 99.0, 1.5, 1.6, layoutEditor);
    }

    LayoutWye wye;
    
    @BeforeEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
             
            wye = new LayoutWye("Wye", layoutEditor);

        }
    }

    @AfterEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() {
        wye = null;
        super.tearDown();
    }
}
