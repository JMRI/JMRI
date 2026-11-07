package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jmri.Block;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SpeedStepMode;
import jmri.ThrottleManager;
import jmri.jmrit.logix.BlockOrder;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.OPath;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;
import jmri.jmrit.logix.ThrottleSetting;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logix.WarrantPreferences;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link InternalThrottleSpeedSource}: the commands it takes from
 * the Internal connection's throttles, and, through
 * {@link SimulationTestUtil}, trains on the default simulated layout obeying
 * those throttles in real time, including when a Warrant drives them.
 * <p>
 * The real-time tests assert the order of events and the ground truth, and
 * times the simulator worked out from the commands' received times, never
 * how long anything took.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class InternalThrottleSpeedSourceTest {

    /** Rounding to whole nanoseconds, in seconds, with room to spare. */
    private static final double TIME_MARGIN = 1e-6;
    private static final DccLocoAddress THREE = new DccLocoAddress(3, false);

    private InternalThrottleSpeedSource source;
    private Warrant warrant;
    /** Sensor changes to ACTIVE or INACTIVE: {sensor, state, time}. */
    private final List<Object[]> changes = Collections.synchronizedList(new ArrayList<>());

    private static ThrottleManager manager() {
        return InstanceManager.getDefault(ThrottleManager.class);
    }

    private static boolean watched(DccThrottle t) {
        for (PropertyChangeListener l : t.getPropertyChangeListeners()) {
            if (l.getClass().getEnclosingClass() == InternalThrottleSpeedSource.class) {
                return true;
            }
        }
        return false;
    }

    private void record(Sensor s) {
        s.addRawStateListener((sensor, oldState, newState, timeNanos) ->
                changes.add(new Object[]{sensor, newState, timeNanos}));
    }

    private long timeOf(Sensor s, int state) {
        synchronized (changes) {
            for (Object[] c : changes) {
                if (c[0] == s && (Integer) c[1] == state) {
                    return (Long) c[2];
                }
            }
        }
        return fail(s.getDisplayName() + " never went to " + state);
    }

    private static void waitForSensor(Block b, int state) {
        Sensor s = Objects.requireNonNull(b.getSensor(), b.getDisplayName() + " has a sensor");
        JUnitUtil.waitFor(() -> s.getKnownState() == state, b.getDisplayName() + "'s sensor " + state);
    }

    // ---------------------------------------------------------------------
    // The source on its own

    @Test
    public void testStep() {
        assertEquals(63, InternalThrottleSpeedSource.step(0.5f, SpeedStepMode.NMRA_DCC_128));
        assertEquals(14, InternalThrottleSpeedSource.step(0.5f, SpeedStepMode.NMRA_DCC_28));
        assertEquals(7, InternalThrottleSpeedSource.step(0.5f, SpeedStepMode.NMRA_DCC_14));
        assertEquals(1, InternalThrottleSpeedSource.step(0.001f, SpeedStepMode.NMRA_DCC_28), "any speed moves");
        assertEquals(0, InternalThrottleSpeedSource.step(0.0f, SpeedStepMode.NMRA_DCC_28));
        assertEquals(126, InternalThrottleSpeedSource.step(1.0f, SpeedStepMode.NMRA_DCC_128));
    }

    @Test
    public void testNothingAttachedUntilWatched() {
        DccThrottle t = SimulationTestUtil.throttle(3);
        t.setSpeedSetting(0.25f);
        assertNull(source.getCommand(THREE), "not watched");
        assertFalse(watched(t), "nothing attached to a throttle while no train obeys it");
        long before = System.nanoTime();
        source.watch(Set.of(THREE));
        assertTrue(watched(t));
        SimulatedSpeedSource.Command c = Objects.requireNonNull(source.getCommand(THREE),
                "a throttle that already exists is read when watching starts");
        assertEquals(32, c.getSpeedStep(), "0.25 of 126 steps");
        assertTrue(c.getTimeNanos() - before >= 0, "received when watching started");
        source.watch(Set.of());
        assertFalse(watched(t), "listener removed");
        assertNull(source.getCommand(THREE), "command forgotten");
    }

    @Test
    public void testSpeedCommand() {
        List<DccLocoAddress> told = new ArrayList<>();
        source.addCommandListener(told::add);
        source.watch(Set.of(THREE));
        DccThrottle t = SimulationTestUtil.throttle(3);
        told.clear();
        long before = System.nanoTime();
        t.setSpeedSetting(0.5f);
        long after = System.nanoTime();
        SimulatedSpeedSource.Command c = Objects.requireNonNull(source.getCommand(THREE), "a command for 3");
        assertEquals(63, c.getSpeedStep());
        assertEquals(SpeedStepMode.NMRA_DCC_128, c.getMode());
        assertTrue(c.isForward());
        assertFalse(c.isEmergencyStop());
        assertTrue(c.getTimeNanos() - before >= 0 && after - c.getTimeNanos() >= 0, "received when the throttle was told");
        assertEquals(List.of(THREE), told, "listener told once");
        assertNull(source.getCommand(new DccLocoAddress(4, false)), "only the address set");
    }

    @Test
    public void testDirection() {
        source.watch(Set.of(THREE));
        DccThrottle t = SimulationTestUtil.throttle(3);
        t.setSpeedSetting(0.5f);
        t.setIsForward(false);
        SimulatedSpeedSource.Command c = Objects.requireNonNull(source.getCommand(THREE), "a command for 3");
        assertFalse(c.isForward());
        assertEquals(63, c.getSpeedStep(), "same speed");
    }

    @Test
    public void testSpeedStepMode() {
        source.watch(Set.of(THREE));
        DccThrottle t = SimulationTestUtil.throttle(3);
        t.setSpeedSetting(0.5f);
        t.setSpeedStepMode(SpeedStepMode.NMRA_DCC_28);
        SimulatedSpeedSource.Command c = Objects.requireNonNull(source.getCommand(THREE), "a command for 3");
        assertEquals(SpeedStepMode.NMRA_DCC_28, c.getMode());
        assertEquals(14, c.getSpeedStep(), "the same setting in the new mode");
        t.setSpeedStepMode(SpeedStepMode.NMRA_DCC_14);
        t.setSpeedSetting(0.25f);
        c = Objects.requireNonNull(source.getCommand(THREE), "a command for 3");
        assertEquals(4, c.getSpeedStep(), "0.25 of 14 steps, rounded");
        t.setSpeedStepMode(SpeedStepMode.INCREMENTAL);
        c = Objects.requireNonNull(source.getCommand(THREE), "a command for 3");
        assertEquals(SpeedStepMode.NMRA_DCC_128, c.getMode(), "a mode with no steps is taken as 128-step");
    }

    @Test
    public void testEmergencyStop() {
        source.watch(Set.of(THREE));
        DccThrottle t = SimulationTestUtil.throttle(3);
        t.setSpeedSetting(0.5f);
        t.setSpeedSetting(-1.0f);
        SimulatedSpeedSource.Command c = Objects.requireNonNull(source.getCommand(THREE), "a command for 3");
        assertTrue(c.isEmergencyStop());
        assertEquals(0.0, c.getSetting());
        t.setSpeedSetting(0.0f);
        c = Objects.requireNonNull(source.getCommand(THREE), "a command for 3");
        assertFalse(c.isEmergencyStop(), "a speed after an emergency stop is a speed");
    }

    @Test
    public void testThrottleManagerStoredLater() {
        InstanceManager.reset(ThrottleManager.class);
        source.watch(Set.of(THREE));
        assertNull(source.getCommand(THREE), "no throttle manager yet");
        JUnitUtil.initDebugThrottleManager();
        assertNotNull(source.getCommand(THREE), "watched as soon as it is stored");
        SimulationTestUtil.throttle(3).setSpeedSetting(1.0f);
        SimulatedSpeedSource.Command c = Objects.requireNonNull(source.getCommand(THREE), "a command for 3");
        assertEquals(126, c.getSpeedStep());
    }

    @Test
    public void testReleasedThrottleStillWatched() {
        source.watch(Set.of(THREE));
        DccThrottle t = SimulationTestUtil.throttle(3);
        manager().releaseThrottle(t, null);
        DccThrottle again = SimulationTestUtil.throttle(3);
        assertSame(t, again, "the throttle is kept while it is watched");
        again.setSpeedSetting(0.5f);
        SimulatedSpeedSource.Command c = Objects.requireNonNull(source.getCommand(THREE), "a command for 3");
        assertEquals(63, c.getSpeedStep());
    }

    // ---------------------------------------------------------------------
    // Trains on the default layout, in real time

    @Test
    public void testNothingChangesWithoutTrains() {
        SimulatedLayout layout = SimulationTestUtil.layout();
        DccThrottle t = SimulationTestUtil.throttle(3);
        assertFalse(watched(t), "a simulated layout with no train attaches nothing");
        Block[] b = SimulationTestUtil.line(300, "A", "B", "C");
        VirtualTrain train = SimulationTestUtil.train("T", 3, 100, 200);
        SimulationTestUtil.place(train, b[1], b[0], 250);
        assertTrue(watched(t), "watched while the train is placed");
        layout.remove(train);
        assertFalse(watched(t), "not watched once the train is removed");
    }

    @Test
    public void testThrottleMovesPlacedTrain() {
        Block[] b = SimulationTestUtil.line(300, "A", "B", "C");
        record(b[2].getSensor());
        VirtualTrain train = SimulationTestUtil.train("T", 3, 100, 200);
        SimulationTestUtil.place(train, b[1], b[0], 250);
        DccThrottle t = SimulationTestUtil.throttle(3);
        long before = System.nanoTime();
        t.setSpeedSetting(0.5f); // step 63 of 126: 100 mm/s
        long after = System.nanoTime();
        SimulationTestUtil.waitForFrontIn(train, b[2]);
        waitForSensor(b[2], Sensor.ACTIVE);
        // 50 mm to go at 100 mm/s: crossed 0.5 s after the throttle was told
        long crossed = timeOf(b[2].getSensor(), Sensor.ACTIVE);
        assertTrue((crossed - before) / 1e9 >= 0.5 - TIME_MARGIN && (crossed - after) / 1e9 <= 0.5 + TIME_MARGIN,
                "crossing worked out from when the throttle was told");
        assertEquals(100.0, SimulationTestUtil.truth(train).getSpeedMmPerSecond(), 1e-9);
    }

    @Test
    public void testThrottleDirection() {
        Block[] b = SimulationTestUtil.line(300, "A", "B", "C");
        VirtualTrain train = SimulationTestUtil.train("T", 3, 100, 200);
        SimulationTestUtil.place(train, b[1], b[0], 250);
        DccThrottle t = SimulationTestUtil.throttle(3);
        t.setIsForward(false);
        t.setSpeedSetting(0.5f);
        // the rear, 150 mm into B, leads back into A
        waitForSensor(b[0], Sensor.ACTIVE);
        SimulationTruth.TrainTruth truth = SimulationTestUtil.truth(train);
        assertTrue(truth.getBlocks().contains(b[0]), "really in A");
        assertEquals(b[1], truth.getFrontBlock(), "front still in B");
        assertTrue(truth.getFrontPositionMm() < 250.0, "moving back");
        assertEquals(Sensor.INACTIVE, Objects.requireNonNull(b[2].getSensor(), "C has a sensor").getKnownState());
    }

    @Test
    public void testThrottleSpeedStepMode() {
        Block[] b = SimulationTestUtil.line(300, "A", "B", "C");
        VirtualTrain train = SimulationTestUtil.train("T", 3, 100, 200);
        SimulationTestUtil.place(train, b[1], b[0], 200);
        DccThrottle t = SimulationTestUtil.throttle(3);
        t.setSpeedStepMode(SpeedStepMode.NMRA_DCC_28);
        t.setSpeedSetting(0.25f); // step 7 of 28: 50 mm/s (in 128-step mode it would be 32 of 126)
        SimulationTestUtil.waitForTruth(
                tr -> Objects.requireNonNull(tr.getTrain(train), "T placed").getSpeedMmPerSecond() > 0, "moving");
        assertEquals(50.0, SimulationTestUtil.truth(train).getSpeedMmPerSecond(), 1e-9);
    }

    @Test
    public void testThrottleEmergencyStop() {
        Block[] b = SimulationTestUtil.line(300, "A", "B", "C");
        // no acceleration momentum; CV4 = 255 takes 1.8 s per step to slow down
        VirtualTrain train = VirtualTrain.builder("T")
                .loco(SimulatedLoco.builder(THREE).length(100).fullSpeed(200).momentum(0, 255).build())
                .length(100).detection(Detection.entireTrain()).build();
        SimulationTestUtil.place(train, b[1], b[0], 150);
        DccThrottle t = SimulationTestUtil.throttle(3);
        t.setSpeedSetting(0.5f);
        SimulationTestUtil.waitForTruth(
                tr -> Objects.requireNonNull(tr.getTrain(train), "T placed").getSpeedMmPerSecond() > 0, "moving");
        t.setSpeedSetting(0.0f);
        long stopAsked = System.nanoTime();
        // half a second later it is still slowing: one step of 63 down, at most
        SimulationTestUtil.waitForTruth(tr -> tr.getTimeNanos() - stopAsked > 500_000_000L, "half a second on");
        assertTrue(SimulationTestUtil.truth(train).getSpeedMmPerSecond() > 90.0, "momentum slows it gently");
        t.setSpeedSetting(-1.0f);
        SimulationTestUtil.waitForTruth(
                tr -> Objects.requireNonNull(tr.getTrain(train), "T placed").getSpeedMmPerSecond() == 0.0,
                "stopped at once");
        double at = SimulationTestUtil.truth(train).getFrontPositionMm();
        long stopped = SimulationTestUtil.truth().getTimeNanos();
        SimulationTestUtil.waitForTruth(tr -> tr.getTimeNanos() - stopped > 200_000_000L, "a while later");
        assertEquals(at, SimulationTestUtil.truth(train).getFrontPositionMm(), 1e-9, "stays stopped");
    }

    /**
     * A Warrant drives a simulated train through three OBlocks: it sets the
     * speed, waits for the train to enter each Block as its sensor reports,
     * and stops it in the last. Checks the order the Blocks were reported in,
     * that the Warrant ended where the train really is, and the ground truth.
     *
     * @param tempDir a folder for the test's profile
     * @throws Exception if a sensor's state can't be set
     */
    @Test
    public void testWarrantDrivesTrain(@TempDir File tempDir) throws Exception {
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(tempDir));
        JUnitUtil.initRosterConfigManager();
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
        OBlockManager obm = InstanceManager.getDefault(OBlockManager.class);
        PortalManager pm = InstanceManager.getDefault(PortalManager.class);
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        OBlock north = Objects.requireNonNull(obm.createNewOBlock("OB1", "North"), "OBlock North created");
        OBlock west = Objects.requireNonNull(obm.createNewOBlock("OB2", "West"), "OBlock West created");
        OBlock south = Objects.requireNonNull(obm.createNewOBlock("OB3", "South"), "OBlock South created");
        north.setLength(400);
        west.setLength(400);
        south.setLength(3000);
        OBlock[] blocks = {north, west, south};
        for (int i = 0; i < blocks.length; i++) {
            Sensor s = sm.provideSensor("IS" + (i + 1));
            s.setKnownState(Sensor.INACTIVE);
            blocks[i].setSensor(s.getSystemName());
            record(s);
        }
        Portal nw = pm.createNewPortal("NorthWest");
        nw.setFromBlock(north, false);
        nw.setToBlock(west, false);
        Portal ws = pm.createNewPortal("WestSouth");
        ws.setFromBlock(west, false);
        ws.setToBlock(south, false);
        north.addPath(new OPath("N", north, null, nw, null));
        west.addPath(new OPath("W", west, nw, ws, null));
        south.addPath(new OPath("S", south, ws, null, null));

        VirtualTrain train = SimulationTestUtil.train("T", 3, 100, 400);
        SimulationTestUtil.place(train, north, null, 350);
        JUnitUtil.waitFor(north::isOccupied, "North occupied by the placed train");

        warrant = new Warrant("IW1", "SimulatedWarrant");
        List<BlockOrder> orders = new ArrayList<>();
        orders.add(new BlockOrder(north, "N", "", "NorthWest"));
        orders.add(new BlockOrder(west, "W", "NorthWest", "WestSouth"));
        orders.add(new BlockOrder(south, "S", "WestSouth", null));
        warrant.setBlockOrders(orders);
        warrant.addThrottleCommand(new ThrottleSetting(0, "Speed", "0.0", "North"));
        warrant.addThrottleCommand(new ThrottleSetting(10, "Speed", "0.5", "North"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "NoOp", "Enter Block", "West"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "NoOp", "Enter Block", "South"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "Speed", "0.0", "South"));
        warrant.getSpeedUtil().setAddress("3(S)");
        warrant.setTrainName("T");
        assertNull(warrant.allocateRoute(false, orders), "route allocated");
        assertNull(warrant.setRunMode(Warrant.MODE_RUN, null, null, null, false), "warrant running");

        // the train enters West, then South, as the sensors report
        waitForSensor(west, Sensor.ACTIVE);
        SimulationTestUtil.waitForFrontIn(train, south);
        waitForSensor(south, Sensor.ACTIVE);
        assertTrue(timeOf(west.getSensor(), Sensor.ACTIVE) - timeOf(north.getSensor(), Sensor.ACTIVE) > 0,
                "West after North");
        assertTrue(timeOf(south.getSensor(), Sensor.ACTIVE) - timeOf(west.getSensor(), Sensor.ACTIVE) > 0,
                "South after West");
        // the Warrant stops it in South and ends
        SimulationTestUtil.waitForTruth(
                tr -> Objects.requireNonNull(tr.getTrain(train), "T placed").getSpeedMmPerSecond() == 0.0,
                "train stopped");
        JUnitUtil.waitFor(() -> warrant.getRunMode() == Warrant.MODE_NONE, "warrant ended");
        SimulationTruth.TrainTruth truth = SimulationTestUtil.truth(train);
        assertEquals(south, truth.getFrontBlock(), "really stopped in South");
        assertNull(truth.getHeldReason(), "stopped by its throttle, not held");
        assertTrue(SimulationTestUtil.truth().getCollisions().isEmpty());
        assertEquals(Sensor.INACTIVE, Objects.requireNonNull(north.getSensor(), "North has a sensor").getKnownState(),
                "left North");
        assertTrue(south.isOccupied(), "JMRI sees the train in South");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        SimulationTestUtil.setUpSimulatedProfile();
        source = new InternalThrottleSpeedSource();
    }

    @AfterEach
    public void tearDown() {
        if (warrant != null) {
            warrant.stopWarrant(true, false);
            warrant = null;
        }
        source.watch(Set.of());
        source = null;
        changes.clear();
        SimulationTestUtil.removeTrains();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
