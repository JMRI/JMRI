package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


import java.util.Objects;

import jmri.BeanSetting;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Path;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link PathEntry}: plain Block paths that name their neighbouring
 * Block, with no panel. These check the simulator's route finding only.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class PathEntryTest {

    private TrackModels models;
    private Turnout to1;

    /**
     * W - X - Y, with a turnout in X: closed to Y, thrown to Z.
     */
    /** Copy the layout's configuration, as a placement does. */
    private void copy() {
        models.use(TrackSnapshot.take(java.util.List.of())).dispose();
    }

    private Block[] plainBlocks() {
        BlockManager bm = InstanceManager.getDefault(BlockManager.class);
        Block w = bm.provideBlock("IB1");
        Block x = bm.provideBlock("IB2");
        Block y = bm.provideBlock("IB3");
        Block z = bm.provideBlock("IB4");
        w.setLength(400);
        x.setLength(450);
        y.setLength(300);
        z.setLength(250);
        w.addPath(new Path(x, 0, 0));
        x.addPath(new Path(w, 0, 0));
        Path xy = new Path(y, 0, 0, new BeanSetting(to1, "IT1", Turnout.CLOSED));
        x.addPath(xy);
        Path xz = new Path(z, 0, 0, new BeanSetting(to1, "IT1", Turnout.THROWN));
        xz.setLength(520);
        x.addPath(xz);
        y.addPath(new Path(x, 0, 0, new BeanSetting(to1, "IT1", Turnout.CLOSED)));
        z.addPath(new Path(x, 0, 0, new BeanSetting(to1, "IT1", Turnout.THROWN)));
        copy();
        return new Block[]{w, x, y, z};
    }

    @Test
    public void testPlainPathsFollowTurnout() {
        Block[] b = plainBlocks();
        TrackEntry into = models.entryInto(b[1], b[0]);
        to1.setCommandedState(Turnout.CLOSED);
        Passage p = into.follow();
        assertEquals(Passage.Stop.NONE, p.stop);
        assertEquals(b[2], Objects.requireNonNull(p.next, "closed: on to Y").getBlock(), "closed: on to Y");
        assertEquals(450.0, p.length, 1e-9, "the Block's own length");
        to1.setCommandedState(Turnout.THROWN);
        p = into.follow();
        assertEquals(b[3], Objects.requireNonNull(p.next, "thrown: on to Z").getBlock(), "thrown: on to Z");
        assertEquals(520.0, p.length, 1e-9, "the path's own length");
    }

    @Test
    public void testPlainPathsPointsAgainst() {
        Block[] b = plainBlocks();
        to1.setCommandedState(Turnout.THROWN);
        Passage p = models.entryInto(b[1], b[2]).follow();
        assertEquals(Passage.Stop.BLOCKED, p.stop, "from Y with the turnout thrown");
        assertNull(p.next);
        // the way to Y now also needs IT2, whose state is not known
        Turnout t2 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT2");
        assertEquals(Turnout.UNKNOWN, t2.getKnownState());
        for (Path path : b[1].getPaths()) {
            if (path.getBlock() == b[2]) {
                path.addSetting(new BeanSetting(t2, "IT2", Turnout.CLOSED));
            }
        }
        copy();
        to1.setCommandedState(Turnout.CLOSED);
        p = models.entryInto(b[1], b[0]).follow();
        assertEquals(Passage.Stop.BLOCKED, p.stop, "from W, neither way out is set");
    }

    @Test
    public void testPlainPathsEndOfTrackAndReverse() {
        Block[] b = plainBlocks();
        to1.setCommandedState(Turnout.CLOSED);
        Passage p = models.entryInto(b[0], b[1]).follow();
        assertEquals(Passage.Stop.END_OF_TRACK, p.stop, "W has no other neighbour");
        Passage back = Objects.requireNonNull(p.back, "a way back").follow();
        assertEquals(b[1], Objects.requireNonNull(back.next, "back the way it came").getBlock(),
                "back the way it came");
        TrackEntry into = models.entryInto(b[1], b[0]);
        TrackEntry rev = Objects.requireNonNull(into.reverse(), "a way back from X");
        assertEquals(b[0], rev.getBlock());
        assertEquals(into, rev.reverse(), "reversing twice is where it started");
        assertNull(models.entryInto(b[0], null).reverse(), "from the end of the track");
        Passage through = into.follow();
        assertTrue(through.route.reverseOf(Objects.requireNonNull(through.back, "a way back through X").follow().route),
                "back is the same way reversed");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        models = new TrackModels();
        to1 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
    }

    @AfterEach
    public void tearDown() {
        models.use(TrackSnapshot.empty()).dispose();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
