package jmri.jmrit.trainrecord;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import jmri.Block;
import jmri.TrainRecord;

/**
 * Where a train's leading end is estimated to be: a range of distances past
 * the boundary where its detected part entered a Block, worked out for one
 * moment. Detection sees wheels, not buffers, so the leading end was already
 * its overhang past the boundary when the Block's sensor reported, and may
 * reach past the far end of the Block by up to its overhang before the next
 * Block's sensor reports (see {@link TrainOverhang}).
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
@Immutable
public final class TrainPositionEstimate {

    private final Block block;
    private final TrainRecord.End end;
    private final double minMm;
    private final double maxMm;
    private final boolean limitedByBlock;
    private final double minSpeed;
    private final double maxSpeed;
    private final long timeNanos;

    /**
     * @param block          the Block the leading end entered
     * @param end            the end of the train that leads
     * @param minMm          the least distance it can have gone into the
     *                       Block, in millimetres
     * @param maxMm          the most, in millimetres
     * @param limitedByBlock true if the train's detected part was taken to
     *                       have gone no further than the Block's length, as
     *                       it hasn't been seen to enter the next Block
     * @param minSpeed       the least speed it can now be moving at, in
     *                       millimetres per second
     * @param maxSpeed       the most, in millimetres per second
     * @param timeNanos      the moment, on the {@link System#nanoTime()}
     *                       scale
     */
    public TrainPositionEstimate(@Nonnull Block block, @Nonnull TrainRecord.End end, double minMm, double maxMm,
            boolean limitedByBlock, double minSpeed, double maxSpeed, long timeNanos) {
        if (!(minMm >= 0) || !(maxMm >= minMm)) {
            throw new IllegalArgumentException("not a range of distances: " + minMm + " to " + maxMm);
        }
        if (!(minSpeed >= 0) || !(maxSpeed >= minSpeed)) {
            throw new IllegalArgumentException("not a range of speeds: " + minSpeed + " to " + maxSpeed);
        }
        this.block = Objects.requireNonNull(block, "block");
        this.end = Objects.requireNonNull(end, "end");
        this.minMm = minMm;
        this.maxMm = maxMm;
        this.limitedByBlock = limitedByBlock;
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
        this.timeNanos = timeNanos;
    }

    /**
     * @return the Block the leading end entered, and is estimated to be in
     */
    @Nonnull
    public Block getBlock() {
        return block;
    }

    /**
     * @return the end of the train that leads
     */
    @Nonnull
    public TrainRecord.End getEnd() {
        return end;
    }

    /**
     * @return the least distance the leading end can have gone past the
     *         boundary where it entered the Block, in millimetres
     */
    public double getMinMm() {
        return minMm;
    }

    /**
     * @return the most distance the leading end can have gone past the
     *         boundary where it entered the Block, in millimetres
     */
    public double getMaxMm() {
        return maxMm;
    }

    /**
     * @return true if the train's detected part has been taken to have gone
     *         no further than the way on through the Block (see
     *         {@link TrainPositionEstimator#lengthOn}): from the speed alone it
     *         would have gone further, but it hasn't been seen to enter the
     *         next Block. The leading end may then reach into the next Block
     *         by up to its overhang.
     */
    public boolean isLimitedByBlock() {
        return limitedByBlock;
    }

    /**
     * @return the least speed the train can now be moving at, in millimetres
     *         per second
     */
    public double getMinSpeed() {
        return minSpeed;
    }

    /**
     * @return the most speed the train can now be moving at, in millimetres
     *         per second
     */
    public double getMaxSpeed() {
        return maxSpeed;
    }

    /**
     * @return the moment this is for, on the {@link System#nanoTime()} scale
     */
    public long getTimeNanos() {
        return timeNanos;
    }

    @Override
    public String toString() {
        return String.format("TrainPositionEstimate[%s %s, %.0f to %.0f mm%s, %.0f to %.0f mm/s]",
                end, block.getDisplayName(), minMm, maxMm, limitedByBlock ? " (end of Block)" : "",
                minSpeed, maxSpeed);
    }
}
