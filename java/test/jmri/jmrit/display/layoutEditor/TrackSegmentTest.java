package jmri.jmrit.display.layoutEditor;

import static jmri.jmrit.display.layoutEditor.LayoutTrack.NONE;
import static jmri.jmrit.display.layoutEditor.LayoutTrack.POS_POINT;
import static jmri.jmrit.display.layoutEditor.LayoutTrack.TRACK;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import jmri.JmriException;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test simple functioning of TrackSegment
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class TrackSegmentTest {

    static private LayoutEditor layoutEditor = null;
    static private TrackSegment trackSegment = null;

    // the amount of variation allowed floating point values in order to be considered equal
    static final double tolerance = 0.000001;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            // Invalid parameters in TrackSegment constructor call
            TrackSegment ts = new TrackSegment("TS01", null, NONE, null, NONE, false, false, layoutEditor);
            Assert.assertNotNull("TrackSegment TS01 not null", ts);
            jmri.util.JUnitAppender.assertErrorMessage("Invalid object in TrackSegment constructor call - TS01");
            jmri.util.JUnitAppender.assertErrorMessage("Invalid connect type 1 ('0') in TrackSegment constructor - TS01");
            jmri.util.JUnitAppender.assertErrorMessage("Invalid connect type 2 ('0') in TrackSegment constructor - TS01");
        }
    }

    @Test
    public void testReplaceTrackConnection() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("trackSegment.replaceTrackConnection(null, null, NONE) fail", trackSegment.replaceTrackConnection(null, null, NONE));
            jmri.util.JUnitAppender.assertErrorMessage("Can't replace null track connection with null");

            LayoutTrack c1 = trackSegment.getConnect1();
            int t1 = trackSegment.getType1();
            Assert.assertTrue("trackSegment.replaceTrackConnection(c1, null, NONE) fail", trackSegment.replaceTrackConnection(c1, null, NONE));
            Assert.assertNull("trackSegment.replaceTrackConnection(c1, null, NONE) fail", trackSegment.getConnect1());

            Assert.assertTrue("trackSegment.replaceTrackConnection(null, c1, t1) fail", trackSegment.replaceTrackConnection(null, c1, t1));
            Assert.assertEquals("trackSegment.replaceTrackConnection(null, c1, t1) fail", c1, trackSegment.getConnect1());

            PositionablePoint a3 = new PositionablePoint("A3", PositionablePoint.ANCHOR, new Point2D.Double(10.0, 10.0), layoutEditor);
            Assert.assertTrue("trackSegment.replaceTrackConnection(c1, a3, POS_POINT) fail", trackSegment.replaceTrackConnection(c1, a3, POS_POINT));
        }
    }

    @Test
    public void testToString() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("trackSegment.toString()", "TrackSegment TS1 c1:{A1 (1)}, c2:{A2 (1)}", trackSegment.toString());
        }
    }

    @Test
    public void testSetNewConnect() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            trackSegment.setNewConnect1(null, NONE);
            Assert.assertEquals("trackSegment.setNewConnect1(null, NONE)", null, trackSegment.getConnect1());
            Assert.assertEquals("trackSegment.setNewConnect1(null, NONE)", NONE, trackSegment.getType1());

            trackSegment.setNewConnect2(null, NONE);
            Assert.assertEquals("trackSegment.setNewConnect1(null, NONE)", null, trackSegment.getConnect2());
            Assert.assertEquals("trackSegment.setNewConnect1(null, NONE)", NONE, trackSegment.getType2());
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
                Assert.assertNull("trackSegment.getConnection()", trackSegment.getConnection(NONE));
            } catch (JmriException e) {
                fail = false;
            }
            Assert.assertFalse("trackSegment.getConnection(NONE) threw JmriException", fail);
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

            try {
                trackSegment.setLayoutBlockByName("invalid name");    //note: invalid name
                Assert.fail("trackSegment.setLayoutBlockByName(\"invalid name\"); NullPointerException not thrown");
            } catch (NullPointerException e) {
            }
            Assert.assertNull("trackSegment.getLayoutBlock() == null", trackSegment.getLayoutBlock());
            jmri.util.JUnitAppender.assertErrorMessage("provideLayoutBlock: The block name does not return a block.");

