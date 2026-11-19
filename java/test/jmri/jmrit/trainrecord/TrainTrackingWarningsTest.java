package jmri.jmrit.trainrecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Path;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.TrainRecord;
import jmri.TrainRecord.Confidence;
import jmri.TrainRecordManager;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.jmrit.trainrecord.TrainTrackingWarnings.Warning;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Train tracking's warnings: unexplained occupancy, and trains overdue.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class TrainTrackingWarningsTest {

    private static final long S = 1_000_000_000L;

    @Test
    public void testUnexplainedAndDetectorStuck() {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        Block a = new Block("IB1", "A");
        Block far = new Block("IB2", "Far");
        a.setSensor(sm.provideSensor("IS1").getSystemName());
        far.setSensor(sm.provideSensor("IS2").getSystemName());
        InstanceManager.getDefault(jmri.BlockManager.class).register(a);
        InstanceManager.getDefault(jmri.BlockManager.class).register(far);
        TrainTrackingWarnings warnings = InstanceManager.getDefault(TrainTrackingWarnings.class);
        TrainRecordManager manager = InstanceManager.getDefault(TrainRecordManager.class);
        ThreadingUtil.runOnLayout(() -> {
            sm.provideSensor("IS1").setCommandedState(Sensor.ACTIVE);
            manager.newRecord("T1").place(List.of(a), false);
            sm.provideSensor("IS2").setCommandedState(Sensor.ACTIVE);
        });
        JUnitUtil.waitFor(() -> warnings.getWarnings().size() == 1, "a warning");
        Warning w = warnings.getWarnings().get(0);
        assertEquals(Warning.Kind.UNEXPLAINED, w.getKind());
        assertSame(far, w.getBlock());
        assertEquals("Occupancy no train explains: Far", w.getTitle());

        ThreadingUtil.runOnLayout(() -> warnings.detectorStuck(far));
        JUnitUtil.waitFor(() -> warnings.getWarnings().isEmpty(), "answered");
        assertTrue(manager.isUntrusted(far));
    }

    @Test
    public void testOverdue() {
        TrainRecord r = shunterEntersB(false);
        TrainTrackingWarnings warnings = InstanceManager.getDefault(TrainTrackingWarnings.class);
        long entered = r.getTrail().get(r.getTrail().size() - 1).getTimeNanos();

        // 200 mm/s: the first detected wheel reaches the end of B's 400 mm after 2 s, and a train half as
        // slow again would have reached it after 3 s, 600 mm at the profile's speed
        ThreadingUtil.runOnGUI(() -> warnings.checkOverdue(entered + 1 * S));
        ThreadingUtil.runOnGUI(() -> warnings.checkOverdue(entered + 29 * S / 10));
        ThreadingUtil.runOnGUI(() -> warnings.checkOverdue(entered + 3 * S
                + TrainTrackingWarnings.OVERDUE_SECONDS * S));
        assertEquals(Confidence.CONFIRMED, r.getConfidence(),
                "a train slower than its profile would still be on its way at 2.9 s; first seen overdue at 8 s");
        ThreadingUtil.runOnGUI(() -> warnings.checkOverdue(entered + 3 * S
                + TrainTrackingWarnings.OVERDUE_SECONDS * S + S / 10));
        assertEquals(Confidence.CONFIRMED, r.getConfidence(), "not for five seconds yet");
        ThreadingUtil.runOnGUI(() -> warnings.checkOverdue(entered + 8 * S
                + TrainTrackingWarnings.OVERDUE_SECONDS * S));
        assertEquals(Confidence.STALLED, r.getConfidence(), "its lead loco told to move, not seen to leave B");
        JUnitUtil.waitFor(() -> warnings.getWarnings().size() == 1, "a warning");
        assertEquals(Warning.Kind.STALLED, warnings.getWarnings().get(0).getKind());
        assertSame(r, warnings.getWarnings().get(0).getRecord());

        ThreadingUtil.runOnGUI(() -> r.place(List.of(b), true));
        JUnitUtil.waitFor(() -> warnings.getWarnings().isEmpty(), "found again");
    }

    /**
     * A train kept in a Block after the Block went empty was seen to leave,
     * only not where it went: it is not "not seen to leave", so it is not
     * reported stalled however long it is overdue.
     */
    @Test
    public void testNotOverdueOnceItsBlockWentEmpty() {
        TrainRecord r = shunterEntersB(false);
        TrainTrackingWarnings warnings = InstanceManager.getDefault(TrainTrackingWarnings.class);
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        long entered = r.getTrail().get(r.getTrail().size() - 1).getTimeNanos();

        ThreadingUtil.runOnLayout(() -> sm.provideSensor("IS1").setCommandedState(Sensor.INACTIVE));
        JUnitUtil.waitFor(() -> r.getBlocks().equals(List.of(b)), "the rear left A");
        ThreadingUtil.runOnLayout(() -> sm.provideSensor("IS2").setCommandedState(Sensor.INACTIVE));
        JUnitUtil.waitFor(() -> r.getConfidence() == Confidence.ESTIMATED, "B went empty: kept there, estimated");

        ThreadingUtil.runOnGUI(() -> warnings.checkOverdue(entered + 3 * S));
        ThreadingUtil.runOnGUI(() -> warnings.checkOverdue(entered + 30 * S));
        assertEquals(Confidence.ESTIMATED, r.getConfidence(), "still estimated, not stalled");
        assertTrue(warnings.getWarnings().isEmpty(), "no warning");
    }

    /**
     * A train placed where a Block's value named it may not be the train in
     * that Block: the value can follow another train. It is not reported
     * stalled, however long it is overdue.
     */
    @Test
    public void testNotOverdueWhenIdentifiedByValue() {
        TrainRecord r = shunterEntersB(true);
        TrainTrackingWarnings warnings = InstanceManager.getDefault(TrainTrackingWarnings.class);
        assertTrue(r.isIdentifiedByValue(), "placed by the Block's value");
        assertEquals(Confidence.CONFIRMED, r.getConfidence(), "seen to enter B");
        long entered = r.getTrail().get(r.getTrail().size() - 1).getTimeNanos();

        ThreadingUtil.runOnGUI(() -> warnings.checkOverdue(entered + 3 * S));
        ThreadingUtil.runOnGUI(() -> warnings.checkOverdue(entered + 30 * S));
        assertEquals(Confidence.CONFIRMED, r.getConfidence(), "not stalled");
        assertTrue(warnings.getWarnings().isEmpty(), "no warning");

        ThreadingUtil.runOnGUI(() -> r.place(List.of(b), true));
        assertFalse(r.isIdentifiedByValue(), "placed by code: no longer identified by a value");
    }

    /**
     * A train told to stop with its front at the end of its Block, as
     * Dispatcher stops one at the end of a Section, hasn't reached the next
     * Block: its first detected wheel is an overhang behind its front, and
     * its profile may be a little fast. It is never reported lost, however
     * long it stands.
     */
    @Test
    public void testStoppedAtTheEndOfItsBlockIsNotOverdue() {
        TrainRecord r = shunterEntersB(false);
        TrainTrackingWarnings warnings = InstanceManager.getDefault(TrainTrackingWarnings.class);
        long entered = r.getTrail().get(r.getTrail().size() - 1).getTimeNanos();
        // told to stop after 2.1 s: by its profile, its wheel 20 mm past the end of B, its front further
        stopAt(entered + 21 * S / 10);
        JUnitUtil.waitFor(() -> r.getCommands().size() == 4, "the stop reached the record");
        for (int s = 1; s <= 60; s++) {
            long at = entered + s * S;
            ThreadingUtil.runOnGUI(() -> warnings.checkOverdue(at));
        }
        assertEquals(Confidence.CONFIRMED, r.getConfidence(), "standing at the end of B, not lost");
        assertTrue(warnings.getWarnings().isEmpty(), "no warning");
    }

    /**
     * A train told to stop where even a train half as slow again as its
     * profile would have been well into the next Block, and not seen there,
     * is lost.
     */
    @Test
    public void testStoppedBeyondTheNextBoundaryIsLost() {
        TrainRecord r = shunterEntersB(false);
        TrainTrackingWarnings warnings = InstanceManager.getDefault(TrainTrackingWarnings.class);
        long entered = r.getTrail().get(r.getTrail().size() - 1).getTimeNanos();
        stopAt(entered + 4 * S); // 800 mm at the profile's speed: past 600
        JUnitUtil.waitFor(() -> r.getCommands().size() == 4, "the stop reached the record");
        ThreadingUtil.runOnGUI(() -> warnings.checkOverdue(entered + 5 * S));
        ThreadingUtil.runOnGUI(() -> warnings.checkOverdue(entered + 10 * S));
        assertEquals(Confidence.LOST, r.getConfidence(), "nothing tells it to move: lost");
    }

    /** The lead loco of {@link #shunterEntersB} told to stop at a moment. */
    private static void stopAt(long nanos) {
        jmri.LocoSpeedSource stop = new jmri.LocoSpeedSource() {
            @Override
            public void addLocoSpeedListener(jmri.LocoSpeedSource.Listener listener) {
                listener.speedCommanded(new jmri.DccLocoAddress(7, false), 0f, true, nanos);
            }

            @Override
            public void removeLocoSpeedListener(jmri.LocoSpeedSource.Listener listener) {
            }
        };
        ThreadingUtil.runOnLayout(() -> InstanceManager.store(stop, jmri.LocoSpeedSource.class));
    }

    private Block a;
    private Block b;

    /**
     * Blocks A (1000 mm) and B (400 mm), joined; a train whose lead loco has
     * a speed profile, placed in A and told to move at 200 mm/s, seen to
     * enter B.
     *
     * @param byValue true to place the train where A's value names it, false
     *                to place it by code
     * @return the train
     */
    private TrainRecord shunterEntersB(boolean byValue) {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        a = new Block("IB1", "A");
        b = new Block("IB2", "B");
        a.setLength(1000);
        b.setLength(400);
        a.setSensor("IS1");
        b.setSensor("IS2");
        sm.provideSensor("IS1");
        sm.provideSensor("IS2");
        a.addPath(new Path(b, 0, 0));
        b.addPath(new Path(a, 0, 0));
        InstanceManager.getDefault(jmri.BlockManager.class).register(a);
        InstanceManager.getDefault(jmri.BlockManager.class).register(b);

        RosterEntry entry = new RosterEntry();
        entry.setId("Class 08");
        entry.setDccAddress("7");
        RosterSpeedProfile profile = new RosterSpeedProfile(entry);
        profile.setSpeed(1000, 400, 400);
        profile.setSpeed(500, 200, 200);
        entry.setSpeedProfile(profile);
        RosterEntry second = new RosterEntry();
        second.setId("Class 08 banker");
        second.setDccAddress("8");

        TrainRecordManager manager = InstanceManager.getDefault(TrainRecordManager.class);
        InstanceManager.getDefault(TrainTrackingWarnings.class);
        TrainRecord r = ThreadingUtil.runOnGUIwithReturn(() -> {
            sm.provideSensor("IS1").setCommandedState(Sensor.ACTIVE);
            TrainRecord t = manager.newRecord("Shunter");
            t.setRosterEntries(List.of(entry, second));
            if (byValue) {
                a.setValue(entry);
            } else {
                t.place(List.of(a), true);
            }
            return t;
        });
        JUnitUtil.waitFor(r::isPlaced, "placed in A");
        long start = System.nanoTime();
        jmri.LocoSpeedSource source = new jmri.LocoSpeedSource() {
            @Override
            public void addLocoSpeedListener(jmri.LocoSpeedSource.Listener listener) {
                listener.speedCommanded(new jmri.DccLocoAddress(7, false), 0.5f, true, start);
                // the second loco's address released last: the lead loco's command still says move
                listener.speedCommanded(new jmri.DccLocoAddress(8, false), 0.5f, true, start + 1);
                listener.released(new jmri.DccLocoAddress(8, false), start + 2);
            }

            @Override
            public void removeLocoSpeedListener(jmri.LocoSpeedSource.Listener listener) {
            }
        };
        ThreadingUtil.runOnLayout(() -> InstanceManager.store(source, jmri.LocoSpeedSource.class));
        JUnitUtil.waitFor(() -> r.getCommands().size() == 3, "the commands reached the record");
        ThreadingUtil.runOnLayout(() -> sm.provideSensor("IS2").setCommandedState(Sensor.ACTIVE));
        JUnitUtil.waitFor(() -> r.getBlocks().size() == 2, "the front entered B");
        return r;
    }

    @BeforeEach
    public void setUp(@TempDir File tempDir) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(tempDir));
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
