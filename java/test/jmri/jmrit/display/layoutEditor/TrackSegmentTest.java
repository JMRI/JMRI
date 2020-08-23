package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;

import java.awt.geom.Rectangle2D;
import jmri.JmriException;
import jmri.util.*;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.Operator;

/**
 * Test simple functioning of TrackSegment.
 * <p>
 * Note this uses <code>@BeforeAll</code> and <code>@AfterAll</code> to do
 * static setup.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class TrackSegmentTest extends LayoutTrackTest {

    static private LayoutEditor layoutEditor = null;
    static private TrackSegment trackSegment = null;

    // the amount of variation allowed floating point values in order to be considered equal
    static final double tolerance = 0.000001;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            // Invalid parameters in TrackSegment constructor call
            TrackSegment ts = new TrackSegment("TS01", null, HitPointType.NONE, null, HitPointType.NONE, false, false, layoutEditor);
            Assert.assertNotNull("TrackSegment TS01 not null", ts);
            JUnitAppender.assertErrorMessage("Invalid object in TrackSegment constructor call - TS01");
            JUnitAppender.assertErrorMessage("Invalid connect type 1 ('NONE') in TrackSegment constructor - TS01");
            JUnitAppender.assertErrorMessage("Invalid connect type 2 ('NONE') in TrackSegment constructor - TS01");
        }
    }

    @Test
    public void testConstructionLinesRead() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        trackSegment.showConstructionLine = 0;
        Assert.assertTrue("From 0", trackSegment.isShowConstructionLines());
        Assert.assertTrue("From 0", trackSegment.hideConstructionLines());

        trackSegment.showConstructionLine = TrackSegment.HIDECONALL;
        Assert.assertFalse("HIDECONALL", trackSegment.isShowConstructionLines());
        Assert.assertTrue("HIDECONALL", trackSegment.hideConstructionLines());

        trackSegment.showConstructionLine = TrackSegment.HIDECON;
        Assert.assertFalse("HIDECON", trackSegment.isShowConstructionLines());
        Assert.assertTrue("HIDECON", trackSegment.hideConstructionLines());

        trackSegment.showConstructionLine = TrackSegment.SHOWCON;
        Assert.assertTrue("SHOWCON", trackSegment.isShowConstructionLines());
        Assert.assertFalse("SHOWCON", trackSegment.hideConstructionLines());

        trackSegment.showConstructionLine = TrackSegment.SHOWCON | TrackSegment.HIDECON | TrackSegment.HIDECONALL;
        Assert.assertFalse("all", trackSegment.isShowConstructionLines());
        Assert.assertFalse("all", trackSegment.hideConstructionLines());

    }

    @Test
    public void hideConstructionLinesOfInt() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        trackSegment.showConstructionLine = 0;
        trackSegment.hideConstructionLines(TrackSegment.SHOWCON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.SHOWCON);

        trackSegment.showConstructionLine = 0;
        trackSegment.hideConstructionLines(TrackSegment.HIDECON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECON);

        trackSegment.showConstructionLine = 0;
        trackSegment.hideConstructionLines(TrackSegment.HIDECONALL);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECONALL);

        // ----
        trackSegment.showConstructionLine = TrackSegment.SHOWCON;
        trackSegment.hideConstructionLines(TrackSegment.SHOWCON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.SHOWCON);

        trackSegment.showConstructionLine = TrackSegment.SHOWCON;
        trackSegment.hideConstructionLines(TrackSegment.HIDECON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECON);

        trackSegment.showConstructionLine = TrackSegment.SHOWCON;
        trackSegment.hideConstructionLines(TrackSegment.HIDECONALL);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.SHOWCON | TrackSegment.HIDECONALL);

        // ----
        trackSegment.showConstructionLine = TrackSegment.HIDECON;
        trackSegment.hideConstructionLines(TrackSegment.SHOWCON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.SHOWCON);

        trackSegment.showConstructionLine = TrackSegment.HIDECON;
        trackSegment.hideConstructionLines(TrackSegment.HIDECON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECON);

        trackSegment.showConstructionLine = TrackSegment.HIDECON;
        trackSegment.hideConstructionLines(TrackSegment.HIDECONALL);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECON | TrackSegment.HIDECONALL);

        // ----
        trackSegment.showConstructionLine = TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.SHOWCON);
        Assert.assertEquals(trackSegment.showConstructionLine, 0);

        trackSegment.showConstructionLine = TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.HIDECON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECON);

        trackSegment.showConstructionLine = TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.HIDECONALL);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECONALL);

        // ----
        trackSegment.showConstructionLine = TrackSegment.HIDECON | TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.SHOWCON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECON);

        trackSegment.showConstructionLine = TrackSegment.HIDECON | TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.HIDECON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECON);

        trackSegment.showConstructionLine = TrackSegment.HIDECON | TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.HIDECONALL);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECON | TrackSegment.HIDECONALL);

        // ----
        trackSegment.showConstructionLine = TrackSegment.SHOWCON | TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.SHOWCON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.SHOWCON);

        trackSegment.showConstructionLine = TrackSegment.SHOWCON | TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.HIDECON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECON);

        trackSegment.showConstructionLine = TrackSegment.SHOWCON | TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.HIDECONALL);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.SHOWCON | TrackSegment.HIDECONALL);

        // ----
        trackSegment.showConstructionLine = TrackSegment.SHOWCON | TrackSegment.HIDECON;
        trackSegment.hideConstructionLines(TrackSegment.SHOWCON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.SHOWCON);

        trackSegment.showConstructionLine = TrackSegment.SHOWCON | TrackSegment.HIDECON;
        trackSegment.hideConstructionLines(TrackSegment.HIDECON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECON);

        trackSegment.showConstructionLine = TrackSegment.SHOWCON | TrackSegment.HIDECON;
        trackSegment.hideConstructionLines(TrackSegment.HIDECONALL);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.SHOWCON | TrackSegment.HIDECON | TrackSegment.HIDECONALL);

        // ----
        trackSegment.showConstructionLine = TrackSegment.SHOWCON | TrackSegment.HIDECON | TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.SHOWCON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.SHOWCON | TrackSegment.HIDECON);

        trackSegment.showConstructionLine = TrackSegment.SHOWCON | TrackSegment.HIDECON | TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.HIDECON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECON);

        trackSegment.showConstructionLine = TrackSegment.SHOWCON | TrackSegment.HIDECON | TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.HIDECONALL);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.SHOWCON | TrackSegment.HIDECON | TrackSegment.HIDECONALL);
    }

    @Test
    public void testReplaceTrackConnection() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("trackSegment.replaceTrackConnection(null, null, NONE) fail", trackSegment.replaceTrackConnection(null, null, HitPointType.NONE));
            JUnitAppender.assertWarnMessage("TS1.replaceTrackConnection(null, null, NONE); Can't replace null track connection with null");

            LayoutTrack c1 = trackSegment.getConnect1();
            HitPointType t1 = trackSegment.getType1();
            Assert.assertTrue("trackSegment.replaceTrackConnection(c1, null, NONE) fail", trackSegment.replaceTrackConnection(c1, null, HitPointType.NONE));
            Assert.assertNull("trackSegment.replaceTrackConnection(c1, null, NONE) fail", trackSegment.getConnect1());

            Assert.assertTrue("trackSegment.replaceTrackConnection(null, c1, t1) fail", trackSegment.replaceTrackConnection(null, c1, t1));
            Assert.assertEquals("trackSegment.replaceTrackConnection(null, c1, t1) fail", c1, trackSegment.getConnect1());

            PositionablePoint a3 = new PositionablePoint("A3", PositionablePoint.PointType.ANCHOR, new Point2D.Double(10.0, 10.0), layoutEditor);
            Assert.assertTrue("trackSegment.replaceTrackConnection(c1, a3, POS_POINT) fail", trackSegment.replaceTrackConnection(c1, a3, HitPointType.POS_POINT));
        }
    }

    @Test
    public void testToString() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("trackSegment.toString()", "TrackSegment TS1 c1:{A1 (POS_POINT)}, c2:{A2 (POS_POINT)}", trackSegment.toString());
        }
    }

    @Test
    public void testSetNewConnect() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            trackSegment.setNewConnect1(null, HitPointType.NONE);
            Assert.assertEquals("trackSegment.setNewConnect1(null, NONE)", null, trackSegment.getConnect1());
            Assert.assertEquals("trackSegment.setNewConnect1(null, NONE)", HitPointType.NONE, trackSegment.getType1());

            trackSegment.setNewConnect2(null, HitPointType.NONE);
            Assert.assertEquals("trackSegment.setNewConnect1(null, NONE)", null, trackSegment.getConnect2());
            Assert.assertEquals("trackSegment.setNewConnect1(null, NONE)", HitPointType.NONE, trackSegment.getType2());
        }
    }

    @Test
    public void test_getDirectionRAD() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("trackSegment.getDirectionRAD()", 4.056693354143153, trackSegment.getDirectionRAD(), tolerance);
            Assert.assertEquals("trackSegment.getDirectionDEG()", 232.4314079711725, trackSegment.getDirectionDEG(), tolerance);
        }
    }

    @Test
    public void test_getConnection() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            boolean fail = true;   // assume failure (pessimist!)
            try {
                Assert.assertNull("trackSegment.getConnection()", trackSegment.getConnection(HitPointType.NONE));
            } catch (JmriException e) {
                fail = false;
            }
            Assert.assertFalse("trackSegment.getConnection(NONE) threw JmriException", fail);
        }
    }

    @Test
    public void test_getBounds() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {

            Rectangle2D expected = new Rectangle2D.Double(10.0, 20.0, 10.0, 13.0);
            Rectangle2D actual = MathUtil.granulize(trackSegment.getBounds(), 0.1); //round to the nearest 1/10th of a pixel
            Assert.assertEquals("trackSegment.getBounds(LINE)", expected, actual);

            trackSegment.setArc(true);
            expected = new Rectangle2D.Double(10.0, 20.0, 10.0, 13.0);
            actual = MathUtil.granulize(trackSegment.getBounds(), 0.1); //round to the nearest 1/10th of a pixel
            Assert.assertEquals("trackSegment.getBounds(ARC)", expected, actual);

            trackSegment.setCircle(true);
            expected = new Rectangle2D.Double(10.0, 20.0, 10.100000000000001, 13.0);
            actual = MathUtil.granulize(trackSegment.getBounds(), 0.1); //round to the nearest 1/10th of a pixel
            Assert.assertEquals("trackSegment.getBounds(CIRCLE)", expected, actual);

            trackSegment.setBezier(true);
            trackSegment.setBezierControlPoint(new Point2D.Double(5.5, 15.5), 0);
            trackSegment.setBezierControlPoint(new Point2D.Double(25.5, 38.5), 1);

            expected = new Rectangle2D.Double(9.3, 19.400000000000002, 11.600000000000001, 14.4);
            actual = MathUtil.granulize(trackSegment.getBounds(), 0.1); //round to the nearest 1/10th of a pixel
            Assert.assertEquals("trackSegment.getBounds(BEZIER)", expected, actual);

            trackSegment.setBezier(false);
            expected = new Rectangle2D.Double(10.0, 20.0, 10.0, 13.0);
            actual = MathUtil.granulize(trackSegment.getBounds(), 0.1); //round to the nearest 1/10th of a pixel
            Assert.assertEquals("trackSegment.getBounds(LINE)", expected, actual);
        }
    }

    @Test
    public void test_getSetBezierControlPoints() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("trackSegment.getNumberOfBezierControlPoints == 0", 0, trackSegment.getNumberOfBezierControlPoints());

            Point2D p0 = new Point2D.Double(11.1, 22.2);
            trackSegment.setBezierControlPoint(p0, 0);
            Assert.assertEquals("trackSegment.getNumberOfBezierControlPoints == 1", 1, trackSegment.getNumberOfBezierControlPoints());
            Assert.assertEquals("trackSegment.getBezierControlPoint(0)", p0, trackSegment.getBezierControlPoint(0));

            Point2D p1 = new Point2D.Double(22.2, 33.3);
            trackSegment.setBezierControlPoint(p1, 1);
            Assert.assertEquals("trackSegment.getNumberOfBezierControlPoints == 2", 2, trackSegment.getNumberOfBezierControlPoints());
            Assert.assertEquals("trackSegment.getBezierControlPoint(0)", p0, trackSegment.getBezierControlPoint(0));
            Assert.assertEquals("trackSegment.getBezierControlPoint(1)", p1, trackSegment.getBezierControlPoint(1));

            Point2D p0P = new Point2D.Double(33.3, 44.4);
            trackSegment.setBezierControlPoint(p0P, 0);
            Assert.assertEquals("trackSegment.getNumberOfBezierControlPoints == 2", 2, trackSegment.getNumberOfBezierControlPoints());
            Assert.assertEquals("trackSegment.getBezierControlPoint(0)", p0P, trackSegment.getBezierControlPoint(0));
            Assert.assertEquals("trackSegment.getBezierControlPoint(1)", p1, trackSegment.getBezierControlPoint(1));

            Point2D p1P = new Point2D.Double(44.4, 55.5);
            trackSegment.setBezierControlPoint(p1P, -1);
            Assert.assertEquals("trackSegment.getNumberOfBezierControlPoints == 2", 2, trackSegment.getNumberOfBezierControlPoints());
            Assert.assertEquals("trackSegment.getBezierControlPoint(0)", p0P, trackSegment.getBezierControlPoint(0));
            Assert.assertEquals("trackSegment.getBezierControlPoint(1)", p1P, trackSegment.getBezierControlPoint(1));

            Assert.assertEquals("trackSegment.getBezierControlPoint(-1)", p1P, trackSegment.getBezierControlPoint(-1));
            Assert.assertEquals("trackSegment.getBezierControlPoint(-2)", p0P, trackSegment.getBezierControlPoint(-2));
        }
    }

    @Test
    public void test_getSetLayoutBlock() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertNull("trackSegment.getLayoutBlock()", trackSegment.getLayoutBlock());
            trackSegment.setLayoutBlock(null);
            Assert.assertNull("trackSegment.getLayoutBlock()", trackSegment.getLayoutBlock());

            LayoutBlock layoutBlock = new LayoutBlock("ILB999", "Test Block");
            trackSegment.setLayoutBlock(layoutBlock);
            Assert.assertEquals("trackSegment.getLayoutBlock()", layoutBlock, trackSegment.getLayoutBlock());

            trackSegment.setLayoutBlock(null);
            Assert.assertNull("trackSegment.getLayoutBlock()", trackSegment.getLayoutBlock());
        }
    }

    @Test
    public void test_setLayoutBlockByName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertNull("trackSegment.getLayoutBlock() == null (default)", trackSegment.getLayoutBlock());
            trackSegment.setLayoutBlockByName(null);
            Assert.assertNull("trackSegment.getLayoutBlock(null) == null", trackSegment.getLayoutBlock());
            trackSegment.setLayoutBlockByName("");
            Assert.assertNull("trackSegment.getLayoutBlock('') == null", trackSegment.getLayoutBlock());

            trackSegment.setLayoutBlockByName("invalid name");    //note: invalid name
            JUnitAppender.assertErrorMessage("provideLayoutBlock: The block name 'invalid name' does not return a block.");

//            LayoutBlock layoutBlock = new LayoutBlock("ILB999", "Test Block");
//            trackSegment.setLayoutBlockByName("Test Block");
//            Assert.assertEquals("trackSegment.getLayoutBlock() == layoutBlock", layoutBlock, trackSegment.getLayoutBlock());
//
//            trackSegment.setLayoutBlock(null);
//            Assert.assertNull("trackSegment.setLayoutBlock(null) == null", trackSegment.getLayoutBlock());
        }
    }

    @Test
    public void test_translateAndScaleCoords() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("trackSegment.getCentreSeg()", new Point2D.Double(15.0, 26.5), trackSegment.getCentreSeg());
            trackSegment.translateCoords((float) 111.1, (float) 222.2);
            Assert.assertEquals("trackSegment.translateCoords()", new Point2D.Double(126.0999984741211, 248.6999969482422), trackSegment.getCoordsCenter());
            trackSegment.scaleCoords((float) 2.2, (float) 3.3);
            Assert.assertEquals("trackSegment.scaleCoords()", new Point2D.Double(277.4200026559829, 820.7099780702592), trackSegment.getCoordsCenter());
        }
    }

    @Test
    public void test_setCoordsCenter() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("trackSegment.getCentreSeg()", new Point2D.Double(15.0, 26.5), trackSegment.getCentreSeg());
            Point2D newC = new Point2D.Double(111.1, 222.2);
            trackSegment.setCoordsCenter(newC);
            Assert.assertEquals("trackSegment.setCoordsCenter(p)", newC, trackSegment.getCoordsCenter());
        }
    }

    @Test
    public void test_findHitPointType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Point2D ccc = trackSegment.getCentreSeg();
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.TRACK, trackSegment.findHitPointType(ccc, false, false));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.NONE, trackSegment.findHitPointType(ccc, false, true));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.TRACK, trackSegment.findHitPointType(ccc, true, false));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.NONE, trackSegment.findHitPointType(ccc, true, true));

            Point2D ep1 = trackSegment.getConnect1().getCoordsForConnectionType(trackSegment.getType1());
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.TRACK, trackSegment.findHitPointType(ep1, false, false));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.NONE, trackSegment.findHitPointType(ep1, false, true));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.TRACK, trackSegment.findHitPointType(ep1, true, false));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.NONE, trackSegment.findHitPointType(ep1, true, true));

            Point2D ep2 = trackSegment.getConnect2().getCoordsForConnectionType(trackSegment.getType2());
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.TRACK, trackSegment.findHitPointType(ep2, false, false));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.NONE, trackSegment.findHitPointType(ep2, false, true));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.TRACK, trackSegment.findHitPointType(ep2, true, false));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.NONE, trackSegment.findHitPointType(ep2, true, true));

            trackSegment.setCircle(true);
            Point2D cp = trackSegment.getCoordsCenterCircle();
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.TRACK_CIRCLE_CENTRE, trackSegment.findHitPointType(cp, false, false));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.NONE, trackSegment.findHitPointType(cp, false, true));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.TRACK_CIRCLE_CENTRE, trackSegment.findHitPointType(cp, true, false));
            Assert.assertEquals("trackSegment.findHitPointType()", HitPointType.NONE, trackSegment.findHitPointType(cp, true, true));
        }
    }

    /*
     *  Arrow Decorations
     */
    @Test
    public void testDefaultGetSetArrowStyle() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("trackSegment.getArrowStyle() == 0 (default).", 0, trackSegment.getArrowStyle());
            trackSegment.setArrowStyle(-1);
            Assert.assertNotEquals("trackSegment.setArrowStyle(-1) not allowed.", -1, trackSegment.getArrowStyle());
            trackSegment.setArrowStyle(5);
            Assert.assertEquals("trackSegment.getArrowStyle() == 5 (after set).", 5, trackSegment.getArrowStyle());
        }
    }

    @Test
    public void testDefaultIsSetArrowEndStart() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("trackSegment.isArrowEndStart() == false (after set).", trackSegment.isArrowEndStart());
            trackSegment.setArrowEndStart(true);
            Assert.assertTrue("trackSegment.isArrowEndStart() == true (default).", trackSegment.isArrowEndStart());
        }
    }

    @Test
    public void testDefaultIsSetArrowEndStop() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("trackSegment.isArrowEndStop() == false (after set).", trackSegment.isArrowEndStop());
            trackSegment.setArrowEndStop(true);
            Assert.assertTrue("trackSegment.isArrowEndStop() == true (default).", trackSegment.isArrowEndStop());
        }
    }

    @Test
    public void testDefaultIsSetArrowDirIn() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("trackSegment.isArrowDirIn() == false (after set).", trackSegment.isArrowDirIn());
            trackSegment.setArrowDirIn(true);
            Assert.assertTrue("trackSegment.isArrowDirIn() == true (default).", trackSegment.isArrowDirIn());
        }
    }

    @Test
    public void testDefaultIsSetArrowDirOut() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("trackSegment.isArrowDirOut() == true (default).", trackSegment.isArrowDirOut());
            trackSegment.setArrowDirOut(true);
            Assert.assertTrue("trackSegment.isArrowDirOut() == false (after set).", trackSegment.isArrowDirOut());
        }
    }

    @Test
    public void testDefaultGetSetArrowLineWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("trackSegment.getArrowLineWidth() == 4 (default).", 4, trackSegment.getArrowLineWidth());
            trackSegment.setArrowLineWidth(-1);
            Assert.assertNotEquals("trackSegment.setArrowLineWidth(-1) not allowed.", -1, trackSegment.getArrowLineWidth());
            trackSegment.setArrowLineWidth(5);
            Assert.assertEquals("trackSegment.getArrowLineWidth() == 5 (after set).", 5, trackSegment.getArrowLineWidth());
        }
    }

    @Test
    public void testDefaultGetSetArrowLength() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("trackSegment.getArrowLength() == 4 (default).", 4, trackSegment.getArrowLength());
            trackSegment.setArrowLength(-1);
            Assert.assertNotEquals("trackSegment.setArrowLength(-1) not allowed.", -1, trackSegment.getArrowLength());
            trackSegment.setArrowLength(5);
            Assert.assertEquals("trackSegment.getArrowLength() == 5 (after set).", 5, trackSegment.getArrowLength());
        }
    }

    /*
        Bridge Decorations
     */
    @Test
    public void testDefaultIsSetBridgeSideRight() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("trackSegment.isBridgeSideRight() == true (default).", trackSegment.isBridgeSideRight());
            trackSegment.setBridgeSideRight(true);
            Assert.assertTrue("trackSegment.isBridgeSideRight() == false (after set).", trackSegment.isBridgeSideRight());
        }
    }

    @Test
    public void testDefaultIsSetBridgeSideLeft() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("trackSegment.isBridgeSideLeft() == true (default).", trackSegment.isBridgeSideLeft());
            trackSegment.setBridgeSideLeft(true);
            Assert.assertTrue("trackSegment.isBridgeSideLeft() == false (after set).", trackSegment.isBridgeSideLeft());
        }
    }

    @Test
    public void testDefaultIsSetBridgeHasEntry() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("trackSegment.isBridgeHasEntry() == true (default).", trackSegment.isBridgeHasEntry());
            trackSegment.setBridgeHasEntry(true);
            Assert.assertTrue("trackSegment.isBridgeHasEntry() == false (after set).", trackSegment.isBridgeHasEntry());
        }
    }

    @Test
    public void testDefaultIsSetBridgeHasExit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("trackSegment.isBridgeHasExit() == true (default).", trackSegment.isBridgeHasExit());
            trackSegment.setBridgeHasExit(true);
            Assert.assertTrue("trackSegment.isBridgeHasExit() == false (after set).", trackSegment.isBridgeHasExit());
        }
    }

    @Test
    public void testDefaultGetSetBridgeDeckWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("trackSegment.getBridgeDeckWidth() == 10 (default).", 10, trackSegment.getBridgeDeckWidth());
            trackSegment.setBridgeDeckWidth(-1);
            Assert.assertNotEquals("trackSegment.setBridgeDeckWidth(-1) not allowed.", -1, trackSegment.getBridgeDeckWidth());
            Assert.assertEquals("trackSegment.getBridgeDeckWidth() == 6 (after set).", 6, trackSegment.getBridgeDeckWidth());
            trackSegment.setBridgeDeckWidth(15);
            Assert.assertEquals("trackSegment.getBridgeDeckWidth() == 15 (after set).", 15, trackSegment.getBridgeDeckWidth());
        }
    }

    @Test
    public void testDefaultGetSetBridgeLineWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("trackSegment.getBridgeLineWidth() == 1 (default).", 1, trackSegment.getBridgeLineWidth());
            trackSegment.setBridgeLineWidth(-1);
            Assert.assertNotEquals("trackSegment.setBridgeLineWidth(-1) not allowed.", -1, trackSegment.getBridgeLineWidth());
            trackSegment.setBridgeLineWidth(3);
            Assert.assertEquals("trackSegment.getBridgeLineWidth() == 3 (after set).", 3, trackSegment.getBridgeLineWidth());
        }
    }

    @Test
    public void testDefaultGetSetBridgeApproachWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("trackSegment.getBridgeApproachWidth() == 4 (default).", 4, trackSegment.getBridgeApproachWidth());
            trackSegment.setBridgeApproachWidth(-1);
            Assert.assertNotEquals("trackSegment.setBridgeApproachWidth(-1) not allowed.", -1, trackSegment.getBridgeApproachWidth());
            Assert.assertEquals("trackSegment.getBridgeApproachWidth() == 8 (after set).", 8, trackSegment.getBridgeApproachWidth());
            trackSegment.setBridgeApproachWidth(16);
            Assert.assertEquals("trackSegment.getBridgeApproachWidth() ==16 (after set).", 16, trackSegment.getBridgeApproachWidth());
        }
    }

    /*
        Bumper Decorations
     */
    @Test
    public void testDefaultIsSetBumperEndStart() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("trackSegment.isBumperEndStart() == true (default).", trackSegment.isBumperEndStart());
            trackSegment.setBumperEndStart(true);
            Assert.assertTrue("trackSegment.isBumperEndStart() == false (after set).", trackSegment.isBumperEndStart());
        }
    }

    @Test
    public void testDefaultIsSetBumperEndStop() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("trackSegment.isBumperEndStop() == true (default).", trackSegment.isBumperEndStop());
            trackSegment.setBumperEndStop(true);
            Assert.assertTrue("trackSegment.isBumperEndStop() == false (after set).", trackSegment.isBumperEndStop());
        }
    }

    @Test
    public void testDefaultGetSetBumperLineWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("trackSegment.getBumperLineWidth() == 8 (default).", 8, trackSegment.getBumperLineWidth());
            trackSegment.setBumperLineWidth(-1);
            Assert.assertNotEquals("trackSegment.setBumperLineWidth(-1) not allowed.", -1, trackSegment.getBumperLineWidth());
            trackSegment.setBumperLineWidth(5);
            Assert.assertEquals("trackSegment.getBumperLineWidth() == 5 (after set).", 5, trackSegment.getBumperLineWidth());
        }
    }

    @Test
    public void testDefaultGetSetBumperLength() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("trackSegment.getBumperLength() == 10 (default).", 10, trackSegment.getBumperLength());
            trackSegment.setBumperLength(-1);
            Assert.assertNotEquals("trackSegment.setBumperLength(-1) not allowed.", -1, trackSegment.getBumperLength());
            trackSegment.setBumperLength(8);
            Assert.assertEquals("trackSegment.getBumperLength() == 8 (after set).", 8, trackSegment.getBumperLength());
        }
    }

    @Test
    public void testDefaultIsSetBumperFlipped() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("trackSegment.isBumperFlipped() == true (default).", trackSegment.isBumperFlipped());
            trackSegment.setBumperFlipped(true);
            Assert.assertTrue("trackSegment.isBumperFlipped() == false (after set).", trackSegment.isBumperFlipped());
        }
    }

    /*
        Tunnel Decorations
     */
    @Test
    public void testDefaultIsSetTunnelSideRight() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("trackSegment.isTunnelSideRight() == true (default).", trackSegment.isTunnelSideRight());
            trackSegment.setTunnelSideRight(true);
            Assert.assertTrue("trackSegment.isTunnelSideRight() == false (after set).", trackSegment.isTunnelSideRight());
        }
    }

    @Test
    public void testDefaultIsSetTunnelSideLeft() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("trackSegment.isTunnelSideLeft() == true (default).", trackSegment.isTunnelSideLeft());
            trackSegment.setTunnelSideLeft(true);
            Assert.assertTrue("trackSegment.isTunnelSideLeft() == false (after set).", trackSegment.isTunnelSideLeft());
        }
    }

    @Test
    public void testDefaultIsSetTunnelHasEntry() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("trackSegment.isTunnelHasEntry() == true (default).", trackSegment.isTunnelHasEntry());
            trackSegment.setTunnelHasEntry(true);
            Assert.assertTrue("trackSegment.isTunnelHasEntry() == false (after set).", trackSegment.isTunnelHasEntry());
        }
    }

    @Test
    public void testDefaultIsSetTunnelHasExit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("trackSegment.isTunnelHasExit() == true (default).", trackSegment.isTunnelHasExit());
            trackSegment.setTunnelHasExit(true);
            Assert.assertTrue("trackSegment.isTunnelHasExit() == false (after set).", trackSegment.isTunnelHasExit());
        }
    }

    @Test
    public void testDefaultGetSetTunnelFloorWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("trackSegment.getTunnelFloorWidth() == 10 (default).", 10, trackSegment.getTunnelFloorWidth());
            trackSegment.setTunnelFloorWidth(-1);
            Assert.assertNotEquals("trackSegment.setTunnelFloorWidth(-1) not allowed.", -1, trackSegment.getTunnelFloorWidth());
            trackSegment.setTunnelFloorWidth(5);
            Assert.assertEquals("trackSegment.getTunnelFloorWidth() == 5 (after set).", 5, trackSegment.getTunnelFloorWidth());
        }
    }

    @Test
    public void testDefaultGetSetTunnelLineWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("trackSegment.getTunnelLineWidth() == 1 (default).", 1, trackSegment.getTunnelLineWidth());
            trackSegment.setTunnelLineWidth(-1);
            Assert.assertNotEquals("trackSegment.setTunnelLineWidth(-1) not allowed.", -1, trackSegment.getTunnelLineWidth());
            trackSegment.setTunnelLineWidth(5);
            Assert.assertEquals("trackSegment.getTunnelLineWidth() == 5 (after set).", 5, trackSegment.getTunnelLineWidth());
        }
    }

    @Test
    public void testDefaultGetSetTunnelEntranceWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("trackSegment.getTunnelEntranceWidth() == 16 (default).", 16, trackSegment.getTunnelEntranceWidth());
            trackSegment.setTunnelEntranceWidth(-1);
            Assert.assertNotEquals("trackSegment.setTunnelEntranceWidth(-1) not allowed.", -1, trackSegment.getTunnelEntranceWidth());
            trackSegment.setTunnelEntranceWidth(5);
            Assert.assertEquals("trackSegment.getTunnelEntranceWidth() == 5 (after set).", 5, trackSegment.getTunnelEntranceWidth());
        }
    }

    @Test
    public void testSetCircleDefault() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            trackSegment.setCircle(true);
            Assert.assertEquals("trackSegment.setCircle(Default)", 90.0D, trackSegment.getAngle(), 0.01D);
        }
    }

    @Test
    public void testSetCircleZeroAngle() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            trackSegment.setAngle(0.0D);
            trackSegment.setCircle(true);
            Assert.assertEquals("trackSegment.setCircle(Zero Angle)", 90.0D, trackSegment.getAngle(), 0.01D);
        }
    }
    
    @Test
    public void testSetCirclePositiveAngle() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            trackSegment.setAngle(50.0D);
            trackSegment.setCircle(true);
            Assert.assertEquals("trackSegment.setCircle(Positive Angle)", 50.0D, trackSegment.getAngle(), 0.01D);
        }
    }

    
    //
    // from here down is testing infrastructure
    //
    /**
     * This is called once before all tests
     *
     * @throws Exception
     */
    @BeforeAll
    public static void setUpClass() throws Exception {
        if (!GraphicsEnvironment.isHeadless()) {

            layoutEditor = new LayoutEditor();

            //save the old string comparator
            stringComparator = Operator.getDefaultStringComparator();
            //set default string matching comparator to one that exactly matches and is case sensitive
            Operator.setDefaultStringComparator(new Operator.DefaultStringComparator(true, true));
        }
    }

    /**
     * This is called once after all tests
     *
     * @throws Exception
     */
    @AfterAll
    public static void tearDownClass() throws Exception {
        if (!GraphicsEnvironment.isHeadless()) {
            if (layoutEditor != null) {
                JUnitUtil.dispose(layoutEditor);
                // release refereces to layout editor
                layoutEditor = null;
            }
            //restore the default string matching comparator
            Operator.setDefaultStringComparator(stringComparator);
        }
    }
    private static Operator.StringComparator stringComparator = null;

    /**
     * This is called before each test
     *
     * @throws Exception
     */
    @BeforeEach
    public void setUpEach() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            if (layoutEditor != null) {
                PositionablePoint p1 = new PositionablePoint("A1", PositionablePoint.PointType.ANCHOR, new Point2D.Double(10.0, 20.0), layoutEditor);
                PositionablePoint p2 = new PositionablePoint("A2", PositionablePoint.PointType.ANCHOR, new Point2D.Double(20.0, 33.0), layoutEditor);
                trackSegment = new TrackSegment("TS1", p1, HitPointType.POS_POINT, p2, HitPointType.POS_POINT, false, true, layoutEditor);
            }
        }
    }

    /**
     * This is called after each test
     *
     * @throws Exception
     */
    @AfterEach
    public void tearDownEach() throws Exception {
        // release refereces to track segment
        trackSegment = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
