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
 * @author	Paul Bender Copyright (C) 2016
 */
public class TrackNodeTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor le = new LayoutEditor();
        LayoutTurnout lt = new LayoutTurnout("T", MathUtil.zeroPoint2D, le);
        PositionablePoint p1 = new PositionablePoint("a", PositionablePoint.ANCHOR, new Point2D.Double(0.0, 0.0), le);
        PositionablePoint p2 = new PositionablePoint("b", PositionablePoint.ANCHOR, new Point2D.Double(1.0, 1.0), le);
        TrackSegment ts = new TrackSegment("test", p1, LayoutTrack.POS_POINT, p2, LayoutTrack.POS_POINT, false, true, le);
        TrackNode tn = new TrackNode(lt, LayoutTrack.TURNOUT_A, ts, false, 0);
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
        JUnitUtil.tearDown();
    }
}
