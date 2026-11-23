package jmri.jmrit.trainrecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jmri.BeanSetting;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Path;
import jmri.SensorManager;
import jmri.TrainRecord;
import jmri.TrainRecordManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.jmrit.trainrecord.TrainTrackingChecks.Finding;
import jmri.jmrit.trainrecord.TrainTrackingChecks.Kind;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The set-up checks for following trains.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class TrainTrackingChecksTest {

    private BlockManager blocks;

    private Block detected(String name, float length) {
        Block b = dark(name, length);
        b.setSensor(InstanceManager.getDefault(SensorManager.class).provideSensor("IS" + name).getSystemName());
        return b;
    }

    private Block dark(String name, float length) {
        Block b = blocks.provideBlock("IB" + name);
        b.setUserName(name);
        b.setLength(length);
        return b;
    }

    private static void join(Block a, Block b) {
        a.addPath(new Path(b, 0, 0));
        b.addPath(new Path(a, 0, 0));
    }

    private List<Finding> run(List<TrainRecord> records) {
        return ThreadingUtil.runOnGUIwithReturn(
                () -> TrainTrackingChecks.run(new ArrayList<>(blocks.getNamedBeanSet()), records));
    }

    private static List<String> messages(List<Finding> found) {
        List<String> m = new ArrayList<>();
        for (Finding f : found) {
            m.add(f.getMessage());
        }
        return m;
    }

    @Test
    public void testWellSetUpLayoutHasNoFindings() {
        Block a = detected("A", 1000);
        Block d = dark("D", 500);
        Block b = detected("B", 1000);
        join(a, d);
        join(d, b);
        assertTrue(run(List.of()).isEmpty());
    }

    @Test
    public void testTrains() {
        TrainRecordManager manager = InstanceManager.getDefault(TrainRecordManager.class);
        List<TrainRecord> records = ThreadingUtil.runOnGUIwithReturn(() -> {
            TrainRecord noLength = manager.newRecord("Up goods");
            noLength.setLength(TrainRecord.LengthSource.ENTERED, 900.0);
            RosterEntry withProfile = new RosterEntry();
            withProfile.setId("Class 37");
            RosterSpeedProfile p = new RosterSpeedProfile(withProfile);
            p.setSpeed(1000, 400, 400);
            withProfile.setSpeedProfile(p);
            noLength.insertFirstRosterEntry(withProfile);

            TrainRecord bare = manager.newRecord("Shunt");
            TrainRecord noProfile = manager.newRecord("Down goods");
            noProfile.setLength(TrainRecord.LengthSource.ENTERED, 600.0);
            RosterEntry withoutProfile = new RosterEntry();
            withoutProfile.setId("Class 08");
            RosterEntry secondWithProfile = new RosterEntry();
            secondWithProfile.setId("Class 08 banker");
            RosterSpeedProfile p2 = new RosterSpeedProfile(secondWithProfile);
            p2.setSpeed(1000, 400, 400);
            secondWithProfile.setSpeedProfile(p2);
            // the lead loco has no profile: a second loco's doesn't stand in for it
            noProfile.setRosterEntries(List.of(withoutProfile, secondWithProfile));
            TrainRecord removed = manager.newRecord("Gone");
            manager.remove(removed);
            return List.of(noLength, bare, noProfile, removed);
        });
        List<Finding> found = run(records);
        assertEquals(List.of(
                "Train \"Shunt\" has no length from any source, so how many Blocks it takes up isn't known. If "
                + "Dispatcher runs it, enter its Max Train Length.",
                "Train \"Down goods\" has no speed profile in its lead loco's roster entry \"Class 08\", so where it "
                + "is between detectors can't be estimated. Measure one with the speed profiling tool.",
                "Train \"Shunt\" has no loco with a roster entry, so its speed can't be followed and where it is "
                + "between detectors can't be estimated."), messages(found));
        assertSame(records.get(1), found.get(0).getSubject());
        assertEquals(Kind.TRAIN_NO_SPEED_PROFILE, found.get(1).getKind());
    }

    @Test
    public void testBlocks() {
        Block a = detected("A", 0); // no length
        Block d = dark("D", 0); // no detector, no length
        Block b = detected("B", 1000);
        Block lonely = detected("Lonely", 300); // no paths
        dark("Unused", 0); // no paths, no detector: not on the layout
        join(a, d);
        join(d, b);
        Block c = detected("C", 1000);
        b.addPath(new Path(c, 0, 0)); // no path back
        join(c, detected("E", 1000));
        List<Finding> found = run(List.of());
        assertEquals(List.of(
                "Block \"A\" has no length, so a train's position in it can't be worked out.",
                "Block \"D\" has no detector and no length: everything known about a train in it depends on its length.",
                "Block \"Lonely\" has a detector but no paths to other Blocks, so a train can't be followed into or out "
                + "of it.",
                "Block \"B\" has a path to Block \"C\", but \"C\" has none back to \"B\"."), messages(found));
        assertEquals(List.of(c), found.get(3).getRelated());
        assertSame(lonely, found.get(2).getSubject());
    }

    @Test
    public void testSharedSensor() {
        Block a = detected("A", 1000);
        Block b = dark("B", 1000);
        b.setSensor("ISA");
        join(a, b);
        List<Finding> found = run(List.of());
        assertEquals(1, found.size());
        assertEquals(Kind.SENSOR_SHARED, found.get(0).getKind());
        assertEquals("Sensor \"ISA\" is the detector of more than one Block: A, B.", found.get(0).getMessage());
        assertEquals(List.of(a, b), found.get(0).getRelated());
    }

    @Test
    public void testDarkBlockWithTurnoutWithoutFeedback() {
        Block a = detected("A", 1000);
        Block d = dark("D", 500);
        Block b = detected("B", 1000);
        Block c = detected("C", 1000);
        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
        Turnout blind = tm.provideTurnout("IT1");
        blind.setUserName("Points 1");
        blind.setFeedbackMode(Turnout.DIRECT);
        Turnout seen = tm.provideTurnout("IT2");
        seen.setFeedbackMode(Turnout.ONESENSOR);
        join(a, d);
        Path db = new Path(b, 0, 0);
        db.addSetting(new BeanSetting(blind, Turnout.CLOSED));
        db.addSetting(new BeanSetting(seen, Turnout.CLOSED));
        d.addPath(db);
        b.addPath(new Path(d, 0, 0));
        Path dc = new Path(c, 0, 0);
        dc.addSetting(new BeanSetting(blind, Turnout.THROWN));
        d.addPath(dc);
        c.addPath(new Path(d, 0, 0));
        List<Finding> found = run(List.of());
        assertEquals(List.of("Block \"D\" has no detector, and its routes depend on turnout \"Points 1\", which has no "
                + "feedback: the way a train takes through it is only what was commanded."), messages(found));
        assertEquals(List.of(blind), found.get(0).getRelated());
    }

    @Test
    public void testParallelDarkBlocks() {
        Block a = detected("A", 1000);
        Block d1 = dark("D1", 500);
        Block d2 = dark("D2", 500);
        Block b = detected("B", 1000);
        join(a, d1);
        join(d1, b);
        join(a, d2);
        join(d2, b);
        List<Finding> found = run(List.of());
        assertEquals(List.of("Blocks \"A\" and \"B\" are joined through Blocks with no detector by more than one way, "
                + "and no turnout chooses between them, so which way a train went can't be told."), messages(found));
    }

    @Test
    public void testDarkWaysRejoiningBeforeTheDetector() {
        Block a = detected("A", 1000);
        Block d1 = dark("D1", 500);
        Block d2 = dark("D2", 500);
        Block d3 = dark("D3", 500); // both ways meet here, still in the dark
        Block b = detected("B", 1000);
        join(a, d1);
        join(a, d2);
        join(d1, d3);
        join(d2, d3);
        join(d3, b);
        List<Finding> found = run(List.of());
        assertEquals(1, found.size(), messages(found).toString());
        assertEquals(Kind.DARK_RUN_SEVERAL_WAYS, found.get(0).getKind());
        assertEquals(a, found.get(0).getSubject(), "from one detected Block");
        assertEquals(List.of(b), found.get(0).getRelated(), "to the other");
    }

    @Test
    public void testLongerDarkWayNotReported() {
        Block a = detected("A", 1000);
        Block d1 = dark("D1", 500);
        Block d2 = dark("D2", 500);
        Block d3 = dark("D3", 500);
        Block b = detected("B", 1000);
        join(a, d1);
        join(d1, b);
        join(a, d2); // a longer way: the shortest is the one taken
        join(d2, d3);
        join(d3, b);
        assertTrue(run(List.of()).isEmpty());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        blocks = InstanceManager.getDefault(BlockManager.class);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
