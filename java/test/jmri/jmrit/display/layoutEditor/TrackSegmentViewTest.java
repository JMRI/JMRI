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
 * Test simple functioning of TrackSegmentView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class TrackSegmentViewTest extends LayoutTrackViewTest {

    @Test
    public void testCtor() {
        new TrackSegmentView(segment, layoutEditor);
    }

    LayoutEditor layoutEditor;
    TrackSegment segment;
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();

            layoutEditor = new LayoutEditor();
            
            PositionablePoint p1 = new PositionablePoint("A1", PositionablePoint.PointType.ANCHOR, new Point2D.Double(10.0, 20.0), layoutEditor);
            PositionablePoint p2 = new PositionablePoint("A2", PositionablePoint.PointType.ANCHOR, new Point2D.Double(20.0, 33.0), layoutEditor);

            segment = new TrackSegment("TS01", p1, HitPointType.POS_POINT, p2, HitPointType.POS_POINT, false, layoutEditor);

        }
    }

    @After
    public void tearDown() {
        if (layoutEditor != null) {
            JUnitUtil.dispose(layoutEditor);
        }
        layoutEditor = null;
        segment = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
