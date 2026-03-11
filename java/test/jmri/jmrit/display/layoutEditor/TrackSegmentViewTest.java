package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.*;

import jmri.util.*;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of TrackSegmentView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
@DisabledIfHeadless
public class TrackSegmentViewTest extends LayoutTrackViewTest {

    // the amount of variation allowed floating point values in order to be considered equal
    static final double TOLERANCE = 0.000001;

    @Test
    public void testCtor() {
        assertNotNull(new TrackSegmentView(segment, layoutEditor));
    }

    @Test
    public void test_findHitPointType() {
        Point2D ccc = segmentView.getCentreSeg();

        assertEquals(HitPointType.TRACK, segmentView.findHitPointType(ccc, false, false), "trackSegment.findHitPointType()");
        assertEquals(HitPointType.NONE, segmentView.findHitPointType(ccc, false, true), "trackSegment.findHitPointType()");
        assertEquals(HitPointType.TRACK, segmentView.findHitPointType(ccc, true, false), "trackSegment.findHitPointType()");
        assertEquals(HitPointType.NONE, segmentView.findHitPointType(ccc, true, true), "trackSegment.findHitPointType()");

        Point2D ep1 = layoutEditor.getLayoutTrackView(segmentView.getConnect1())
                .getCoordsForConnectionType(segmentView.getType1());

        assertEquals(HitPointType.TRACK, segmentView.findHitPointType(ep1, false, false), "trackSegment.findHitPointType()");
        assertEquals(HitPointType.NONE, segmentView.findHitPointType(ep1, false, true), "trackSegment.findHitPointType()");
        assertEquals(HitPointType.TRACK, segmentView.findHitPointType(ep1, true, false), "trackSegment.findHitPointType()");
        assertEquals(HitPointType.NONE, segmentView.findHitPointType(ep1, true, true), "trackSegment.findHitPointType()");

        Point2D ep2 = layoutEditor.getLayoutTrackView(segmentView.getConnect2())
                .getCoordsForConnectionType(segmentView.getType2());

        assertEquals(HitPointType.TRACK, segmentView.findHitPointType(ep2, false, false), "trackSegment.findHitPointType()");
        assertEquals(HitPointType.NONE, segmentView.findHitPointType(ep2, false, true), "trackSegment.findHitPointType()");
        assertEquals(HitPointType.TRACK, segmentView.findHitPointType(ep2, true, false), "trackSegment.findHitPointType()");
        assertEquals(HitPointType.NONE, segmentView.findHitPointType(ep2, true, true), "trackSegment.findHitPointType()");

        segmentView.setCircle(true);
        Point2D cp = segmentView.getCoordsCenterCircle();

        assertEquals(HitPointType.TRACK_CIRCLE_CENTRE, segmentView.findHitPointType(cp, false, false), "trackSegment.findHitPointType()");
        assertEquals(HitPointType.NONE, segmentView.findHitPointType(cp, false, true), "trackSegment.findHitPointType()");
        assertEquals(HitPointType.TRACK_CIRCLE_CENTRE, segmentView.findHitPointType(cp, true, false), "trackSegment.findHitPointType()");
        assertEquals(HitPointType.NONE, segmentView.findHitPointType(cp, true, true), "trackSegment.findHitPointType()");
    }

    @Test
    public void test_getDirectionRAD() {
        assertEquals(4.056693354143153, segmentView.getDirectionRAD(), TOLERANCE, "segmentView.getDirectionRAD()");
        assertEquals(232.4314079711725, segmentView.getDirectionDEG(), TOLERANCE, "segmentView.getDirectionDEG()");
    }

