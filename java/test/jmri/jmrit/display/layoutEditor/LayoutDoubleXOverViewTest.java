package jmri.jmrit.display.layoutEditor;

import java.awt.geom.Point2D;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of LayoutDoubleXOverView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class  LayoutDoubleXOverViewTest extends  LayoutXOverViewTest {

    @Test
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void testCtor() {
        xoverC = new LayoutDoubleXOverView(xover, 
            new Point2D.Double(150.0, 100.0), 
            0., 100., 100., 
            layoutEditor);
        Assertions.assertNotNull(xoverC);
    }

    LayoutDoubleXOver xover;
    LayoutDoubleXOverView xoverC;
    
    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        xover = new LayoutDoubleXOver("XO", layoutEditor);
    }

    @AfterEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    @Override
    public void tearDown() {
        xover = null;
        super.tearDown();
    }
}
