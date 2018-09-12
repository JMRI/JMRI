package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import jmri.util.JUnitUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
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
        Assert.assertNotNull("exists", trackSegment);
    }

    @Test
    public void testDefaultGetSetArrowLineWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue("TrackSegment.getArrowLineWidth() == 4 (default).", trackSegment.getArrowLength() == 4);
        trackSegment.setArrowLength(-1);
        Assert.assertNotEquals("TrackSegment.setArrowLength(-1) not allowed.", -1, trackSegment.getArrowLength());
        trackSegment.setArrowLength(5);
        Assert.assertEquals("TrackSegment.getArrowLength() == 5 (after set).", 5, trackSegment.getArrowLength());
    }

    @Test
    public void testDefaultGetSetBridgeLineWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue("TrackSegment.getBridgeLineWidth() == 1 (default).", trackSegment.getBridgeLineWidth() == 1);
        trackSegment.setBridgeLineWidth(-1);
        Assert.assertNotEquals("TrackSegment.setBridgeLineWidth(-1) not allowed.", -1, trackSegment.getBridgeLineWidth());
        trackSegment.setBridgeLineWidth(5);
        Assert.assertEquals("TrackSegment.getBridgeLineWidth() == 5 (after set).", 5, trackSegment.getBridgeLineWidth());
    }

    @Test
    public void testDefaultGetSetBumperLineWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("TrackSegment.getBumperLineWidth() == 4 (default).", 4, trackSegment.getBumperLineWidth());
        trackSegment.setBumperLineWidth(-1);
        Assert.assertNotEquals("TrackSegment.setBumperLineWidth(-1) not allowed.", -1, trackSegment.getBumperLineWidth());
        trackSegment.setBumperLineWidth(5);
        Assert.assertEquals("TrackSegment.getBumperLineWidth() == 5 (after set).", 5, trackSegment.getBumperLineWidth());
    }

    @Test
    public void testDefaultGetSetTunnelLineWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue("TrackSegment.getTunnelLineWidth() == 1 (default).", trackSegment.getTunnelLineWidth() == 1);
        trackSegment.setTunnelLineWidth(-1);
        Assert.assertNotEquals("TrackSegment.setTunnelLineWidth(-1) not allowed.", -1, trackSegment.getTunnelLineWidth());
        trackSegment.setTunnelLineWidth(5);
        Assert.assertEquals("TrackSegment.getTunnelLineWidth() == 5 (after set).", 5, trackSegment.getTunnelLineWidth());
    }

    // from here down is testing infrastructure
    @BeforeClass
    static public void setUp() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            layoutEditor = new LayoutEditor();
            PositionablePoint p1 = new PositionablePoint("a", PositionablePoint.ANCHOR, new Point2D.Double(0.0, 0.0), layoutEditor);
            PositionablePoint p2 = new PositionablePoint("b", PositionablePoint.ANCHOR, new Point2D.Double(1.0, 1.0), layoutEditor);
            trackSegment = new TrackSegment("test", p1, LayoutTrack.POS_POINT, p2, LayoutTrack.POS_POINT, false, true, layoutEditor);
        }
    }

    @AfterClass
    static public void tearDown() throws Exception {
        JUnitUtil.dispose(layoutEditor);
        JUnitUtil.tearDown();
    }
}
