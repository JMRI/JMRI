package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of PositionablePointView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class PositionablePointViewTest extends LayoutTrackViewTest {

    @Test
    public void testCtor() {
        new PositionablePointView(pPoint, layoutEditor);
    }


    LayoutEditor layoutEditor;
    PositionablePoint pPoint;
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();

            layoutEditor = new LayoutEditor();
            
            Point2D point2D = new Point2D.Double(150.0, 100.0);
 
            pPoint = new PositionablePoint("PP", PositionablePoint.PointType.ANCHOR, point2D, layoutEditor);

        }
    }

    @After
    public void tearDown() {
        if (layoutEditor != null) {
            JUnitUtil.dispose(layoutEditor);
        }
        layoutEditor = null;
        pPoint = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
