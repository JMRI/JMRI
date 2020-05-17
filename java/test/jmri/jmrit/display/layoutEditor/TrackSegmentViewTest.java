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

    @Test
    public void test_findHitPointType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (segmentView != null)) {
            Point2D ccc = segmentView.getCentreSeg();
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.TRACK,  segmentView.findHitPointType(ccc, false, false));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.NONE,   segmentView.findHitPointType(ccc, false, true));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.TRACK,  segmentView.findHitPointType(ccc, true, false));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.NONE,   segmentView.findHitPointType(ccc, true, true));

            Point2D ep1 = segmentView.getConnect1().getCoordsForConnectionType(segmentView.getType1());
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.TRACK,  segmentView.findHitPointType(ep1, false, false));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.NONE,   segmentView.findHitPointType(ep1, false, true));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.TRACK,  segmentView.findHitPointType(ep1, true, false));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.NONE,   segmentView.findHitPointType(ep1, true, true));

            Point2D ep2 = segmentView.getConnect2().getCoordsForConnectionType(segmentView.getType2());
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.TRACK,  segmentView.findHitPointType(ep2, false, false));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.NONE,   segmentView.findHitPointType(ep2, false, true));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.TRACK,  segmentView.findHitPointType(ep2, true, false));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.NONE,   segmentView.findHitPointType(ep2, true, true));

            segmentView.setCircle(true);
            Point2D cp = segmentView.getCoordsCenterCircle();
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.TRACK_CIRCLE_CENTRE, segmentView.findHitPointType(cp, false, false));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.NONE,   segmentView.findHitPointType(cp, false, true));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.TRACK_CIRCLE_CENTRE, segmentView.findHitPointType(cp, true, false));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.NONE,   segmentView.findHitPointType(cp, true, true));
        }
    }


    TrackSegment segment;
    TrackSegmentView segmentView;
    
    @Before
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            
//             PositionablePoint p1 = new PositionablePoint("A1", PositionablePoint.PointType.ANCHOR, new Point2D.Double(10.0, 20.0), layoutEditor);
//             PositionablePoint p2 = new PositionablePoint("A2", PositionablePoint.PointType.ANCHOR, new Point2D.Double(20.0, 33.0), layoutEditor);
            PositionablePoint p1 = new PositionablePoint("A1", PositionablePoint.PointType.ANCHOR, layoutEditor);
            PositionablePointView p1v = new PositionablePointView(p1, new Point2D.Double(20.0, 33.0), layoutEditor);
            layoutEditor.addLayoutTrack(p1, p1v);

            PositionablePoint p2 = new PositionablePoint("A2", PositionablePoint.PointType.ANCHOR, layoutEditor);
            PositionablePointView p2v = new PositionablePointView(p2, new Point2D.Double(20.0, 33.0), layoutEditor);
            layoutEditor.addLayoutTrack(p2, p2v);

            segment = new TrackSegment("TS01", p1, HitPointType.POS_POINT, p2, HitPointType.POS_POINT, false, layoutEditor);
            segmentView = new TrackSegmentView(segment, layoutEditor);
            layoutEditor.addLayoutTrack(segment, segmentView);
        }
    }

    @After
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() {
        segment = null;
        segmentView = null;
        super.tearDown();
    }
}