    @Test
    public void test_getBounds() {
        Rectangle2D expected = new Rectangle2D.Double(50.0, 50.0, 170.0, 183.0);
        Rectangle2D actual = MathUtil.granulize(segmentView.getBounds(), 0.2); //round to the nearest 2/10th of a pixel
        assertEquals(expected, actual, "segmentView.getBounds(LINE)");

        segmentView.setArc(true);
        expected = new Rectangle2D.Double(210.0, 220.0, 10.0, 13.0);
        actual = MathUtil.granulize(segmentView.getBounds(), 0.2); //round to the nearest 2/10th of a pixel
        assertEquals(expected, actual, "segmentView.getBounds(ARC)");

        segmentView.setCircle(true);
        expected = new Rectangle2D.Double(0.0, 0.0, 220.0, 233.0);
        actual = MathUtil.granulize(segmentView.getBounds(), 0.2); //round to the nearest 2/10th of a pixel
        assertEquals(expected, actual, "segmentView.getBounds(CIRCLE)");

        segmentView.setBezier(true);
        segmentView.setBezierControlPoint(new Point2D.Double(25.5, 215.5), 0);
        segmentView.setBezierControlPoint(new Point2D.Double(225.5, 238.5), 1);

        expected = new Rectangle2D.Double(25.5, 215.5, 200.0, 23.0);
        actual = MathUtil.granulize(segmentView.getBounds(), 0.1); //round to the nearest 1/10th of a pixel
        assertEquals(expected, actual, "segmentView.getBounds(BEZIER)");

        segmentView.setBezier(false);
        expected = new Rectangle2D.Double(210.0, 220.0, 10.0, 13.0);
        actual = MathUtil.granulize(segmentView.getBounds(), 0.2); //round to the nearest 2/10th of a pixel
        assertEquals(expected, actual, "segmentView.getBounds(LINE)");
    }

    @Test
    public void test_getSetBezierControlPoints() {
        assertEquals(0, segmentView.getNumberOfBezierControlPoints(), "trackSegment.getNumberOfBezierControlPoints == 0");

        Point2D p0 = new Point2D.Double(211.1, 222.2);
        segmentView.setBezierControlPoint(p0, 0);
        assertEquals(1, segmentView.getNumberOfBezierControlPoints(), "trackSegment.getNumberOfBezierControlPoints == 1");
        assertEquals(p0, segmentView.getBezierControlPoint(0), "trackSegment.getBezierControlPoint(0)");

        Point2D p1 = new Point2D.Double(222.2, 233.3);
        segmentView.setBezierControlPoint(p1, 1);
        assertEquals(2, segmentView.getNumberOfBezierControlPoints(), "trackSegment.getNumberOfBezierControlPoints == 2");
        assertEquals(p0, segmentView.getBezierControlPoint(0), "trackSegment.getBezierControlPoint(0)");
        assertEquals(p1, segmentView.getBezierControlPoint(1), "trackSegment.getBezierControlPoint(1)");

        Point2D p0P = new Point2D.Double(233.3, 244.4);
        segmentView.setBezierControlPoint(p0P, 0);
        assertEquals(2, segmentView.getNumberOfBezierControlPoints(), "trackSegment.getNumberOfBezierControlPoints == 2");
        assertEquals(p0P, segmentView.getBezierControlPoint(0), "trackSegment.getBezierControlPoint(0)");
        assertEquals(p1, segmentView.getBezierControlPoint(1), "trackSegment.getBezierControlPoint(1)");

        Point2D p1P = new Point2D.Double(244.4, 255.5);
        segmentView.setBezierControlPoint(p1P, -1);
        assertEquals(2, segmentView.getNumberOfBezierControlPoints(), "trackSegment.getNumberOfBezierControlPoints == 2");
        assertEquals(p0P, segmentView.getBezierControlPoint(0), "trackSegment.getBezierControlPoint(0)");
        assertEquals(p1P, segmentView.getBezierControlPoint(1), "trackSegment.getBezierControlPoint(1)");

        assertEquals(p1P, segmentView.getBezierControlPoint(-1), "trackSegment.getBezierControlPoint(-1)");
        assertEquals(p0P, segmentView.getBezierControlPoint(-2), "trackSegment.getBezierControlPoint(-2)");
    }

