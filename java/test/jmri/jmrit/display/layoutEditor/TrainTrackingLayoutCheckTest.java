package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import jmri.Block;
import jmri.Turnout;
import jmri.jmrit.trainrecord.TrainTrackingChecks.Finding;
import jmri.jmrit.trainrecord.TrainTrackingChecks.Kind;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link TrainTrackingLayoutCheck}, on turnouts and track built
 * from mocks.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class TrainTrackingLayoutCheckTest {

    private static LayoutBlock block(String name) {
        LayoutBlock lb = mock(LayoutBlock.class);
        when(lb.getBlock()).thenReturn(new Block("IB" + name, name));
        return lb;
    }

    /** A track segment in a Block, joined at each end to a track or null. */
    private static TrackSegment segment(LayoutBlock lb) {
        TrackSegment ts = mock(TrackSegment.class);
        when(ts.getLayoutBlock()).thenReturn(lb);
        return ts;
    }

    private static void join(TrackSegment ts, LayoutTrack one, LayoutTrack two) {
        when(ts.getConnect1()).thenReturn(one);
        when(ts.getConnect2()).thenReturn(two);
    }

    private static LayoutTurnout turnout(String name, LayoutTurnout.TurnoutType type, LayoutBlock lb) {
        LayoutTurnout lt = mock(LayoutTurnout.class);
        Turnout t = mock(Turnout.class);
        when(t.getDisplayName()).thenReturn(name);
        when(lt.getTurnout()).thenReturn(t);
        when(lt.getTurnoutType()).thenReturn(type);
        when(lt.getLayoutBlock()).thenReturn(lb);
        when(lt.getLayoutBlockB()).thenReturn(lb);
        when(lt.getLayoutBlockC()).thenReturn(lb);
        when(lt.getLayoutBlockD()).thenReturn(lb);
        return lt;
    }

    @Test
    public void testRoutesIntoDifferentBlocksAreFine() {
        // a turnout in a short Block of its own, each route going on into a Block of its own
        LayoutBlock own = block("Points");
        LayoutTurnout lt = turnout("T1", LayoutTurnout.TurnoutType.RH_TURNOUT, own);
        TrackSegment legB = segment(own);
        TrackSegment main = segment(block("Main"));
        TrackSegment legC = segment(own);
        TrackSegment loop = segment(block("Loop"));
        join(legB, lt, main);
        join(main, legB, null);
        join(legC, lt, loop);
        join(loop, legC, null);
        when(lt.getConnectB()).thenReturn(legB);
        when(lt.getConnectC()).thenReturn(legC);
        assertTrue(TrainTrackingLayoutCheck.check(List.of(lt)).isEmpty(), "nothing to report");
    }

    @Test
    public void testBothRoutesIntoOneBlock() {
        // a balloon loop drawn as one Block: both routes from the turnout lead into it
        LayoutBlock own = block("Points");
        LayoutBlock balloon = block("Balloon");
        LayoutTurnout lt = turnout("T1", LayoutTurnout.TurnoutType.LH_TURNOUT, own);
        TrackSegment legB = segment(balloon);
        TrackSegment legC = segment(balloon);
        join(legB, lt, legC);
        join(legC, legB, lt);
        when(lt.getConnectB()).thenReturn(legB);
        when(lt.getConnectC()).thenReturn(legC);
        List<Finding> found = TrainTrackingLayoutCheck.check(List.of(lt));
        assertEquals(1, found.size(), "one finding");
        assertEquals(Kind.ROUTES_INTO_ONE_BLOCK, found.get(0).getKind());
        assertEquals(balloon.getBlock(), found.get(0).getSubject(), "about the Block both lead into");
        assertEquals(List.of(lt.getTurnout()), found.get(0).getRelated(), "and the turnout");
        assertEquals("Both routes from turnout \"T1\" lead into Block \"Balloon\", so which of them a train took can't "
                + "be told from detection. Give each route a Block of its own: a balloon or run-round loop drawn as "
                + "one Block shows as this.", found.get(0).getMessage());
    }

    @Test
    public void testRoutesRejoinInsideTheTurnoutsBlock() {
        // a run-round loop drawn inside one Block: both routes from T1 meet again at T2
        LayoutBlock platform = block("Platform");
        LayoutTurnout t1 = turnout("T1", LayoutTurnout.TurnoutType.RH_TURNOUT, platform);
        LayoutTurnout t2 = turnout("T2", LayoutTurnout.TurnoutType.LH_TURNOUT, platform);
        TrackSegment road = segment(platform);
        TrackSegment loop = segment(platform);
        join(road, t1, t2);
        join(loop, t1, t2);
        when(t1.getConnectB()).thenReturn(road);
        when(t1.getConnectC()).thenReturn(loop);
        when(t2.getConnectB()).thenReturn(road);
        when(t2.getConnectC()).thenReturn(loop);
        List<Finding> found = TrainTrackingLayoutCheck.check(List.of(t1, t2));
        assertEquals(1, found.size(), "one finding for the loop, not one from each end");
        assertEquals(platform.getBlock(), found.get(0).getSubject());
        assertEquals(List.of(t1.getTurnout(), t2.getTurnout()), found.get(0).getRelated());
        assertTrue(found.get(0).getMessage().startsWith("Both routes from turnout \"T1\" meet again at turnout \"T2\""),
                found.get(0).getMessage());
    }

    @Test
    public void testCrossoverTracksAtOneEndIntoOneBlock() {
        // a crossover whose two tracks run on into the same Block at its B and C end
        LayoutBlock own = block("Crossover");
        LayoutTurnout xo = turnout("X1", LayoutTurnout.TurnoutType.RH_XOVER, own);
        LayoutBlock up = block("Up");
        LayoutBlock down = block("Down");
        LayoutBlock beyond = block("Beyond");
        TrackSegment a = segment(up);
        TrackSegment d = segment(down);
        TrackSegment b = segment(beyond);
        TrackSegment c = segment(beyond);
        join(a, xo, null);
        join(d, xo, null);
        join(b, xo, null);
        join(c, xo, null);
        when(xo.getConnectA()).thenReturn(a);
        when(xo.getConnectB()).thenReturn(b);
        when(xo.getConnectC()).thenReturn(c);
        when(xo.getConnectD()).thenReturn(d);
        List<Finding> found = TrainTrackingLayoutCheck.check(List.of(xo));
        assertEquals(1, found.size(), "only the B and C end");
        assertEquals(beyond.getBlock(), found.get(0).getSubject());
    }

    @Test
    public void testFollowedThroughAnchorsAndAReachedTurnoutsBlock() {
        LayoutBlock own = block("Points");
        LayoutBlock beyond = block("Beyond");
        LayoutTurnout lt = turnout("T1", LayoutTurnout.TurnoutType.WYE_TURNOUT, own);
        LayoutTurnout other = turnout("T9", LayoutTurnout.TurnoutType.RH_TURNOUT, beyond);
        TrackSegment legB = segment(own);
        PositionablePoint anchor = mock(PositionablePoint.class);
        TrackSegment onB = segment(own);
        TrackSegment legC = segment(own);
        join(legB, lt, anchor);
        when(anchor.getConnect1()).thenReturn(legB);
        when(anchor.getConnect2()).thenReturn(onB);
        join(onB, anchor, other);
        join(legC, lt, other);
        when(lt.getConnectB()).thenReturn(legB);
        when(lt.getConnectC()).thenReturn(legC);
        List<Finding> found = TrainTrackingLayoutCheck.check(List.of(lt));
        assertEquals(1, found.size(), "both reach T9, in Block Beyond");
        assertEquals(beyond.getBlock(), found.get(0).getSubject());
    }

    @Test
    public void testRouteEndingInsideTheBlockIsNotReported() {
        // a siding that ends in the turnout's own Block: nothing to compare
        LayoutBlock own = block("Yard");
        LayoutTurnout lt = turnout("T1", LayoutTurnout.TurnoutType.RH_TURNOUT, own);
        TrackSegment legB = segment(own);
        TrackSegment legC = segment(own);
        join(legB, lt, null);
        join(legC, lt, null);
        when(lt.getConnectB()).thenReturn(legB);
        when(lt.getConnectC()).thenReturn(legC);
        assertTrue(TrainTrackingLayoutCheck.check(List.of(lt)).isEmpty());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
