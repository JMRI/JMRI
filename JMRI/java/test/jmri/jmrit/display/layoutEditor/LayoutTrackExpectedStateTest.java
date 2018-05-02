package jmri.jmrit.display.layoutEditor;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LayoutTrackExpectedStateTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor le = new LayoutEditor();
        PositionablePoint p1 = new PositionablePoint("a", PositionablePoint.ANCHOR, new Point2D.Double(0.0, 0.0), le);
        PositionablePoint p2 = new PositionablePoint("b", PositionablePoint.ANCHOR, new Point2D.Double(1.0, 1.0), le);
        TrackSegment s = new TrackSegment("test", p1, LayoutTrack.POS_POINT, p2, LayoutTrack.POS_POINT, false, true, le);
        LayoutTrackExpectedState t = new LayoutTrackExpectedState(s,0);
        Assert.assertNotNull("exists",t);
        jmri.util.JUnitUtil.dispose(le);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