    @Test
    public void test_translateAndScaleCoords() {
        assertEquals(new Point2D.Double(215.0, 226.5), segmentView.getCentreSeg(), "trackSegment.calcCentreSeg()");
        segmentView.translateCoords((float) 111.1, (float) 222.2);
        assertEquals(new Point2D.Double(326.0999984741211, 448.6999969482422), segmentView.getCoordsCenter(), "segmentView.translateCoords()");
        segmentView.scaleCoords((float) 2.2, (float) 3.3);
        assertEquals(new Point2D.Double(717.4200121927261, 1480.709968533516), segmentView.getCoordsCenter(), "trackSegment.scaleCoords()");

        assertEquals(new Point2D.Double(215.0, 226.5), segmentView.getCentreSeg(), "trackSegment.calcCentreSeg()");
    }

    @Test
    public void test_setCoordsCenter() {
        assertEquals(new Point2D.Double(215.0, 226.5), segmentView.getCentreSeg(), "trackSegment.calcCentreSeg()");
        Point2D newC = new Point2D.Double(311.1, 422.2);
        segmentView.setCoordsCenter(newC);
        assertEquals(newC, segmentView.getCoordsCenter(), "segmentView.setCoordsCenter(p)");

        assertEquals(new Point2D.Double(215.0, 226.5), segmentView.getCentreSeg(), "trackSegment.calcCentreSeg()");
    }

    @Test
    public void testSetCircleDefault() {
        segmentView.setCircle(true);
        assertEquals(90.0D, segmentView.getAngle(), 0.01D, "segmentView.setCircle(Default)");
    }

    @Test
    public void testSetCircleZeroAngle() {
        segmentView.setAngle(0.0D);
        segmentView.setCircle(true);
        assertEquals(90.0D, segmentView.getAngle(), 0.01D, "segmentView.setCircle(Zero Angle)");
    }

    @Test
    public void testSetCirclePositiveAngle() {
        segmentView.setAngle(50.0D);
        segmentView.setCircle(true);
        assertEquals(50.0D, segmentView.getAngle(), 0.01D, "segmentView.setCircle(Positive Angle)");
    }

    @Test
    public void testConstructionLinesRead() {

        segmentView.showConstructionLine = 0;
        assertTrue(segmentView.isShowConstructionLines(), "From 0");
        assertTrue(segmentView.hideConstructionLines(), "From 0");

        segmentView.showConstructionLine = TrackSegmentView.HIDECONALL;
        assertFalse(segmentView.isShowConstructionLines(), "HIDECONALL");
        assertTrue(segmentView.hideConstructionLines(), "HIDECONALL");

        segmentView.showConstructionLine = TrackSegmentView.HIDECON;
        assertFalse(segmentView.isShowConstructionLines(), "HIDECON");
        assertTrue(segmentView.hideConstructionLines(), "HIDECON");

        segmentView.showConstructionLine = TrackSegmentView.SHOWCON;
        assertTrue(segmentView.isShowConstructionLines(), "SHOWCON");
        assertFalse(segmentView.hideConstructionLines(), "SHOWCON");

        segmentView.showConstructionLine = TrackSegmentView.SHOWCON | TrackSegmentView.HIDECON | TrackSegmentView.HIDECONALL;
        assertFalse(segmentView.isShowConstructionLines(), "all");
        assertFalse(segmentView.hideConstructionLines(), "all");

    }

