package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
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
import jmri.TrainRecord.Reason.Rule;
import jmri.TrainRecordManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Occupancy no train explains, untrusted detectors, and trains reported
 * stalled or lost.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class TrainRecordUnexplainedTest {

    private TrainRecordManager manager;
    private long nanos = 1_000_000_000L;

    private Block detected(String name) {
        Block b = InstanceManager.getDefault(BlockManager.class).provideBlock("IB" + name);
        b.setUserName(name);
        Sensor s = InstanceManager.getDefault(SensorManager.class).provideSensor("IS" + name);
        set(s, Sensor.INACTIVE);
        b.setSensor(s.getSystemName());
        return b;
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

    private TrainRecord placed(String name, Block b) {
        occupy(b);
        return ThreadingUtil.runOnGUIwithReturn(() -> {
            TrainRecord r = manager.newRecord(name);
            r.place(List.of(b), true);
            return r;
        });
    }

    @Test
    public void testUnexplainedOccupancy() {
        Block a = detected("A");
        Block b = detected("B");
        Block far = detected("Far");
        a.addPath(new Path(b, 0, 0));
        b.addPath(new Path(a, 0, 0));
        List<Object> events = new ArrayList<>();
        manager.addPropertyChangeListener(TrainRecordManager.PROPERTY_UNEXPLAINED,
                e -> events.add(e.getNewValue() != null ? e.getNewValue() : "-" + e.getOldValue()));

        occupy(far);
        assertTrue(manager.getUnexplained().isEmpty(), "not followed while no train is placed");
        clear(far);

        placed("T1", a);
        occupy(far);
        assertEquals(List.of(far), manager.getUnexplained(), "no train could have entered it");
        occupy(b);
        assertEquals(List.of(far), manager.getUnexplained(), "T1 explains B");
        clear(far);
        assertTrue(manager.getUnexplained().isEmpty(), "empty again");
        assertEquals(List.of(far, "-" + far), events);
    }

    @Test
    public void testPlacingATrainExplainsIt() {
        Block a = detected("A");
        Block far = detected("Far");
        placed("T1", a);
        occupy(far);
        assertEquals(List.of(far), manager.getUnexplained());
        TrainRecord r2 = manager.newRecord("T2");
        ThreadingUtil.runOnGUI(() -> r2.place(List.of(far), false));
        assertTrue(manager.getUnexplained().isEmpty(), "a train is there now");
    }

    @Test
    public void testUntrustedDetector() {
        Block a = detected("A");
        Block b = detected("B");
        a.addPath(new Path(b, 0, 0));
        b.addPath(new Path(a, 0, 0));
        TrainRecord r = placed("T1", a);
        Block far = detected("Far");
        occupy(far);
        ThreadingUtil.runOnGUI(() -> manager.setUntrusted(far, true));
        assertTrue(manager.isUntrusted(far));
        assertEquals(List.of(far), manager.getUntrusted());
        assertTrue(manager.getUnexplained().isEmpty(), "marking it untrusted ends the warning");
        clear(far);
        occupy(far);
        assertTrue(manager.getUnexplained().isEmpty(), "no notice taken of it");

        ThreadingUtil.runOnGUI(() -> manager.setUntrusted(b, true));
        occupy(b);
        assertEquals(List.of(a), r.getBlocks(), "nor seen to enter it by an untrusted detector");
        ThreadingUtil.runOnGUI(() -> manager.setUntrusted(far, false));
        assertFalse(manager.isUntrusted(far));
    }

    @Test
    public void testReportOverdue() {
        Block a = detected("A");
        TrainRecord r1 = placed("T1", a);
        Block b = detected("B");
        TrainRecord r2 = placed("T2", b);
        ThreadingUtil.runOnGUI(() -> {
            manager.reportOverdue(r1, a, true);
            manager.reportOverdue(r2, b, false);
        });
        assertEquals(Confidence.STALLED, r1.getConfidence());
        assertEquals(Rule.STALLED, Objects.requireNonNull(r1.getConfidenceReason()).getRule());
        assertEquals(Rule.STALLED, r1.getHistory().get(0).getRule());
        assertEquals(Confidence.LOST, r2.getConfidence());
        assertEquals(Rule.LOST, Objects.requireNonNull(r2.getConfidenceReason()).getRule());

        ThreadingUtil.runOnGUI(() -> r1.place(List.of(a), true));
        assertEquals(Confidence.CONFIRMED, r1.getConfidence(), "found again when placed");
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
