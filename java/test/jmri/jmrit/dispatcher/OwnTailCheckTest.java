package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import jmri.Block;
import jmri.JmriException;
import jmri.Path;
import jmri.Section;
import jmri.TrainRecord;
import jmri.implementation.DefaultSection;
import jmri.managers.DefaultTrainRecordManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link OwnTailCheck}.
 * <p>
 * The train's head is in Block H. Block T, behind it, is in Section N, the
 * Section the train is waiting for. Block G is beyond T.
 */
public class OwnTailCheckTest {

    private Block h;
    private Block t;
    private Block g;
    private Section n;

    private static void link(Block a, Block b) {
        a.addPath(new Path(b, Path.NONE, Path.NONE));
        b.addPath(new Path(a, Path.NONE, Path.NONE));
    }

    @Test
    public void testATailJoiningTheTrainIsProbablyItsOwn() throws JmriException {
        h.setState(Block.OCCUPIED);
        t.setState(Block.OCCUPIED);
        String how = OwnTailCheck.ownTail("Train", null, List.of(h), n, null);
        assertNotNull(how);
        assertTrue(how.contains("Block T joins") && how.contains("probably"), how);
    }

    @Test
    public void testAnOccupiedBlockBeyondAGapIsNotTheTrains() throws JmriException {
        h.setState(Block.OCCUPIED);
        g.setState(Block.OCCUPIED);  // T between them is clear
        n.addBlock(g);
        assertNull(OwnTailCheck.ownTail("Train", null, List.of(h), n, null));
    }

    @Test
    public void testAFreeSectionIsNotReported() {
        h.setState(Block.OCCUPIED);
        assertNull(OwnTailCheck.ownTail("Train", null, List.of(h), n, null));
    }

    /**
     * With train tracking, the train's own record shows it in the Section:
     * certain, even with no Block of its own allocation occupied next to it.
     */
    @Test
    public void testTrainTrackingShowsTheTail() throws JmriException {
        t.setState(Block.OCCUPIED);
        DefaultTrainRecordManager records = new DefaultTrainRecordManager();
        jmri.util.ThreadingUtil.runOnLayout(() -> {
            TrainRecord r = records.newRecord("Train");
            r.place(List.of(h, t), true);
        });
        String how = OwnTailCheck.ownTail("Train", null, List.of(h), n, records);
        assertNotNull(how);
        assertTrue(how.contains("train tracking has Train in Block T"), how);
    }

    /**
     * Train tracking has another train in the Section: not this train's tail,
     * and with a gap to this train, not reported.
     */
    @Test
    public void testTrainTrackingShowsAnotherTrain() throws JmriException {
        h.setState(Block.OCCUPIED);
        g.setState(Block.OCCUPIED);
        n.addBlock(g);
        DefaultTrainRecordManager records = new DefaultTrainRecordManager();
        jmri.util.ThreadingUtil.runOnLayout(() -> {
            records.newRecord("Train").place(List.of(h), true);
            records.newRecord("Other").place(List.of(g), true);
        });
        assertNull(OwnTailCheck.ownTail("Train", null, List.of(h), n, records));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        h = new Block("IB_H", "H");
        t = new Block("IB_T", "T");
        g = new Block("IB_G", "G");
        link(h, t);
        link(t, g);
        n = new DefaultSection("TS1", "N");
        n.addBlock(t);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