    @Test
    public void hideConstructionLinesOfInt() {
        segmentView.showConstructionLine = 0;
        segmentView.hideConstructionLines(TrackSegmentView.SHOWCON);
        assertEquals(TrackSegmentView.SHOWCON, segmentView.showConstructionLine);

        segmentView.showConstructionLine = 0;
        segmentView.hideConstructionLines(TrackSegmentView.HIDECON);
        assertEquals(TrackSegmentView.HIDECON, segmentView.showConstructionLine);

        segmentView.showConstructionLine = 0;
        segmentView.hideConstructionLines(TrackSegmentView.HIDECONALL);
        assertEquals(TrackSegmentView.HIDECONALL, segmentView.showConstructionLine);

        // ----
        segmentView.showConstructionLine = TrackSegmentView.SHOWCON;
        segmentView.hideConstructionLines(TrackSegmentView.SHOWCON);
        assertEquals(TrackSegmentView.SHOWCON, segmentView.showConstructionLine);

        segmentView.showConstructionLine = TrackSegmentView.SHOWCON;
        segmentView.hideConstructionLines(TrackSegmentView.HIDECON);
        assertEquals(TrackSegmentView.HIDECON, segmentView.showConstructionLine);

        segmentView.showConstructionLine = TrackSegmentView.SHOWCON;
        segmentView.hideConstructionLines(TrackSegmentView.HIDECONALL);
        assertEquals(TrackSegmentView.SHOWCON | TrackSegmentView.HIDECONALL, segmentView.showConstructionLine);

        // ----
        segmentView.showConstructionLine = TrackSegmentView.HIDECON;
        segmentView.hideConstructionLines(TrackSegmentView.SHOWCON);
        assertEquals(TrackSegmentView.SHOWCON, segmentView.showConstructionLine);

        segmentView.showConstructionLine = TrackSegmentView.HIDECON;
        segmentView.hideConstructionLines(TrackSegmentView.HIDECON);
        assertEquals(TrackSegmentView.HIDECON, segmentView.showConstructionLine);

        segmentView.showConstructionLine = TrackSegmentView.HIDECON;
        segmentView.hideConstructionLines(TrackSegmentView.HIDECONALL);
        assertEquals(TrackSegmentView.HIDECON | TrackSegmentView.HIDECONALL, segmentView.showConstructionLine);

        // ----
        segmentView.showConstructionLine = TrackSegmentView.HIDECONALL;
        segmentView.hideConstructionLines(TrackSegmentView.SHOWCON);
        assertEquals(0, segmentView.showConstructionLine);

        segmentView.showConstructionLine = TrackSegmentView.HIDECONALL;
        segmentView.hideConstructionLines(TrackSegmentView.HIDECON);
        assertEquals(TrackSegmentView.HIDECON, segmentView.showConstructionLine);

        segmentView.showConstructionLine = TrackSegmentView.HIDECONALL;
        segmentView.hideConstructionLines(TrackSegmentView.HIDECONALL);
        assertEquals(TrackSegmentView.HIDECONALL, segmentView.showConstructionLine);

        // ----
        segmentView.showConstructionLine = TrackSegmentView.HIDECON | TrackSegmentView.HIDECONALL;
        segmentView.hideConstructionLines(TrackSegmentView.SHOWCON);
        assertEquals(TrackSegmentView.HIDECON, segmentView.showConstructionLine);

        segmentView.showConstructionLine = TrackSegmentView.HIDECON | TrackSegmentView.HIDECONALL;
        segmentView.hideConstructionLines(TrackSegmentView.HIDECON);
        assertEquals(TrackSegmentView.HIDECON, segmentView.showConstructionLine);

        segmentView.showConstructionLine = TrackSegmentView.HIDECON | TrackSegmentView.HIDECONALL;
        segmentView.hideConstructionLines(TrackSegmentView.HIDECONALL);
        assertEquals(TrackSegmentView.HIDECON | TrackSegmentView.HIDECONALL, segmentView.showConstructionLine);

        // ----
        segmentView.showConstructionLine = TrackSegmentView.SHOWCON | TrackSegmentView.HIDECONALL;
        segmentView.hideConstructionLines(TrackSegmentView.SHOWCON);
        assertEquals(TrackSegmentView.SHOWCON, segmentView.showConstructionLine);

        segmentView.showConstructionLine = TrackSegmentView.SHOWCON | TrackSegmentView.HIDECONALL;
        segmentView.hideConstructionLines(TrackSegmentView.HIDECON);
        assertEquals(TrackSegmentView.HIDECON, segmentView.showConstructionLine);

        segmentView.showConstructionLine = TrackSegmentView.SHOWCON | TrackSegmentView.HIDECONALL;
        segmentView.hideConstructionLines(TrackSegmentView.HIDECONALL);
        assertEquals(TrackSegmentView.SHOWCON | TrackSegmentView.HIDECONALL, segmentView.showConstructionLine);

        // ----
        segmentView.showConstructionLine = TrackSegmentView.SHOWCON | TrackSegmentView.HIDECON;
        segmentView.hideConstructionLines(TrackSegmentView.SHOWCON);
        assertEquals(TrackSegmentView.SHOWCON, segmentView.showConstructionLine);

        segmentView.showConstructionLine = TrackSegmentView.SHOWCON | TrackSegmentView.HIDECON;
        segmentView.hideConstructionLines(TrackSegmentView.HIDECON);
        assertEquals(TrackSegmentView.HIDECON, segmentView.showConstructionLine);

        segmentView.showConstructionLine = TrackSegmentView.SHOWCON | TrackSegmentView.HIDECON;
        segmentView.hideConstructionLines(TrackSegmentView.HIDECONALL);
        assertEquals(TrackSegmentView.SHOWCON | TrackSegmentView.HIDECON | TrackSegmentView.HIDECONALL, segmentView.showConstructionLine);

        // ----
        segmentView.showConstructionLine = TrackSegmentView.SHOWCON | TrackSegmentView.HIDECON | TrackSegmentView.HIDECONALL;
        segmentView.hideConstructionLines(TrackSegmentView.SHOWCON);
        assertEquals(TrackSegmentView.SHOWCON | TrackSegmentView.HIDECON, segmentView.showConstructionLine);

        segmentView.showConstructionLine = TrackSegmentView.SHOWCON | TrackSegmentView.HIDECON | TrackSegmentView.HIDECONALL;
        segmentView.hideConstructionLines(TrackSegmentView.HIDECON);
        assertEquals(TrackSegmentView.HIDECON, segmentView.showConstructionLine);

        segmentView.showConstructionLine = TrackSegmentView.SHOWCON | TrackSegmentView.HIDECON | TrackSegmentView.HIDECONALL;
        segmentView.hideConstructionLines(TrackSegmentView.HIDECONALL);
        assertEquals(TrackSegmentView.SHOWCON | TrackSegmentView.HIDECON | TrackSegmentView.HIDECONALL, segmentView.showConstructionLine);
    }

