package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Objects;

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
import jmri.TrainRecordManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Every change to the Blocks a train is in, and to how sure JMRI is of where
 * it is, carries its reason, and is kept in the train's history.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class TrainRecordReasonsTest {

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

    private static void chain(Block... blocks) {
        for (int i = 1; i < blocks.length; i++) {
            blocks[i - 1].addPath(new Path(blocks[i], 0, 0));
            blocks[i].addPath(new Path(blocks[i - 1], 0, 0));
        }
    }

    private void set(Sensor s, int state) {
        nanos += 1_000_000_000L;
        long t = nanos;
        ThreadingUtil.runOnLayout(() -> ((AbstractSensor) s).setOwnState(state, t));
    }

    private void occupy(Block b) {
        set(Objects.requireNonNull(b.getSensor()), Sensor.ACTIVE);
    }

    private void clear(Block b) {
        set(Objects.requireNonNull(b.getSensor()), Sensor.INACTIVE);
    }

    private TrainRecord placed(String name, Block... blocks) {
        for (Block b : blocks) {
            occupy(b);
        }
        return ThreadingUtil.runOnGUIwithReturn(() -> {
            TrainRecord r = manager.newRecord(name);
            r.place(List.of(blocks), true);
            return r;
        });
    }

    @Test
    public void testPlacedHasItsReason() {
        Block a = detected("A");
        TrainRecord r = placed("T1", a);
        Reason reason = r.getReason(a);
        assertEquals(Rule.PLACED, Objects.requireNonNull(reason).getRule());
        assertSame(a, reason.getBlock());
        assertSame(reason, r.getConfidenceReason(), "placed: confirmed, for the same reason");
        assertEquals(List.of(reason), r.getHistory());
    }

    @Test
    public void testPlacedByDispatcherOrValueKeepsTheRule() {
        Block a = detected("A");
        Block b = detected("B");
        TrainRecord r1 = manager.newRecord("T1");
        TrainRecord r2 = manager.newRecord("T2");
        ThreadingUtil.runOnGUI(() -> {
            r1.place(List.of(a), false, Rule.PLACED_BY_DISPATCHER);
            r2.place(List.of(b), false, Rule.PLACED_BY_VALUE);
        });
        assertEquals(Rule.PLACED_BY_DISPATCHER, Objects.requireNonNull(r1.getReason(a)).getRule());
        assertEquals(Rule.PLACED_BY_VALUE, Objects.requireNonNull(r2.getReason(b)).getRule());
        assertThrows(IllegalArgumentException.class, () -> r1.place(List.of(a), false, Rule.ENTERED),
                "only the rules that place can place");
    }

    @Test
    public void testEnteredAndLeft() {
        Block a = detected("A");
        Block b = detected("B");
        chain(a, b);
        TrainRecord r = placed("T1", a);

        occupy(b);
        Reason entered = Objects.requireNonNull(r.getReason(b));
        assertEquals(Rule.ENTERED, entered.getRule());
        assertSame(a, entered.getFrom());
        assertSame(b.getSensor(), entered.getSensor(), "set off by B's sensor");
        assertSame(entered, r.getConfidenceReason());

        clear(a);
        assertEquals(List.of(b), r.getBlocks());
        assertNull(r.getReason(a), "no longer in A");
        Reason left = r.getHistory().get(0);
        assertEquals(Rule.LEFT, left.getRule());
        assertSame(a, left.getBlock());
        assertSame(a.getSensor(), left.getSensor());
        assertEquals(List.of(Rule.LEFT, Rule.ENTERED, Rule.PLACED),
                r.getHistory().stream().map(Reason::getRule).collect(java.util.stream.Collectors.toList()),
                "most recent first");
    }

    @Test
    public void testDropOutAndSeenAgain() {
        Block a = detected("A");
        TrainRecord r = placed("T1", a);

        clear(a);
        assertEquals(Confidence.ESTIMATED, r.getConfidence());
        Reason c = Objects.requireNonNull(r.getConfidenceReason());
        assertEquals(Rule.KEPT_AFTER_DROPOUT, c.getRule(), "with no dark Block beside it, it can't have left");
        assertSame(a, c.getBlock());
        assertEquals(Rule.PLACED, Objects.requireNonNull(r.getReason(a)).getRule(),
                "still in A, for the reason it was put there");

        occupy(a);
        assertEquals(Confidence.CONFIRMED, r.getConfidence());
        assertEquals(Rule.SEEN_AGAIN, Objects.requireNonNull(r.getConfidenceReason()).getRule());
        assertEquals(Rule.SEEN_AGAIN, r.getHistory().get(0).getRule());
    }

    @Test
    public void testAmbiguousThenSettledByValue() {
        Block a = detected("A");
        Block b = detected("B");
        Block c = detected("C");
        chain(a, b, c);
        TrainRecord r1 = placed("T1", a);
        TrainRecord r2 = placed("T2", c);

        occupy(b);
        assertEquals(Confidence.AMBIGUOUS, r1.getConfidence());
        assertEquals(Rule.AMBIGUOUS, Objects.requireNonNull(r1.getConfidenceReason()).getRule());
        assertSame(b, Objects.requireNonNull(r1.getConfidenceReason()).getBlock());

        ThreadingUtil.runOnLayout(() -> b.setValue("T2"));
        assertEquals(Rule.SETTLED_BY_VALUE, Objects.requireNonNull(r2.getReason(b)).getRule());
        assertEquals(Rule.AMBIGUITY_CLEARED, r1.getHistory().get(0).getRule());
        assertEquals(Confidence.CONFIRMED, r1.getConfidence());
    }

    @Test
    public void testHistoryIsLimited() {
        Block a = detected("A");
        TrainRecord r = placed("T1", a);
        for (int i = 0; i < TrainRecord.HISTORY_LENGTH; i++) {
            clear(a);
            occupy(a);
        }
        assertEquals(TrainRecord.HISTORY_LENGTH, r.getHistory().size());
        assertEquals(Rule.SEEN_AGAIN, r.getHistory().get(0).getRule());
    }

    @Test
    public void testUnplaced() {
        Block a = detected("A");
        TrainRecord r = placed("T1", a);
        ThreadingUtil.runOnGUI(r::unplace);
        assertNull(r.getConfidenceReason());
        assertNull(r.getReason(a));
        assertEquals(Rule.UNPLACED, r.getHistory().get(0).getRule());
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
