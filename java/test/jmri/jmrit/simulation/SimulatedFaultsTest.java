package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jmri.BeanSetting;
import jmri.Block;
import jmri.BlockManager;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.Path;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SpeedStepMode;
import jmri.Turnout;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Tests of the faults a test can set, on plain Blocks with internal sensors,
 * run step by step on a manual clock, so every time asserted is one the mover
 * worked out. Times are compared to within 1 microsecond, the margin for
 * rounding a crossing time to whole nanoseconds. The answers are worked by
 * hand: a loco at step 100 runs at 100 mm/s, and each Block of the line is
 * 450 mm.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class SimulatedFaultsTest {

    private static final double TIME_MARGIN = 1e-6;

    private SimulatedLayout layout;
    private InMemorySpeedSource throttle;
    private final List<long[]> changes = new ArrayList<>(); // {sensor index, state, time}
    private final List<Sensor> sensors = new ArrayList<>();
    private long t0;

    /** A Block with an internal sensor. */
    private Block block(String name, double length) {
        Block b = InstanceManager.getDefault(BlockManager.class).provideBlock("IB" + name);
        b.setUserName(name);
        b.setLength((float) length);
        Sensor s = InstanceManager.getDefault(SensorManager.class).provideSensor("IS" + name);
        try {
            s.setKnownState(Sensor.INACTIVE);
        } catch (jmri.JmriException e) {
            throw new IllegalStateException(e);
        }
        b.setSensor(s.getSystemName());
        int index = sensors.size();
        sensors.add(s);
        s.addRawStateListener((sensor, oldState, newState, timeNanos) -> changes.add(new long[]{index, newState, timeNanos}));
        return b;
    }

    private static void join(Block a, Block b) {
        a.addPath(new Path(b, 0, 0));
        b.addPath(new Path(a, 0, 0));
    }

    /** A - B - C - D, each 450 mm. */
    private Block[] line() {
        Block[] b = {block("A", 450), block("B", 450), block("C", 450), block("D", 450)};
        join(b[0], b[1]);
        join(b[1], b[2]);
        join(b[2], b[3]);
        return b;
    }

    private static VirtualTrain train(String name, int address, double length, Detection d) {
        return VirtualTrain.builder(name)
                .loco(SimulatedLoco.builder(new DccLocoAddress(address, false)).length(100).fullSpeed(126).build())
                .length(length).detection(d).build();
    }

    private void speed(int address, int step, boolean forward) {
        throttle.setSpeed(new DccLocoAddress(address, false), step, SpeedStepMode.NMRA_DCC_128, forward,
                layout.now());
    }

    private void flush() {
        ThreadingUtil.runOnGUI(() -> {
        });
    }

    /** Times, in seconds after t0, of a Block's sensor's changes to a state. */
    private List<Double> times(Block b, int state) {
        flush();
        int index = sensors.indexOf(b.getSensor());
        List<Double> list = new ArrayList<>();
        for (long[] c : changes) {
            if (c[0] == index && c[1] == state) {
                list.add((c[2] - t0) / 1e9);
            }
        }
        return list;
    }

    private static void assertTimes(List<Double> expected, List<Double> actual, String what) {
        assertEquals(expected.size(), actual.size(), what + ": " + actual);
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i), TIME_MARGIN, what + " " + i);
        }
    }

    private SimulationTruth.TrainTruth truthOf(VirtualTrain t) {
        return Objects.requireNonNull(layout.getTruth().getTrain(t), t.getName() + " placed");
    }

    @Test
    public void testNoFaultUnlessSet() {
        Block[] b = line();
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        layout.place(t, b[1], b[0], 0.0);
        t0 = layout.now();
        speed(3, 100, true);
        layout.runFor(5.0);
        assertTimes(List.of(), times(b[1], Sensor.INACTIVE), "B stays occupied");
        assertTrue(layout.getTruth().getFaults().isEmpty());
        assertTrue(layout.getTruth().getDropOuts().isEmpty());
        assertFalse(truthOf(t).isStalled());
        assertFalse(truthOf(t).isDerailed());
    }

    @Test
    public void testDropOutWhileTheFrontIsInTheStretch() {
        Block[] b = line();
        layout.getFaults().dropOut(b[1], b[0], 100, 200);
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        layout.place(t, b[1], b[0], 0.0);
        t0 = layout.now();
        speed(3, 100, true);
        layout.runFor(5.0);
        // the front is 100 to 200 mm into B from 1.0 to 2.0 s; B's only train, so B shows empty then
        assertTimes(List.of(1.0), times(b[1], Sensor.INACTIVE), "B drops out as the front reaches the stretch");
        assertTimes(List.of(0.0, 2.0), times(b[1], Sensor.ACTIVE), "seen when placed, and again as the front leaves the stretch");
        assertTimes(List.of(2.0), times(b[0], Sensor.INACTIVE), "A clears as usual");
        assertTimes(List.of(4.5), times(b[2], Sensor.ACTIVE), "C as usual");
        List<SimulationTruth.DropOut> d = layout.getTruth().getDropOuts();
        assertEquals(1, d.size());
        assertEquals(b[1], d.get(0).getBlock());
        assertEquals(t, d.get(0).getTrain());
        assertEquals(1.0, (d.get(0).getStartNanos() - t0) / 1e9, TIME_MARGIN);
        assertEquals(2.0, (d.get(0).getEndNanos() - t0) / 1e9, TIME_MARGIN);
        assertEquals(List.of("drop-out in B from 100.0 to 200.0 mm from A"), layout.getTruth().getFaults());
    }

    @Test
    public void testDropOutMeasuredFromTheOtherEnd() {
        Block[] b = line();
        layout.getFaults().dropOut(b[1], b[2], 100, 200); // 250 to 350 mm from A's end
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        layout.place(t, b[1], b[0], 0.0);
        t0 = layout.now();
        speed(3, 100, true);
        layout.runFor(4.0);
        assertTimes(List.of(2.5), times(b[1], Sensor.INACTIVE), "the stretch is at the far end of B");
        assertTimes(List.of(0.0, 3.5), times(b[1], Sensor.ACTIVE), "seen when placed, and again");
    }

    @Test
    public void testNoDropOutWhileAnotherTrainIsSeen() {
        Block[] b = line();
        layout.getFaults().dropOut(b[1], b[0], 100, 200);
        VirtualTrain u = train("U", 4, 100, Detection.entireTrain());
        layout.place(u, b[1], b[0], 400.0); // standing at the far end of B
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        layout.place(t, b[1], b[0], 0.0);
        t0 = layout.now();
        speed(3, 100, true);
        layout.runFor(2.5);
        speed(3, 0, true);
        layout.runFor(1.0);
        assertTimes(List.of(), times(b[1], Sensor.INACTIVE), "U is still seen in B");
        assertTrue(layout.getTruth().getDropOuts().isEmpty());
        assertTrue(layout.getTruth().getCollisions().isEmpty());
    }

    @Test
    public void testRandomDropOutsAreTheSameForTheSameSeed() {
        List<List<Double>> runs = new ArrayList<>();
        Block[] b = line();
        for (int run = 0; run < 2; run++) {
            layout.getFaults().dropOutAtRandom(42L, 1.0, 50);
            VirtualTrain t = train("T" + run, 3, 200, Detection.entireTrain());
            layout.place(t, b[1], b[0], 0.0);
            t0 = layout.now();
            speed(3, 100, true);
            layout.runFor(8.0);
            speed(3, 0, true);
            List<Double> seen = new ArrayList<>();
            for (SimulationTruth.DropOut d : layout.getTruth().getDropOuts()) {
                seen.add((d.getStartNanos() - t0) / 1e9);
                if (d.getEndNanos() != 0) { // one may still be going on when the train stops
                    assertEquals(0.5, (d.getEndNanos() - d.getStartNanos()) / 1e9, TIME_MARGIN, "50 mm at 100 mm/s");
                }
            }
            runs.add(seen);
            layout.dispose();
            changes.clear();
            sensors.clear();
            layout = new SimulatedLayout(true);
            layout.addSpeedSource(throttle);
        }
        assertTrue(runs.get(0).size() >= 2, "a drop-out in each Block entered, with probability 1: " + runs.get(0));
        assertTimes(runs.get(0), runs.get(1), "the same seed, the same drop-outs");
    }

    @Test
    public void testPhantom() {
        Block[] b = line();
        assertThrows(IllegalStateException.class, () -> layout.getFaults().phantom(b[3]), "needs a train placed");
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        layout.place(t, b[1], b[0], 0.0);
        t0 = layout.now();
        layout.getFaults().phantom(b[3]);
        assertTimes(List.of(0.0), times(b[3], Sensor.ACTIVE), "D shows occupied with no train");
        assertEquals(List.of("phantom occupancy in D"), layout.getTruth().getFaults());
        layout.getFaults().clearPhantom(b[3]);
        assertTimes(List.of(0.0), times(b[3], Sensor.INACTIVE), "and empty again");
        layout.getFaults().phantom(b[3]);
        layout.remove(t);
        flush();
        assertEquals(Sensor.INACTIVE, sensors.get(3).getKnownState(), "cleared when the last train is removed");
        assertTrue(layout.getTruth().getFaults().isEmpty());
    }

    @Test
    public void testSwappedSensors() {
        Block[] b = line();
        layout.getFaults().swapSensors(b[1], b[2]);
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        layout.place(t, b[1], b[0], 0.0);
        t0 = layout.now();
        assertThrows(IllegalStateException.class, () -> layout.getFaults().swapSensors(b[0], b[3]),
                "only before a train is placed");
        speed(3, 100, true);
        layout.runFor(5.0);
        assertTimes(List.of(0.0), times(b[2], Sensor.ACTIVE), "B's occupancy shown on C's sensor");
        assertTimes(List.of(4.5), times(b[1], Sensor.ACTIVE), "C's on B's");
        assertEquals(List.of(b[2], b[1]), truthOf(t).getBlocks(), "the train moves as before");
    }

    @Test
    public void testTrueLengthDiffers() {
        Block[] b = line();
        layout.getFaults().setTrueLength(b[1], 300);
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        layout.place(t, b[1], b[0], 0.0);
        t0 = layout.now();
        assertThrows(IllegalStateException.class, () -> layout.getFaults().setTrueLength(b[2], 300));
        speed(3, 100, true);
        layout.runFor(4.0);
        assertEquals(450f, b[1].getLengthMm(), "JMRI still has 450 mm");
        assertTimes(List.of(3.0), times(b[2], Sensor.ACTIVE), "C reached after 300 mm, not 450");
    }

    @Test
    public void testStallAndClear() {
        Block[] b = line();
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        layout.place(t, b[1], b[0], 0.0);
        speed(3, 100, true);
        layout.runFor(1.0);
        layout.getFaults().stall(t);
        layout.runFor(2.0);
        assertTrue(truthOf(t).isStalled());
        assertEquals(100.0, truthOf(t).getFrontPositionMm(), 1e-6, "stopped at once where it was");
        assertEquals(0.0, truthOf(t).getSpeedMmPerSecond());
        assertEquals("The train has stalled.", truthOf(t).getHeldReason(), "held by the stall while its loco drives it");
        JUnitAppender.assertWarnMessage("Simulated train \"T\" has stopped: The train has stalled.");
        speed(3, 0, true);
        layout.runFor(1.0);
        assertNull(truthOf(t).getHeldReason(), "not held once its loco is stopped");
        assertTrue(truthOf(t).isStalled(), "though still stalled");
        speed(3, 100, true);
        layout.getFaults().clearStall(t);
        layout.runFor(1.0);
        assertFalse(truthOf(t).isStalled());
        assertEquals(200.0, truthOf(t).getFrontPositionMm(), 1e-6, "obeys its loco again");
        assertEquals(List.of("train T stalled"), layout.getTruth().getFaults());
    }

    @Test
    public void testStallAtAPoint() {
        Block[] b = line();
        layout.getFaults().stallAt(b[1], b[0], 150);
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        layout.place(t, b[1], b[0], 0.0);
        speed(3, 100, true);
        layout.runFor(3.0);
        assertTrue(truthOf(t).isStalled());
        assertEquals(150.0, truthOf(t).getFrontPositionMm(), 1e-6, "stopped as its front reached the point");
        assertEquals("The train has stalled in \"B\".", truthOf(t).getHeldReason());
        JUnitAppender.assertWarnMessage("Simulated train \"T\" has stopped: The train has stalled in \"B\".");
        layout.getFaults().clearStall(t);
        layout.runFor(1.0);
        assertEquals(250.0, truthOf(t).getFrontPositionMm(), 1e-6, "moves on: it stalls once there");
    }

    @Test
    public void testBreakawayEntireTrain() {
        Block[] b = line();
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        layout.place(t, b[1], b[0], 0.0);
        t0 = layout.now();
        speed(3, 40, true); // 40 mm/s: stock coasts 40 * 40 / (2 * 200) = 4 mm to a stand
        layout.runFor(1.0);
        layout.getFaults().stallAt(b[0], null, 392.0); // in the stock's way as it coasts
        VirtualTrain rear = layout.getFaults().breakaway(t, 100);
        assertTrue(rear.isStock());
        assertEquals("T (rear)", rear.getName());
        assertEquals(100.0, rear.getLengthMm());
        layout.runFor(15.0);
        SimulationTruth.TrainTruth r = truthOf(rear);
        assertEquals(0.0, r.getSpeedMmPerSecond(), "coasted to a stand");
        assertEquals(List.of(b[0]), r.getBlocks());
        assertEquals(450.0 - 60.0 + 4.0, r.getFrontPositionMm(), 1e-6, "its front was 60 mm short of B; 4 mm coasting");
        assertFalse(r.isStalled(), "stock has no locos to stall: it coasts past the stall point");
        assertEquals(b[2], truthOf(t).getFrontBlock(), "the front part ran on");
        assertTimes(List.of(), times(b[0], Sensor.INACTIVE), "A never went empty"); // times() waits for the sensors
        assertEquals(Sensor.ACTIVE, sensors.get(0).getKnownState(), "A still shows the stock");
        assertTrue(layout.getTruth().getCollisions().isEmpty());
    }

    @Test
    public void testBreakawayHeadOnlyLeavesStockUnseen() {
        Block[] b = line();
        VirtualTrain t = train("T", 3, 200, Detection.headOnly()); // the 100 mm loco is detected
        layout.place(t, b[1], b[0], 0.0);
        t0 = layout.now();
        speed(3, 40, true);
        layout.runFor(1.0);
        assertThrows(IllegalArgumentException.class, () -> layout.getFaults().breakaway(t, 200), "within the train");
        VirtualTrain rear = layout.getFaults().breakaway(t, 100);
        layout.runFor(15.0);
        assertEquals(List.of(b[0]), truthOf(rear).getBlocks(), "the wagons stand in A");
        flush();
        assertEquals(Sensor.INACTIVE, sensors.get(0).getKnownState(), "but A shows empty: nothing there is detected");
        assertEquals(b[2], truthOf(t).getFrontBlock());
    }

    /**
     * A - B - C, with B's way on to C or E set by turnout IT1 in B (100 mm),
     * closed.
     *
     * @return A, B, C and E
     */
    private Block[] junction() {
        Block a = block("A", 450);
        Block bb = block("B", 100);
        Block c = block("C", 450);
        Block e = block("E", 450);
        Turnout to = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to.setCommandedState(Turnout.CLOSED);
        join(a, bb);
        Path toC = new Path(c, 0, 0);
        toC.addSetting(new BeanSetting(to, Turnout.CLOSED));
        bb.addPath(toC);
        c.addPath(new Path(bb, 0, 0));
        Path toE = new Path(e, 0, 0);
        toE.addSetting(new BeanSetting(to, Turnout.THROWN));
        bb.addPath(toE);
        e.addPath(new Path(bb, 0, 0));
        return new Block[]{a, bb, c, e};
    }

    @Test
    public void testPointsMovedUnderATrainDerailIt() {
        Block[] j = junction();
        Block bb = j[1];
        Block c = j[2];
        Turnout to = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        layout.place(t, c, bb, 50.0); // over B and the turnout
        speed(3, 100, true);
        layout.runFor(0.2);
        to.setCommandedState(Turnout.THROWN);
        layout.runFor(1.0);
        JUnitAppender.assertWarnMessage("Simulated train \"T\" has stopped: The way through \"B\" changed under the train.");
        assertTrue(truthOf(t).isDerailed());
        double at = truthOf(t).getFrontPositionMm();
        to.setCommandedState(Turnout.CLOSED);
        layout.runFor(1.0);
        assertTrue(truthOf(t).isDerailed(), "still derailed with the points put back");
        assertEquals(at, truthOf(t).getFrontPositionMm(), 1e-9, "and where it stopped");
        assertEquals("The way through \"B\" changed under the train.", truthOf(t).getHeldReason(),
                "held by the derailment while its loco drives it");
        layout.getFaults().rerail(t);
        layout.runFor(1.0);
        assertFalse(truthOf(t).isDerailed());
        assertNull(truthOf(t).getHeldReason(), "not held once rerailed");
        assertEquals(at + 100.0, truthOf(t).getFrontPositionMm(), 1e-6, "back on the track, it moves again");
    }

    @Test
    public void testFaultsRefuseWhatCanNotBe() {
        Block[] b = line();
        SimulatedFaults f = layout.getFaults();
        assertThrows(IllegalArgumentException.class, () -> f.dropOut(b[1], b[0], -1, 100), "a stretch from before the end");
        assertThrows(IllegalArgumentException.class, () -> f.dropOut(b[1], b[0], 200, 100), "a stretch running backwards");
        assertThrows(IllegalArgumentException.class, () -> f.dropOutAtRandom(1L, 1.5, 50), "a probability over 1");
        assertThrows(IllegalArgumentException.class, () -> f.dropOutAtRandom(1L, -0.1, 50), "a probability under 0");
        assertThrows(IllegalArgumentException.class, () -> f.dropOutAtRandom(1L, 0.5, 0), "no length");
        assertThrows(IllegalArgumentException.class, () -> f.setTrueLength(b[1], 0), "no length");
        assertThrows(IllegalArgumentException.class, () -> f.stallAt(b[1], b[0], -1), "a point before the end");
        VirtualTrain notPlaced = train("N", 5, 200, Detection.entireTrain());
        assertThrows(IllegalStateException.class, () -> f.stall(notPlaced), "a train not placed");
        assertTrue(layout.getTruth().getFaults().isEmpty(), "nothing was set");
    }

    @Test
    public void testNoRandomDropOutWithProbabilityNought() {
        Block[] b = line();
        layout.getFaults().dropOutAtRandom(42L, 0.0, 50);
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        layout.place(t, b[1], b[0], 0.0);
        t0 = layout.now();
        speed(3, 100, true);
        layout.runFor(8.0);
        assertTrue(layout.getTruth().getDropOuts().isEmpty());
        assertTimes(List.of(6.5), times(b[1], Sensor.INACTIVE), "B seen until the rear leaves it");
        assertTimes(List.of(4.5), times(b[2], Sensor.ACTIVE), "C as usual");
    }

    @Test
    public void testPlacedWithTheFrontInAStretch() {
        Block[] b = line();
        layout.getFaults().dropOut(b[1], b[0], 0, 200);
        VirtualTrain u = train("U", 4, 100, Detection.entireTrain());
        layout.place(u, b[3], b[2], 200.0); // standing in D: another train, elsewhere
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        layout.place(t, b[1], b[0], 100.0);
        t0 = layout.now();
        speed(3, 100, true);
        layout.runFor(2.0);
        speed(3, 0, true);
        // the front is in the stretch from the start until 1.0 s, when it reaches 200 mm
        assertTimes(List.of(0.0), times(b[1], Sensor.INACTIVE), "B drops out from the first step");
        assertTimes(List.of(0.0, 1.0), times(b[1], Sensor.ACTIVE), "seen when placed, and again as the front leaves");
        List<SimulationTruth.DropOut> d = layout.getTruth().getDropOuts();
        assertEquals(1, d.size());
        assertEquals(t, d.get(0).getTrain());
        assertEquals(0.0, (d.get(0).getStartNanos() - t0) / 1e9, TIME_MARGIN);
        assertEquals(1.0, (d.get(0).getEndNanos() - t0) / 1e9, TIME_MARGIN);
        assertTimes(List.of(), times(b[3], Sensor.INACTIVE), "D still shows U");
    }

    @Test
    public void testBreakawayWithTheFrontInAStretch() {
        Block[] b = line();
        layout.getFaults().dropOut(b[1], b[0], 100, 300);
        VirtualTrain t = train("T", 3, 200, Detection.headOnly()); // only the 100 mm loco is detected
        layout.place(t, b[1], b[0], 0.0);
        t0 = layout.now();
        speed(3, 100, true);
        layout.runFor(1.5); // the front at 150 mm, in the stretch
        VirtualTrain rear = layout.getFaults().breakaway(t, 100);
        layout.runFor(2.0);
        speed(3, 0, true);
        // the front part carries the drop-out on: B is seen again when the loco's front leaves the stretch at 3.0 s
        assertTimes(List.of(1.0), times(b[1], Sensor.INACTIVE), "B drops out once");
        assertTimes(List.of(0.0, 3.0), times(b[1], Sensor.ACTIVE), "and is seen again as the front leaves the stretch");
        List<SimulationTruth.DropOut> d = layout.getTruth().getDropOuts();
        assertEquals(1, d.size(), "one drop-out: " + d);
        assertEquals(t, d.get(0).getTrain(), "the train that kept the locos");
        assertEquals(1.0, (d.get(0).getStartNanos() - t0) / 1e9, TIME_MARGIN);
        assertEquals(3.0, (d.get(0).getEndNanos() - t0) / 1e9, TIME_MARGIN);
        assertEquals(0.0, truthOf(rear).getSpeedMmPerSecond(), "the stock has stopped");
    }

    @Test
    public void testStallPointPassedOnlyByATrainThatCanMove() {
        Block[] b = line();
        layout.getFaults().stallAt(b[1], b[0], 150);
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        layout.place(t, b[1], b[0], 50.0);
        layout.getFaults().stall(t);
        speed(3, 100, true);
        layout.runFor(1.0);
        assertEquals(50.0, truthOf(t).getFrontPositionMm(), 1e-6, "stalled where it was placed");
        JUnitAppender.assertWarnMessage("Simulated train \"T\" has stopped: The train has stalled.");
        layout.getFaults().clearStall(t);
        layout.runFor(2.0);
        assertTrue(truthOf(t).isStalled(), "stalled again at the point");
        assertEquals(150.0, truthOf(t).getFrontPositionMm(), 1e-6);
        JUnitAppender.assertWarnMessage("Simulated train \"T\" has stopped: The train has stalled in \"B\".");
    }

    @Test
    public void testDropOutOffTheTrainsWayIsNotMet() {
        Block[] j = junction();
        layout.getFaults().dropOut(j[1], j[3], 0, 100); // in B, measured from E: off the way from A to C
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        layout.place(t, j[0], null, 300.0);
        t0 = layout.now();
        speed(3, 100, true);
        layout.runFor(4.0);
        speed(3, 0, true);
        assertEquals(j[2], truthOf(t).getFrontBlock(), "through B to C");
        assertTrue(layout.getTruth().getDropOuts().isEmpty(), "the stretch is on the other road");
        assertTimes(List.of(1.5), times(j[1], Sensor.ACTIVE), "B seen as the front comes in");
    }

    @Test
    public void testClearAll() {
        Block[] b = line();
        layout.getFaults().dropOut(b[1], b[0], 100, 200);
        layout.getFaults().swapSensors(b[2], b[3]);
        layout.getFaults().setTrueLength(b[2], 300);
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        layout.place(t, b[1], b[0], 0.0);
        layout.getFaults().phantom(b[3]);
        layout.getFaults().clearAll();
        assertTrue(layout.getTruth().getFaults().isEmpty());
        flush();
        changes.clear();
        t0 = layout.now();
        speed(3, 100, true);
        layout.runFor(5.0);
        assertTimes(List.of(), times(b[1], Sensor.INACTIVE), "no drop-out");
        assertTimes(List.of(4.5), times(b[2], Sensor.ACTIVE), "C's own sensor, at C's own length");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initConnectionConfigManager();
        layout = new SimulatedLayout(true);
        throttle = new InMemorySpeedSource();
        layout.addSpeedSource(throttle);
    }

    @AfterEach
    public void tearDown() {
        layout.dispose();
        layout = null;
        throttle = null;
        changes.clear();
        sensors.clear();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
