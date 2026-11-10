package jmri.implementation;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jmri.Block;
import jmri.BlockManager;
import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.Path;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.TrainRecord;
import jmri.TrainRecord.Confidence;
import jmri.TrainRecord.End;
import jmri.TrainRecord.Reason.Rule;
import jmri.TrainRecord.TrailEntry;
import jmri.TrainRecordManager;
import jmri.Turnout;
import jmri.BeanSetting;
import jmri.TurnoutManager;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TrainRecordWatcher}: a placed train record follows its
 * train from Block to Block, on plain Blocks with internal sensors and
 * paths. Sensor changes are made on the layout thread (Swing thread), as a
 * connection's are, so each is handled before the next.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class TrainRecordWatcherTest {

    private TrainRecordManager manager;
    private long nanos = 1_000_000_000L;

    private Block block(String name) {
        Block b = InstanceManager.getDefault(BlockManager.class).provideBlock("IB" + name);
        b.setUserName(name);
        Sensor s = InstanceManager.getDefault(SensorManager.class).provideSensor("IS" + name);
        set(s, Sensor.INACTIVE);
        b.setSensor(s.getSystemName());
        return b;
    }

    private static void join(Block a, Block b) {
        a.addPath(new Path(b, 0, 0));
        b.addPath(new Path(a, 0, 0));
    }

    /** Blocks in a line, each joined to the next. */
    private Block[] line(String... names) {
        Block[] blocks = new Block[names.length];
        for (int i = 0; i < names.length; i++) {
            blocks[i] = block(names[i]);
            if (i > 0) {
                join(blocks[i - 1], blocks[i]);
            }
        }
        return blocks;
    }

    /** Blocks in a loop: a line with the last joined back to the first. */
    private Block[] loop(String... names) {
        Block[] blocks = line(names);
        join(blocks[blocks.length - 1], blocks[0]);
        return blocks;
    }

    /** Set a sensor's state on the layout thread, at the next test time. */
    private long set(Sensor s, int state) {
        nanos += 1_000_000_000L;
        long t = nanos;
        ThreadingUtil.runOnLayout(() -> ((AbstractSensor) s).setOwnState(state, t));
        return t;
    }

    private static Sensor sensor(Block b) {
        return Objects.requireNonNull(b.getSensor(), "sensor of " + b.getDisplayName());
    }

    private long occupy(Block b) {
        return set(sensor(b), Sensor.ACTIVE);
    }

    private long clear(Block b) {
        return set(sensor(b), Sensor.INACTIVE);
    }

    private void place(TrainRecord r, boolean frontKnown, Block... blocks) {
        for (Block b : blocks) {
            occupy(b);
        }
        ThreadingUtil.runOnLayout(() -> r.place(List.of(blocks), frontKnown));
    }

    private static TrailEntry last(TrainRecord r) {
        List<TrailEntry> trail = r.getTrail();
        return trail.get(trail.size() - 1);
    }

    @Test
    public void testEnteringAtTheFront() {
        Block[] b = line("A", "B", "C");
        TrainRecord r = manager.newRecord("T1");
        place(r, true, b[0]);
        assertEquals(Confidence.CONFIRMED, r.getConfidence());
        assertNull(r.getLeadingEnd(), "not seen to move yet");
        long t = occupy(b[1]);
        assertEquals(List.of(b[1], b[0]), r.getBlocks(), "front first");
        assertEquals(End.FRONT, r.getLeadingEnd());
        TrailEntry e = last(r);
        assertSame(b[1], e.getBlock());
        assertSame(b[0], e.getFrom());
        assertEquals(End.FRONT, e.getEnd());
        assertEquals(t, e.getTimeNanos(), "dated by the sensor's report");
        clear(b[0]);
        assertEquals(List.of(b[1]), r.getBlocks(), "the rear has left A");
        occupy(b[2]);
        clear(b[1]);
        assertEquals(List.of(b[2]), r.getBlocks());
        assertEquals(Confidence.CONFIRMED, r.getConfidence());
    }

    @Test
    public void testFrontLearnedFromFirstMove() {
        Block[] b = line("A", "B", "C", "D");
        TrainRecord r = manager.newRecord("T1");
        place(r, false, b[1], b[2]);
        assertFalse(r.isFrontKnown());
        occupy(b[0]);
        assertTrue(r.isFrontKnown(), "the end that led is the front");
        assertEquals(List.of(b[0], b[1], b[2]), r.getBlocks());

        TrainRecord s = manager.newRecord("T2");
        Block[] c = line("E", "F", "G");
        place(s, false, c[0], c[1]);
        occupy(c[2]);
        assertTrue(s.isFrontKnown());
        assertEquals(List.of(c[2], c[1], c[0]), s.getBlocks(), "led at the far end: that end is the front");
        assertEquals(End.FRONT, s.getLeadingEnd());
    }

    @Test
    public void testReversing() {
        Block[] b = line("A", "B", "C", "D");
        TrainRecord r = manager.newRecord("T1");
        place(r, true, b[2], b[1]);
        occupy(b[0]);
        assertEquals(End.REAR, r.getLeadingEnd(), "entered at the rear: reversing");
        assertEquals(List.of(b[2], b[1], b[0]), r.getBlocks(), "front still first");
        assertEquals(End.REAR, last(r).getEnd());
        clear(b[2]);
        assertEquals(List.of(b[1], b[0]), r.getBlocks(), "the front has left C");
        assertTrue(r.isFrontKnown());
    }

    @Test
    public void testFlickerForgotten() {
        Block[] b = line("A", "B");
        Sensor sb = sensor(b[1]);
        sb.setSensorDebounceGoingActiveTimer(60_000);
        sb.setUseDefaultTimerSettings(false);
        TrainRecord r = manager.newRecord("T1");
        place(r, true, b[0]);
        set(sb, Sensor.ACTIVE); // held by the debounce
        set(sb, Sensor.INACTIVE); // gone: a flicker
        assertEquals(List.of(b[0]), r.getBlocks(), "a flicker enters nothing");
        sb.setSensorDebounceGoingActiveTimer(0);
        long t = occupy(b[1]);
        assertEquals(t, last(r).getTimeNanos(), "the flicker's time is forgotten");
    }

    @Test
    public void testDebouncedDatedByFirstReport() {
        Block[] b = line("A", "B");
        Sensor sb = sensor(b[1]);
        sb.setSensorDebounceGoingActiveTimer(100);
        sb.setUseDefaultTimerSettings(false);
        TrainRecord r = manager.newRecord("T1");
        place(r, true, b[0]);
        long t = set(sb, Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> r.getBlocks().size() == 2, "entered once the debounce let it through");
        assertEquals(t, last(r).getTimeNanos(), "dated by the first report, not the debounce's end");
    }

    @Test
    public void testOnlyBlockEmptiesKept() {
        Block[] b = line("A", "B");
        TrainRecord r = manager.newRecord("T1");
        place(r, true, b[0]);
        clear(b[0]);
        assertEquals(List.of(b[0]), r.getBlocks(), "kept: a drop-out or a departure onto undetected track");
        assertEquals(Confidence.ESTIMATED, r.getConfidence());
        occupy(b[0]);
        assertEquals(Confidence.CONFIRMED, r.getConfidence(), "seen again");
    }

    @Test
    public void testEmptiedOnlyBlockLeftWhenTheNextIsEntered() {
        Block[] b = line("A", "B", "C");
        TrainRecord r = manager.newRecord("T1");
        place(r, true, b[0]);
        clear(b[0]); // a short train: A empties before B shows it
        assertEquals(Confidence.ESTIMATED, r.getConfidence());
        occupy(b[1]);
        assertEquals(List.of(b[1]), r.getBlocks(), "A was left: the train is in B");
        assertEquals(Confidence.CONFIRMED, r.getConfidence());
        assertEquals(TrainRecord.End.FRONT, r.getLeadingEnd());

        occupy(b[2]); // B still shows it: B is kept
        assertEquals(List.of(b[2], b[1]), r.getBlocks());
    }

    @Test
    public void testGapKept() {
        Block[] b = line("A", "B", "C");
        TrainRecord r = manager.newRecord("T1");
        place(r, true, b[2], b[1], b[0]);
        clear(b[1]);
        assertEquals(List.of(b[2], b[1], b[0]), r.getBlocks(), "a Block between others is kept");
        assertEquals(Confidence.ESTIMATED, r.getConfidence());
    }

    @Test
    public void testPathSetAgainstNotFollowed() {
        Block a = block("A");
        Block b = block("B");
        Block c = block("C");
        Turnout t = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        t.setCommandedState(Turnout.CLOSED);
        Path ab = new Path(b, 0, 0);
        ab.addSetting(new BeanSetting(t, Turnout.CLOSED));
        a.addPath(ab);
        Path ac = new Path(c, 0, 0);
        ac.addSetting(new BeanSetting(t, Turnout.THROWN));
        a.addPath(ac);
        TrainRecord r = manager.newRecord("T1");
        place(r, true, a);
        occupy(c);
        assertEquals(List.of(a), r.getBlocks(), "points set for B: C isn't entered from A");
        occupy(b);
        assertEquals(List.of(b, a), r.getBlocks());
    }

    @Test
    public void testTwoTrainsAmbiguousThenSettledByValue() {
        Block[] b = line("A", "B", "C");
        TrainRecord left = manager.newRecord("Left");
        TrainRecord right = manager.newRecord("Right");
        place(left, true, b[0]);
        place(right, true, b[2]);
        occupy(b[1]);
        assertEquals(Confidence.AMBIGUOUS, left.getConfidence(), "either could have entered B");
        assertEquals(Confidence.AMBIGUOUS, right.getConfidence());
        assertEquals(List.of(b[0]), left.getBlocks(), "nothing added while it isn't known");
        ThreadingUtil.runOnLayout(() -> b[1].setValue("Right"));
        assertEquals(List.of(b[1], b[2]), right.getBlocks(), "the value says which");
        assertEquals(Confidence.CONFIRMED, right.getConfidence());
        assertEquals(Confidence.CONFIRMED, left.getConfidence(), "the other is as it was");
        assertEquals(List.of(b[0]), left.getBlocks());
    }

    @Test
    public void testAmbiguityEndsWhenBlockEmpties() {
        Block[] b = line("A", "B", "C");
        TrainRecord left = manager.newRecord("Left");
        TrainRecord right = manager.newRecord("Right");
        place(left, true, b[0]);
        place(right, true, b[2]);
        occupy(b[1]);
        clear(b[1]);
        assertEquals(Confidence.CONFIRMED, left.getConfidence());
        assertEquals(Confidence.CONFIRMED, right.getConfidence());
    }

    @Test
    public void testPlacedFromValue() {
        Block[] b = line("A", "B");
        RosterEntry e = new RosterEntry();
        e.setId("Class 43");
        occupy(b[0]);
        ThreadingUtil.runOnLayout(() -> b[0].setValue(e));
        TrainRecord r = Objects.requireNonNull(manager.getByRosterEntry(e), "a roster entry value makes a record");
        assertEquals("Class 43", r.getName());
        assertEquals(List.of(e), r.getRosterEntries(), "with the entry as its first and only loco");
        assertEquals(List.of(b[0]), r.getBlocks(), "placed where the value was set");

        IdTag tag = InstanceManager.getDefault(IdTagManager.class).provideIdTag("ID1234");
        occupy(b[1]);
        ThreadingUtil.runOnLayout(() -> b[1].setValue(tag));
        TrainRecord rt = Objects.requireNonNull(manager.getByIdTag(tag), "an ID tag value makes a record");
        assertEquals(List.of(b[1]), rt.getBlocks());
    }

    @Test
    public void testStringValuePlacesOnlyExistingRecord() {
        Block[] b = line("A", "B", "C");
        occupy(b[0]);
        ThreadingUtil.runOnLayout(() -> b[0].setValue("Goods"));
        assertTrue(manager.getRecords().isEmpty(), "a String makes no record");
        TrainRecord r = manager.newRecord("Goods");
        occupy(b[2]);
        ThreadingUtil.runOnLayout(() -> b[2].setValue("Goods"));
        assertEquals(List.of(b[2]), r.getBlocks(), "a String names an existing record");
    }

    @Test
    public void testValueInEmptyBlockPlacesNothing() {
        Block[] b = line("A");
        TrainRecord r = manager.newRecord("Goods");
        ThreadingUtil.runOnLayout(() -> b[0].setValue("Goods"));
        assertFalse(r.isPlaced(), "a value in an empty Block, as Dispatcher writes ahead of a train, places nothing");
    }

    @Test
    public void testPlacedRecordNotMovedByValue() {
        Block[] b = line("A", "B", "C");
        TrainRecord r = manager.newRecord("Goods");
        place(r, true, b[0]);
        occupy(b[2]);
        ThreadingUtil.runOnLayout(() -> b[2].setValue("Goods"));
        assertEquals(List.of(b[0]), r.getBlocks(), "a placed record is followed, not moved by values");
    }

    @Test
    public void testSensorsWatchedOnlyWhilePlaced() {
        Block[] b = line("A", "B");
        Sensor sa = sensor(b[0]);
        int before = sa.getPropertyChangeListeners().length;
        TrainRecord r = manager.newRecord("T1");
        assertEquals(before, sa.getPropertyChangeListeners().length, "nothing placed: sensors not listened to");
        place(r, true, b[0]);
        assertEquals(before + 1, sa.getPropertyChangeListeners().length, "listened to while a record is placed");
        ThreadingUtil.runOnLayout(r::unplace);
        assertEquals(before, sa.getPropertyChangeListeners().length, "and not once none is");
        place(r, true, b[0]);
        ThreadingUtil.runOnLayout(() -> manager.remove(r));
        assertEquals(before, sa.getPropertyChangeListeners().length, "a removed record is not placed");
    }

    @Test
    public void testValueNamingAnotherTrainSettlesNothing() {
        Block[] b = line("A", "B", "C");
        TrainRecord left = manager.newRecord("Left");
        TrainRecord right = manager.newRecord("Right");
        manager.newRecord("Elsewhere");
        place(left, true, b[0]);
        place(right, true, b[2]);
        occupy(b[1]);
        ThreadingUtil.runOnLayout(() -> b[1].setValue("Elsewhere"));
        assertEquals(Confidence.AMBIGUOUS, left.getConfidence(), "a value naming neither train settles nothing");
        assertEquals(Confidence.AMBIGUOUS, right.getConfidence());
        ThreadingUtil.runOnLayout(() -> b[1].setValue("Left"));
        assertEquals(List.of(b[1], b[0]), left.getBlocks(), "still settled by a value that names one");
    }

    @Test
    public void testBlockDeletedNoLongerFollowed() {
        Block[] b = line("A", "B");
        TrainRecord r = manager.newRecord("T1");
        place(r, true, b[0]);
        Sensor s = sensor(b[1]);
        ThreadingUtil.runOnLayout(() -> InstanceManager.getDefault(BlockManager.class).deregister(b[1]));
        set(s, Sensor.ACTIVE);
        assertEquals(List.of(b[0]), r.getBlocks(), "a deleted Block's sensor is no longer followed");
    }

    @Test
    public void testOldSensorIgnoredAfterSensorChange() {
        Block[] b = line("A", "B");
        TrainRecord r = manager.newRecord("T1");
        place(r, true, b[0]);
        Sensor old = sensor(b[1]);
        Sensor replacement = InstanceManager.getDefault(SensorManager.class).provideSensor("ISB2");
        set(replacement, Sensor.INACTIVE);
        ThreadingUtil.runOnLayout(() -> b[1].setSensor(replacement.getSystemName()));
        set(old, Sensor.ACTIVE);
        assertEquals(List.of(b[0]), r.getBlocks(), "the Block's old sensor is no longer followed");
        set(replacement, Sensor.ACTIVE);
        assertEquals(List.of(b[1], b[0]), r.getBlocks(), "its new sensor is");
    }

    @Test
    public void testBlockManagerStoredLater() {
        InstanceManager.getDefault(BlockManager.class); // the first, whose shutdown task goes now
        JUnitUtil.deregisterBlockManagerShutdownTask();
        BlockManager replacement = new BlockManager();
        ThreadingUtil.runOnLayout(() -> InstanceManager.setDefault(BlockManager.class, replacement));
        Block[] b = line("A", "B");
        assertSame(replacement, InstanceManager.getDefault(BlockManager.class));
        TrainRecord r = manager.newRecord("T1");
        place(r, true, b[0]);
        occupy(b[1]);
        assertEquals(List.of(b[1], b[0]), r.getBlocks(), "the Blocks of a Block manager stored later are followed");
    }

    @Test
    public void testBlockAddedLater() {
        Block a = block("A");
        TrainRecord r = manager.newRecord("T1");
        place(r, true, a);
        Block b = block("B");
        join(a, b);
        occupy(b);
        assertEquals(List.of(b, a), r.getBlocks(), "a Block made after the watcher started is followed");
    }

    @Test
    public void testEventsFromAnotherThread() {
        Block[] b = line("A", "B", "C");
        TrainRecord r = manager.newRecord("T1");
        place(r, true, b[0]);
        // set from this (test) thread: handed to the layout thread, in order
        ((AbstractSensor) sensor(b[1])).setOwnState(Sensor.ACTIVE, 5);
        ((AbstractSensor) sensor(b[0])).setOwnState(Sensor.INACTIVE, 6);
        ((AbstractSensor) sensor(b[2])).setOwnState(Sensor.ACTIVE, 7);
        JUnitUtil.waitFor(() -> r.getBlocks().equals(List.of(b[2], b[1])), "followed in order");
        List<TrailEntry> trail = r.getTrail();
        assertEquals(5, trail.get(trail.size() - 2).getTimeNanos());
        assertEquals(7, trail.get(trail.size() - 1).getTimeNanos());
    }

    @Test
    public void testTrailBounded() {
        Block[] b = line("A", "B");
        TrainRecord r = manager.newRecord("T1");
        place(r, true, b[0]);
        for (int i = 0; i < TrainRecord.TRAIL_LENGTH + 10; i++) {
            occupy(b[(i + 1) % 2]);
            clear(b[i % 2]);
        }
        assertEquals(TrainRecord.TRAIL_LENGTH, r.getTrail().size());
    }

    @Test
    public void testEventsFired() {
        Block[] b = line("A", "B");
        TrainRecord r = manager.newRecord("T1");
        List<String> names = new ArrayList<>();
        r.addPropertyChangeListener((PropertyChangeEvent e) -> names.add(e.getPropertyName()));
        place(r, true, b[0]);
        assertTrue(names.contains(TrainRecord.PROPERTY_BLOCKS));
        assertTrue(names.contains(TrainRecord.PROPERTY_CONFIDENCE));
        names.clear();
        occupy(b[1]);
        assertEquals(List.of(TrainRecord.PROPERTY_DIRECTION, TrainRecord.PROPERTY_BLOCKS, TrainRecord.PROPERTY_TRAIL,
                TrainRecord.PROPERTY_HISTORY), names, "the history entry last");
    }

    @Test
    public void testLoopLastBlockEnteredAtTheLeadingEnd() {
        Block[] b = loop("A", "B", "C", "D");
        TrainRecord r = manager.newRecord("T1");
        place(r, true, b[1], b[0]);
        occupy(b[2]); // now in C, B, A, moving A to B to C
        assertEquals(List.of(b[2], b[1], b[0]), r.getBlocks());
        assertEquals(End.FRONT, r.getLeadingEnd());
        occupy(b[3]); // D is next to both ends of the train: C (front) and A (rear)
        assertEquals(List.of(b[3], b[2], b[1], b[0]), r.getBlocks(), "entered at the end that leads");
        assertEquals(End.FRONT, r.getLeadingEnd());
        assertEquals(Confidence.CONFIRMED, r.getConfidence(), "one train is not ambiguous with itself");
        clear(b[0]);
        assertEquals(List.of(b[3], b[2], b[1]), r.getBlocks(), "the rear has left A");
    }

    @Test
    public void testLoopLastBlockEnteredReversing() {
        Block[] b = loop("A", "B", "C", "D");
        TrainRecord r = manager.newRecord("T1");
        place(r, true, b[2], b[1]);
        occupy(b[0]); // entered at the rear: reversing, now C, B, A
        assertEquals(End.REAR, r.getLeadingEnd());
        occupy(b[3]); // next to both ends
        assertEquals(List.of(b[2], b[1], b[0], b[3]), r.getBlocks(), "entered at the rear, which leads");
        assertEquals(End.REAR, r.getLeadingEnd());
        assertEquals(Confidence.CONFIRMED, r.getConfidence());
    }

    @Test
    public void testLoopLastBlockWithNoLeadingEndSettledByTheEndLeft() {
        Block[] b = loop("A", "B", "C", "D");
        TrainRecord r = manager.newRecord("T1");
        place(r, false, b[0], b[1], b[2]);
        assertNull(r.getLeadingEnd());
        occupy(b[3]); // next to both ends, and nothing says which end leads
        assertEquals(List.of(b[0], b[1], b[2]), r.getBlocks(), "nothing added while the end isn't known");
        assertEquals(Confidence.AMBIGUOUS, r.getConfidence());
        TrainRecord.Reason why = Objects.requireNonNull(r.getConfidenceReason());
        assertEquals(Rule.ENTERED_END_UNKNOWN, why.getRule(),
                "not one of several trains: one train whose leading end isn't known");
        assertEquals(b[3], why.getBlock());
        clear(b[2]); // the train has left C: it moved away from C, so it entered D at A's end
        assertEquals(List.of(b[3], b[0], b[1]), r.getBlocks(), "entered at the other end, then left C");
        assertEquals(Confidence.CONFIRMED, r.getConfidence());
        assertTrue(r.isFrontKnown(), "the end that led is the front");
    }

    @Test
    public void testLoopLastBlockWithNoLeadingEndEmptiedKeepsConfidence() {
        Block[] b = loop("A", "B", "C", "D");
        TrainRecord r = manager.newRecord("T1");
        place(r, false, b[0], b[1], b[2]);
        occupy(b[3]);
        assertEquals(Confidence.AMBIGUOUS, r.getConfidence());
        clear(b[3]);
        assertEquals(Confidence.CONFIRMED, r.getConfidence(), "as it was: never left without a confidence");
        assertTrue(r.isPlaced());
        assertEquals(List.of(b[0], b[1], b[2]), r.getBlocks());
    }

    @Test
    public void testLoopLastBlockWithNoLeadingEndValueDoesNotPickAnEnd() {
        Block[] b = loop("A", "B", "C", "D");
        TrainRecord r = manager.newRecord("T1");
        place(r, false, b[0], b[1], b[2]);
        occupy(b[3]);
        ThreadingUtil.runOnLayout(() -> b[3].setValue("T1"));
        assertEquals(List.of(b[0], b[1], b[2]), r.getBlocks(), "the value names the train, not the end it entered at");
        assertEquals(Confidence.AMBIGUOUS, r.getConfidence());
        clear(b[0]);
        assertEquals(List.of(b[3], b[2], b[1]), r.getBlocks(), "settled by the end it left; that end led");
        assertEquals(Confidence.CONFIRMED, r.getConfidence());
    }

    @Test
    public void testLoopSecondTrainAtBothEndsSettledByValue() {
        Block[] b = loop("A", "B", "C", "D", "E", "F");
        TrainRecord r = manager.newRecord("T1");
        TrainRecord other = manager.newRecord("T2");
        place(r, true, b[1], b[0]);
        occupy(b[2]); // T1 in C, B, A, front at C
        place(other, true, b[4]); // T2 in E
        occupy(b[3]); // D: next to T1's front (C) and to T2 (E)
        assertEquals(Confidence.AMBIGUOUS, r.getConfidence());
        assertEquals(Confidence.AMBIGUOUS, other.getConfidence());
        ThreadingUtil.runOnLayout(() -> b[3].setValue("T1"));
        assertEquals(List.of(b[3], b[2], b[1], b[0]), r.getBlocks());
        assertEquals(Confidence.CONFIRMED, r.getConfidence());
        assertEquals(Confidence.CONFIRMED, other.getConfidence(), "the other train is as it was");
    }

    @Test
    public void testLoopBothEndsAndAnotherTrainValueClearsTheOther() {
        Block[] b = loop("A", "B", "C", "D");
        Block e = block("E");
        join(b[3], e);
        TrainRecord r = manager.newRecord("T1");
        TrainRecord other = manager.newRecord("T2");
        place(r, false, b[0], b[1], b[2]);
        place(other, true, e);
        occupy(b[3]); // next to both ends of T1, and to T2
        assertEquals(Confidence.AMBIGUOUS, r.getConfidence());
        assertEquals(Confidence.AMBIGUOUS, other.getConfidence());
        assertEquals(Rule.AMBIGUOUS, Objects.requireNonNull(r.getConfidenceReason()).getRule(), "one of two trains");
        ThreadingUtil.runOnLayout(() -> b[3].setValue("T1"));
        assertEquals(Confidence.CONFIRMED, other.getConfidence(), "the value rules out T2");
        assertEquals(List.of(e), other.getBlocks());
        assertEquals(Confidence.AMBIGUOUS, r.getConfidence(), "T1 entered, but at which end isn't known yet");
        assertEquals(Rule.ENTERED_END_UNKNOWN, Objects.requireNonNull(r.getConfidenceReason()).getRule(),
                "no longer one of two trains: only its leading end isn't known");
        assertTrue(r.getHistory().stream().anyMatch(h -> h.getRule() == Rule.ENTERED_END_UNKNOWN),
                "the new reason is in the history");
        clear(b[2]);
        assertEquals(List.of(b[3], b[0], b[1]), r.getBlocks(), "it left C, so it entered D at A's end");
        assertEquals(Confidence.CONFIRMED, r.getConfidence());
    }

    @Test
    public void testLoopBothEndsWhileAmbiguousAtAnotherBlock() {
        Block[] b = loop("A", "B", "C", "D");
        Block f = block("F");
        Block g = block("G");
        join(b[0], f);
        join(f, g);
        TrainRecord r = manager.newRecord("T1");
        TrainRecord other = manager.newRecord("T2");
        place(r, false, b[0], b[1], b[2]);
        place(other, true, g);
        occupy(b[3]); // next to both ends of T1
        occupy(f); // next to T1's A end and to T2
        assertEquals(Confidence.AMBIGUOUS, r.getConfidence());
        clear(b[2]);
        assertEquals(List.of(b[3], b[0], b[1]), r.getBlocks(), "D settled by the end T1 left");
        assertEquals(Confidence.AMBIGUOUS, r.getConfidence(), "still one of two trains that could be in F");
        clear(f);
        assertEquals(Confidence.CONFIRMED, r.getConfidence(), "as it was before F");
        assertEquals(Confidence.CONFIRMED, other.getConfidence());
    }

    @Test
    public void testJoinedBySetPath() {
        Block a = block("A");
        Block b = block("B");
        assertFalse(TrainRecordWatcher.joinedBySetPath(a, b));
        a.addPath(new Path(b, 0, 0));
        assertTrue(TrainRecordWatcher.joinedBySetPath(a, b));
        assertTrue(TrainRecordWatcher.joinedBySetPath(b, a), "a path either way joins them");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initIdTagManager();
        manager = InstanceManager.getDefault(TrainRecordManager.class);
    }

    @AfterEach
    public void tearDown() {
        manager = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
