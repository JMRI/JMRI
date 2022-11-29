package jmri.jmrit.display.layoutEditor;

import java.awt.geom.Point2D;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of LayoutLHTurnoutView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class LayoutLHTurnoutViewTest extends LayoutTurnoutViewTest {

    @Test
    public void testCtor() {

        Point2D point = new Point2D.Double(150.0, 100.0);

        var llhtv = new LayoutLHTurnoutView(turnout, point, 99.0, 1.5, 1.6, layoutEditor);
        Assertions.assertNotNull(llhtv);
    }

    LayoutLHTurnout turnout;
    LayoutLHTurnoutView turnoutView;

    @Override
    @BeforeEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        super.setUp();
        turnout = new LayoutLHTurnout("LH", layoutEditor);
        turnoutView = new LayoutLHTurnoutView(turnout, 
                                        jmri.util.MathUtil.zeroPoint2D, 0., 1., 1.,
                                        layoutEditor);
        layoutEditor.addLayoutTrack(turnout, turnoutView);
    }

    @Override
    @AfterEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() {
        turnout = null;
        super.tearDown();
    }
}
