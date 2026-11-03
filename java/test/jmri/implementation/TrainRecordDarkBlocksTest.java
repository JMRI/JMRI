package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Objects;

import jmri.BeanSetting;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Path;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.TrainRecord;
import jmri.TrainRecord.Confidence;
import jmri.TrainRecord.Reason;
import jmri.TrainRecord.Reason.Rule;
import jmri.TrainRecord.TrailEntry;
import jmri.TrainRecordManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A train record follows its train through Blocks with no detector: it is
 * seen to reach the next detected Block, and it leaves the dark Blocks when
 * it is seen to leave a detected Block in front of them.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class TrainRecordDarkBlocksTest {

    private TrainRecordManager manager;
    private long nanos = 1_000_000_000L;

    private Block detected(String name) {
        Block b = dark(name);
        Sensor s = InstanceManager.getDefault(SensorManager.class).provideSensor("IS" + name);
        set(s, Sensor.INACTIVE);
        b.setSensor(s.getSystemName());
        return b;
    }

    private static Block dark(String name) {
        Block b = InstanceManager.getDefault(BlockManager.class).provideBlock("IB" + name);
        b.setUserName(name);
        return b;
    }

    private static void join(Block a, Block b) {
        a.addPath(new Path(b, 0, 0));
        b.addPath(new Path(a, 0, 0));
    }

    private static void chain(Block... blocks) {
        for (int i = 1; i < blocks.length; i++) {
            join(blocks[i - 1], blocks[i]);
        }
    }

    private long set(Sensor s, int state) {
        nanos += 1_000_000_000L;
        long t = nanos;
        ThreadingUtil.runOnLayout(() -> ((AbstractSensor) s).setOwnState(state, t));
        return t;
    }

    private long occupy(Block b) {
        return set(Objects.requireNonNull(b.getSensor()), Sensor.ACTIVE);
    }

    private long clear(Block b) {
        return set(Objects.requireNonNull(b.getSensor()), Sensor.INACTIVE);
    }

    private TrainRecord placed(Block... blocks) {
        for (Block b : blocks) {
            occupy(b);
        }
        return ThreadingUtil.runOnGUIwithReturn(() -> {
            TrainRecord r = manager.newRecord("T1");
            r.place(List.of(blocks), true);
            return r;
        });
    }

    private static TrailEntry last(TrainRecord r) {
        List<TrailEntry> trail = r.getTrail();
        return trail.get(trail.size() - 1);
    }

    @Test
    public void testThroughOneDarkBlock() {
        Block a = detected("A");
        Block d = dark("D");
        Block b = detected("B");
        Block c = detected("C");
        chain(a, d, b, c);
        TrainRecord r = placed(a);

        long t = occupy(b);
        assertEquals(List.of(b, d, a), r.getBlocks(), "the dark Block it passed through, and the one it came from");
        assertEquals(Confidence.CONFIRMED, r.getConfidence());
        assertSame(b, last(r).getBlock());
        assertSame(d, last(r).getFrom(), "entered from the dark Block");
        assertEquals(t, last(r).getTimeNanos());

        clear(a);
        assertEquals(List.of(b, d), r.getBlocks(), "a dark Block can't show it is empty: kept");

        occupy(c);
        clear(b);
        assertEquals(List.of(c), r.getBlocks(), "the rear has left B, so it has left D too");
        assertEquals(Confidence.CONFIRMED, r.getConfidence());
    }

    @Test
    public void testThroughSeveralDarkBlocks() {
        Block a = detected("A");
        Block d1 = dark("D1");
        Block d2 = dark("D2");
        Block d3 = dark("D3");
        Block b = detected("B");
        chain(a, d1, d2, d3, b);
        TrainRecord r = placed(a);
        occupy(b);
        assertEquals(List.of(b, d3, d2, d1, a), r.getBlocks());
    }

    @Test
    public void testShortTrainAlreadyOutOfItsBlock() {
        Block a = detected("A");
        Block d = dark("D");
        Block b = detected("B");
        chain(a, d, b);
        TrainRecord r = placed(a);
        clear(a); // gone into D, unseen
        assertEquals(Confidence.ESTIMATED, r.getConfidence());
        occupy(b);
        assertEquals(List.of(b, d), r.getBlocks(), "A was left; D is kept until something shows the rear is past it");
        assertEquals(Confidence.CONFIRMED, r.getConfidence());
    }

    @Test
    public void testOnlyDarkBlocksLeftIsEstimated() {
        Block a = detected("A");
        Block d = dark("D");
        Block b = detected("B");
        Block e = dark("E");
        chain(a, d, b, e);
        TrainRecord r = placed(a);
        occupy(b);
        clear(a);
        assertEquals(List.of(b, d), r.getBlocks());
        clear(b); // the front end has left B: into E, unseen
        assertEquals(List.of(d), r.getBlocks());
        assertEquals(Confidence.ESTIMATED, r.getConfidence(), "seen nowhere now");
    }

    @Test
    public void testTwoWaysThroughDarkBlocksNotFollowed() {
        Block a = detected("A");
        Block d1 = dark("D1");
        Block d2 = dark("D2");
        Block b = detected("B");
        chain(a, d1, b);
        chain(a, d2, b);
        TrainRecord r = placed(a);
        occupy(b);
        assertEquals(List.of(a), r.getBlocks(), "which dark Block it went through isn't known");
        assertNull(TrainRecordWatcher.darkRoute(a, b, List.of(a)));
    }

    @Test
    public void testShortestWayTaken() {
        Block a = detected("A");
        Block d1 = dark("D1");
        Block d2 = dark("D2");
        Block d3 = dark("D3");
        Block b = detected("B");
        chain(a, d1, b);
        chain(a, d2, d3, b);
        assertEquals(List.of(d1), TrainRecordWatcher.darkRoute(a, b, List.of(a)));
    }

    @Test
    public void testDarkBlockSetAgainstNotFollowed() {
        Block a = detected("A");
        Block d = dark("D");
        Block b = detected("B");
        Turnout t = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        t.setCommandedState(Turnout.CLOSED);
        Path ad = new Path(d, 0, 0);
        a.addPath(ad);
        Path da = new Path(a, 0, 0);
        da.addSetting(new BeanSetting(t, Turnout.THROWN));
        d.addPath(da);
        Path db = new Path(b, 0, 0);
        db.addSetting(new BeanSetting(t, Turnout.THROWN));
        d.addPath(db);
        Path bd = new Path(d, 0, 0);
        bd.addSetting(new BeanSetting(t, Turnout.THROWN));
        b.addPath(bd);
        TrainRecord r = placed(a);
        occupy(b);
        assertEquals(List.of(a), r.getBlocks(), "the turnout in D is set against B");
        clear(b);
        ThreadingUtil.runOnLayout(() -> t.setCommandedState(Turnout.THROWN));
        occupy(b);
        assertEquals(List.of(b, d, a), r.getBlocks());
    }

    @Test
    public void testDirectNeighbourPreferred() {
        Block a = detected("A");
        Block d = dark("D");
        Block b = detected("B");
        chain(a, b);
        chain(a, d, b);
        TrainRecord r = placed(a);
        occupy(b);
        assertEquals(List.of(b, a), r.getBlocks(), "joined directly: no dark Block passed through");
    }

    @Test
    public void testEntersThroughDarkBlocksAtItsLastBlock() {
        Block a = detected("A");
        Block b = detected("B");
        Block d = dark("D");
        Block c = detected("C");
        chain(a, b, d, c);
        TrainRecord r = placed(a, b);
        occupy(c);
        assertEquals(List.of(a, b, d, c), r.getBlocks(), "front still A; the rear end went on through D into C");
        assertSame(TrainRecord.End.REAR, last(r).getEnd(), "entered at the rear end");
        assertSame(c, last(r).getBlock());
        assertSame(d, last(r).getFrom(), "entered from the dark Block beyond B");
    }

    @Test
    public void testTwoWaysMeetingInADarkBlockNotFollowed() {
        Block a = detected("A");
        Block d1 = dark("D1");
        Block d2 = dark("D2");
        Block d3 = dark("D3");
        Block b = detected("B");
        chain(a, d1, d3, b);
        chain(a, d2, d3);
        TrainRecord r = placed(a);
        occupy(b);
        assertEquals(List.of(a), r.getBlocks(), "D1 or D2 isn't known, though both lead through D3");
        assertNull(TrainRecordWatcher.darkRoute(a, b, List.of(a)));
    }

    @Test
    public void testRecordNotPlacedIsLeftAlone() {
        Block a = detected("A");
        Block d = dark("D");
        Block b = detected("B");
        chain(a, d, b);
        TrainRecord unplaced = ThreadingUtil.runOnGUIwithReturn(() -> manager.newRecord("T2"));
        TrainRecord r = placed(a);
        occupy(b);
        assertEquals(List.of(b, d, a), r.getBlocks());
        assertTrue(unplaced.getBlocks().isEmpty(), "a record not placed is in no Block");
    }

    @Test
    public void testMiddleBlockDropOutKeptWithADetectedBlockBehind() {
        Block a = detected("A");
        Block b = detected("B");
        Block c = detected("C");
        chain(a, b, c);
        TrainRecord r = placed(a, b);
        occupy(c); // it moves: C entered beyond B
        List<Block> before = r.getBlocks();
        clear(b); // B, between A and C, drops out
        assertEquals(before, r.getBlocks(), "nothing left: A behind B is detected and still occupied");
        assertEquals(Confidence.ESTIMATED, r.getConfidence());
        assertEquals(Rule.KEPT_AFTER_DROPOUT, Objects.requireNonNull(r.getConfidenceReason()).getRule());
    }

    @Test
    public void testOnlyBlockEmptiesBesideADarkBlock() {
        Block a = detected("A");
        Block d = dark("D");
        chain(a, d);
        TrainRecord r = placed(a);

        clear(a);
        assertEquals(Confidence.ESTIMATED, r.getConfidence());
        Reason c = Objects.requireNonNull(r.getConfidenceReason());
        assertEquals(Rule.NOT_SEEN, c.getRule(), "it may have gone on into D, so not a drop-out: " + c);
        assertTrue(r.getHistory().stream().noneMatch(h -> h.getRule() == Rule.KEPT_AFTER_DROPOUT));
        assertEquals(List.of(a), r.getBlocks(), "still kept in A");
    }

    @Test
    public void testThroughDarkBlocksReason() {
        Block a = detected("A");
        Block d = dark("D");
        Block b = detected("B");
        chain(a, d, b);
        TrainRecord r = placed(a);

        occupy(b);
        Reason reason = Objects.requireNonNull(r.getReason(b));
        assertEquals(Rule.ENTERED_THROUGH_DARK, reason.getRule());
        assertEquals(List.of(d), reason.getThrough());
        assertSame(reason, r.getReason(d), "in the dark Block for the same reason");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        manager = InstanceManager.getDefault(TrainRecordManager.class);
    }

    @AfterEach
    public void tearDown() {
        manager = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
