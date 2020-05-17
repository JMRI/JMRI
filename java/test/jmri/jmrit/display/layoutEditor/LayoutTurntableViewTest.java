package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of LayoutTurntableView
 *
 * @author Bob Jacobsen Copyright (C) 2016
 */
public class LayoutTurntableViewTest extends LayoutTrackViewTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        Point2D point = new Point2D.Double(150.0, 100.0);
        
        new LayoutTurntableView(turntable, point, layoutEditor);
    }

    LayoutTurntable turntable;
    
    @Before
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            
            turntable = new LayoutTurntable("T1", layoutEditor);
        }
    }

    @After
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() {
        super.tearDown();
    }

}
