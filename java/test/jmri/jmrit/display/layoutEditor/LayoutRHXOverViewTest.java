package jmri.jmrit.display.layoutEditor;

import java.awt.geom.Point2D;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of LayoutRHXOverView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class  LayoutRHXOverViewTest extends  LayoutXOverViewTest {

    @Test
    public void testCtor() {

        Point2D point = new Point2D.Double(150.0, 100.0);

        var lrhxov = new LayoutRHXOverView(xover, point, 99.0, 1.5, 1.6, layoutEditor);
        Assertions.assertNotNull(lrhxov);
    }

    LayoutRHXOver xover;

    @Override
    @BeforeEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        super.setUp();
        xover = new LayoutRHXOver("Wye", layoutEditor);

    }

    @Override
    @AfterEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() {
        xover = null;
        super.tearDown();
    }
}
