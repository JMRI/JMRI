package jmri.jmrit.display.layoutEditor;

import org.junit.*;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;

import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LayoutTrackExpectedStateTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor le = new LayoutEditor();
        PositionablePoint p1 = new PositionablePoint("a", PositionablePoint.PointType.ANCHOR, new Point2D.Double(0.0, 0.0), le);
        PositionablePoint p2 = new PositionablePoint("b", PositionablePoint.PointType.ANCHOR, new Point2D.Double(1.0, 1.0), le);
        TrackSegment s = new TrackSegment("test", p1, HitPointType.POS_POINT, p2, HitPointType.POS_POINT, false, true, le);
        LayoutTrackExpectedState<LayoutTrack> t = new LayoutTrackExpectedState<LayoutTrack>(s, 0);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(le);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