//            LayoutBlock layoutBlock = new LayoutBlock("ILB999", "Test Block");
//            trackSegment.setLayoutBlockByName("ILB999");
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
            Assert.assertEquals("trackSegment.findHitPointType()", TRACK, trackSegment.findHitPointType(ccc, false, false));
            Assert.assertEquals("trackSegment.findHitPointType()", NONE, trackSegment.findHitPointType(ccc, false, true));
            Assert.assertEquals("trackSegment.findHitPointType()", TRACK, trackSegment.findHitPointType(ccc, true, false));
            Assert.assertEquals("trackSegment.findHitPointType()", NONE, trackSegment.findHitPointType(ccc, true, true));

            Point2D ep1 = trackSegment.getConnect1().getCoordsForConnectionType(trackSegment.getType1());
            Assert.assertEquals("trackSegment.findHitPointType()", TRACK, trackSegment.findHitPointType(ep1, false, false));
            Assert.assertEquals("trackSegment.findHitPointType()", NONE, trackSegment.findHitPointType(ep1, false, true));
            Assert.assertEquals("trackSegment.findHitPointType()", TRACK, trackSegment.findHitPointType(ep1, true, false));
            Assert.assertEquals("trackSegment.findHitPointType()", NONE, trackSegment.findHitPointType(ep1, true, true));

            Point2D ep2 = trackSegment.getConnect2().getCoordsForConnectionType(trackSegment.getType2());
            Assert.assertEquals("trackSegment.findHitPointType()", TRACK, trackSegment.findHitPointType(ep2, false, false));
            Assert.assertEquals("trackSegment.findHitPointType()", NONE, trackSegment.findHitPointType(ep2, false, true));
            Assert.assertEquals("trackSegment.findHitPointType()", TRACK, trackSegment.findHitPointType(ep2, true, false));
            Assert.assertEquals("trackSegment.findHitPointType()", NONE, trackSegment.findHitPointType(ep2, true, true));
        }
    }

    /*
        Arrow Decorations
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
            Assert.assertEquals("trackSegment.getBumperLineWidth() == 1 (default).", 1, trackSegment.getBumperLineWidth());
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

    //
    // from here down is testing infrastructure
    //
    /**
     * This is called once before all tests
     *
     * @throws Exception
     */
    @BeforeClass
    static public void setUpAll() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            layoutEditor = new LayoutEditor();
        }
    }

    /**
     * This is called once after all tests
     *
     * @throws Exception
     */
    @AfterClass
    static public void tearDownAll() throws Exception {
        if (layoutEditor != null) {
            JUnitUtil.dispose(layoutEditor);
        }

        // release refereces to layout editor
        layoutEditor = null;

        JUnitUtil.tearDown();
    }

    /**
     * This is called before each test
     *
     * @throws Exception
     */
    @Before
    public void setUpEach() throws Exception {
        //JUnitUtil.setUp();
        //jmri.util.JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            if (layoutEditor != null) {
                PositionablePoint p1 = new PositionablePoint("A1", PositionablePoint.ANCHOR, new Point2D.Double(10.0, 20.0), layoutEditor);
                PositionablePoint p2 = new PositionablePoint("A2", PositionablePoint.ANCHOR, new Point2D.Double(20.0, 33.0), layoutEditor);
                trackSegment = new TrackSegment("TS1", p1, LayoutTrack.POS_POINT, p2, LayoutTrack.POS_POINT, false, true, layoutEditor);
            }
        }
    }

    /**
     * This is called after each test
     *
     * @throws Exception
     */
    @After
    public void tearDownEach() throws Exception {
        // release refereces to track segment
        trackSegment = null;
        //JUnitUtil.tearDown();
    }
}
