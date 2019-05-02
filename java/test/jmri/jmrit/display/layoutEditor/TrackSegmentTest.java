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

    @Test
    public void testDefaultGetSetArrowLineWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertTrue("TrackSegment.getArrowLineWidth() == 4 (default).", trackSegment.getArrowLength() == 4);
            trackSegment.setArrowLength(-1);
            Assert.assertNotEquals("TrackSegment.setArrowLength(-1) not allowed.", -1, trackSegment.getArrowLength());
            trackSegment.setArrowLength(5);
            Assert.assertEquals("TrackSegment.getArrowLength() == 5 (after set).", 5, trackSegment.getArrowLength());
        }
    }

    @Test
    public void testDefaultGetSetBridgeLineWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertTrue("TrackSegment.getBridgeLineWidth() == 1 (default).", trackSegment.getBridgeLineWidth() == 1);
            trackSegment.setBridgeLineWidth(-1);
            Assert.assertNotEquals("TrackSegment.setBridgeLineWidth(-1) not allowed.", -1, trackSegment.getBridgeLineWidth());
            trackSegment.setBridgeLineWidth(5);
            Assert.assertEquals("TrackSegment.getBridgeLineWidth() == 5 (after set).", 5, trackSegment.getBridgeLineWidth());
        }
    }

    @Test
    public void testDefaultGetSetBumperLineWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("TrackSegment.getBumperLineWidth() == 4 (default).", 4, trackSegment.getBumperLineWidth());
            trackSegment.setBumperLineWidth(-1);
            Assert.assertNotEquals("TrackSegment.setBumperLineWidth(-1) not allowed.", -1, trackSegment.getBumperLineWidth());
            trackSegment.setBumperLineWidth(5);
            Assert.assertEquals("TrackSegment.getBumperLineWidth() == 5 (after set).", 5, trackSegment.getBumperLineWidth());
        }
    }

    @Test
    public void testDefaultGetSetTunnelLineWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertTrue("TrackSegment.getTunnelLineWidth() == 1 (default).", trackSegment.getTunnelLineWidth() == 1);
            trackSegment.setTunnelLineWidth(-1);
            Assert.assertNotEquals("TrackSegment.setTunnelLineWidth(-1) not allowed.", -1, trackSegment.getTunnelLineWidth());
            trackSegment.setTunnelLineWidth(5);
            Assert.assertEquals("TrackSegment.getTunnelLineWidth() == 5 (after set).", 5, trackSegment.getTunnelLineWidth());
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
