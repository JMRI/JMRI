package jmri.jmrit.trainrecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
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
 * Reasons and states in words, and each Block's history.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class TrainRecordReasonsTest {

    private Block block(String name) {
        Block b = InstanceManager.getDefault(BlockManager.class).provideBlock("IB" + name);
        b.setUserName(name);
        return b;
    }

    @Test
    public void testEveryRuleHasWords() {
        Block a = block("A");
        Block b = block("B");
        Block d = block("D");
        Sensor s = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        for (Rule rule : Rule.values()) {
            String text = TrainRecordReasons.describe(new Reason(rule, a, b, s, List.of(d), Instant.now()));
            assertFalse(text.isEmpty(), rule.name());
            assertFalse(text.contains("{"), rule.name() + ": " + text);
            assertFalse(text.startsWith("Reason"), rule.name() + " has no message: " + text);
        }
    }

    @Test
    public void testEnteredInWords() {
        Block a = block("LB3");
        Block b = block("LB4");
        Sensor s = InstanceManager.getDefault(SensorManager.class).provideSensor("IS4");
        Instant t = Instant.now();
        String text = TrainRecordReasons.describe(new Reason(Rule.ENTERED, b, a, s, List.of(), t));
        assertEquals("Entered LB4 from LB3 at " + TrainRecordReasons.time(t) + ": sensor " + s.getDisplayName()
                + " went Active.", text);
    }

    @Test
    public void testConfidenceAndDisplayName() {
        for (Confidence c : Confidence.values()) {
            assertFalse(TrainRecordReasons.describe(c).isEmpty(), c.name());
        }
        assertEquals("", TrainRecordReasons.describe((Confidence) null));
        TrainRecord r = InstanceManager.getDefault(TrainRecordManager.class).newRecord("60009 Mail");
        assertEquals("60009 Mail", TrainRecordReasons.displayName(r), "not placed: no marking");
    }

    @Test
    public void testAmbiguousTrainsMarked() throws jmri.JmriException {
        Block a = block("LB1");
        Block b = block("LB2");
        Block c = block("LB3");
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        for (Block k : new Block[]{a, b, c}) {
            k.setSensor(sm.provideSensor("IS" + k.getUserName()).getSystemName());
        }
        for (Block[] j : new Block[][]{{a, b}, {c, b}}) {
            j[0].addPath(new jmri.Path(j[1], 0, 0));
            j[1].addPath(new jmri.Path(j[0], 0, 0));
        }
        TrainRecordManager m = InstanceManager.getDefault(TrainRecordManager.class);
        TrainRecord up = m.newRecord("Up goods");
        TrainRecord down = m.newRecord("Down goods");
        ThreadingUtil.runOnGUI(() -> {
            up.place(List.of(a), false);
            down.place(List.of(c), false);
        });
        assertEquals("Up goods", TrainRecordReasons.displayName(up), "confirmed: no marking");
        assertEquals(TrainRecordReasons.describe(java.util.Objects.requireNonNull(up.getReason(a))), TrainRecordReasons.why(up, a),
                "confirmed: only why it is there");

        // LB2 goes occupied: either could have entered it
        ThreadingUtil.runOnGUI(() -> {
            try {
                sm.provideSensor("ISLB2").setKnownState(Sensor.ACTIVE);
            } catch (jmri.JmriException e) {
                throw new RuntimeException(e);
            }
        });
        JUnitUtil.waitFor(() -> up.getConfidence() == Confidence.AMBIGUOUS, "ambiguous");
        assertEquals("Up goods (?)", TrainRecordReasons.displayName(up), "marked");
        String why = TrainRecordReasons.why(up, a);
        assertTrue(why.startsWith(TrainRecordReasons.describe(java.util.Objects.requireNonNull(up.getReason(a))) + " "), why);
        assertTrue(why.endsWith(TrainRecordReasons.describe(java.util.Objects.requireNonNull(up.getConfidenceReason()))), "and why it isn't sure: " + why);
    }

    @Test
    public void testBlockHistory() {
        Block a = block("A");
        TrainRecordManager histories = InstanceManager.getDefault(TrainRecordManager.class);
        TrainRecord r = histories.newRecord("T1");
        ThreadingUtil.runOnGUI(() -> r.place(List.of(a), false));
        List<TrainRecordManager.BlockEvent> h = histories.getHistory(a);
        assertEquals(1, h.size());
        assertEquals("T1", h.get(0).getTrain());
        assertEquals(Rule.PLACED, h.get(0).getReason().getRule());
        assertEquals(List.of(r), histories.getRecordsIn(a));
        assertTrue(TrainRecordReasons.why(r, a).startsWith("Placed in A"), TrainRecordReasons.why(r, a));

        ThreadingUtil.runOnGUI(r::unplace);
        assertEquals(1, histories.getHistory(a).size(), "taking it off names no Block");
        assertTrue(histories.getRecordsIn(a).isEmpty());
        assertEquals("", TrainRecordReasons.why(r, a));
    }

    @Test
    public void testBlockHistoryIsLimited() {
        Block a = block("A");
        TrainRecordManager histories = InstanceManager.getDefault(TrainRecordManager.class);
        TrainRecord r = histories.newRecord("T1");
        for (int i = 0; i < TrainRecordManager.BLOCK_HISTORY_LENGTH + 5; i++) {
            ThreadingUtil.runOnGUI(() -> r.place(List.of(a), false));
        }
        assertEquals(TrainRecordManager.BLOCK_HISTORY_LENGTH, histories.getHistory(a).size());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
