package jmri.jmrit.trainrecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        Block a = new Block("IB1", "A");
        Block b = new Block("IB2", "B");
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
        TrainTrackingWarnings warnings = InstanceManager.getDefault(TrainTrackingWarnings.class);
        TrainRecord r = ThreadingUtil.runOnGUIwithReturn(() -> {
            sm.provideSensor("IS1").setCommandedState(Sensor.ACTIVE);
            TrainRecord t = manager.newRecord("Shunter");
            t.setRosterEntries(List.of(entry, second));
            t.place(List.of(a), true);
            return t;
        });
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
        long entered = r.getTrail().get(r.getTrail().size() - 1).getTimeNanos();

        // 200 mm/s: the front reaches the end of B's 400 mm after 2 s
        ThreadingUtil.runOnGUI(() -> warnings.checkOverdue(entered + 1 * S));
        ThreadingUtil.runOnGUI(() -> warnings.checkOverdue(entered + 3 * S));
        assertEquals(Confidence.CONFIRMED, r.getConfidence(), "past the end, but not for long");
        ThreadingUtil.runOnGUI(() -> warnings.checkOverdue(entered + 3 * S
                + TrainTrackingWarnings.OVERDUE_SECONDS * S));
        assertEquals(Confidence.STALLED, r.getConfidence(), "its lead loco told to move, not seen to leave B");
        JUnitUtil.waitFor(() -> warnings.getWarnings().size() == 1, "a warning");
        assertEquals(Warning.Kind.STALLED, warnings.getWarnings().get(0).getKind());
        assertSame(r, warnings.getWarnings().get(0).getRecord());

        ThreadingUtil.runOnGUI(() -> r.place(List.of(b), true));
        JUnitUtil.waitFor(() -> warnings.getWarnings().isEmpty(), "found again");
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
