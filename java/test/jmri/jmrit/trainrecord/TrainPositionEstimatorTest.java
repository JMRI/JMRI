package jmri.jmrit.trainrecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jmri.BeanSetting;
import jmri.Block;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.Path;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.TrainRecord;
import jmri.TrainRecord.End;
import jmri.TrainRecord.SpeedCommand;
import jmri.TrainRecordManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.jmrit.trainrecord.TrainPositionEstimator.Momentum;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Where a leading end is estimated to be, from its entry time, the speed
 * commands, a speed table and momentum. The arithmetic is checked by hand in
 * each test; the ramps are straight lines in a speed table that is a straight
 * line, so their distances are exact up to the integration's rounding.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class TrainPositionEstimatorTest {

    private static final long S = 1_000_000_000L; // a second, in nanoseconds
    /** Allowed rounding in the integration, mm: far less than any distance compared. */
    private static final double MM = 0.5;

    /** 400 mm/s at full setting, forward; nothing in reverse. */
    private static final TrainPositionEstimator.SpeedTable FORWARD_ONLY = (s, f) -> f ? 400 * s : 0;
    /** 400 mm/s at full setting, either way. */
    private static final TrainPositionEstimator.SpeedTable BOTH = (s, f) -> 400 * s;

    private Block block;

    /** The loco the tests' commands are for. */
    private static final DccLocoAddress LOCO = new DccLocoAddress(7, false);

    private static SpeedCommand cmd(double seconds, float setting, boolean forward) {
        return new SpeedCommand(LOCO, setting, forward, false, (long) (seconds * S), Instant.EPOCH);
    }

    private static SpeedCommand release(double seconds, float setting, boolean forward) {
        return new SpeedCommand(LOCO, setting, forward, true, (long) (seconds * S), Instant.EPOCH);
    }

    private TrainPositionEstimate estimate(List<SpeedCommand> cs, Momentum m, double fix, double now) {
        return TrainPositionEstimator.estimate(block, null, End.FRONT, (long) (fix * S), cs, BOTH, m, (long) (now * S));
    }

    @Test
    public void testSteadySpeed() {
        // 0.5 is 200 mm/s; entered at 1 s; 2 s later, 400 mm in
        TrainPositionEstimate e = Objects.requireNonNull(estimate(List.of(cmd(0, 0.5f, true)), Momentum.NONE, 1, 3));
        assertEquals(400, e.getMinMm(), MM);
        assertEquals(400, e.getMaxMm(), MM);
        assertFalse(e.isLimitedByBlock());
        assertEquals(200, e.getMinSpeed(), MM);
        assertEquals(200, e.getMaxSpeed(), MM);
        assertSame(block, e.getBlock());
        assertEquals(End.FRONT, e.getEnd());
        assertEquals(3 * S, e.getTimeNanos());
    }

    @Test
    public void testSteadyWithMomentumAgrees() {
        // the speed reached long before it entered: momentum makes no difference
        TrainPositionEstimate e = Objects.requireNonNull(
                estimate(List.of(cmd(0, 0.5f, true)), new Momentum(10, 10), 20, 22));
        assertEquals(400, e.getMinMm(), MM);
        assertEquals(400, e.getMaxMm(), MM);
    }

    @Test
    public void testAcceleratingWithMomentumRunsLess() {
        // stopped, then 0.5 at 1 s, the moment it entered (a creep in); 10 s for the
        // whole range, so 5 s to reach 0.5. At 6 s: with none, 5 s at 200 = 1000;
        // with momentum, 0 to 200 mm/s evenly over 5 s = 500
        TrainPositionEstimate e = Objects.requireNonNull(estimate(
                List.of(cmd(0, 0f, true), cmd(1, 0.5f, true)), new Momentum(10, 0), 1, 6));
        assertEquals(500, e.getMinMm(), MM);
        assertEquals(1000, e.getMaxMm(), MM);
        assertEquals(200, e.getMinSpeed(), MM, "reached");
    }

    @Test
    public void testMidRampSpeedRange() {
        // 2.5 s into the 5 s ramp: the decoder at 0.25 (100 mm/s), commanded 0.5 (200 mm/s)
        TrainPositionEstimate e = Objects.requireNonNull(estimate(
                List.of(cmd(0, 0f, true), cmd(1, 0.5f, true)), new Momentum(10, 0), 1, 3.5));
        assertEquals(100, e.getMinSpeed(), MM);
        assertEquals(200, e.getMaxSpeed(), MM);
        assertEquals(125, e.getMinMm(), MM, "0 to 100 mm/s over 2.5 s");
        assertEquals(500, e.getMaxMm(), MM, "200 mm/s for 2.5 s");
    }

    @Test
    public void testDeceleratingWithMomentumRunsFurther() {
        // 0.5 since 0 s, entered at 1 s, stop commanded at 2 s; 10 s for the whole
        // range down, so 5 s to stop. At 10 s: with none, 1 s at 200 = 200; with
        // momentum, 200 plus 200 to 0 mm/s over 5 s = 200 + 500 = 700
        TrainPositionEstimate e = Objects.requireNonNull(estimate(
                List.of(cmd(0, 0.5f, true), cmd(2, 0f, true)), new Momentum(0, 10), 1, 10));
        assertEquals(200, e.getMinMm(), MM);
        assertEquals(700, e.getMaxMm(), MM);
        assertEquals(0, e.getMaxSpeed(), MM, "stopped either way");
    }

    @Test
    public void testEmergencyStopStopsAtOnce() {
        TrainPositionEstimate e = Objects.requireNonNull(estimate(
                List.of(cmd(0, 0.5f, true), cmd(2, -1f, true)), new Momentum(10, 10), 1, 10));
        assertEquals(200, e.getMinMm(), MM);
        assertEquals(200, e.getMaxMm(), MM, "momentum doesn't apply to an emergency stop");
    }

    @Test
    public void testReleaseLeavesItRunning() {
        TrainPositionEstimate e = Objects.requireNonNull(estimate(
                List.of(cmd(0, 0.5f, true), release(2, 0.5f, true)), Momentum.NONE, 1, 4));
        assertEquals(600, e.getMinMm(), MM, "3 s at 200 mm/s: a release commands nothing");
    }

    @Test
    public void testLimitedByTheBlock() {
        // 9 s at 200 = 1800, but the Block is 1000 long and the next not entered
        TrainPositionEstimate e = Objects.requireNonNull(estimate(List.of(cmd(0, 0.5f, true)), Momentum.NONE, 1, 10));
        assertEquals(1000, e.getMinMm(), MM);
        assertEquals(1000, e.getMaxMm(), MM);
        assertTrue(e.isLimitedByBlock());
    }

    /**
     * A junction Block, J, entered from A, with a turnout leading on to B
     * when closed and to C when thrown; the ways through it have lengths of
     * their own.
     */
    private Block junction(Turnout t, Block a, Block b, Block c) {
        Block j = new Block("IB10", "J");
        j.setLength(1000);
        j.addPath(new Path(a, 0, 0));
        Path toB = new Path(b, 0, 0);
        toB.addSetting(new BeanSetting(t, Turnout.CLOSED));
        j.addPath(toB);
        Path toC = new Path(c, 0, 0);
        toC.addSetting(new BeanSetting(t, Turnout.THROWN));
        j.addPath(toC);
        return j;
    }

    @Test
    public void testLengthOnTheWaySet() {
        Turnout t = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        Block a = new Block("IB11", "A");
        Block b = new Block("IB12", "B");
        Block c = new Block("IB13", "C");
        Block j = junction(t, a, b, c);
        j.setRouteLengthMm(a, c, 1200);

        t.setCommandedState(Turnout.CLOSED);
        assertEquals(1000, TrainPositionEstimator.lengthOn(j, a), 1e-9,
                "set for B, a way with no length of its own: the Block's length");
        j.setRouteLengthMm(a, b, 800);
        assertEquals(800, TrainPositionEstimator.lengthOn(j, a), 1e-9, "set for B: the way to B");
        t.setCommandedState(Turnout.THROWN);
        assertEquals(1200, TrainPositionEstimator.lengthOn(j, a), 1e-9, "set for C: the way to C");
        t.setCommandedState(Turnout.INCONSISTENT);
        assertEquals(1200, TrainPositionEstimator.lengthOn(j, a), 1e-9, "set for neither: the longer way");
        t.setCommandedState(Turnout.CLOSED);
        assertEquals(1200, TrainPositionEstimator.lengthOn(j, null), 1e-9,
                "where it came from isn't known: the longest way through");
        assertEquals(1000, TrainPositionEstimator.lengthOn(j, new Block("IB14", "Elsewhere")), 1e-9,
                "from a Block that isn't a neighbour: the Block's length");
        assertEquals(1000, TrainPositionEstimator.lengthOn(block, null), 1e-9, "no neighbours: the Block's length");
        block.setLength(0);
        assertEquals(0, TrainPositionEstimator.lengthOn(block, null), 1e-9, "no length at all");
    }

    @Test
    public void testLimitedByTheWayOn() {
        // 9 s at 200 = 1800; entered J from A and set for B, a way 800 long
        Turnout t = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        Block a = new Block("IB11", "A");
        Block b = new Block("IB12", "B");
        Block j = junction(t, a, b, new Block("IB13", "C"));
        j.setRouteLengthMm(a, b, 800);
        t.setCommandedState(Turnout.CLOSED);
        TrainPositionEstimate e = Objects.requireNonNull(TrainPositionEstimator.estimate(j, a, End.FRONT, S,
                List.of(cmd(0, 0.5f, true)), BOTH, Momentum.NONE, 10 * S));
        assertEquals(800, e.getMaxMm(), MM);
        assertTrue(e.isLimitedByBlock());
    }

    @Test
    public void testNoLengthNoLimit() {
        block.setLength(0);
        TrainPositionEstimate e = Objects.requireNonNull(estimate(List.of(cmd(0, 0.5f, true)), Momentum.NONE, 1, 10));
        assertEquals(1800, e.getMaxMm(), MM);
        assertFalse(e.isLimitedByBlock());
    }

    @Test
    public void testNoEstimate() {
        assertNull(estimate(List.of(cmd(2, 0.5f, true)), Momentum.NONE, 1, 3), "no command known when it entered");
        assertNull(estimate(List.of(), Momentum.NONE, 1, 3), "no commands at all");
        assertNull(estimate(List.of(cmd(0, 0.5f, true), cmd(2, 0.5f, false)), Momentum.NONE, 1, 3),
                "the direction changed: it may have reversed");
        assertNull(TrainPositionEstimator.estimate(block, null, End.FRONT, S, List.of(cmd(0, 0.5f, false)), FORWARD_ONLY,
                Momentum.NONE, 3 * S), "no speeds in reverse");
        TrainPositionEstimate later = estimate(List.of(cmd(0, 0.5f, true), cmd(5, 0.5f, false)), Momentum.NONE, 1, 3);
        assertEquals(400, Objects.requireNonNull(later).getMaxMm(), MM, "a command after the moment asked about is not used");
    }

    @Test
    public void testOutOfOrderCommandsSorted() {
        // a second source's report arrived after a later one: taken in time order
        TrainPositionEstimate e = Objects.requireNonNull(estimate(
                List.of(cmd(0, 0.5f, true), cmd(3, 0f, true), cmd(2, 0.25f, true)), Momentum.NONE, 1, 4));
        assertEquals(200 + 100, e.getMaxMm(), MM, "1 s at 200, 1 s at 100, then stopped");
    }

    @Test
    public void testMomentumFromCvs() {
        Momentum m = Momentum.fromCvs(10, 20);
        assertEquals(8.96, m.getAccelerationSeconds(), 1e-9);
        assertEquals(17.92, m.getDecelerationSeconds(), 1e-9);
        assertEquals(255 * 0.896, Momentum.fromCvs(300, -1).getAccelerationSeconds(), 1e-9, "kept to 0 to 255");
        assertEquals(0, Momentum.fromCvs(300, -1).getDecelerationSeconds(), 1e-9);
        assertThrows(IllegalArgumentException.class, () -> new Momentum(-1, 0));
    }

    @Test
    public void testMomentumFromRoster() throws IOException {
        File dir = new File(Roster.getDefault().getRosterFilesLocation());
        FileUtil.createDirectory(dir);
        Files.write(new File(dir, "momentum.xml").toPath(), ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<locomotive-config><locomotive id=\"momentum\" fileName=\"momentum.xml\" dccAddress=\"3\">"
                + "<values><CVvalue name=\"1\" value=\"3\"/><CVvalue name=\"3\" value=\"5\"/>"
                + "<CVvalue name=\"4\" value=\"7\"/><CVvalue name=\"23\" value=\"10\"/></values>"
                + "</locomotive></locomotive-config>\n").getBytes(StandardCharsets.UTF_8));
        Momentum m = Momentum.fromRoster(new RosterEntry("momentum.xml"));
        assertEquals(5 * 0.896, m.getAccelerationSeconds(), 1e-9);
        assertEquals(7 * 0.896, m.getDecelerationSeconds(), 1e-9);
        assertSame(Momentum.NONE, Momentum.fromRoster(new RosterEntry("missing.xml")), "no file");
        assertSame(Momentum.NONE, Momentum.fromRoster(new RosterEntry()), "no file name");

        Files.write(new File(dir, "bad.xml").toPath(), ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<locomotive-config><locomotive id=\"bad\" fileName=\"bad.xml\" dccAddress=\"3\">"
                + "<values><CVvalue name=\"3\" value=\"x\"/><CVvalue name=\"4\"/></values>"
                + "</locomotive></locomotive-config>\n").getBytes(StandardCharsets.UTF_8));
        Momentum bad = Momentum.fromRoster(new RosterEntry("bad.xml"));
        assertEquals(0, bad.getAccelerationSeconds(), 1e-9, "a CV value that isn't a number is taken as 0");
        assertEquals(0, bad.getDecelerationSeconds(), 1e-9, "as is one with no value");

        Files.write(new File(dir, "novalues.xml").toPath(), ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<locomotive-config><locomotive id=\"novalues\" fileName=\"novalues.xml\" dccAddress=\"3\"/>"
                + "</locomotive-config>\n").getBytes(StandardCharsets.UTF_8));
        assertSame(Momentum.NONE, Momentum.fromRoster(new RosterEntry("novalues.xml")), "no CVs read yet");

        Files.write(new File(dir, "broken.xml").toPath(), "<locomotive-config><locomotive"
                .getBytes(StandardCharsets.UTF_8));
        assertSame(Momentum.NONE, Momentum.fromRoster(new RosterEntry("broken.xml")), "a file that can't be read");
        jmri.util.JUnitAppender.assertWarnMessageStartsWith("Can't read the momentum CVs from");
    }

    /**
     * From a record, through Blocks with sensors, as the record's watcher
     * follows them: the estimate starts when the front entered its Block.
     */
    @Test
    public void testFromARecord() {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        Block a = new Block("IB1", "A");
        Block b = new Block("IB2", "B");
        a.setLength(1000);
        b.setLength(3000);
        Sensor sa = sm.provideSensor("IS1");
        Sensor sb = sm.provideSensor("IS2");
        a.setSensor("IS1");
        b.setSensor("IS2");
        a.addPath(new Path(b, 0, 0));
        b.addPath(new Path(a, 0, 0));
        InstanceManager.getDefault(jmri.BlockManager.class).register(a);
        InstanceManager.getDefault(jmri.BlockManager.class).register(b);

        RosterEntry entry = new RosterEntry();
        entry.setId("Class 08");
        entry.setDccAddress("7");
        RosterSpeedProfile profile = new RosterSpeedProfile(entry);
        profile.setSpeed(1000, 400, 400); // 400 mm/s at full setting
        profile.setSpeed(500, 200, 200);
        entry.setSpeedProfile(profile);
        RosterEntry second = new RosterEntry();
        second.setId("Class 08 banker");
        second.setDccAddress("8");

        TrainRecordManager manager = InstanceManager.getDefault(TrainRecordManager.class);
        List<TrainRecord> made = new ArrayList<>();
        ThreadingUtil.runOnLayout(() -> {
            sa.setCommandedState(Sensor.ACTIVE);
            TrainRecord r = manager.newRecord("Shunter");
            r.setRosterEntries(List.of(entry, second));
            r.setTrainComponents(List.of(new TrainRecord.TrainComponent("Shunter", 300.0, 20.0, 15.0)));
            r.place(List.of(a), true);
            made.add(r);
        });
        TrainRecord r = made.get(0);
        assertNull(TrainPositionEstimator.estimate(r, Momentum.NONE, System.nanoTime()), "placed, not seen to move");

        // the locos' commands, as a source would report them: the second
        // loco's, stopped, are not the lead loco's and are not used
        long start = System.nanoTime();
        jmri.LocoSpeedSource source = new jmri.LocoSpeedSource() {
            @Override
            public void addLocoSpeedListener(jmri.LocoSpeedSource.Listener listener) {
                listener.speedCommanded(new DccLocoAddress(7, false), 0.5f, true, start);
                listener.speedCommanded(new DccLocoAddress(8, false), 0f, true, start + 1);
            }

            @Override
            public void removeLocoSpeedListener(jmri.LocoSpeedSource.Listener listener) {
            }
        };
        ThreadingUtil.runOnLayout(() -> InstanceManager.store(source, jmri.LocoSpeedSource.class));
        JUnitUtil.waitFor(() -> r.getCommands().size() == 2, "the commands reached the record");
        ThreadingUtil.runOnLayout(() -> sb.setCommandedState(Sensor.ACTIVE));
        JUnitUtil.waitFor(() -> r.getBlocks().size() == 2, "the front entered B");

        long entered = r.getTrail().get(r.getTrail().size() - 1).getTimeNanos();
        TrainPositionEstimate e = Objects.requireNonNull(
                TrainPositionEstimator.estimate(r, Momentum.NONE, entered + 2 * S));
        assertSame(b, e.getBlock());
        assertEquals(End.FRONT, e.getEnd());
        assertEquals(420, e.getMinMm(), 1, "2 s at 200 mm/s, the lead loco's speed, from the moment its wheels "
                + "entered B, and its 20 mm front overhang");
        assertEquals(420, e.getMaxMm(), 1);

        // detected only at its rear, a 100 mm loco propelling the 300 mm train its component gives:
        // the train ahead of the loco is added
        ThreadingUtil.runOnLayout(() -> r.setCustomMarkers(List.of(TrainRecord.DetectionMarker.rear(100.0))));
        TrainPositionEstimate propelled = Objects.requireNonNull(
                TrainPositionEstimator.estimate(r, Momentum.NONE, entered + 2 * S));
        assertEquals(600, propelled.getMinMm(), 1, "400 mm, and the 200 mm of train ahead of the 100 mm loco");

        // with no length for the train, where its front is can't be known
        ThreadingUtil.runOnLayout(() -> r.setTrainComponents(List.of(new TrainRecord.TrainComponent("Shunter", null))));
        assertNull(TrainPositionEstimator.estimate(r, Momentum.NONE, entered + 2 * S), "no length, no front");

        // a lead loco with no speed profile
        ThreadingUtil.runOnLayout(() -> r.setRosterEntries(List.of(second)));
        assertNull(TrainPositionEstimator.estimate(r, Momentum.NONE, entered + 2 * S), "no speed profile");
    }

    @BeforeEach
    public void setUp(@TempDir File tempDir) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(tempDir));
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        block = new Block("IB99", "Platform");
        block.setLength(1000);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
