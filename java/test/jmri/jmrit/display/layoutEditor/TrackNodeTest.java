package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;

import jmri.util.JUnitUtil;
import jmri.util.MathUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of TrackNode
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class TrackNodeTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor le = new LayoutEditor();
        LayoutTurnout lt = new LayoutRHTurnout("T", MathUtil.zeroPoint2D, 0., 1., 1., le);
        PositionablePoint p1 = new PositionablePoint("a", PositionablePoint.PointType.ANCHOR, new Point2D.Double(0.0, 0.0), le);
        PositionablePoint p2 = new PositionablePoint("b", PositionablePoint.PointType.ANCHOR, new Point2D.Double(1.0, 1.0), le);
        TrackSegment ts = new TrackSegment("test", p1, HitPointType.POS_POINT, p2, HitPointType.POS_POINT, false, true, le);
        TrackNode tn = new TrackNode(lt, HitPointType.TURNOUT_A, ts, false, 0);
        Assert.assertNotNull("exists", tn);
        JUnitUtil.dispose(le);
    }

    // from here down is testing infrastructure
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
