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

    // the amount of variation allowed floating point values in order to be considered equal
    static final double tolerance = 0.000001;

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

            Point2D ep1 = layoutEditor.getLayoutTrackView(segmentView.getConnect1()).getCoordsForConnectionType(segmentView.getType1());
            
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.TRACK,  segmentView.findHitPointType(ep1, false, false));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.NONE,   segmentView.findHitPointType(ep1, false, true));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.TRACK,  segmentView.findHitPointType(ep1, true, false));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.NONE,   segmentView.findHitPointType(ep1, true, true));

            Point2D ep2 = layoutEditor.getLayoutTrackView(segmentView.getConnect2()).getCoordsForConnectionType(segmentView.getType2());
            
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

    @Test
    public void test_getDirectionRAD() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (segmentView != null)) {
        
            Assert.assertEquals("segmentView.getDirectionRAD()", 4.056693354143153, segmentView.getDirectionRAD(), tolerance);
            Assert.assertEquals("segmentView.getDirectionDEG()", 232.4314079711725, segmentView.getDirectionDEG(), tolerance);
        }
    }

    @Test
    public void test_getSetBezierControlPoints() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (segmentView != null)) {
        
            Assert.assertEquals("trackSegment.getNumberOfBezierControlPoints == 0", 0, segmentView.getNumberOfBezierControlPoints());

            Point2D p0 = new Point2D.Double(11.1, 22.2);
            segmentView.setBezierControlPoint(p0, 0);
            Assert.assertEquals("trackSegment.getNumberOfBezierControlPoints == 1", 1, segmentView.getNumberOfBezierControlPoints());
            Assert.assertEquals("trackSegment.getBezierControlPoint(0)", p0, segmentView.getBezierControlPoint(0));

            Point2D p1 = new Point2D.Double(22.2, 33.3);
            segmentView.setBezierControlPoint(p1, 1);
            Assert.assertEquals("trackSegment.getNumberOfBezierControlPoints == 2", 2, segmentView.getNumberOfBezierControlPoints());
            Assert.assertEquals("trackSegment.getBezierControlPoint(0)", p0, segmentView.getBezierControlPoint(0));
            Assert.assertEquals("trackSegment.getBezierControlPoint(1)", p1, segmentView.getBezierControlPoint(1));

            Point2D p0P = new Point2D.Double(33.3, 44.4);
            segmentView.setBezierControlPoint(p0P, 0);
            Assert.assertEquals("trackSegment.getNumberOfBezierControlPoints == 2", 2, segmentView.getNumberOfBezierControlPoints());
            Assert.assertEquals("trackSegment.getBezierControlPoint(0)", p0P, segmentView.getBezierControlPoint(0));
            Assert.assertEquals("trackSegment.getBezierControlPoint(1)", p1, segmentView.getBezierControlPoint(1));

            Point2D p1P = new Point2D.Double(44.4, 55.5);
            segmentView.setBezierControlPoint(p1P, -1);
            Assert.assertEquals("trackSegment.getNumberOfBezierControlPoints == 2", 2, segmentView.getNumberOfBezierControlPoints());
            Assert.assertEquals("trackSegment.getBezierControlPoint(0)", p0P, segmentView.getBezierControlPoint(0));
            Assert.assertEquals("trackSegment.getBezierControlPoint(1)", p1P, segmentView.getBezierControlPoint(1));

            Assert.assertEquals("trackSegment.getBezierControlPoint(-1)", p1P, segmentView.getBezierControlPoint(-1));
            Assert.assertEquals("trackSegment.getBezierControlPoint(-2)", p0P, segmentView.getBezierControlPoint(-2));
        }
    }

    @Test
    public void test_translateAndScaleCoords() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (segmentView != null)) {
        
            Assert.assertEquals("trackSegment.getCentreSeg()", new Point2D.Double(15.0, 26.5), segmentView.getCentreSeg());
            segmentView.translateCoords((float) 111.1, (float) 222.2);
            Assert.assertEquals("segmentView.translateCoords()", new Point2D.Double(126.0999984741211, 248.6999969482422), segmentView.getCoordsCenter());
            segmentView.scaleCoords((float) 2.2, (float) 3.3);
            Assert.assertEquals("trackSegment.scaleCoords()", new Point2D.Double(277.4200026559829, 820.7099780702592), segmentView.getCoordsCenter());
        }
    }

    @Test
    public void test_setCoordsCenter() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (segmentView != null)) {
        
            Assert.assertEquals("trackSegment.getCentreSeg()", new Point2D.Double(15.0, 26.5), segmentView.getCentreSeg());
            Point2D newC = new Point2D.Double(111.1, 222.2);
            segmentView.setCoordsCenter(newC);
            Assert.assertEquals("segmentView.setCoordsCenter(p)", newC, segmentView.getCoordsCenter());
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
//            PositionablePointView p1v = new PositionablePointView(p1, new Point2D.Double(20.0, 33.0), layoutEditor);
            PositionablePointView p1v = new PositionablePointView(p1, new Point2D.Double(10.0, 20.0), layoutEditor);
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
