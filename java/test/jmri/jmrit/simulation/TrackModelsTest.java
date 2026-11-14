package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


import jmri.BeanSetting;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Path;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link TrackModels}: lengths and finding where a Block is entered.
 * These check the simulator's own code only.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class TrackModelsTest {

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
    public void testNoLengthWarnsOnce() {
        Block[] b = plainBlocks();
        b[2].setLength(0);
        copy();
        to1.setCommandedState(Turnout.CLOSED);
        Passage p = models.entryInto(b[2], b[1]).follow();
        assertEquals(TrackModels.DEFAULT_LENGTH_MM, p.length, 1e-9);
        JUnitAppender.assertWarnMessageStartsWith("Block \"IB3\" has no length");
        models.entryInto(b[2], b[1]).follow();
        // no second warning: JUnitUtil.tearDown fails on any unexpected one
    }

    @Test
    public void testPlacementErrors() {
        Block[] b = plainBlocks();
        assertThrows(IllegalArgumentException.class, () -> models.entryInto(b[0], b[2]), "W and Y don't meet");
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