    /**
     *  Bridge Decorations
     */
    @Test
    public void testDefaultIsSetBridgeSideRight() {
        assertFalse(segmentView.isBridgeSideRight(), "segmentView.isBridgeSideRight() == false (default).");
        segmentView.setBridgeSideRight(true);
        assertTrue(segmentView.isBridgeSideRight(), "segmentView.isBridgeSideRight() == true (after set).");
    }

    @Test
    public void testDefaultIsSetBridgeSideLeft() {
        assertFalse(segmentView.isBridgeSideLeft(), "segmentView.isBridgeSideLeft() == false (default).");
        segmentView.setBridgeSideLeft(true);
        assertTrue(segmentView.isBridgeSideLeft(), "segmentView.isBridgeSideLeft() == true (after set).");
    }

    @Test
    public void testDefaultIsSetBridgeHasEntry() {
        assertFalse(segmentView.isBridgeHasEntry(), "segmentView.isBridgeHasEntry() == false (default).");
        segmentView.setBridgeHasEntry(true);
        assertTrue(segmentView.isBridgeHasEntry(), "segmentView.isBridgeHasEntry() == true (after set).");
    }

    @Test
    public void testDefaultIsSetBridgeHasExit() {
        assertFalse(segmentView.isBridgeHasExit(), "segmentView.isBridgeHasExit() == false (default).");
        segmentView.setBridgeHasExit(true);
        assertTrue(segmentView.isBridgeHasExit(), "segmentView.isBridgeHasExit() == true (after set).");
    }

