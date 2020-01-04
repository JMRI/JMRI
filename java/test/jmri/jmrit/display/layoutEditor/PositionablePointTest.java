package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import jmri.JmriException;
import jmri.util.JUnitUtil;
import jmri.util.MathUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test simple functioning of PositionablePoint
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PositionablePointTest {

    private LayoutEditor le = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.ANCHOR, MathUtil.zeroPoint2D, le);
        Assert.assertNotNull("exists", pp);
    }

    @Test
    public void testCtorFail() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        // 2nd parameter is illegal type
        PositionablePoint pp = new PositionablePoint("test", 0, MathUtil.zeroPoint2D, le);
        Assert.assertNotNull("exists", pp);
        jmri.util.JUnitAppender.assertErrorMessage("Illegal type of PositionablePoint - 0");
    }

    @Test
    public void testToString() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        PositionablePoint ppA = new PositionablePoint("test", PositionablePoint.ANCHOR, MathUtil.zeroPoint2D, le);
        Assert.assertNotNull("exists", ppA);
        Assert.assertEquals("Anchor 'test'", ppA.toString());

        PositionablePoint ppEB = new PositionablePoint("test", PositionablePoint.END_BUMPER, MathUtil.zeroPoint2D, le);
        Assert.assertNotNull("exists", ppEB);
        Assert.assertEquals("End Bumper 'test'", ppEB.toString());

        PositionablePoint ppEC = new PositionablePoint("test", PositionablePoint.EDGE_CONNECTOR, MathUtil.zeroPoint2D, le);
        Assert.assertNotNull("exists", ppEC);
        Assert.assertEquals("Edge Connector 'test'", ppEC.toString());
    }

    @Test
    public void testGetCoordsCenter() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        PositionablePoint pp1 = new PositionablePoint("test", PositionablePoint.ANCHOR, MathUtil.zeroPoint2D, le);
        Assert.assertNotNull("exists", pp1);
        Assert.assertEquals("getCoordsCenter equal to zeroPoint2D", MathUtil.zeroPoint2D, pp1.getCoordsCenter());

        Point2D point2 = new Point2D.Double(666.6, 999.9);
        PositionablePoint pp2 = new PositionablePoint("test", PositionablePoint.ANCHOR, point2, le);
        Assert.assertNotNull("exists", pp2);
        Assert.assertEquals("getCoordsCenter equal to {666.6, 999.9}", point2, pp2.getCoordsCenter());
    }

    @Test
    public void testSetCoords() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.ANCHOR, MathUtil.zeroPoint2D, le);
        Assert.assertNotNull("exists", pp);
        Assert.assertEquals("getCoordsCenter equal to zeroPoint2D", MathUtil.zeroPoint2D, pp.getCoordsCenter());

        Point2D point2 = new Point2D.Double(666.6, 999.9);
        pp.setCoordsCenter(point2);
        Assert.assertEquals("getCoordsCenter equal to {666.6, 999.9}", point2, pp.getCoordsCenter());
    }

    @Test
    public void testScaleCoords() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        Point2D point = new Point2D.Double(666.6, 999.9);
        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.ANCHOR, point, le);
        Assert.assertNotNull("exists", pp);
        pp.scaleCoords(2.F, 2.F);
        Point2D pointX2 = MathUtil.granulize(MathUtil.multiply(point, 2.0), 1.0);
        Assert.assertEquals("getCoordsCenter equal to {2000.0, 3000.0}", pointX2, pp.getCoordsCenter());
    }

    @Test
    public void testTranslateCoords() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        Point2D point = new Point2D.Double(666.6, 999.9);
        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.ANCHOR, point, le);
        Assert.assertNotNull("exists", pp);
        Point2D delta = new Point2D.Float(333.3F, 444.4F);
        pp.translateCoords((float) delta.getX(), (float) delta.getY());
        Point2D pointX2 = MathUtil.add(point, delta);
        Assert.assertEquals("getCoordsCenter equal to {999.9, 1444.3}", pointX2, pp.getCoordsCenter());
    }

    @Test
    public void testGetBounds() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        Point2D point = new Point2D.Double(666.6, 999.9);
        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.ANCHOR, point, le);
        Assert.assertNotNull("exists", pp);
        Rectangle2D bounds = new Rectangle2D.Double(point.getX() - 0.5, point.getY() - 0.5, 1.0, 1.0);
        Assert.assertEquals("getBounds equal to {666.6, 999.9, 0.0, 0.0}", bounds, pp.getBounds());
    }

    @Test
    public void testDefaultLinkedInfo() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.EDGE_CONNECTOR, MathUtil.zeroPoint2D, le);
        Assert.assertNotNull("exists", pp);

        Assert.assertNull("Default linked Editor is null", pp.getLinkedEditor());
        Assert.assertTrue("Default linked Editor Name is empty", pp.getLinkedEditorName().isEmpty());
        Assert.assertNull("Default linked point is null", pp.getLinkedPoint());
        Assert.assertTrue("Default linked point ID is empty", pp.getLinkedPointId().isEmpty());
    }

    @Test
    public void testSetLinkedPoint() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.EDGE_CONNECTOR, MathUtil.zeroPoint2D, le);
        Assert.assertNotNull("exists", pp);

        pp.setLinkedPoint(pp);

        Assert.assertNotNull("Linked Editor is not null", pp.getLinkedEditor());
        Assert.assertEquals("Linked Editor is le", le, pp.getLinkedEditor());
        Assert.assertFalse("Linked Editor Name is not empty", pp.getLinkedEditorName().isEmpty());
        Assert.assertEquals("Linked Editor Name is 'My Layout'", "My Layout", pp.getLinkedEditorName());
        Assert.assertNotNull("Linked point is not null", pp.getLinkedPoint());
        Assert.assertEquals("Linked point is pp", pp, pp.getLinkedPoint());
        Assert.assertFalse("Linked point ID is not empty", pp.getLinkedPointId().isEmpty());
        Assert.assertEquals("Linked point ID is 'test'", "test", pp.getLinkedPointId());
    }

    @Test
    public void testSetLinkedPoint2() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.EDGE_CONNECTOR, MathUtil.zeroPoint2D, le);
        Assert.assertNotNull("exists", pp);

        pp.setLinkedPoint(pp);

        PositionablePoint pp2 = new PositionablePoint("test2", PositionablePoint.EDGE_CONNECTOR, MathUtil.zeroPoint2D, le);
        Assert.assertNotNull("exists", pp2);
        pp.setLinkedPoint(pp2);

        Assert.assertNotNull("Linked Editor is not null", pp.getLinkedEditor());
        Assert.assertEquals("Linked Editor is le", le, pp.getLinkedEditor());
        Assert.assertFalse("Linked Editor Name is not empty", pp.getLinkedEditorName().isEmpty());
        Assert.assertEquals("Linked Editor Name is 'My Layout'", "My Layout", pp.getLinkedEditorName());
        Assert.assertNotNull("Linked point is not null", pp.getLinkedPoint());
        Assert.assertEquals("Linked point is pp2", pp2, pp.getLinkedPoint());
        Assert.assertFalse("Linked point ID is not empty", pp.getLinkedPointId().isEmpty());
        Assert.assertEquals("Linked point ID is 'test2'", "test2", pp.getLinkedPointId());
    }

    @Test
    public void testRemoveLinkedPoint() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.EDGE_CONNECTOR, MathUtil.zeroPoint2D, le);
        Assert.assertNotNull("exists", pp);

        pp.setLinkedPoint(pp);

        PositionablePoint pp2 = new PositionablePoint("test2", PositionablePoint.EDGE_CONNECTOR, MathUtil.zeroPoint2D, le);
        Assert.assertNotNull("exists", pp2);
        pp.setLinkedPoint(pp2);

        pp.removeLinkedPoint();

        Assert.assertNull("Removed linked Editor is null", pp.getLinkedEditor());
        Assert.assertTrue("Removed linked Editor Name is empty", pp.getLinkedEditorName().isEmpty());
        Assert.assertNull("Removed linked point is null", pp.getLinkedPoint());
        Assert.assertTrue("Removed linked point ID is empty", pp.getLinkedPointId().isEmpty());
    }

    @Test
    public void testMaxWidthAndHeight() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.EDGE_CONNECTOR, MathUtil.zeroPoint2D, le);
        Assert.assertNotNull("exists", pp);

        Assert.assertTrue("maxWidth == 5", pp.maxWidth() == 5);
        Assert.assertTrue("maxHeight == 5", pp.maxHeight() == 5);
    }

    @Test
    public void testFindHitPointType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        Point2D thePoint = new Point2D.Double(666.6, 999.9);
        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.ANCHOR, thePoint, le);
        Assert.assertNotNull("exists", pp);

        // first, try hit
        int hitType = pp.findHitPointType(thePoint, true, false);
        Assert.assertTrue("pp.findHitPointType equals POS_POINT", hitType == LayoutTrack.POS_POINT);

        // Now, try miss
        hitType = pp.findHitPointType(MathUtil.zeroPoint2D, true, false);
        Assert.assertTrue("pp.findHitPointType equals NONE", hitType == LayoutTrack.NONE);
    }

    @Test
    public void testGetCoordsForConnectionType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        Point2D thePoint = new Point2D.Double(666.6, 999.9);
        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.ANCHOR, thePoint, le);
        Assert.assertNotNull("exists", pp);

        // test failure
        Assert.assertEquals("pp.getCoordsForConnectionType(LayoutTrack.NONE) == {666.6, 999.9}",
                thePoint, pp.getCoordsForConnectionType(LayoutTrack.NONE));
        jmri.util.JUnitAppender.assertErrorMessage("Invalid connection type 0");

        // test success
        Assert.assertEquals("pp.getCoordsForConnectionType(LayoutTrack.POS_POINT) == {666.6, 999.9}",
                thePoint, pp.getCoordsForConnectionType(LayoutTrack.POS_POINT));
    }

    @Test
    public void testGetConnection() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.ANCHOR, MathUtil.zeroPoint2D, le);
        Assert.assertNotNull("exists", pp);

        try {
            // test invalid connection type
            Assert.assertNull("pp.getConnection(invalid type) is null",
                    pp.getConnection(LayoutTrack.NONE));
            Assert.fail("No exception thrown on pp.getConnection(invalid type)");
        } catch (JmriException ex) {
        }
        jmri.util.JUnitAppender.assertErrorMessage("Invalid connection type 0");

        try {
            // test valid connection type (null value)
            Assert.assertNull("pp.getConnection(valid type) is null",
                    pp.getConnection(LayoutTrack.POS_POINT));
        } catch (JmriException ex) {
            Assert.fail("Exception thrown on pp.getConnection(valid type)");
        }
    }

    @Test
    public void testSetConnection() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.ANCHOR, MathUtil.zeroPoint2D, le);
        Assert.assertNotNull("exists", pp);

        try {
            // test invalid connection type
            pp.setConnection(LayoutTrack.NONE, null, LayoutTrack.NONE);
            Assert.fail("No exception thrown on pp.setConnection(invalid connection type)");
        } catch (JmriException ex) {
        }
        jmri.util.JUnitAppender.assertErrorMessage("Invalid Connection Type 0");

        try {
            // test invalid object type
            pp.setConnection(LayoutTrack.POS_POINT, null, LayoutTrack.POS_POINT);
            Assert.fail("No exception thrown on pp.setConnection(invalid object type)");
        } catch (JmriException ex) {
        }
        jmri.util.JUnitAppender.assertErrorMessage("unexpected type of connection to positionable point - 1");

        try {
            // test valid types
            pp.setConnection(LayoutTrack.POS_POINT, null, LayoutTrack.NONE);
        } catch (JmriException ex) {
            Assert.fail("Exception thrown on pp.setConnection(valid types)");
        }
    }

    @Test
    public void testIsDisconnected() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.ANCHOR, MathUtil.zeroPoint2D, le);
        Assert.assertNotNull("exists", pp);

        // test invalid connection type
        Assert.assertFalse("pp.isDisconnected(invalid type) is null",
                pp.isDisconnected(LayoutTrack.NONE));
        jmri.util.JUnitAppender.assertErrorMessage("Invalid connection type 0");

        // test valid connection type
        Assert.assertTrue("pp.isDisconnected(valid type) is null",
                pp.isDisconnected(LayoutTrack.POS_POINT));
    }

    @Test
    public void testSetTrackConnection() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.ANCHOR, MathUtil.zeroPoint2D, le);
        Assert.assertNotNull("exists", pp);

        // test null track segment
        Assert.assertFalse("pp.setTrackConnection(null) is false",
                pp.setTrackConnection(null));
        jmri.util.JUnitAppender.assertErrorMessage("Attempt to remove non-existant track connection: null");

        PositionablePoint ppA = new PositionablePoint("A", PositionablePoint.ANCHOR, new Point2D.Double(0.0, 0.0), le);
        PositionablePoint ppB = new PositionablePoint("B", PositionablePoint.ANCHOR, new Point2D.Double(10.0, 10.0), le);
        PositionablePoint ppC = new PositionablePoint("C", PositionablePoint.ANCHOR, new Point2D.Double(20.0, 20.0), le);
        PositionablePoint ppD = new PositionablePoint("D", PositionablePoint.ANCHOR, new Point2D.Double(30.0, 30.0), le);
        TrackSegment tsAB = new TrackSegment("testAB", ppA, LayoutTrack.POS_POINT, ppB, LayoutTrack.POS_POINT, false, false, le);
        Assert.assertNotNull("Track Segment AB exists", tsAB);
        TrackSegment tsBC = new TrackSegment("testBC", ppB, LayoutTrack.POS_POINT, ppC, LayoutTrack.POS_POINT, false, false, le);
        Assert.assertNotNull("Track Segment BC exists", tsBC);
        TrackSegment tsCD = new TrackSegment("testCD", ppC, LayoutTrack.POS_POINT, ppD, LayoutTrack.POS_POINT, false, false, le);
        Assert.assertNotNull("Track Segment CD exists", tsCD);

        // test non-null track segment
        Assert.assertTrue("pp.setTrackConnection(tsAB) is true",
                pp.setTrackConnection(tsAB));

        // test already connected
        Assert.assertFalse("pp.setTrackConnection(tsAB) is false",
                pp.setTrackConnection(tsAB));
        jmri.util.JUnitAppender.assertErrorMessage("Already connected to testAB");

        // test 2nd non-null track segment
        Assert.assertTrue("pp.setTrackConnection(tsBC) is true",
                pp.setTrackConnection(tsBC));

        // test already connected
        Assert.assertFalse("pp.setTrackConnection(tsBC) is false",
                pp.setTrackConnection(tsBC));
        jmri.util.JUnitAppender.assertErrorMessage("Already connected to testBC");

        // test 3rd non-null track segment
        Assert.assertFalse("pp.setTrackConnection(tsCD) is false",
                pp.setTrackConnection(tsCD));
        jmri.util.JUnitAppender.assertErrorMessage("Attempt to assign more than allowed number of connections");
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            le = new LayoutEditor();
        }
    }

    @After
    public void tearDown() {
        if (le != null) {
            JUnitUtil.dispose(le);
        }
        le = null;
        JUnitUtil.tearDown();
    }
}
