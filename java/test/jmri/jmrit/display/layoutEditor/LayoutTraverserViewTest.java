package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;

import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutTraverserView
 *
 * @author Bob Jacobsen Copyright (C) 2016
 */
public class LayoutTraverserViewTest extends LayoutTrackViewTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        Point2D point = new Point2D.Double(150.0, 100.0);
        
        new LayoutTraverserView(traverser, point, layoutEditor);
    }

    LayoutTraverser traverser;
    
    @Override
    @BeforeEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            
            traverser = new LayoutTraverser("T1", layoutEditor);
        }
    }

    @Override
    @AfterEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() {
        super.tearDown();
    }

}
