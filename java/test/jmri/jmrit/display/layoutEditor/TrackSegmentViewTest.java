package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.*;

import jmri.util.*;

import org.junit.*;

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
    public void test_getBounds() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (segmentView != null)) {

            Rectangle2D expected = new Rectangle2D.Double(10.0, 20.0, 10.0, 13.0);
            Rectangle2D actual = MathUtil.granulize(segmentView.getBounds(), 0.1); //round to the nearest 1/10th of a pixel
            Assert.assertEquals("segmentView.getBounds(LINE)", expected, actual);

            segmentView.setArc(true);
            expected = new Rectangle2D.Double(10.0, 20.0, 10.0, 13.0);
            actual = MathUtil.granulize(segmentView.getBounds(), 0.1); //round to the nearest 1/10th of a pixel
            Assert.assertEquals("segmentView.getBounds(ARC)", expected, actual);

            segmentView.setCircle(true);
            expected = new Rectangle2D.Double(10.0, 20.0, 10.1, 13.0);
            actual = MathUtil.granulize(segmentView.getBounds(), 0.1); //round to the nearest 1/10th of a pixel
            Assert.assertEquals("segmentView.getBounds(CIRCLE)", expected, actual);

            segmentView.setBezier(true);
            segmentView.setBezierControlPoint(new Point2D.Double(5.5, 15.5), 0);
            segmentView.setBezierControlPoint(new Point2D.Double(25.5, 38.5), 1);

            expected = new Rectangle2D.Double(9.3, 19.4, 11.6, 14.4);
            actual = MathUtil.granulize(segmentView.getBounds(), 0.1); //round to the nearest 1/10th of a pixel
            Assert.assertEquals("segmentView.getBounds(BEZIER)", expected, actual);

            segmentView.setBezier(false);
            expected = new Rectangle2D.Double(10.0, 20.0, 10.0, 13.0);
            actual = MathUtil.granulize(segmentView.getBounds(), 0.1); //round to the nearest 1/10th of a pixel
            Assert.assertEquals("segmentView.getBounds(LINE)", expected, actual);
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



    /*
        Bridge Decorations
     */
    @Test
    public void testDefaultIsSetBridgeSideRight() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (segmentView != null)) {
            Assert.assertFalse("segmentView.isBridgeSideRight() == true (default).", segmentView.isBridgeSideRight());
            segmentView.setBridgeSideRight(true);
            Assert.assertTrue("segmentView.isBridgeSideRight() == false (after set).", segmentView.isBridgeSideRight());
        }
    }

    @Test
    public void testDefaultIsSetBridgeSideLeft() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (segmentView != null)) {
            Assert.assertFalse("segmentView.isBridgeSideLeft() == true (default).", segmentView.isBridgeSideLeft());
            segmentView.setBridgeSideLeft(true);
            Assert.assertTrue("segmentView.isBridgeSideLeft() == false (after set).", segmentView.isBridgeSideLeft());
        }
    }

    @Test
    public void testDefaultIsSetBridgeHasEntry() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (segmentView != null)) {
            Assert.assertFalse("segmentView.isBridgeHasEntry() == true (default).", segmentView.isBridgeHasEntry());
            segmentView.setBridgeHasEntry(true);
            Assert.assertTrue("segmentView.isBridgeHasEntry() == false (after set).", segmentView.isBridgeHasEntry());
        }
    }

    @Test
    public void testDefaultIsSetBridgeHasExit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (segmentView != null)) {
            Assert.assertFalse("segmentView.isBridgeHasExit() == true (default).", segmentView.isBridgeHasExit());
            segmentView.setBridgeHasExit(true);
            Assert.assertTrue("segmentView.isBridgeHasExit() == false (after set).", segmentView.isBridgeHasExit());
        }
    }

    @Test
    public void testDefaultGetSetBridgeDeckWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (segmentView != null)) {
            Assert.assertEquals("segmentView.getBridgeDeckWidth() == 10 (default).", 10, segmentView.getBridgeDeckWidth());
            segmentView.setBridgeDeckWidth(-1);
            Assert.assertNotEquals("segmentView.setBridgeDeckWidth(-1) not allowed.", -1, segmentView.getBridgeDeckWidth());
            Assert.assertEquals("segmentView.getBridgeDeckWidth() == 6 (after set).", 6, segmentView.getBridgeDeckWidth());
            segmentView.setBridgeDeckWidth(15);
            Assert.assertEquals("segmentView.getBridgeDeckWidth() == 15 (after set).", 15, segmentView.getBridgeDeckWidth());
        }
    }

    @Test
    public void testDefaultGetSetBridgeLineWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (segmentView != null)) {
            Assert.assertEquals("segmentView.getBridgeLineWidth() == 1 (default).", 1, segmentView.getBridgeLineWidth());
            segmentView.setBridgeLineWidth(-1);
            Assert.assertNotEquals("segmentView.setBridgeLineWidth(-1) not allowed.", -1, segmentView.getBridgeLineWidth());
            segmentView.setBridgeLineWidth(3);
            Assert.assertEquals("segmentView.getBridgeLineWidth() == 3 (after set).", 3, segmentView.getBridgeLineWidth());
        }
    }

    @Test
    public void testDefaultGetSetBridgeApproachWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (segmentView != null)) {
            Assert.assertEquals("segmentView.getBridgeApproachWidth() == 4 (default).", 4, segmentView.getBridgeApproachWidth());
            segmentView.setBridgeApproachWidth(-1);
            Assert.assertNotEquals("segmentView.setBridgeApproachWidth(-1) not allowed.", -1, segmentView.getBridgeApproachWidth());
            Assert.assertEquals("segmentView.getBridgeApproachWidth() == 8 (after set).", 8, segmentView.getBridgeApproachWidth());
            segmentView.setBridgeApproachWidth(16);
            Assert.assertEquals("segmentView.getBridgeApproachWidth() ==16 (after set).", 16, segmentView.getBridgeApproachWidth());
        }
    }



    /*
        Tunnel Decorations
     */
    @Test
    public void testDefaultIsSetTunnelSideRight() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (segmentView != null)) {
            Assert.assertFalse("segmentView.isTunnelSideRight() == true (default).", segmentView.isTunnelSideRight());
            segmentView.setTunnelSideRight(true);
            Assert.assertTrue("segmentView.isTunnelSideRight() == false (after set).", segmentView.isTunnelSideRight());
        }
    }

    @Test
    public void testDefaultIsSetTunnelSideLeft() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (segmentView != null)) {
            Assert.assertFalse("segmentView.isTunnelSideLeft() == true (default).", segmentView.isTunnelSideLeft());
            segmentView.setTunnelSideLeft(true);
            Assert.assertTrue("segmentView.isTunnelSideLeft() == false (after set).", segmentView.isTunnelSideLeft());
        }
    }

    @Test
    public void testDefaultIsSetTunnelHasEntry() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (segmentView != null)) {
            Assert.assertFalse("segmentView.isTunnelHasEntry() == true (default).", segmentView.isTunnelHasEntry());
            segmentView.setTunnelHasEntry(true);
            Assert.assertTrue("segmentView.isTunnelHasEntry() == false (after set).", segmentView.isTunnelHasEntry());
        }
    }

    @Test
    public void testDefaultIsSetTunnelHasExit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (segmentView != null)) {
            Assert.assertFalse("segmentView.isTunnelHasExit() == true (default).", segmentView.isTunnelHasExit());
            segmentView.setTunnelHasExit(true);
            Assert.assertTrue("segmentView.isTunnelHasExit() == false (after set).", segmentView.isTunnelHasExit());
        }
    }

    @Test
    public void testDefaultGetSetTunnelFloorWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (segmentView != null)) {
            Assert.assertEquals("segmentView.getTunnelFloorWidth() == 10 (default).", 10, segmentView.getTunnelFloorWidth());
            segmentView.setTunnelFloorWidth(-1);
            Assert.assertNotEquals("segmentView.setTunnelFloorWidth(-1) not allowed.", -1, segmentView.getTunnelFloorWidth());
            segmentView.setTunnelFloorWidth(5);
            Assert.assertEquals("segmentView.getTunnelFloorWidth() == 5 (after set).", 5, segmentView.getTunnelFloorWidth());
        }
    }

    @Test
    public void testDefaultGetSetTunnelLineWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (segmentView != null)) {
            Assert.assertEquals("segmentView.getTunnelLineWidth() == 1 (default).", 1, segmentView.getTunnelLineWidth());
            segmentView.setTunnelLineWidth(-1);
            Assert.assertNotEquals("segmentView.setTunnelLineWidth(-1) not allowed.", -1, segmentView.getTunnelLineWidth());
            segmentView.setTunnelLineWidth(5);
            Assert.assertEquals("segmentView.getTunnelLineWidth() == 5 (after set).", 5, segmentView.getTunnelLineWidth());
        }
    }

    @Test
    public void testDefaultGetSetTunnelEntranceWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (segmentView != null)) {
            Assert.assertEquals("segmentView.getTunnelEntranceWidth() == 16 (default).", 16, segmentView.getTunnelEntranceWidth());
            segmentView.setTunnelEntranceWidth(-1);
            Assert.assertNotEquals("segmentView.setTunnelEntranceWidth(-1) not allowed.", -1, segmentView.getTunnelEntranceWidth());
            segmentView.setTunnelEntranceWidth(5);
            Assert.assertEquals("segmentView.getTunnelEntranceWidth() == 5 (after set).", 5, segmentView.getTunnelEntranceWidth());
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
