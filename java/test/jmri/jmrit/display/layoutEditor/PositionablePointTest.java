package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.*;

import jmri.JmriException;
import jmri.util.*;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of PositionablePoint
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PositionablePointTest extends LayoutTrackTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.PointType.ANCHOR, le);
        Assert.assertNotNull("exists", pp);
    }

    @Test
    public void testCtorFail() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        // 2nd parameter is illegal type
        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.PointType.NONE, le);
        Assert.assertNotNull("exists", pp);
        JUnitAppender.assertErrorMessage("Illegal type of PositionablePoint - NONE");
    }

    @Test
    public void testToString() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        PositionablePoint ppA = new PositionablePoint("test", PositionablePoint.PointType.ANCHOR, le);
        Assert.assertNotNull("exists", ppA);
        Assert.assertEquals("Anchor 'test'", ppA.toString());

        PositionablePoint ppEB = new PositionablePoint("test", PositionablePoint.PointType.END_BUMPER, le);
        Assert.assertNotNull("exists", ppEB);
        Assert.assertEquals("End Bumper 'test'", ppEB.toString());

        PositionablePoint ppEC = new PositionablePoint("test", PositionablePoint.PointType.EDGE_CONNECTOR, le);
        Assert.assertNotNull("exists", ppEC);
        Assert.assertEquals("Edge Connector 'test'", ppEC.toString());
    }


    @Test
    public void testDefaultLinkedInfo() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.PointType.EDGE_CONNECTOR, le);
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

        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.PointType.EDGE_CONNECTOR, le);
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

        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.PointType.EDGE_CONNECTOR, le);
        Assert.assertNotNull("exists", pp);

        pp.setLinkedPoint(pp);

        PositionablePoint pp2 = new PositionablePoint("test2", PositionablePoint.PointType.EDGE_CONNECTOR, le);
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

        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.PointType.EDGE_CONNECTOR, le);
        Assert.assertNotNull("exists", pp);

        pp.setLinkedPoint(pp);

        PositionablePoint pp2 = new PositionablePoint("test2", PositionablePoint.PointType.EDGE_CONNECTOR, le);
        Assert.assertNotNull("exists", pp2);
        pp.setLinkedPoint(pp2);

        pp.removeLinkedPoint();

        Assert.assertNull("Removed linked Editor is null", pp.getLinkedEditor());
        Assert.assertTrue("Removed linked Editor Name is empty", pp.getLinkedEditorName().isEmpty());
        Assert.assertNull("Removed linked point is null", pp.getLinkedPoint());
        Assert.assertTrue("Removed linked point ID is empty", pp.getLinkedPointId().isEmpty());
    }

    @Test
    public void testGetConnection() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.PointType.ANCHOR, le);
        Assert.assertNotNull("exists", pp);

        try {
            // test Invalid Connection Type
            Assert.assertNull("pp.getConnection(invalid type) is null",
                    pp.getConnection(HitPointType.NONE));
            Assert.fail("No exception thrown on pp.getConnection(invalid type)");
        } catch (JmriException ex) {
        }
        JUnitAppender.assertErrorMessage("will throw test.getConnection(NONE); Invalid Connection Type");

        try {
            // test valid connection type (null value)
            Assert.assertNull("pp.getConnection(valid type) is null",
                    pp.getConnection(HitPointType.POS_POINT));
        } catch (JmriException ex) {
            Assert.fail("Exception thrown on pp.getConnection(valid type)");
        }
    }

    @Test
    public void testSetConnection() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.PointType.ANCHOR, le);
        Assert.assertNotNull("exists", pp);

        try {
            // test Invalid Connection Type
            pp.setConnection(HitPointType.NONE, null, HitPointType.NONE);
            Assert.fail("No exception thrown on pp.setConnection(Invalid Connection Type)");
        } catch (JmriException ex) {
        }
        JUnitAppender.assertErrorMessage("will throw test.setConnection(NONE, null, NONE); Invalid Connection Type");

        try {
            // test invalid object type
            pp.setConnection(HitPointType.POS_POINT, null, HitPointType.POS_POINT);
            Assert.fail("No exception thrown on pp.setConnection(invalid object type)");
        } catch (JmriException ex) {
        }
        JUnitAppender.assertErrorMessage("will throw test.setConnection(POS_POINT, null, POS_POINT); unexpected type");

        try {
            // test valid types
            pp.setConnection(HitPointType.POS_POINT, null, HitPointType.NONE);
        } catch (JmriException ex) {
            Assert.fail("Exception thrown on pp.setConnection(valid types)");
        }
    }

    @Test
    public void testIsDisconnected() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.PointType.ANCHOR, le);
        Assert.assertNotNull("exists", pp);

        // test Invalid Connection Type
        Assert.assertFalse("pp.isDisconnected(invalid type) is null",
                pp.isDisconnected(HitPointType.NONE));
        JUnitAppender.assertErrorMessage("test.isDisconnected(NONE); Invalid Connection Type");

        // test valid connection type
        Assert.assertTrue("pp.isDisconnected(valid type) is null",
                pp.isDisconnected(HitPointType.POS_POINT));
    }

    @Test
    public void testSetTrackConnection() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.PointType.ANCHOR, le);
        Assert.assertNotNull("exists", pp);

        // test null track segment
        Assert.assertFalse("pp.setTrackConnection(null) is false",
                pp.setTrackConnection(null));
        JUnitAppender.assertErrorMessage("test.replaceTrackConnection(null, null); Attempt to remove non-existant track connection");

        PositionablePoint ppA = new PositionablePoint("A", PositionablePoint.PointType.ANCHOR, le);
        PositionablePoint ppB = new PositionablePoint("B", PositionablePoint.PointType.ANCHOR, le);
        PositionablePoint ppC = new PositionablePoint("C", PositionablePoint.PointType.ANCHOR, le);
        PositionablePoint ppD = new PositionablePoint("D", PositionablePoint.PointType.ANCHOR, le);
        TrackSegment tsAB = new TrackSegment("testAB", ppA, HitPointType.POS_POINT, ppB, HitPointType.POS_POINT, false, le);
        Assert.assertNotNull("Track Segment AB exists", tsAB);
        TrackSegment tsBC = new TrackSegment("testBC", ppB, HitPointType.POS_POINT, ppC, HitPointType.POS_POINT, false, le);
        Assert.assertNotNull("Track Segment BC exists", tsBC);
        TrackSegment tsCD = new TrackSegment("testCD", ppC, HitPointType.POS_POINT, ppD, HitPointType.POS_POINT, false, le);
        Assert.assertNotNull("Track Segment CD exists", tsCD);

        // test non-null track segment
        Assert.assertTrue("pp.setTrackConnection(tsAB) is true",
                pp.setTrackConnection(tsAB));

        // test already connected
        Assert.assertFalse("pp.setTrackConnection(tsAB) is false",
                pp.setTrackConnection(tsAB));
        JUnitAppender.assertWarnMessage("test.replaceTrackConnection(null, testAB); Already connected");

        // test 2nd non-null track segment
        Assert.assertTrue("pp.setTrackConnection(tsBC) is true",
                pp.setTrackConnection(tsBC));

        // test already connected
        Assert.assertFalse("pp.setTrackConnection(tsBC) is false",
                pp.setTrackConnection(tsBC));
        JUnitAppender.assertWarnMessage("test.replaceTrackConnection(null, testBC); Already connected");

        // test 3rd non-null track segment
        Assert.assertFalse("pp.setTrackConnection(tsCD) is false",
                pp.setTrackConnection(tsCD));
        JUnitAppender.assertErrorMessage("test.replaceTrackConnection(null, testCD); Attempt to assign more than allowed number of connections");
    }

    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        super.tearDown();
    }
}
