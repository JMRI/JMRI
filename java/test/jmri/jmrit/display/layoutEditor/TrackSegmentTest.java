package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
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

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if (layoutEditor != null) {
            Assert.assertNotNull("exists", trackSegment);
        }
    }

    /*
        Arrow Decorations
    */

    @Test
    public void testDefaultGetSetArrowStyle() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("TrackSegment.getArrowStyle() == 0 (default).", 0, trackSegment.getArrowStyle());
            trackSegment.setArrowStyle(-1);
            Assert.assertNotEquals("TrackSegment.setArrowStyle(-1) not allowed.", -1, trackSegment.getArrowStyle());
            trackSegment.setArrowStyle(5);
            Assert.assertEquals("TrackSegment.getArrowStyle() == 5 (after set).", 5, trackSegment.getArrowStyle());
        }
    }

    @Test
    public void testDefaultIsSetArrowEndStart() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("TrackSegment.isArrowEndStart() == false (after set).", trackSegment.isArrowEndStart());
            trackSegment.setArrowEndStart(true);
            Assert.assertTrue("TrackSegment.isArrowEndStart() == true (default).", trackSegment.isArrowEndStart());
        }
    }

    @Test
    public void testDefaultIsSetArrowEndStop() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("TrackSegment.isArrowEndStop() == false (after set).", trackSegment.isArrowEndStop());
            trackSegment.setArrowEndStop(true);
            Assert.assertTrue("TrackSegment.isArrowEndStop() == true (default).", trackSegment.isArrowEndStop());
        }
    }

    @Test
    public void testDefaultIsSetArrowDirIn() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("TrackSegment.isArrowDirIn() == false (after set).", trackSegment.isArrowDirIn());
            trackSegment.setArrowDirIn(true);
            Assert.assertTrue("TrackSegment.isArrowDirIn() == true (default).", trackSegment.isArrowDirIn());
        }
    }

    @Test
    public void testDefaultIsSetArrowDirOut() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("TrackSegment.isArrowDirOut() == true (default).", trackSegment.isArrowDirOut());
            trackSegment.setArrowDirOut(true);
            Assert.assertTrue("TrackSegment.isArrowDirOut() == false (after set).", trackSegment.isArrowDirOut());
        }
    }

    @Test
    public void testDefaultGetSetArrowLineWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("TrackSegment.getArrowLineWidth() == 4 (default).", 4, trackSegment.getArrowLineWidth());
            trackSegment.setArrowLineWidth(-1);
            Assert.assertNotEquals("TrackSegment.setArrowLineWidth(-1) not allowed.", -1, trackSegment.getArrowLineWidth());
            trackSegment.setArrowLineWidth(5);
            Assert.assertEquals("TrackSegment.getArrowLineWidth() == 5 (after set).", 5, trackSegment.getArrowLineWidth());
        }
    }

    @Test
    public void testDefaultGetSetArrowLength() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("TrackSegment.getArrowLength() == 4 (default).", 4, trackSegment.getArrowLength());
            trackSegment.setArrowLength(-1);
            Assert.assertNotEquals("TrackSegment.setArrowLength(-1) not allowed.", -1, trackSegment.getArrowLength());
            trackSegment.setArrowLength(5);
            Assert.assertEquals("TrackSegment.getArrowLength() == 5 (after set).", 5, trackSegment.getArrowLength());
        }
    }

    /*
        Bridge Decorations
    */

    @Test
    public void testDefaultIsSetBridgeSideRight() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("TrackSegment.isBridgeSideRight() == true (default).", trackSegment.isBridgeSideRight());
            trackSegment.setBridgeSideRight(true);
            Assert.assertTrue("TrackSegment.isBridgeSideRight() == false (after set).", trackSegment.isBridgeSideRight());
        }
    }

    @Test
    public void testDefaultIsSetBridgeSideLeft() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("TrackSegment.isBridgeSideLeft() == true (default).", trackSegment.isBridgeSideLeft());
            trackSegment.setBridgeSideLeft(true);
            Assert.assertTrue("TrackSegment.isBridgeSideLeft() == false (after set).", trackSegment.isBridgeSideLeft());
        }
    }

    @Test
    public void testDefaultIsSetBridgeHasEntry() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("TrackSegment.isBridgeHasEntry() == true (default).", trackSegment.isBridgeHasEntry());
            trackSegment.setBridgeHasEntry(true);
            Assert.assertTrue("TrackSegment.isBridgeHasEntry() == false (after set).", trackSegment.isBridgeHasEntry());
        }
    }

    @Test
    public void testDefaultIsSetBridgeHasExit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("TrackSegment.isBridgeHasExit() == true (default).", trackSegment.isBridgeHasExit());
            trackSegment.setBridgeHasExit(true);
            Assert.assertTrue("TrackSegment.isBridgeHasExit() == false (after set).", trackSegment.isBridgeHasExit());
        }
    }

    @Test
    public void testDefaultGetSetBridgeDeckWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("TrackSegment.getBridgeDeckWidth() == 10 (default).", 10, trackSegment.getBridgeDeckWidth());
            trackSegment.setBridgeDeckWidth(-1);
            Assert.assertNotEquals("TrackSegment.setBridgeDeckWidth(-1) not allowed.", -1, trackSegment.getBridgeDeckWidth());
            Assert.assertEquals("TrackSegment.getBridgeDeckWidth() == 6 (after set).", 6, trackSegment.getBridgeDeckWidth());
            trackSegment.setBridgeDeckWidth(15);
            Assert.assertEquals("TrackSegment.getBridgeDeckWidth() == 15 (after set).", 15, trackSegment.getBridgeDeckWidth());
        }
    }

    @Test
    public void testDefaultGetSetBridgeLineWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("TrackSegment.getBridgeLineWidth() == 1 (default).", 1, trackSegment.getBridgeLineWidth());
            trackSegment.setBridgeLineWidth(-1);
            Assert.assertNotEquals("TrackSegment.setBridgeLineWidth(-1) not allowed.", -1, trackSegment.getBridgeLineWidth());
            trackSegment.setBridgeLineWidth(3);
            Assert.assertEquals("TrackSegment.getBridgeLineWidth() == 3 (after set).", 3, trackSegment.getBridgeLineWidth());
        }
    }

    @Test
    public void testDefaultGetSetBridgeApproachWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("TrackSegment.getBridgeApproachWidth() == 4 (default).", 4, trackSegment.getBridgeApproachWidth());
            trackSegment.setBridgeApproachWidth(-1);
            Assert.assertNotEquals("TrackSegment.setBridgeApproachWidth(-1) not allowed.", -1, trackSegment.getBridgeApproachWidth());
            Assert.assertEquals("TrackSegment.getBridgeApproachWidth() == 8 (after set).", 8, trackSegment.getBridgeApproachWidth());
            trackSegment.setBridgeApproachWidth(16);
            Assert.assertEquals("TrackSegment.getBridgeApproachWidth() ==16 (after set).", 16, trackSegment.getBridgeApproachWidth());
        }
    }

    /*
        Bumper Decorations
    */

    @Test
    public void testDefaultIsSetBumperEndStart() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("TrackSegment.isBumperEndStart() == true (default).", trackSegment.isBumperEndStart());
            trackSegment.setBumperEndStart(true);
            Assert.assertTrue("TrackSegment.isBumperEndStart() == false (after set).", trackSegment.isBumperEndStart());
        }
    }

    @Test
    public void testDefaultIsSetBumperEndStop() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("TrackSegment.isBumperEndStop() == true (default).", trackSegment.isBumperEndStop());
            trackSegment.setBumperEndStop(true);
            Assert.assertTrue("TrackSegment.isBumperEndStop() == false (after set).", trackSegment.isBumperEndStop());
        }
    }
    
    @Test
    public void testDefaultGetSetBumperLineWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("TrackSegment.getBumperLineWidth() == 1 (default).", 1, trackSegment.getBumperLineWidth());
            trackSegment.setBumperLineWidth(-1);
            Assert.assertNotEquals("TrackSegment.setBumperLineWidth(-1) not allowed.", -1, trackSegment.getBumperLineWidth());
            trackSegment.setBumperLineWidth(5);
            Assert.assertEquals("TrackSegment.getBumperLineWidth() == 5 (after set).", 5, trackSegment.getBumperLineWidth());
        }
    }
   
    @Test
    public void testDefaultGetSetBumperLength() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("TrackSegment.getBumperLength() == 10 (default).", 10, trackSegment.getBumperLength());
            trackSegment.setBumperLength(-1);
            Assert.assertNotEquals("TrackSegment.setBumperLength(-1) not allowed.", -1, trackSegment.getBumperLength());
            trackSegment.setBumperLength(8);
            Assert.assertEquals("TrackSegment.getBumperLength() == 8 (after set).", 8, trackSegment.getBumperLength());
        }
    }

    @Test
    public void testDefaultIsSetBumperFlipped() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("TrackSegment.isBumperFlipped() == true (default).", trackSegment.isBumperFlipped());
            trackSegment.setBumperFlipped(true);
            Assert.assertTrue("TrackSegment.isBumperFlipped() == false (after set).", trackSegment.isBumperFlipped());
        }
    }

    /*
        Tunnel Decorations
    */

    @Test
    public void testDefaultIsSetTunnelSideRight() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("TrackSegment.isTunnelSideRight() == true (default).", trackSegment.isTunnelSideRight());
            trackSegment.setTunnelSideRight(true);
            Assert.assertTrue("TrackSegment.isTunnelSideRight() == false (after set).", trackSegment.isTunnelSideRight());
        }
    }

    @Test
    public void testDefaultIsSetTunnelSideLeft() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("TrackSegment.isTunnelSideLeft() == true (default).", trackSegment.isTunnelSideLeft());
            trackSegment.setTunnelSideLeft(true);
            Assert.assertTrue("TrackSegment.isTunnelSideLeft() == false (after set).", trackSegment.isTunnelSideLeft());
        }
    }

    @Test
    public void testDefaultIsSetTunnelHasEntry() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("TrackSegment.isTunnelHasEntry() == true (default).", trackSegment.isTunnelHasEntry());
            trackSegment.setTunnelHasEntry(true);
            Assert.assertTrue("TrackSegment.isTunnelHasEntry() == false (after set).", trackSegment.isTunnelHasEntry());
        }
    }

    @Test
    public void testDefaultIsSetTunnelHasExit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("TrackSegment.isTunnelHasExit() == true (default).", trackSegment.isTunnelHasExit());
            trackSegment.setTunnelHasExit(true);
            Assert.assertTrue("TrackSegment.isTunnelHasExit() == false (after set).", trackSegment.isTunnelHasExit());
        }
    }

    @Test
    public void testDefaultGetSetTunnelFloorWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("TrackSegment.getTunnelFloorWidth() == 10 (default).", 10, trackSegment.getTunnelFloorWidth());
            trackSegment.setTunnelFloorWidth(-1);
            Assert.assertNotEquals("TrackSegment.setTunnelFloorWidth(-1) not allowed.", -1, trackSegment.getTunnelFloorWidth());
            trackSegment.setTunnelFloorWidth(5);
            Assert.assertEquals("TrackSegment.getTunnelFloorWidth() == 5 (after set).", 5, trackSegment.getTunnelFloorWidth());
        }
    }

    @Test
    public void testDefaultGetSetTunnelLineWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("TrackSegment.getTunnelLineWidth() == 1 (default).", 1, trackSegment.getTunnelLineWidth());
            trackSegment.setTunnelLineWidth(-1);
            Assert.assertNotEquals("TrackSegment.setTunnelLineWidth(-1) not allowed.", -1, trackSegment.getTunnelLineWidth());
            trackSegment.setTunnelLineWidth(5);
            Assert.assertEquals("TrackSegment.getTunnelLineWidth() == 5 (after set).", 5, trackSegment.getTunnelLineWidth());
        }
    }

    @Test
    public void testDefaultGetSetTunnelEntranceWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("TrackSegment.getTunnelEntranceWidth() == 16 (default).", 16, trackSegment.getTunnelEntranceWidth());
            trackSegment.setTunnelEntranceWidth(-1);
            Assert.assertNotEquals("TrackSegment.setTunnelEntranceWidth(-1) not allowed.", -1, trackSegment.getTunnelEntranceWidth());
            trackSegment.setTunnelEntranceWidth(5);
            Assert.assertEquals("TrackSegment.getTunnelEntranceWidth() == 5 (after set).", 5, trackSegment.getTunnelEntranceWidth());
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
                PositionablePoint p1 = new PositionablePoint("a", PositionablePoint.ANCHOR, new Point2D.Double(0.0, 0.0), layoutEditor);
                PositionablePoint p2 = new PositionablePoint("b", PositionablePoint.ANCHOR, new Point2D.Double(1.0, 1.0), layoutEditor);
                trackSegment = new TrackSegment("test", p1, LayoutTrack.POS_POINT, p2, LayoutTrack.POS_POINT, false, true, layoutEditor);
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