    @Test
    public void testDefaultGetSetBridgeDeckWidth() {
        assertEquals(10, segmentView.getBridgeDeckWidth(), "segmentView.getBridgeDeckWidth() == 10 (default).");
        segmentView.setBridgeDeckWidth(-1);
        assertNotEquals(-1, segmentView.getBridgeDeckWidth(), "segmentView.setBridgeDeckWidth(-1) not allowed.");
        assertEquals(6, segmentView.getBridgeDeckWidth(), "segmentView.getBridgeDeckWidth() == 6 (after set).");
        segmentView.setBridgeDeckWidth(15);
        assertEquals(15, segmentView.getBridgeDeckWidth(), "segmentView.getBridgeDeckWidth() == 15 (after set).");
    }

    @Test
    public void testDefaultGetSetBridgeLineWidth() {
        assertEquals(1, segmentView.getBridgeLineWidth(), "segmentView.getBridgeLineWidth() == 1 (default).");
        segmentView.setBridgeLineWidth(-1);
        assertNotEquals(-1, segmentView.getBridgeLineWidth(), "segmentView.setBridgeLineWidth(-1) not allowed.");
        segmentView.setBridgeLineWidth(3);
        assertEquals(3, segmentView.getBridgeLineWidth(), "segmentView.getBridgeLineWidth() == 3 (after set).");
    }

    @Test
    public void testDefaultGetSetBridgeApproachWidth() {
        assertEquals(4, segmentView.getBridgeApproachWidth(), "segmentView.getBridgeApproachWidth() == 4 (default).");
        segmentView.setBridgeApproachWidth(-1);
        assertNotEquals(-1, segmentView.getBridgeApproachWidth(), "segmentView.setBridgeApproachWidth(-1) not allowed.");
        assertEquals(8, segmentView.getBridgeApproachWidth(), "segmentView.getBridgeApproachWidth() == 8 (after set).");
        segmentView.setBridgeApproachWidth(16);
        assertEquals(16, segmentView.getBridgeApproachWidth(), "segmentView.getBridgeApproachWidth() ==16 (after set).");
    }

    /**
     *  Arrow Decorations
     */
    @Test
    public void testDefaultGetSetArrowStyle() {
        assertEquals(0, segmentView.getArrowStyle(), "segmentView.getArrowStyle() == 0 (default).");
        segmentView.setArrowStyle(-1);
        assertNotEquals(-1, segmentView.getArrowStyle(), "segmentView.setArrowStyle(-1) not allowed.");
        segmentView.setArrowStyle(5);
        assertEquals(5, segmentView.getArrowStyle(), "segmentView.getArrowStyle() == 5 (after set).");
    }

    @Test
    public void testDefaultIsSetArrowEndStart() {
        assertFalse(segmentView.isArrowEndStart(), "segmentView.isArrowEndStart() == false (default).");
        segmentView.setArrowEndStart(true);
        assertTrue(segmentView.isArrowEndStart(), "segmentView.isArrowEndStart() == true (default).");
    }

    @Test
    public void testDefaultIsSetArrowEndStop() {
        assertFalse(segmentView.isArrowEndStop(), "segmentView.isArrowEndStop() == false (default).");
        segmentView.setArrowEndStop(true);
        assertTrue(segmentView.isArrowEndStop(), "segmentView.isArrowEndStop() == true (default).");
    }

    @Test
    public void testDefaultIsSetArrowDirIn() {
        assertFalse(segmentView.isArrowDirIn(), "segmentView.isArrowDirIn() == false (default).");
        segmentView.setArrowDirIn(true);
        assertTrue(segmentView.isArrowDirIn(), "segmentView.isArrowDirIn() == true (default).");
    }

    @Test
    public void testDefaultIsSetArrowDirOut() {
        assertFalse(segmentView.isArrowDirOut(), "segmentView.isArrowDirOut() == false (default).");
        segmentView.setArrowDirOut(true);
        assertTrue(segmentView.isArrowDirOut(), "segmentView.isArrowDirOut() == false (after set).");
    }

