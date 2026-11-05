package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import jmri.TurnoutManager;
import jmri.jmrix.ConnectionConfigManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Tests of the mover on plain Blocks with internal sensors, run step by step
 * on a manual clock, so every time asserted is one the mover worked out, not
 * a wall-clock duration. Times are compared to within 1 microsecond, the
 * margin for rounding a crossing time to whole nanoseconds.
 * <p>
 * These check the simulator's own code against answers worked by hand. Only
 * {@link #testRunsOffItsProfile()}, {@link #testLongerThanJmriIsTold()} and
 * {@link #testAdjustmentOutsideConsistTruthOption()} set a truth offset (speed
 * factor; length offset; CV23 and CV24 outside a consist), and they check
 * that the simulator applies it; no test here checks any JMRI estimate.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class SimulatedLayoutTest {

    /** Rounding to whole nanoseconds, in seconds, with room to spare. */
    private static final double TIME_MARGIN = 1e-6;

    private SimulatedLayout layout;
    /** A layout with its own thread, for the one test that uses one; stopped in tearDown. */
    private SimulatedLayout real;
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

    /**
     * A Block's sensor, which every Block made by {@link #block} has.
     *
     * @param b the Block
     * @return its sensor
     */
    private static Sensor sensorOf(Block b) {
        return Objects.requireNonNull(b.getSensor(), b.getDisplayName() + " has a sensor");
    }

    /**
     * A placed train's part of some ground truth.
     *
     * @param truth the ground truth
     * @param t     the train
     * @return the train's ground truth
     */
    private static SimulationTruth.TrainTruth trainIn(SimulationTruth truth, VirtualTrain t) {
        return Objects.requireNonNull(truth.getTrain(t), t.getName() + " placed");
    }

    /**
     * A placed train's ground truth now.
     *
     * @param t the train
     * @return its ground truth
     */
    private SimulationTruth.TrainTruth truthOf(VirtualTrain t) {
        return trainIn(layout.getTruth(), t);
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

    private static SimulatedLoco loco(int address, double length) {
        // proportional, 1 mm/s per step of 126: step 100 is 100 mm/s
        return SimulatedLoco.builder(new DccLocoAddress(address, false)).length(length).fullSpeed(126).build();
    }

    private static VirtualTrain train(String name, int address, double length, Detection d) {
        return VirtualTrain.builder(name).loco(loco(address, 100)).length(length).detection(d).build();
    }

    private void speed(int address, int step, boolean forward) {
        throttle.setSpeed(new DccLocoAddress(address, false), step, SpeedStepMode.NMRA_DCC_128, forward,
                layout.now());
    }

    private void flush() {
        ThreadingUtil.runOnGUI(() -> {
        });
    }

    /**
     * Time, in seconds after t0, of the n-th change of a sensor to a state.
     */
    private double when(Block b, int state, int n) {
        flush();
        int index = sensors.indexOf(b.getSensor());
        int seen = 0;
        for (long[] c : changes) {
            if (c[0] == index && c[1] == state) {
                if (seen++ == n) {
                    return (c[2] - t0) / 1e9;
                }
            }
        }
        return Double.NaN;
    }

    private int count(Block b, int state) {
        flush();
        int index = sensors.indexOf(b.getSensor());
        int n = 0;
        for (long[] c : changes) {
            if (c[0] == index && c[1] == state) {
                n++;
            }
        }
        return n;
    }

    @Test
    public void testWorkedAnswersEntireTrain() {
        Block[] b = line();
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        layout.place(t, b[1], b[0], 0.0);
        t0 = layout.now();
        speed(3, 100, true);
        layout.runFor(5.0);
        assertEquals(0.0, when(b[1], Sensor.ACTIVE, 0), TIME_MARGIN, "B active as the front enters");
        assertEquals(4.5, when(b[2], Sensor.ACTIVE, 0), TIME_MARGIN, "next Block active at 4.5 s");
        assertEquals(2.0, when(b[0], Sensor.INACTIVE, 0), TIME_MARGIN, "Block behind clear at 2.0 s");
        assertEquals(Sensor.ACTIVE, sensorOf(b[1]).getKnownState());
        SimulationTruth.TrainTruth truth = truthOf(t);
        assertEquals(b[2], truth.getFrontBlock());
        assertEquals(50.0, truth.getFrontPositionMm(), 1e-6, "500 mm run, 450 of it in B");
        assertEquals(List.of(b[2], b[1]), truth.getBlocks());
        assertEquals(100.0, truth.getSpeedMmPerSecond(), 1e-9);
    }

    @Test
    public void testWorkedAnswerHeadOnly() {
        // head at the entry of B; a Head Only 200 mm train whose loco is 100 mm long clears A at 1.0 s,
        // although half the train is still there
        Block[] b = line();
        VirtualTrain t = train("T", 3, 200, Detection.headOnly());
        layout.place(t, b[1], b[0], 0.0);
        t0 = layout.now();
        flush();
        assertEquals(Sensor.ACTIVE, sensorOf(b[0]).getKnownState(), "the loco is in A");
        speed(3, 100, true);
        layout.runFor(1.5);
        assertEquals(1.0, when(b[0], Sensor.INACTIVE, 0), TIME_MARGIN);
        assertEquals(List.of(b[1], b[0]), truthOf(t).getBlocks(), "the train still reaches into A");
    }

    @Test
    public void testHeadOnlyTrainBehindLocoNotSeen() {
        Block[] b = line();
        VirtualTrain t = train("T", 3, 200, Detection.headOnly());
        layout.place(t, b[1], b[0], 100.0); // loco all in B, the rest of the train in A
        flush();
        assertEquals(Sensor.INACTIVE, sensorOf(b[0]).getKnownState());
        assertEquals(Sensor.ACTIVE, sensorOf(b[1]).getKnownState());
        assertEquals(0, count(b[0], Sensor.ACTIVE));
    }

    @Test
    public void testHeadAndTail() {
        Block[] b = line();
        VirtualTrain t = train("T", 3, 200, Detection.headAndTail(20));
        layout.place(t, b[1], b[0], 0.0);
        t0 = layout.now();
        speed(3, 100, true);
        layout.runFor(3.0);
        // the loco leaves A at 1.0 s, but the tail marker keeps A active until 2.0 s
        assertEquals(2.0, when(b[0], Sensor.INACTIVE, 0), TIME_MARGIN);
        assertEquals(1, count(b[0], Sensor.INACTIVE));
    }

    @Test
    public void testCustomMarkers() {
        Block[] b = line();
        Detection d = Detection.markers(List.of(new Detection.Marker(0, 50), new Detection.Marker(150, 10)));
        VirtualTrain t = train("T", 3, 200, d);
        layout.place(t, b[1], b[0], 0.0);
        t0 = layout.now();
        speed(3, 100, true);
        layout.runFor(5.0);
        // second marker: front 150 behind the train front, so at -150; its rear at -160
        assertEquals(1.6, when(b[0], Sensor.INACTIVE, 0), TIME_MARGIN);
        assertEquals(4.5, when(b[2], Sensor.ACTIVE, 0), TIME_MARGIN);
    }

    @Test
    public void testRunsOffItsProfile() {
        // truth offset: speed factor +10%, so the train runs at 110 mm/s, not the 100 its profile says
        Block[] b = line();
        VirtualTrain t = VirtualTrain.builder("T").loco(loco(3, 100)).length(200).speedFactorPercent(10).build();
        layout.place(t, b[1], b[0], 0.0);
        t0 = layout.now();
        speed(3, 100, true);
        layout.runFor(5.0);
        assertEquals(450.0 / 110.0, when(b[2], Sensor.ACTIVE, 0), TIME_MARGIN);
    }

    @Test
    public void testLongerThanJmriIsTold() {
        // truth offset: length offset +50 mm; JMRI is told 200 mm, the train is 250 mm
        Block[] b = line();
        VirtualTrain t = VirtualTrain.builder("T").loco(loco(3, 100)).length(200).lengthOffsetMm(50).build();
        layout.place(t, b[1], b[0], 0.0);
        t0 = layout.now();
        speed(3, 100, true);
        layout.runFor(3.0);
        assertEquals(2.5, when(b[0], Sensor.INACTIVE, 0), TIME_MARGIN);
    }

    @Test
    public void testMomentum() {
        // CV3 = 10: stop to full (126 mm/s) in 8.96 s; 450 mm = 0.5 x (126 / 8.96) x t^2 -> t = 8.0 s
        Block[] b = line();
        SimulatedLoco l = SimulatedLoco.builder(new DccLocoAddress(3, false)).length(100).fullSpeed(126)
                .momentum(10, 0).build();
        VirtualTrain t = VirtualTrain.builder("T").loco(l).length(200).build();
        layout.place(t, b[1], b[0], 0.0);
        t0 = layout.now();
        speed(3, 126, true);
        layout.runFor(9.0);
        assertEquals(8.0, when(b[2], Sensor.ACTIVE, 0), TIME_MARGIN);
    }

    @Test
    public void testCommandTimeWithinStep() {
        // a command received 20 ms into a step takes effect then, not at the step
        Block[] b = line();
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        layout.place(t, b[1], b[0], 0.0);
        t0 = layout.now();
        throttle.setSpeed(new DccLocoAddress(3, false), 100, SpeedStepMode.NMRA_DCC_128, true,
                t0 + 20_000_000L);
        layout.runFor(5.0);
        assertEquals(4.52, when(b[2], Sensor.ACTIVE, 0), TIME_MARGIN);
    }

    @Test
    public void testReversingBack() {
        Block[] b = line();
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        layout.place(t, b[1], b[0], 300.0);
        t0 = layout.now();
        speed(3, 100, false); // rear first: back towards A
        layout.runFor(2.0);
        // the rear was 100 mm into B; it reaches A at once and A goes active at 1.0 s
        assertEquals(1.0, when(b[0], Sensor.ACTIVE, 0), TIME_MARGIN);
        assertEquals(-100.0, truthOf(t).getSpeedMmPerSecond(), 1e-9);
    }

    @Test
    public void testEndOfTrack() {
        Block[] b = line();
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        layout.place(t, b[3], b[2], 400.0);
        speed(3, 100, true);
        layout.runFor(1.0);
        JUnitAppender.assertWarnMessageStartsWith("Simulated train \"T\" has stopped: The track ends");
        SimulationTruth.TrainTruth truth = truthOf(t);
        assertEquals(450.0, truth.getFrontPositionMm(), 1e-6, "stopped at the end of D");
        assertNotNull(truth.getHeldReason());
    }

    @Test
    public void testPointsSetAgainst() {
        // A - B - C, with a turnout in C: closed on to D, thrown to E; entering C from B needs it closed
        Block a = block("A", 450);
        Block bb = block("B", 450);
        Block c = block("C", 450);
        Block d = block("D", 450);
        Block e = block("E", 450);
        Turnout to = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        join(a, bb);
        bb.addPath(new Path(c, 0, 0));
        c.addPath(new Path(bb, 0, 0, new BeanSetting(to, "IT1", Turnout.CLOSED)));
        c.addPath(new Path(d, 0, 0, new BeanSetting(to, "IT1", Turnout.CLOSED)));
        c.addPath(new Path(e, 0, 0, new BeanSetting(to, "IT1", Turnout.THROWN)));
        d.addPath(new Path(c, 0, 0));
        e.addPath(new Path(c, 0, 0));
        to.setCommandedState(Turnout.THROWN);
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        layout.place(t, bb, a, 400.0);
        t0 = layout.now();
        speed(3, 100, true);
        layout.runFor(2.0);
        JUnitAppender.assertWarnMessageStartsWith("Simulated train \"T\" has stopped: The turnouts in \"C\" are set against");
        SimulationTruth.TrainTruth truth = truthOf(t);
        assertEquals(bb, truth.getFrontBlock(), "stopped at the boundary, not in C");
        assertEquals(450.0, truth.getFrontPositionMm(), 1e-6);
        assertEquals(0, count(c, Sensor.ACTIVE));
        // set the points: it goes on from where it stood
        to.setCommandedState(Turnout.CLOSED);
        layout.runFor(1.0);
        assertNull(truthOf(t).getHeldReason());
        assertEquals(c, truthOf(t).getFrontBlock());
    }

    @Test
    public void testTwoTrainsInOneBlock() {
        Block[] b = line();
        VirtualTrain first = train("First", 3, 200, Detection.entireTrain());
        VirtualTrain second = train("Second", 4, 40, Detection.entireTrain());
        layout.place(first, b[1], b[0], 300.0);
        layout.place(second, b[1], b[0], 50.0);
        t0 = layout.now();
        assertEquals(1, count(b[1], Sensor.ACTIVE), "B active once");
        speed(3, 100, true);
        layout.runFor(5.0);
        // first's rear leaves B at 3.5 s, but second is still there
        assertEquals(0, count(b[1], Sensor.INACTIVE));
        flush();
        assertEquals(Sensor.ACTIVE, sensorOf(b[1]).getKnownState());
        layout.remove(second);
        assertEquals(1, count(b[1], Sensor.INACTIVE));
        assertTrue(layout.getTruth().getCollisions().isEmpty());
    }

    @Test
    public void testPlacingOnAnotherTrainRefused() {
        Block[] b = line();
        layout.place(train("First", 3, 200, Detection.entireTrain()), b[1], b[0], 300.0);
        VirtualTrain second = train("Second", 4, 100, Detection.entireTrain());
        assertThrows(IllegalArgumentException.class, () -> layout.place(second, b[1], b[0], 250.0));
        // behind it instead: front touching the other's rear
        layout.placeBehind(second, layout.getTrains().get(0));
        assertEquals(100.0, truthOf(second).getFrontPositionMm(), 1e-9);
    }

    @Test
    public void testRearEndCollision() {
        Block[] b = line();
        VirtualTrain standing = train("Standing", 3, 100, Detection.entireTrain());
        VirtualTrain moving = train("Moving", 4, 50, Detection.entireTrain());
        layout.place(standing, b[1], b[0], 300.0); // 200 to 300 in B
        layout.place(moving, b[1], b[0], 100.0); // 50 to 100 in B
        t0 = layout.now();
        speed(4, 100, true);
        layout.runFor(2.0);
        JUnitAppender.assertWarnMessage("Simulated trains \"Standing\" and \"Moving\" have collided in \"B\".");
        SimulationTruth truth = layout.getTruth();
        assertEquals(1, truth.getCollisions().size());
        SimulationTruth.Collision c = truth.getCollisions().get(0);
        assertTrue(c.involves(standing) && c.involves(moving));
        assertEquals(b[1], c.getBlock());
        assertEquals(1.0, (c.getTimeNanos() - t0) / 1e9, TIME_MARGIN, "front reaches 200 mm at 1.0 s");
        assertEquals(200.0, trainIn(truth, moving).getFrontPositionMm(), 1e-6, "stopped where they touched");
        assertTrue(trainIn(truth, moving).isCrashed());
        assertTrue(trainIn(truth, standing).isCrashed());
        layout.runFor(1.0);
        assertEquals(200.0, truthOf(moving).getFrontPositionMm(), 1e-6, "stays stopped");
    }

    @Test
    public void testHeadOnCollision() {
        Block[] b = line();
        VirtualTrain east = train("East", 3, 50, Detection.entireTrain());
        VirtualTrain west = train("West", 4, 50, Detection.entireTrain());
        layout.place(east, b[1], b[0], 100.0);
        layout.place(west, b[2], b[3], 100.0); // 100 mm into C from D: 350 mm from B's side
        t0 = layout.now();
        speed(3, 100, true);
        speed(4, 50, true);
        layout.runFor(6.0);
        JUnitAppender.assertWarnMessage("Simulated trains \"East\" and \"West\" have collided in \"C\".");
        SimulationTruth.Collision c = layout.getTruth().getCollisions().get(0);
        // East's front in C at 100 t - 350, West's at 350 - 50 t (from B's side): they meet at t = 14/3 s
        assertEquals(14.0 / 3.0, (c.getTimeNanos() - t0) / 1e9, TIME_MARGIN);
        assertEquals(b[2], c.getBlock());
    }

    @Test
    public void testDuplicateAddressBothMove() {
        Block[] b = line();
        VirtualTrain one = train("One", 3, 100, Detection.entireTrain());
        VirtualTrain two = train("Two", 3, 100, Detection.entireTrain());
        layout.place(one, b[1], b[0], 200.0);
        layout.place(two, b[3], b[2], 200.0);
        speed(3, 100, true);
        layout.runFor(1.0);
        assertEquals(300.0, truthOf(one).getFrontPositionMm(), 1e-6);
        assertEquals(300.0, truthOf(two).getFrontPositionMm(), 1e-6);
    }

    @Test
    public void testConsist() {
        Block[] b = line();
        // consist address 10; the second loco faces the rear, with CV19 bit 7 set to match
        SimulatedLoco lead = SimulatedLoco.builder(new DccLocoAddress(3, false)).length(100).fullSpeed(126)
                .consist(10, 0, 0).build();
        SimulatedLoco rear = SimulatedLoco.builder(new DccLocoAddress(4, false)).length(100).fullSpeed(110)
                .consist(10 | 0x80, 0, 0).facingRear().build();
        VirtualTrain t = VirtualTrain.builder("Consist").loco(lead).loco(rear).length(300).build();
        layout.place(t, b[1], b[0], 300.0);
        speed(3, 100, true);
        layout.runFor(1.0);
        assertEquals(300.0, truthOf(t).getFrontPositionMm(), 1e-9,
                "a loco in a consist ignores its own address");
        speed(10, 63, true);
        layout.runFor(1.0);
        // 63 of 126: lead 63 mm/s, rear loco 55 mm/s; the train goes at the slower
        assertEquals(355.0, truthOf(t).getFrontPositionMm(), 1e-6);
        JUnitAppender.assertWarnMessage("The locos of train \"Consist\" differ in speed by more than 10%; it moves at the slowest one's speed.");
    }

    @Test
    public void testConsistMomentumAdjustment() {
        // CV3 = 5 plus CV23 = 5 in a consist: 10, so 8.0 s to cover 450 mm at full 126 mm/s
        Block[] b = line();
        SimulatedLoco l = SimulatedLoco.builder(new DccLocoAddress(3, false)).length(100).fullSpeed(126)
                .momentum(5, 0).consist(10, 5, 0).build();
        VirtualTrain t = VirtualTrain.builder("T").loco(l).length(200).build();
        layout.place(t, b[1], b[0], 0.0);
        t0 = layout.now();
        speed(10, 126, true);
        layout.runFor(9.0);
        assertEquals(8.0, when(b[2], Sensor.ACTIVE, 0), TIME_MARGIN);
    }

    @Test
    public void testAdjustmentNotUsedOutsideConsist() {
        // CV23 set but no consist: CV3 = 10 alone, so 8.0 s
        Block[] b = line();
        SimulatedLoco l = SimulatedLoco.builder(new DccLocoAddress(3, false)).length(100).fullSpeed(126)
                .momentum(10, 0).consist(0, 10, 0).build();
        VirtualTrain t = VirtualTrain.builder("T").loco(l).length(200).build();
        layout.place(t, b[1], b[0], 0.0);
        t0 = layout.now();
        speed(3, 126, true);
        layout.runFor(9.0);
        assertEquals(8.0, when(b[2], Sensor.ACTIVE, 0), TIME_MARGIN);
    }

    @Test
    public void testAdjustmentOutsideConsistTruthOption() {
        // truth option: this train's decoder applies CV23 outside a consist, so CV3 10 + 10 = 20:
        // 450 = 0.5 x (126 / (20 x 0.896)) x t^2
        Block[] b = line();
        SimulatedLoco l = SimulatedLoco.builder(new DccLocoAddress(3, false)).length(100).fullSpeed(126)
                .momentum(10, 0).consist(0, 10, 0).build();
        VirtualTrain t = VirtualTrain.builder("T").loco(l).length(200).appliesAdjustmentOutsideConsist(true).build();
        layout.place(t, b[1], b[0], 0.0);
        t0 = layout.now();
        speed(3, 126, true);
        layout.runFor(12.0);
        assertEquals(Math.sqrt(2 * 450 * 20 * DecoderModel.SECONDS_PER_UNIT / 126), when(b[2], Sensor.ACTIVE, 0),
                TIME_MARGIN);
    }

    @Test
    public void testEmergencyStop() {
        Block[] b = line();
        SimulatedLoco l = SimulatedLoco.builder(new DccLocoAddress(3, false)).length(100).fullSpeed(126)
                .momentum(0, 50).build();
        VirtualTrain t = VirtualTrain.builder("T").loco(l).length(200).build();
        layout.place(t, b[1], b[0], 0.0);
        speed(3, 100, true);
        layout.runFor(1.0);
        throttle.emergencyStop(new DccLocoAddress(3, false), layout.now());
        layout.runFor(1.0);
        assertEquals(100.0, truthOf(t).getFrontPositionMm(), 1e-6, "stopped at once");
    }

    @Test
    public void testLatestCommandFromAnySource() {
        Block[] b = line();
        InMemorySpeedSource other = new InMemorySpeedSource();
        layout.addSpeedSource(other);
        VirtualTrain t = train("T", 3, 100, Detection.entireTrain());
        layout.place(t, b[1], b[0], 0.0);
        speed(3, 100, true);
        layout.runFor(1.0);
        // the source added second sends a later command, which the loco obeys
        other.setSpeed(new DccLocoAddress(3, false), 50, SpeedStepMode.NMRA_DCC_128, true, layout.now());
        layout.runFor(1.0);
        assertEquals(150.0, truthOf(t).getFrontPositionMm(), 1e-6, "100 mm, then 50 mm");
        // and then the first source again
        speed(3, 100, true);
        layout.runFor(1.0);
        assertEquals(250.0, truthOf(t).getFrontPositionMm(), 1e-6, "then 100 mm again");
    }

    @Test
    public void testSourcesToldAddresses() {
        Block[] b = line();
        List<java.util.Set<DccLocoAddress>> told = new ArrayList<>();
        SimulatedSpeedSource watching = new InMemorySpeedSource() {
            @Override
            public void watch(java.util.Set<DccLocoAddress> addresses) {
                told.add(addresses);
            }
        };
        layout.addSpeedSource(watching);
        SimulatedLoco consisted = SimulatedLoco.builder(new DccLocoAddress(1234, true)).length(100).fullSpeed(126)
                .consist(5, 0, 0).build();
        VirtualTrain one = train("One", 3, 100, Detection.entireTrain());
        VirtualTrain two = VirtualTrain.builder("Two").loco(consisted).length(100).build();
        layout.place(one, b[1], b[0], 200.0);
        layout.place(two, b[3], b[2], 200.0);
        layout.remove(one);
        layout.removeAll();
        layout.removeSpeedSource(watching);
        DccLocoAddress three = new DccLocoAddress(3, false);
        DccLocoAddress five = new DccLocoAddress(5, false);
        assertEquals(List.of(java.util.Set.of(), java.util.Set.of(three), java.util.Set.of(three, five),
                java.util.Set.of(five), java.util.Set.of(), java.util.Set.of()), told,
                "told on adding, each placement and removal (a consisted loco by its consist address), and on leaving");
    }

    @Test
    public void testOnlyInSimulatedProfile() {
        Block[] b = line();
        ConnectionConfigManager ccm = InstanceManager.getDefault(ConnectionConfigManager.class);
        jmri.jmrix.ConnectionConfig real = new jmri.jmrix.can.adapters.gridconnect.canusb.serialdriver.ConnectionConfig();
        ccm.add(real);
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        assertThrows(IllegalStateException.class, () -> layout.place(t, b[1], b[0], 0.0));
        ccm.remove(real);
        layout.place(t, b[1], b[0], 300.0);
        flush();
        assertEquals(Sensor.ACTIVE, sensorOf(b[1]).getKnownState());
        ccm.add(real);
        JUnitAppender.assertWarnMessage("A connection that is not a simulator was added, so the simulated trains have been removed.");
        assertTrue(layout.getTrains().isEmpty(), "trains removed when a real connection is added");
        flush();
        assertEquals(Sensor.INACTIVE, sensorOf(b[1]).getKnownState());
        ccm.remove(real);
    }

    @Test
    public void testDoesNotFit() {
        Block[] b = line();
        VirtualTrain t = train("T", 3, 600, Detection.entireTrain());
        assertThrows(IllegalArgumentException.class, () -> layout.place(t, b[0], null, 300.0),
                "the track ends 300 mm behind the front");
    }

    @Test
    public void testDarkBlock() {
        Block[] b = line();
        b[2].setSensor(null);
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        layout.place(t, b[1], b[0], 400.0);
        speed(3, 100, true);
        layout.runFor(2.0);
        assertEquals(b[2], truthOf(t).getFrontBlock(), "crossed the dark Block's boundary");
        flush();
        assertEquals(Sensor.INACTIVE, sensors.get(2).getKnownState(), "nothing reported for a dark Block");
    }

    @Test
    public void testSensorOnAnotherConnection() {
        Block[] b = line();
        // a sensor of no connection the simulator has an output for
        Sensor other = new jmri.implementation.AbstractSensor("XS1") {
            @Override
            public void requestUpdateFromLayout() {
            }
        };
        b[2].setNamedSensor(InstanceManager.getDefault(jmri.NamedBeanHandleManager.class)
                .getNamedBeanHandle("XS1", other));
        VirtualTrain t = train("T", 3, 200, Detection.entireTrain());
        layout.place(t, b[1], b[0], 400.0);
        // told when the train is placed, once
        JUnitAppender.assertWarnMessage("Sensor \"XS1\" of Block \"C\" is not on a connection simulated trains can report on, so it is left alone.");
        speed(3, 100, true);
        layout.runFor(1.0);
        flush();
        assertEquals(Sensor.UNKNOWN, other.getKnownState(), "left alone");
        assertEquals(java.util.Set.of(other), layout.getSensorsNotReported());
        // an output for that connection reports it, in time order
        List<Object[]> reported = new ArrayList<>();
        layout.addDetectorOutput(new SimulatedDetectorOutput() {
            @Override
            public boolean reports(Sensor sensor) {
                return sensor == other;
            }

            @Override
            public void report(Sensor sensor, int state, long timeNanos) {
                reported.add(new Object[]{sensor, state, timeNanos});
            }
        });
        speed(3, 100, false);
        layout.runFor(2.0);
        assertEquals(1, reported.size(), "the front leaves C going back");
        assertEquals(Sensor.INACTIVE, reported.get(0)[1]);
        assertTrue(layout.getSensorsNotReported().isEmpty());
    }

    @Test
    public void testMoverThread() {
        real = new SimulatedLayout();
        SimulatedLayout real = this.real;
        real.addSpeedSource(throttle);
        Block[] b = line();
        VirtualTrain t = train("T", 3, 100, Detection.entireTrain());
        assertFalse(real.isMoverRunning(), "nothing runs until a train is placed");
        real.place(t, b[1], b[0], 440.0);
        assertTrue(real.isMoverRunning());
        long sent = System.nanoTime();
        throttle.setSpeed(new DccLocoAddress(3, false), 100, SpeedStepMode.NMRA_DCC_128, true, sent);
        // 10 mm at 100 mm/s: the crossing time is worked out, so exact whatever the thread's timing
        JUnitUtil.waitFor(() -> sensorOf(b[2]).getKnownState() == Sensor.ACTIVE, "C active");
        t0 = sent;
        assertEquals(0.1, when(b[2], Sensor.ACTIVE, 0), TIME_MARGIN);
        real.remove(t);
        JUnitUtil.waitFor(() -> !real.isMoverRunning(), "mover stopped with the last train removed");
        JUnitUtil.waitThreadTerminated(SimulatedLayout.THREAD_NAME);
    }

    @Test
    public void testTruthIsDatedWhenItWasTrue() {
        real = new SimulatedLayout();
        SimulatedLayout real = this.real;
        real.addSpeedSource(throttle);
        Block[] b = line();
        VirtualTrain t = train("T", 3, 100, Detection.entireTrain());
        real.place(t, b[1], b[0], 10.0);
        long sent = System.nanoTime();
        throttle.setSpeed(new DccLocoAddress(3, false), 100, SpeedStepMode.NMRA_DCC_128, true, sent);
        for (int i = 0; i < 20; i++) {
            JUnitUtil.waitFor(7);
            SimulationTruth truth = real.getTruth();
            SimulationTruth.TrainTruth tt = trainIn(truth, t);
            // 100 mm/s from when the command was sent, with no momentum
            double expected = 10.0 + Math.max(0, truth.getTimeNanos() - sent) / 1e9 * 100.0;
            assertEquals(expected, tt.getFrontPositionMm(), 1e-3,
                    "the front is where it was at the truth's time, " + i);
        }
        real.remove(t);
        JUnitUtil.waitFor(() -> !real.isMoverRunning(), "mover stopped with the last train removed");
        JUnitUtil.waitThreadTerminated(SimulatedLayout.THREAD_NAME);
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
        if (real != null) {
            real.dispose();
            JUnitUtil.waitFor(() -> !real.isMoverRunning(), "mover stopped");
            real = null;
        }
        throttle = null;
        changes.clear();
        sensors.clear();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
