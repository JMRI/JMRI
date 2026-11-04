package jmri.jmrit.simulation;

import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InvokeOnAnyThread;
import jmri.Block;

/**
 * Where every simulated train really is, at one moment: for tests to check
 * what JMRI concluded against, and for showing beside what JMRI believes.
 * A copy: it does not change as the trains move.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
@javax.annotation.concurrent.Immutable
public final class SimulationTruth {

    private final long timeNanos;
    private final List<TrainTruth> trains;
    private final List<Collision> collisions;
    private final List<DropOut> dropOuts;
    private final List<String> faults;

    SimulationTruth(long timeNanos, List<TrainTruth> trains, List<Collision> collisions, List<DropOut> dropOuts,
            List<String> faults) {
        this.timeNanos = timeNanos;
        this.trains = Collections.unmodifiableList(trains);
        this.collisions = Collections.unmodifiableList(collisions);
        this.dropOuts = Collections.unmodifiableList(dropOuts);
        this.faults = Collections.unmodifiableList(faults);
    }

    /**
     * @return when this was true, on the {@link System#nanoTime()} scale
     */
    @InvokeOnAnyThread
    public long getTimeNanos() {
        return timeNanos;
    }

    /**
     * @return where each placed train is
     */
    @Nonnull
    @InvokeOnAnyThread
    public List<TrainTruth> getTrains() {
        return trains;
    }

    /**
     * @param train a train
     * @return its truth, or null if it is not placed
     */
    @CheckForNull
    @InvokeOnAnyThread
    public TrainTruth getTrain(@Nonnull VirtualTrain train) {
        for (TrainTruth t : trains) {
            if (t.getTrain() == train) {
                return t;
            }
        }
        return null;
    }

    /**
     * @return every collision since the trains involved were placed
     */
    @Nonnull
    @InvokeOnAnyThread
    public List<Collision> getCollisions() {
        return collisions;
    }

    /**
     * @return every detector drop-out since the trains were placed, in the
     *         order they began
     * @see SimulatedFaults#dropOut
     */
    @Nonnull
    @InvokeOnAnyThread
    public List<DropOut> getDropOuts() {
        return dropOuts;
    }

    /**
     * @return the faults set, in words, in the order set; empty when there
     *         are none
     * @see SimulatedFaults
     */
    @Nonnull
    @InvokeOnAnyThread
    public List<String> getFaults() {
        return faults;
    }

    /** A time a Block's detector didn't see the train in it. */
    @javax.annotation.concurrent.Immutable
    public static final class DropOut {

        private final Block block;
        private final VirtualTrain train;
        private final long startNanos;
        private final long endNanos;

        DropOut(Block block, VirtualTrain train, long startNanos, long endNanos) {
            this.block = block;
            this.train = train;
            this.startNanos = startNanos;
            this.endNanos = endNanos;
        }

        /**
         * @return the Block whose detector dropped out
         */
        @Nonnull
        @InvokeOnAnyThread
        public Block getBlock() {
            return block;
        }

        /**
         * @return the train it didn't see
         */
        @Nonnull
        @InvokeOnAnyThread
        public VirtualTrain getTrain() {
            return train;
        }

        /**
         * @return when the detector stopped seeing the train, on the
         *         {@link System#nanoTime()} scale
         */
        @InvokeOnAnyThread
        public long getStartNanos() {
            return startNanos;
        }

        /**
         * @return when it saw the train again, or 0 while it still doesn't
         */
        @InvokeOnAnyThread
        public long getEndNanos() {
            return endNanos;
        }

        DropOut ended(long when) {
            return new DropOut(block, train, startNanos, when);
        }
    }

    /** Where one train is. */
    @javax.annotation.concurrent.Immutable
    public static final class TrainTruth {

        private final VirtualTrain train;
        private final List<Block> blocks;
        private final double frontPositionMm;
        private final double speedMmPerSecond;
        private final boolean crashed;
        private final boolean stalled;
        private final boolean derailed;
        @CheckForNull
        private final String heldReason;

        TrainTruth(VirtualTrain train, List<Block> blocks, double frontPositionMm, double speedMmPerSecond,
                boolean crashed, boolean stalled, boolean derailed, @CheckForNull String heldReason) {
            this.train = train;
            this.blocks = Collections.unmodifiableList(blocks);
            this.frontPositionMm = frontPositionMm;
            this.speedMmPerSecond = speedMmPerSecond;
            this.crashed = crashed;
            this.stalled = stalled;
            this.derailed = derailed;
            this.heldReason = heldReason;
        }

        /**
         * @return the train
         */
        @Nonnull
        @InvokeOnAnyThread
        public VirtualTrain getTrain() {
            return train;
        }

        /**
         * @return the Blocks the train is in, from its front (the end the
         *         first loco is at) to its rear
         */
        @Nonnull
        @InvokeOnAnyThread
        public List<Block> getBlocks() {
            return blocks;
        }

        /**
         * @return the Block the front of the train is in
         */
        @Nonnull
        @InvokeOnAnyThread
        public Block getFrontBlock() {
            return blocks.get(0);
        }

        /**
         * @return how far the front of the train is into its Block, mm,
         *         measured from where a train running front first enters it
         */
        @InvokeOnAnyThread
        public double getFrontPositionMm() {
            return frontPositionMm;
        }

        /**
         * @return speed in mm/s: positive when moving front first, negative
         *         when moving rear first
         */
        @InvokeOnAnyThread
        public double getSpeedMmPerSecond() {
            return speedMmPerSecond;
        }

        /**
         * @return true if the train has been in a collision and is stopped
         */
        @InvokeOnAnyThread
        public boolean isCrashed() {
            return crashed;
        }

        /**
         * @return true if the train is stalled, so does not move whatever its
         *         locos are told
         * @see SimulatedFaults#stall
         */
        @InvokeOnAnyThread
        public boolean isStalled() {
            return stalled;
        }

        /**
         * @return true if the train has derailed, because the points were moved
         *         under it, and is stopped
         * @see SimulatedFaults#rerail
         */
        @InvokeOnAnyThread
        public boolean isDerailed() {
            return derailed;
        }

        /**
         * @return why the train is held where its locos are driving it, or
         *         null if it is not held
         */
        @CheckForNull
        @InvokeOnAnyThread
        public String getHeldReason() {
            return heldReason;
        }
    }

    /** Two trains that ran into each other. */
    @javax.annotation.concurrent.Immutable
    public static final class Collision {

        private final VirtualTrain first;
        private final VirtualTrain second;
        private final Block block;
        private final long timeNanos;

        Collision(VirtualTrain first, VirtualTrain second, Block block, long timeNanos) {
            this.first = first;
            this.second = second;
            this.block = block;
            this.timeNanos = timeNanos;
        }

        /**
         * @return one of the trains
         */
        @Nonnull
        @InvokeOnAnyThread
        public VirtualTrain getFirst() {
            return first;
        }

        /**
         * @return the other train
         */
        @Nonnull
        @InvokeOnAnyThread
        public VirtualTrain getSecond() {
            return second;
        }

        /**
         * @return the Block where they met
         */
        @Nonnull
        @InvokeOnAnyThread
        public Block getBlock() {
            return block;
        }

        /**
         * @return when, on the {@link System#nanoTime()} scale
         */
        @InvokeOnAnyThread
        public long getTimeNanos() {
            return timeNanos;
        }

        /**
         * @param train a train
         * @return true if it was one of the two
         */
        @InvokeOnAnyThread
        public boolean involves(@Nonnull VirtualTrain train) {
            return first == train || second == train;
        }
    }
}
