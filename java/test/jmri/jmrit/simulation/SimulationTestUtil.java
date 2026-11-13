package jmri.jmrit.simulation;

import java.util.function.Predicate;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.Block;
import jmri.BlockManager;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.LocoAddress;
import jmri.Path;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import jmri.jmrix.ConnectionConfigManager;
import jmri.util.JUnitUtil;

/**
 * Simulated trains for tests in any package: a fully simulated profile with
 * an Internal connection, a plain layout of Blocks with internal sensors,
 * trains placed on it, throttles to drive them, and the ground truth to
 * compare with what JMRI concluded.
 * <p>
 * Typical use, in a test's set-up after {@code JUnitUtil.setUp()}:
 * <pre>
 * SimulationTestUtil.setUpSimulatedProfile();
 * Block[] b = SimulationTestUtil.line(500, "A", "B", "C");
 * VirtualTrain t = SimulationTestUtil.train("T", 3, 200, 250);
 * SimulationTestUtil.place(t, b[1], b[0], 300);
 * SimulationTestUtil.throttle(3).setSpeedSetting(0.5f);
 * SimulationTestUtil.waitForFrontIn(t, b[2]);
 * </pre>
 * and in its tear-down, once anything driving the trains has been stopped
 * and before {@code JUnitUtil.tearDown()}, {@link #removeTrains()}.
 * <p>
 * Trains here move in real time, on the simulator's own thread, as they do
 * in use. Dispatcher, Warrants and the throttles' users run on real timers
 * and threads, so a simulated train they drive has to move on the same
 * clock. The simulator's own tests also run it step by step on a manual
 * clock, so that the times they check are exact; that clock is kept inside
 * the package, because a test of other JMRI code driven by it would see
 * JMRI's timers and the trains disagree about the time. Tests using this
 * class should therefore assert the order of events and the ground truth,
 * not how long things took: wait for a condition with
 * {@link #waitForFrontIn} or {@link #waitForTruth}, never for a time.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public final class SimulationTestUtil {

    private SimulationTestUtil() {
        // static methods only
    }

    /**
     * Make the profile fully simulated, with one Internal connection: a
     * connection manager listing an Internal connection (with no adapter
     * started), internal sensor and turnout managers, and a debug throttle
     * manager on the Internal connection. Call after
     * {@code JUnitUtil.setUp()}.
     */
    public static void setUpSimulatedProfile() {
        JUnitUtil.initConnectionConfigManager();
        InstanceManager.getDefault(ConnectionConfigManager.class).add(new jmri.jmrix.internal.ConnectionConfig());
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initDebugThrottleManager();
    }

    /**
     * A Block with an internal sensor, which is set inactive: Block "IB" +
     * name and sensor "IS" + name, with the name as the Block's user name.
     *
     * @param name     the Block's user name
     * @param lengthMm its length
     * @return the Block
     */
    @Nonnull
    public static Block block(@Nonnull String name, double lengthMm) {
        Block b = InstanceManager.getDefault(BlockManager.class).provideBlock("IB" + name);
        b.setUserName(name);
        b.setLength((float) lengthMm);
        Sensor s = InstanceManager.getDefault(SensorManager.class).provideSensor("IS" + name);
        try {
            s.setKnownState(Sensor.INACTIVE);
        } catch (JmriException e) {
            throw new IllegalStateException(e);
        }
        b.setSensor(s.getSystemName());
        return b;
    }

    /**
     * Join two Blocks with plain paths, each naming the other.
     *
     * @param a one Block
     * @param b the other
     */
    public static void join(@Nonnull Block a, @Nonnull Block b) {
        a.addPath(new Path(b, 0, 0));
        b.addPath(new Path(a, 0, 0));
    }

    /**
     * Blocks of the same length in a line, each joined to the next, made by
     * {@link #block}.
     *
     * @param lengthMm each Block's length
     * @param names    the Blocks' names, in order
     * @return the Blocks, in order
     */
    @Nonnull
    public static Block[] line(double lengthMm, @Nonnull String... names) {
        double[] lengths = new double[names.length];
        java.util.Arrays.fill(lengths, lengthMm);
        return line(names, lengths);
    }

    /**
     * Blocks in a line, each joined to the next, made by {@link #block}.
     *
     * @param names     the Blocks' names, in order
     * @param lengthsMm their lengths
     * @return the Blocks, in order
     */
    @Nonnull
    public static Block[] line(@Nonnull String[] names, @Nonnull double[] lengthsMm) {
        if (names.length != lengthsMm.length) {
            throw new IllegalArgumentException("one length for each name");
        }
        Block[] blocks = new Block[names.length];
        for (int i = 0; i < names.length; i++) {
            blocks[i] = block(names[i], lengthsMm[i]);
            if (i > 0) {
                join(blocks[i - 1], blocks[i]);
            }
        }
        return blocks;
    }

    /**
     * The address a throttle on the Internal connection uses for a number:
     * short up to 127, long above.
     *
     * @param number the address number
     * @return the address
     */
    @Nonnull
    public static DccLocoAddress address(int number) {
        return new DccLocoAddress(number, number > 127);
    }

    /**
     * A one-loco train, detected along its whole length, with no momentum,
     * running in proportion to its throttle.
     *
     * @param name                 the train's name
     * @param address              its loco's address number (see
     *                             {@link #address})
     * @param lengthMm             its length
     * @param fullSpeedMmPerSecond its speed at full throttle
     * @return the train
     */
    @Nonnull
    public static VirtualTrain train(@Nonnull String name, int address, double lengthMm,
            double fullSpeedMmPerSecond) {
        return VirtualTrain.builder(name)
                .loco(SimulatedLoco.builder(address(address)).length(lengthMm).fullSpeed(fullSpeedMmPerSecond).build())
                .length(lengthMm)
                .detection(Detection.entireTrain())
                .build();
    }

    /**
     * @return the default simulated layout, which takes speed commands from
     *         the Internal connection's throttles
     */
    @Nonnull
    public static SimulatedLayout layout() {
        return SimulatedLayout.getDefault();
    }

    /**
     * Place a train on the default simulated layout.
     *
     * @param train   the train
     * @param block   the Block its front is in
     * @param from    the Block its rear is towards, or null at the end of
     *                the track
     * @param frontMm how far its front is into the Block
     */
    public static void place(@Nonnull VirtualTrain train, @Nonnull Block block, @CheckForNull Block from,
            double frontMm) {
        layout().place(train, block, from, frontMm);
    }

    /**
     * A throttle from the InstanceManager's throttle manager, as Dispatcher,
     * Warrants and throttle windows get one.
     *
     * @param address the address number (see {@link #address})
     * @return the throttle
     */
    @Nonnull
    public static DccThrottle throttle(int address) {
        DccThrottle[] found = new DccThrottle[1];
        ThrottleListener listener = new ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                found[0] = t;
            }

            @Override
            public void notifyFailedThrottleRequest(LocoAddress a, String reason) {
                throw new IllegalStateException("no throttle for " + a + ": " + reason);
            }

            @Override
            public void notifyDecisionRequired(LocoAddress a, DecisionType question) {
                InstanceManager.getDefault(ThrottleManager.class).responseThrottleDecision(a, this, question);
            }
        };
        InstanceManager.getDefault(ThrottleManager.class).requestThrottle(address(address), listener, true);
        JUnitUtil.waitFor(() -> found[0] != null, "throttle for " + address);
        return found[0];
    }

    /**
     * @return the default layout's ground truth now
     */
    @Nonnull
    public static SimulationTruth truth() {
        return layout().getTruth();
    }

    /**
     * @param train a placed train
     * @return its ground truth now
     */
    @Nonnull
    public static SimulationTruth.TrainTruth truth(@Nonnull VirtualTrain train) {
        SimulationTruth.TrainTruth t = truth().getTrain(train);
        if (t == null) {
            throw new IllegalStateException("train " + train.getName() + " is not placed");
        }
        return t;
    }

    /**
     * Wait until a train's front is really in a Block, by the ground truth.
     *
     * @param train the train
     * @param block the Block
     */
    public static void waitForFrontIn(@Nonnull VirtualTrain train, @Nonnull Block block) {
        waitForTruth(t -> {
            SimulationTruth.TrainTruth tt = t.getTrain(train);
            return tt != null && tt.getFrontBlock() == block;
        }, "front of " + train.getName() + " in " + block.getDisplayName());
    }

    /**
     * Wait until the ground truth meets a condition.
     *
     * @param condition   the condition
     * @param description what is waited for, for the failure message
     */
    public static void waitForTruth(@Nonnull Predicate<SimulationTruth> condition, @Nonnull String description) {
        JUnitUtil.waitFor(() -> condition.test(truth()), description);
    }

    /**
     * Remove every train and wait for the simulator's thread to stop. Call in
     * a test's tear-down, before {@code JUnitUtil.tearDown()}.
     * <p>
     * Removing a train leaves every Block it is in, so their sensors go
     * inactive. Call this only after anything driving the trains has been
     * stopped: Dispatcher's Active Trains terminated and Warrants ended.
     * Otherwise Dispatcher or the Warrant sees its train leave its Blocks
     * without entering others, and logs that the train has gone.
     */
    public static void removeTrains() {
        SimulatedLayout layout = InstanceManager.getNullableDefault(SimulatedLayout.class);
        if (layout != null) {
            layout.removeAll();
            JUnitUtil.waitFor(() -> !layout.isMoverRunning(), "simulated trains' thread stopped");
        }
    }
}
