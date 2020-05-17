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
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new TrackSegmentView(segment, layoutEditor);
    }

    TrackSegment segment;
    
    @Before
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            
//             PositionablePoint p1 = new PositionablePoint("A1", PositionablePoint.PointType.ANCHOR, new Point2D.Double(10.0, 20.0), layoutEditor);
//             PositionablePoint p2 = new PositionablePoint("A2", PositionablePoint.PointType.ANCHOR, new Point2D.Double(20.0, 33.0), layoutEditor);
            PositionablePoint p1 = new PositionablePoint("A1", PositionablePoint.PointType.ANCHOR, layoutEditor);
            PositionablePoint p2 = new PositionablePoint("A2", PositionablePoint.PointType.ANCHOR, layoutEditor);

            segment = new TrackSegment("TS01", p1, HitPointType.POS_POINT, p2, HitPointType.POS_POINT, false, layoutEditor);

        }
    }

    @After
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() {
        segment = null;
        super.tearDown();
    }
}