    @Test
    public void testDefaultGetSetArrowLineWidth() {
        assertEquals(4, segmentView.getArrowLineWidth(), "segmentView.getArrowLineWidth() == 4 (default).");
        segmentView.setArrowLineWidth(-1);
        assertNotEquals(-1, segmentView.getArrowLineWidth(), "segmentView.setArrowLineWidth(-1) not allowed.");
        segmentView.setArrowLineWidth(5);
        assertEquals(5, segmentView.getArrowLineWidth(), "segmentView.getArrowLineWidth() == 5 (after set).");
    }

    @Test
    public void testDefaultGetSetArrowLength() {
        assertEquals(4, segmentView.getArrowLength(), "segmentView.getArrowLength() == 4 (default).");
        segmentView.setArrowLength(-1);
        assertNotEquals(-1, segmentView.getArrowLength(), "segmentView.setArrowLength(-1) not allowed.");
        segmentView.setArrowLength(5);
        assertEquals(5, segmentView.getArrowLength(), "segmentView.getArrowLength() == 5 (after set).");
    }


    /**
     *  Bumper Decorations
     */
    @Test
    public void testDefaultIsSetBumperEndStart() {
        assertFalse(segmentView.isBumperEndStart(), "segmentView.isBumperEndStart() == false (default).");
        segmentView.setBumperEndStart(true);
        assertTrue(segmentView.isBumperEndStart(), "segmentView.isBumperEndStart() == true (after set).");
    }

    @Test
    public void testDefaultIsSetBumperEndStop() {
        assertFalse(segmentView.isBumperEndStop(), "segmentView.isBumperEndStop() == false (default).");
        segmentView.setBumperEndStop(true);
        assertTrue(segmentView.isBumperEndStop(), "segmentView.isBumperEndStop() == true (after set).");
    }

    @Test
    public void testDefaultGetSetBumperLineWidth() {
        assertEquals(8, segmentView.getBumperLineWidth(), "segmentView.getBumperLineWidth() == 8 (default).");
        segmentView.setBumperLineWidth(-1);
        assertNotEquals(-1, segmentView.getBumperLineWidth(), "segmentView.setBumperLineWidth(-1) not allowed.");
        segmentView.setBumperLineWidth(5);
        assertEquals(5, segmentView.getBumperLineWidth(), "segmentView.getBumperLineWidth() == 5 (after set).");
    }

    @Test
    public void testDefaultGetSetBumperLength() {
        assertEquals(8, segmentView.getBumperLength(), "segmentView.getBumperLength() == 8 (default).");
        segmentView.setBumperLength(-1);
        assertNotEquals(-1, segmentView.getBumperLength(), "segmentView.setBumperLength(-1) not allowed.");
        segmentView.setBumperLength(12);
        assertEquals(12, segmentView.getBumperLength(), "segmentView.getBumperLength() == 12 (after set).");
    }

    @Test
    public void testDefaultIsSetBumperFlipped() {
        assertFalse(segmentView.isBumperFlipped(), "segmentView.isBumperFlipped() == false (default).");
        segmentView.setBumperFlipped(true);
        assertTrue(segmentView.isBumperFlipped(), "segmentView.isBumperFlipped() == true (after set).");
    }

    /**
     *  Tunnel Decorations
     */
    @Test
    public void testDefaultIsSetTunnelSideRight() {
        assertFalse(segmentView.isTunnelSideRight(), "segmentView.isTunnelSideRight() == false (default).");
        segmentView.setTunnelSideRight(true);
        assertTrue(segmentView.isTunnelSideRight(), "segmentView.isTunnelSideRight() == true (after set).");
    }

    @Test
    public void testDefaultIsSetTunnelSideLeft() {
        assertFalse(segmentView.isTunnelSideLeft(), "segmentView.isTunnelSideLeft() == false (default).");
        segmentView.setTunnelSideLeft(true);
        assertTrue(segmentView.isTunnelSideLeft(), "segmentView.isTunnelSideLeft() == true (after set).");
    }

