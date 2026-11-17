package jmri.jmrit.trainrecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Path;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.TrainRecord;
import jmri.TrainRecord.Reason;
import jmri.TrainRecordManager;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.jmrit.trainrecord.TrainTrackingMeasurements.Measurement;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * What train tracking measures as trains run.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class TrainTrackingMeasurementsTest {

    private static final long S = 1_000_000_000L;
    private SensorManager sm;

    private Block block(String name, int number, float length) {
        Block b = new Block("IB" + number, name);
        b.setLength(length);
        b.setSensor("IS" + number);
        sm.provideSensor("IS" + number);
        InstanceManager.getDefault(BlockManager.class).register(b);
        return b;
    }

    private static void join(Block a, Block b) {
        a.addPath(new Path(b, 0, 0));
        b.addPath(new Path(a, 0, 0));
    }

    @Test
    public void testCrossingsAreMeasured() {
        Block a = block("A", 1, 1000);
        Block b = block("B", 2, 400);
        Block c = block("C", 3, 600);
        join(a, b);
        join(b, c);
        TrainTrackingMeasurements measurements = InstanceManager.getDefault(TrainTrackingMeasurements.class);
        TrainRecordManager manager = InstanceManager.getDefault(TrainRecordManager.class);
        TrainRecord r = ThreadingUtil.runOnGUIwithReturn(() -> {
            sm.provideSensor("IS1").setCommandedState(Sensor.ACTIVE);
            TrainRecord t = manager.newRecord("T1");
            t.place(List.of(a), true);
            return t;
        });
        ThreadingUtil.runOnLayout(() -> sm.provideSensor("IS2").setCommandedState(Sensor.ACTIVE));
        JUnitUtil.waitFor(() -> r.getBlocks().size() == 2, "the front entered B");
        assertTrue(measurements.getMeasurements().isEmpty(), "A was not entered at a boundary: placed there");

        ThreadingUtil.runOnLayout(() -> sm.provideSensor("IS3").setCommandedState(Sensor.ACTIVE));
        JUnitUtil.waitFor(() -> r.getBlocks().size() == 3, "the front entered C");
        List<Measurement> list = measurements.getMeasurements();
        assertEquals(1, list.size());
        Measurement m = list.get(0);
        assertSame(b, m.getBlock(), "B was crossed");
        assertSame(a, m.getFrom(), "entered from A");
        assertSame(c, m.getTo(), "left for C");
        assertEquals(1, m.getCrossings());
        assertTrue(m.getAverageSpeed() > 0);
        assertEquals(0, m.getCompared(), "the train has no speed profile");
        assertEquals(List.of(m), measurements.getMeasurements(b));
        assertTrue(measurements.getMeasurements(a).isEmpty());
    }

    @Test
    public void testAgainstProfile() {
        Block a = block("A", 1, 1000);
        Block b = block("B", 2, 400);
        Block c = block("C", 3, 600);
        RosterEntry entry = new RosterEntry();
        entry.setId("Class 08");
        entry.setDccAddress("7");
        RosterSpeedProfile profile = new RosterSpeedProfile(entry);
        profile.setSpeed(1000, 400, 400);
        profile.setSpeed(500, 200, 200);
        entry.setSpeedProfile(profile);
        TrainRecordManager manager = InstanceManager.getDefault(TrainRecordManager.class);
        TrainRecord r = ThreadingUtil.runOnGUIwithReturn(() -> {
            TrainRecord t = manager.newRecord("Shunter");
            t.setRosterEntries(List.of(entry));
            return t;
        });
        long start = System.nanoTime();
        jmri.LocoSpeedSource source = new jmri.LocoSpeedSource() {
            @Override
            public void addLocoSpeedListener(jmri.LocoSpeedSource.Listener listener) {
                listener.speedCommanded(new jmri.DccLocoAddress(7, false), 0.5f, true, start);
            }

            @Override
            public void removeLocoSpeedListener(jmri.LocoSpeedSource.Listener listener) {
            }
        };
        ThreadingUtil.runOnLayout(() -> InstanceManager.store(source, jmri.LocoSpeedSource.class));
        JUnitUtil.waitFor(() -> r.getLastCommand() != null, "the command reached the record");
        TrainTrackingMeasurements measurements = InstanceManager.getDefault(TrainTrackingMeasurements.class);

        // the profile says 200 mm/s: 400 mm in 2.5 s is 160 mm/s, 80%
        Measurement m = ThreadingUtil.runOnGUIwithReturn(
                () -> measurements.measure(r, b, a, c, start + S, start + S + 5 * S / 2));
        assertNotNull(m);
        assertEquals(160, m.getAverageSpeed(), 0.001);
        assertEquals(1, m.getCompared());
        assertEquals(0.8, m.getAgainstProfile(), 0.001);
        assertEquals("80% (1 of 1)", jmri.jmrit.trainrecord.swing.MeasurementsFrame.againstProfile(m));

        // in 2 s: 200 mm/s, 100%; the average of the two is 90%
        ThreadingUtil.runOnLayout(() -> measurements.measure(r, b, a, c, start + 5 * S, start + 7 * S));
        assertEquals(2, m.getCrossings());
        assertEquals(180, m.getAverageSpeed(), 0.001);
        assertEquals(0.9, m.getAgainstProfile(), 0.001);

        // before the command: nothing to compare with
        ThreadingUtil.runOnLayout(() -> measurements.measure(r, b, a, c, start - 3 * S, start - S));
        assertEquals(3, m.getCrossings());
        assertEquals(2, m.getCompared());

        assertNull(TrainTrackingMeasurements.profileSpeed(r, start - S, start + S), "the command came during it");
        assertNull(ThreadingUtil.runOnGUIwithReturn(() -> measurements.measure(r, b, a, c, start, start)),
                "no time taken");
    }

    @Test
    public void testOnlyTheLeadLocosCommandsAreCompared() {
        Block a = block("A", 1, 1000);
        Block b = block("B", 2, 400);
        Block c = block("C", 3, 600);
        RosterEntry lead = new RosterEntry();
        lead.setId("Class 08");
        lead.setDccAddress("7");
        RosterSpeedProfile profile = new RosterSpeedProfile(lead);
        profile.setSpeed(500, 200, 200);
        lead.setSpeedProfile(profile);
        RosterEntry second = new RosterEntry();
        second.setId("Class 03");
        second.setDccAddress("8");
        TrainRecordManager manager = InstanceManager.getDefault(TrainRecordManager.class);
        TrainRecord r = ThreadingUtil.runOnGUIwithReturn(() -> {
            TrainRecord t = manager.newRecord("Pair");
            t.setRosterEntries(List.of(lead, second));
            return t;
        });
        long start = System.nanoTime();
        jmri.LocoSpeedSource source = new jmri.LocoSpeedSource() {
            @Override
            public void addLocoSpeedListener(jmri.LocoSpeedSource.Listener listener) {
                listener.speedCommanded(new jmri.DccLocoAddress(7, false), 0.5f, true, start);
                // the second loco's command changes during the first crossing below
                listener.speedCommanded(new jmri.DccLocoAddress(8, false), 0.5f, true, start + 2 * S);
                // and the lead's own during the second
                listener.speedCommanded(new jmri.DccLocoAddress(7, false), 0.6f, true, start + 6 * S);
            }

            @Override
            public void removeLocoSpeedListener(jmri.LocoSpeedSource.Listener listener) {
            }
        };
        ThreadingUtil.runOnLayout(() -> InstanceManager.store(source, jmri.LocoSpeedSource.class));
        JUnitUtil.waitFor(() -> r.getCommands().size() == 3, "the commands reached the record");
        TrainTrackingMeasurements measurements = InstanceManager.getDefault(TrainTrackingMeasurements.class);

        Measurement m = ThreadingUtil.runOnGUIwithReturn(
                () -> measurements.measure(r, b, a, c, start + S, start + 3 * S));
        assertNotNull(m);
        assertEquals(1, m.getCompared(), "the lead's command didn't change: the second loco's is not the train's speed");
        assertEquals(1.0, m.getAgainstProfile(), 0.001);

        ThreadingUtil.runOnLayout(() -> measurements.measure(r, b, a, c, start + 5 * S, start + 7 * S));
        assertEquals(2, m.getCrossings());
        assertEquals(1, m.getCompared(), "the lead's command changed on the way");
    }

    @Test
    public void testAWayThroughIsMeasuredByItsRouteLength() {
        Block a = block("A", 1, 1000);
        Block b = block("B", 2, 400);
        Block c = block("C", 3, 600);
        Block d = block("D", 4, 600);
        b.setRouteLengthMm(a, d, 900); // the way to D is longer than the Block's own length
        TrainTrackingMeasurements measurements = InstanceManager.getDefault(TrainTrackingMeasurements.class);
        TrainRecord r = InstanceManager.getDefault(TrainRecordManager.class).newRecord("T1");
        long start = System.nanoTime();

        Measurement toC = ThreadingUtil.runOnGUIwithReturn(() -> measurements.measure(r, b, a, c, start, start + 2 * S));
        Measurement toD = ThreadingUtil.runOnGUIwithReturn(() -> measurements.measure(r, b, a, d, start, start + 2 * S));
        assertNotNull(toC);
        assertNotNull(toD);
        assertEquals(200, toC.getAverageSpeed(), 0.001, "no route length set: the Block's length, 400 mm in 2 s");
        assertEquals(450, toD.getAverageSpeed(), 0.001, "its route length, 900 mm in 2 s");
    }

    @Test
    public void testOnlyDetectorsAreMeasuredFrom() {
        Block a = block("A", 1, 1000);
        Block b = block("B", 2, 400);
        Block c = block("C", 3, 600);
        Block dark = new Block("IB4", "Dark"); // no occupancy sensor
        dark.setLength(500);
        InstanceManager.getDefault(BlockManager.class).register(dark);
        TrainTrackingMeasurements measurements = InstanceManager.getDefault(TrainTrackingMeasurements.class);
        TrainRecord r = InstanceManager.getDefault(TrainRecordManager.class).newRecord("T1");
        long start = System.nanoTime();

        assertTrue(TrainTrackingDetectors.isSimulated(), "no connections: no layout hardware");
        assertTrue(TrainTrackingDetectors.isDetector(b), "simulated trains' detectors are internal sensors");
        assertTrue(!TrainTrackingDetectors.isDetector(dark), "no sensor, no detector");
        assertNull(ThreadingUtil.runOnGUIwithReturn(() -> measurements.measure(r, dark, a, c, start, start + S)),
                "when it entered a Block with no detector isn't known");
        assertNull(ThreadingUtil.runOnGUIwithReturn(() -> measurements.measure(r, b, a, dark, start, start + S)),
                "nor when it left for one");

        // a layout with hardware: its internal sensors are set by logic, not by trains
        jmri.jmrix.ConnectionConfig hardware = org.mockito.Mockito.mock(jmri.jmrix.ConnectionConfig.class);
        org.mockito.Mockito.when(hardware.isSimulator()).thenReturn(false);
        InstanceManager.getDefault(jmri.jmrix.ConnectionConfigManager.class).add(hardware);
        assertTrue(!TrainTrackingDetectors.isSimulated());
        assertTrue(!TrainTrackingDetectors.isDetector(b), "an internal sensor has no hardware behind it");
        assertNull(ThreadingUtil.runOnGUIwithReturn(() -> measurements.measure(r, b, a, c, start, start + S)));

        // the train is still followed through such Blocks, with nothing counted
        join(a, b);
        join(b, c);
        ThreadingUtil.runOnLayout(() -> {
            sm.provideSensor("IS1").setCommandedState(Sensor.ACTIVE);
            r.place(List.of(a), true);
        });
        ThreadingUtil.runOnLayout(() -> sm.provideSensor("IS2").setCommandedState(Sensor.ACTIVE));
        JUnitUtil.waitFor(() -> r.getBlocks().size() == 2, "the front entered B");
        ThreadingUtil.runOnLayout(() -> sm.provideSensor("IS3").setCommandedState(Sensor.ACTIVE));
        JUnitUtil.waitFor(() -> r.getBlocks().size() == 3, "the front entered C");
        ThreadingUtil.runOnLayout(() -> sm.provideSensor("IS2").setCommandedState(Sensor.INACTIVE));
        JUnitUtil.waitFor(() -> r.getHistory().stream().anyMatch(
                x -> x.getRule() == TrainRecord.Reason.Rule.KEPT_AFTER_DROPOUT), "B went empty under the train");
        assertTrue(measurements.getMeasurements().isEmpty(), "no crossing measured");
        assertEquals(0, measurements.getDropOuts(b), "no drop-out counted");
        assertTrue(measurements.getDropOutBlocks().isEmpty());
    }

    @Test
    public void testDropOuts() {
        Block a = block("A", 1, 1000);
        TrainTrackingMeasurements measurements = InstanceManager.getDefault(TrainTrackingMeasurements.class);
        TrainRecordManager manager = InstanceManager.getDefault(TrainRecordManager.class);
        ThreadingUtil.runOnLayout(() -> {
            sm.provideSensor("IS1").setCommandedState(Sensor.ACTIVE);
            manager.newRecord("T1").place(List.of(a), false);
        });
        assertEquals(0, measurements.getDropOuts(a));
        ThreadingUtil.runOnLayout(() -> sm.provideSensor("IS1").setCommandedState(Sensor.INACTIVE));
        JUnitUtil.waitFor(() -> measurements.getDropOuts(a) == 1, "a drop-out");
        assertEquals(List.of(a), measurements.getDropOutBlocks());
    }

    @Test
    public void testDropOutsBesideADarkBlock() {
        Block a = block("A", 1, 1000);
        Block b = block("B", 2, 400);
        TrainTrackingMeasurements measurements = InstanceManager.getDefault(TrainTrackingMeasurements.class);
        TrainRecord r = InstanceManager.getDefault(TrainRecordManager.class).newRecord("T1");

        // Where a train could have gone on into a Block with no detector, its record's history says
        // "not seen" when its Block goes empty, not "kept after a drop-out": given here as the history gives them.
        ThreadingUtil.runOnLayout(() -> measurements.reason(r, Reason.of(Reason.Rule.NOT_SEEN, a)));
        assertEquals(0, measurements.getDropOuts(a), "it may have gone on into the Block with no detector");

        ThreadingUtil.runOnLayout(() -> measurements.reason(r, Reason.of(Reason.Rule.SEEN_AGAIN, b)));
        assertEquals(0, measurements.getDropOuts(a), "seen in another Block: it had gone on");
        assertEquals(0, measurements.getDropOuts(b));

        ThreadingUtil.runOnLayout(() -> measurements.reason(r, Reason.of(Reason.Rule.NOT_SEEN, a)));
        ThreadingUtil.runOnLayout(() -> measurements.reason(r, Reason.of(Reason.Rule.SEEN_AGAIN, a)));
        assertEquals(1, measurements.getDropOuts(a), "seen again in A, so it was a drop-out");
        assertEquals(List.of(a), measurements.getDropOutBlocks());
    }

    @Test
    public void testATrainBesideADarkBlockIsADropOutOnlyWhenSeenAgain() {
        Block a = block("A", 1, 1000);
        Block d = new Block("IB2", "D"); // no detector
        InstanceManager.getDefault(BlockManager.class).register(d);
        join(a, d);
        TrainTrackingMeasurements measurements = InstanceManager.getDefault(TrainTrackingMeasurements.class);
        TrainRecordManager manager = InstanceManager.getDefault(TrainRecordManager.class);
        TrainRecord r = manager.newRecord("T1");
        ThreadingUtil.runOnLayout(() -> {
            sm.provideSensor("IS1").setCommandedState(Sensor.ACTIVE);
            r.place(List.of(a), false);
        });
        ThreadingUtil.runOnLayout(() -> sm.provideSensor("IS1").setCommandedState(Sensor.INACTIVE));
        JUnitUtil.waitFor(() -> r.getConfidence() == TrainRecord.Confidence.ESTIMATED, "not seen");
        assertEquals(0, measurements.getDropOuts(a), "it may have gone on into D");

        ThreadingUtil.runOnLayout(() -> sm.provideSensor("IS1").setCommandedState(Sensor.ACTIVE));
        JUnitUtil.waitFor(() -> measurements.getDropOuts(a) == 1, "seen again in A, so it was a drop-out");
        assertEquals(List.of(a), measurements.getDropOutBlocks());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        // no connections, so no layout hardware: the internal sensors are simulated trains' detectors
        JUnitUtil.initConnectionConfigManager();
        sm = InstanceManager.getDefault(SensorManager.class);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
