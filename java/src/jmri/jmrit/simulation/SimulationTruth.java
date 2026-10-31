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

    SimulationTruth(long timeNanos, List<TrainTruth> trains, List<Collision> collisions) {
        this.timeNanos = timeNanos;
        this.trains = Collections.unmodifiableList(trains);
        this.collisions = Collections.unmodifiableList(collisions);
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

    /** Where one train is. */
    @javax.annotation.concurrent.Immutable
    public static final class TrainTruth {

        private final VirtualTrain train;
        private final List<Block> blocks;
        private final double frontPositionMm;
        private final double speedMmPerSecond;
        private final boolean crashed;
        @CheckForNull
        private final String heldReason;

        TrainTruth(VirtualTrain train, List<Block> blocks, double frontPositionMm, double speedMmPerSecond,
                boolean crashed, @CheckForNull String heldReason) {
            this.train = train;
            this.blocks = Collections.unmodifiableList(blocks);
            this.frontPositionMm = frontPositionMm;
            this.speedMmPerSecond = speedMmPerSecond;
            this.crashed = crashed;
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
