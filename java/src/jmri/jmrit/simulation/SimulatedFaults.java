package jmri.jmrit.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;

import jmri.Block;
import jmri.InvokeOnAnyThread;

/**
 * Faults a test or script can set on a simulated layout, so that JMRI's
 * handling of what goes wrong on real layouts can be checked against what
 * really happened. Nothing here is set unless asked for, and each fault set
 * is listed in the ground truth ({@link SimulationTruth#getFaults()}).
 * <p>
 * Detection faults change only what the detectors report; the trains move as
 * before:
 * <ul>
 * <li>a drop-out: a stretch of a Block where the detector doesn't see a
 * train's front, as on dirty track, or such stretches at random;</li>
 * <li>a phantom: a Block's sensor shows occupied with no train there;</li>
 * <li>two Blocks' sensors swapped, as if wired to each other's Block;</li>
 * <li>a Block whose real length differs from the length JMRI has.</li>
 * </ul>
 * Faults of the trains:
 * <ul>
 * <li>a stall: a train stops and doesn't move whatever its locos are told,
 * at once or when its front reaches a point, as on a dead frog;</li>
 * <li>a breakaway: a train parts at a coupling; the front part runs on with
 * its locos, and the rear coasts to a stand as unpowered stock.</li>
 * </ul>
 * A turnout moved under a train is not a fault to set: moving one is enough.
 * The train derails, stops where it is and keeps its occupancy until
 * {@link #rerail} or until it is removed.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public final class SimulatedFaults {

    /** A stretch of a Block, measured from the end next to a neighbour. */
    static final class Stretch {
        final Block block;
        @CheckForNull
        final Block from;
        final double fromMm;
        final double toMm;

        Stretch(Block block, @CheckForNull Block from, double fromMm, double toMm) {
            this.block = block;
            this.from = from;
            this.fromMm = fromMm;
            this.toMm = toMm;
        }
    }

    private final SimulatedLayout layout;
    @GuardedBy("layout")
    final List<Stretch> dropOuts = new ArrayList<>();
    @GuardedBy("layout")
    final List<Stretch> stallPoints = new ArrayList<>();
    @GuardedBy("layout")
    final Set<Block> phantoms = new HashSet<>();
    @GuardedBy("layout")
    final Map<Block, Block> swapped = new HashMap<>();
    @GuardedBy("layout")
    @CheckForNull
    Random random;
    @GuardedBy("layout")
    double randomProbability;
    @GuardedBy("layout")
    double randomLengthMm;
    @GuardedBy("layout")
    private final List<String> described = new ArrayList<>();

    SimulatedFaults(@Nonnull SimulatedLayout layout) {
        this.layout = layout;
    }

    private static String name(@CheckForNull Block b) {
        return b == null ? "the end of the track" : b.getDisplayName();
    }

    private static void checkStretch(double fromMm, double toMm) {
        if (fromMm < 0 || toMm <= fromMm) {
            throw new IllegalArgumentException("a stretch runs from 0 or more to further along");
        }
    }

    /**
     * The detector of a Block doesn't see a train while the train's front is
     * in a stretch of it, as on dirty track: the Block's sensor shows empty,
     * unless another train or another part of the train is seen there too.
     *
     * @param block  the Block
     * @param from   the neighbour Block whose end the stretch is measured
     *               from, or null for the end of the track
     * @param fromMm where the stretch starts, mm from that end
     * @param toMm   where it ends, mm from that end
     */
    @InvokeOnAnyThread
    public void dropOut(@Nonnull Block block, @CheckForNull Block from, double fromMm, double toMm) {
        checkStretch(fromMm, toMm);
        synchronized (layout) {
            dropOuts.add(new Stretch(Objects.requireNonNull(block), from, fromMm, toMm));
            described.add("drop-out in " + name(block) + " from " + fromMm + " to " + toMm + " mm from "
                    + name(from));
        }
    }

    /**
     * Drop-outs at random: each time a train's front enters a Block, with a
     * given probability, the Block's detector doesn't see the train while its
     * front is in a stretch of the given length at a random place. The same
     * seed gives the same drop-outs for the same runs.
     *
     * @param seed        the seed
     * @param probability the chance for each Block entered, 0 to 1
     * @param lengthMm    the stretch's length, mm
     */
    @InvokeOnAnyThread
    public void dropOutAtRandom(long seed, double probability, double lengthMm) {
        if (probability < 0 || probability > 1 || lengthMm <= 0) {
            throw new IllegalArgumentException("a probability from 0 to 1 and a length more than 0");
        }
        synchronized (layout) {
            random = new Random(seed);
            randomProbability = probability;
            randomLengthMm = lengthMm;
            described.add("drop-outs at random, seed " + seed + ", probability " + probability + ", "
                    + lengthMm + " mm");
        }
    }

    /**
     * A Block's sensor shows occupied with no train there, until
     * {@link #clearPhantom}. Needs a train placed somewhere, as the layout's
     * detectors are known only then; removing the last train clears it.
     *
     * @param block the Block
     * @throws IllegalStateException if no train is placed
     */
    @InvokeOnAnyThread
    public void phantom(@Nonnull Block block) {
        layout.phantom(Objects.requireNonNull(block), true);
    }

    /**
     * A Block's sensor shows what is really there again.
     *
     * @param block the Block
     */
    @InvokeOnAnyThread
    public void clearPhantom(@Nonnull Block block) {
        layout.phantom(Objects.requireNonNull(block), false);
    }

    /**
     * Two Blocks' sensors are swapped: what is in each Block is shown on the
     * other's sensor. Set before any train is placed, as a wiring fault is
     * there from the start.
     *
     * @param a one Block
     * @param b the other
     * @throws IllegalStateException if a train is placed
     */
    @InvokeOnAnyThread
    public void swapSensors(@Nonnull Block a, @Nonnull Block b) {
        synchronized (layout) {
            layout.checkNoTrains();
            swapped.put(Objects.requireNonNull(a), Objects.requireNonNull(b));
            swapped.put(b, a);
            described.add("sensors of " + name(a) + " and " + name(b) + " swapped");
        }
    }

    /**
     * A Block's real length differs from the length JMRI has: the trains
     * cross it in this length. Set before any train is placed.
     *
     * @param block    the Block
     * @param lengthMm its real length, mm
     * @throws IllegalStateException if a train is placed
     */
    @InvokeOnAnyThread
    public void setTrueLength(@Nonnull Block block, double lengthMm) {
        if (lengthMm <= 0) {
            throw new IllegalArgumentException("a length more than 0");
        }
        synchronized (layout) {
            layout.checkNoTrains();
            layout.models().setTrueLength(Objects.requireNonNull(block), lengthMm);
            described.add("real length of " + name(block) + " " + lengthMm + " mm");
        }
    }

    /**
     * A train stalls: it stops at once and doesn't move, whatever its locos
     * are told, until {@link #clearStall}.
     *
     * @param train the train
     * @throws IllegalStateException if it is not placed
     */
    @InvokeOnAnyThread
    public void stall(@Nonnull VirtualTrain train) {
        layout.stall(Objects.requireNonNull(train), true);
        synchronized (layout) {
            described.add("train " + train.getName() + " stalled");
        }
    }

    /**
     * A train stalls when its front reaches a point, as on a dead frog.
     *
     * @param block the Block
     * @param from  the neighbour Block whose end the point is measured from,
     *              or null for the end of the track
     * @param mm    where the point is, mm from that end
     */
    @InvokeOnAnyThread
    public void stallAt(@Nonnull Block block, @CheckForNull Block from, double mm) {
        if (mm < 0) {
            throw new IllegalArgumentException("a point 0 or more from the end");
        }
        synchronized (layout) {
            stallPoints.add(new Stretch(Objects.requireNonNull(block), from, mm, mm));
            described.add("stall in " + name(block) + " at " + mm + " mm from " + name(from));
        }
    }

    /**
     * A stalled train obeys its locos again.
     *
     * @param train the train
     * @throws IllegalStateException if it is not placed
     */
    @InvokeOnAnyThread
    public void clearStall(@Nonnull VirtualTrain train) {
        layout.stall(Objects.requireNonNull(train), false);
    }

    /**
     * Put a derailed train back on the track, where it is, to obey its locos
     * again; if the points are still against it, it derails again when it
     * moves.
     *
     * @param train the train
     * @throws IllegalStateException if it is not placed
     */
    @InvokeOnAnyThread
    public void rerail(@Nonnull VirtualTrain train) {
        layout.rerail(Objects.requireNonNull(train));
    }

    /**
     * A train parts at a coupling. The front part keeps the train's locos
     * and runs on as the same train; the rear becomes unpowered stock, placed
     * as a train of its own (named after the train, with " (rear)"), which
     * coasts to a stand. Each part keeps the detection markers on it.
     *
     * @param train       the train
     * @param fromFrontMm where it parts, mm from its front
     * @return the rear part
     * @throws IllegalStateException    if the train is not placed
     * @throws IllegalArgumentException if it doesn't part there: the point
     *                                  must be within the train
     */
    @Nonnull
    @InvokeOnAnyThread
    public VirtualTrain breakaway(@Nonnull VirtualTrain train, double fromFrontMm) {
        VirtualTrain rear = layout.breakaway(Objects.requireNonNull(train), fromFrontMm);
        synchronized (layout) {
            described.add("train " + train.getName() + " parted " + fromFrontMm + " mm from its front");
        }
        return rear;
    }

    /**
     * Clear every fault that is set. Trains stalled or derailed stay so until
     * {@link #clearStall} or {@link #rerail}; stock stays placed.
     */
    @InvokeOnAnyThread
    public void clearAll() {
        List<Block> wasPhantom;
        synchronized (layout) {
            wasPhantom = new ArrayList<>(phantoms);
        }
        for (Block b : wasPhantom) {
            clearPhantom(b);
        }
        synchronized (layout) {
            dropOuts.clear();
            stallPoints.clear();
            swapped.clear();
            random = null;
            layout.models().clearTrueLengths();
            described.clear();
        }
    }

    @GuardedBy("layout")
    List<String> describe() {
        List<String> list = new ArrayList<>(described);
        for (Block b : phantoms) {
            list.add("phantom occupancy in " + name(b));
        }
        return list;
    }

    @GuardedBy("layout")
    Block reportedOn(Block block) {
        return swapped.getOrDefault(block, block);
    }
}
