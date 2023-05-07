package jmri.jmrit.display.layoutEditor;

import java.awt.geom.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of LayoutLHXOverView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class  LayoutLHXOverViewTest extends  LayoutXOverViewTest {

    @Test
    public void testCtor() {

        Point2D point = new Point2D.Double(150.0, 100.0);

        var llhxov = new LayoutLHXOverView(xover, point, 99.0, 1.5, 1.6, layoutEditor);
        Assertions.assertNotNull(llhxov);
    }

    LayoutLHXOver xover;

    @Override
    @BeforeEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        super.setUp();
        xover = new LayoutLHXOver("Wye", layoutEditor);

    }

    @Override
    @AfterEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() {
        xover = null;
        super.tearDown();
    }
}
