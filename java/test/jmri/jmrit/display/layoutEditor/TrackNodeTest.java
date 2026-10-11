package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of TrackNode
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class TrackNodeTest {

    @Test
    @DisabledIfHeadless
    public void testCtor() {

        LayoutEditor le = new LayoutEditor();
        LayoutTurnout lt = new LayoutRHTurnout("T", le);
        PositionablePoint p1 = new PositionablePoint("a", PositionablePoint.PointType.END_BUMPER, le);
        PositionablePoint p2 = new PositionablePoint("b", PositionablePoint.PointType.END_BUMPER, le);

        TrackSegment ts = new TrackSegment("test", p1, HitPointType.POS_POINT, p2, HitPointType.POS_POINT, true, le);

        TrackNode tn = new TrackNode(lt, HitPointType.TURNOUT_A, ts, false, 0);
        Assertions.assertNotNull( tn, "exists");
        JUnitUtil.dispose(le);
    }

    // from here down is testing infrastructure
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