    @Test
    public void testDefaultIsSetTunnelHasEntry() {
        assertFalse(segmentView.isTunnelHasEntry(), "segmentView.isTunnelHasEntry() == false (default).");
        segmentView.setTunnelHasEntry(true);
        assertTrue(segmentView.isTunnelHasEntry(), "segmentView.isTunnelHasEntry() == true (after set).");
    }

    @Test
    public void testDefaultIsSetTunnelHasExit() {
        assertFalse(segmentView.isTunnelHasExit(), "segmentView.isTunnelHasExit() == false (default).");
        segmentView.setTunnelHasExit(true);
        assertTrue(segmentView.isTunnelHasExit(), "segmentView.isTunnelHasExit() == true (after set).");
    }

    @Test
    public void testDefaultGetSetTunnelFloorWidth() {
        assertEquals(10, segmentView.getTunnelFloorWidth(), "segmentView.getTunnelFloorWidth() == 10 (default).");
        segmentView.setTunnelFloorWidth(-1);
        assertNotEquals(-1, segmentView.getTunnelFloorWidth(), "segmentView.setTunnelFloorWidth(-1) not allowed.");
        segmentView.setTunnelFloorWidth(5);
        assertEquals(5, segmentView.getTunnelFloorWidth(), "segmentView.getTunnelFloorWidth() == 5 (after set).");
    }

    @Test
    public void testDefaultGetSetTunnelLineWidth() {
        assertEquals(1, segmentView.getTunnelLineWidth(), "segmentView.getTunnelLineWidth() == 1 (default).");
        segmentView.setTunnelLineWidth(-1);
        assertNotEquals(-1, segmentView.getTunnelLineWidth(), "segmentView.setTunnelLineWidth(-1) not allowed.");
        segmentView.setTunnelLineWidth(5);
        assertEquals(5, segmentView.getTunnelLineWidth(), "segmentView.getTunnelLineWidth() == 5 (after set).");
    }

    @Test
    public void testDefaultGetSetTunnelEntranceWidth() {
        assertEquals(16, segmentView.getTunnelEntranceWidth(), "segmentView.getTunnelEntranceWidth() == 16 (default).");
        segmentView.setTunnelEntranceWidth(-1);
        assertNotEquals(-1, segmentView.getTunnelEntranceWidth(), "segmentView.setTunnelEntranceWidth(-1) not allowed.");
        segmentView.setTunnelEntranceWidth(5);
        assertEquals(5, segmentView.getTunnelEntranceWidth(), "segmentView.getTunnelEntranceWidth() == 5 (after set).");
    }

    private TrackSegment segment;
    private TrackSegmentView segmentView;

    @Override
    @BeforeEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        super.setUp();
        assertNotNull(layoutEditor);

        PositionablePoint p1 = new PositionablePoint("A1", PositionablePoint.PointType.ANCHOR, layoutEditor);
        PositionablePointView p1v = new PositionablePointView(p1, new Point2D.Double(210.0, 220.0), layoutEditor);
        layoutEditor.addLayoutTrack(p1, p1v);

        PositionablePoint p2 = new PositionablePoint("A2", PositionablePoint.PointType.ANCHOR, layoutEditor);
        PositionablePointView p2v = new PositionablePointView(p2, new Point2D.Double(220.0, 233.0), layoutEditor);
        layoutEditor.addLayoutTrack(p2, p2v);

        segment = new TrackSegment("TS01", p1, HitPointType.POS_POINT, p2, HitPointType.POS_POINT, false, layoutEditor);
        segmentView = new TrackSegmentView(segment, layoutEditor);
        assertNotNull(segmentView);
        layoutEditor.addLayoutTrack(segment, segmentView);

    }

    @Override
    @AfterEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() {
        segment = null;
        segmentView = null;
        super.tearDown();
    }
}
