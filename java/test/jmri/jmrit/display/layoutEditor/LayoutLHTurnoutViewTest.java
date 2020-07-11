package jmri.jmrit.display.layoutEditor;

import java.awt.geom.Point2D;
import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutLHTurnoutView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutLHTurnoutViewTest extends LayoutTurnoutViewTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Point2D point = new Point2D.Double(150.0, 100.0);

        new LayoutLHTurnoutView(turnout, point, 99.0, 1.5, 1.6, layoutEditor);
    }

    LayoutLHTurnout turnout;
    LayoutLHTurnoutView turnoutView;
    
    @BeforeEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            turnout = new LayoutLHTurnout("LH", layoutEditor);
            turnoutView = new LayoutLHTurnoutView(turnout, 
                                            jmri.util.MathUtil.zeroPoint2D, 0., 1., 1.,
                                            layoutEditor);
            layoutEditor.addLayoutTrack(turnout, turnoutView);

        }
    }

    @AfterEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() {
        turnout = null;
        super.tearDown();
    }
}
