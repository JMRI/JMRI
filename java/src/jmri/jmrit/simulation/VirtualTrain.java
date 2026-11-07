package jmri.jmrit.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jmri.InvokeOnAnyThread;
import javax.annotation.Nonnull;

/**
 * A simulated train: a name, its locos (the first is at the front), its
 * length, how detectors see it, and how the truth differs from what JMRI is
 * told.
 * <p>
 * The truth offsets let a test check JMRI's estimates against a reality that
 * is not simply their own input: the train can run a set percentage faster or
 * slower than its locos' profiles, and be longer or shorter than the length
 * JMRI is given. Both are 0 unless set. A third option makes its decoders
 * apply CV23 and CV24 outside a consist, as some decoders may (their manuals
 * don't say); off unless set.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
@javax.annotation.concurrent.Immutable
public final class VirtualTrain {

    private final String name;
    private final List<SimulatedLoco> locos;
    private final double lengthMm;
    private final Detection detection;
    private final double speedFactorPercent;
    private final double lengthOffsetMm;
    private final boolean adjustmentOutsideConsist;

    private VirtualTrain(Builder b) {
        name = b.name;
        locos = Collections.unmodifiableList(new ArrayList<>(b.locos));
        lengthMm = b.lengthMm;
        detection = b.detection;
        speedFactorPercent = b.speedFactorPercent;
        lengthOffsetMm = b.lengthOffsetMm;
        adjustmentOutsideConsist = b.adjustmentOutsideConsist;
    }

    /**
     * @param name the train's name, for messages and ground truth
     * @return a builder
     */
    @InvokeOnAnyThread
    public static Builder builder(@Nonnull String name) {
        return new Builder(name);
    }

    /** Builds a {@link VirtualTrain}. */
    public static final class Builder {

        private final String name;
        private final List<SimulatedLoco> locos = new ArrayList<>();
        private double lengthMm;
        private Detection detection = Detection.entireTrain();
        private double speedFactorPercent;
        private double lengthOffsetMm;
        private boolean adjustmentOutsideConsist;

        private Builder(String name) {
            this.name = name;
        }

        /**
         * @param loco a loco, added behind any already added
         * @return this
         */
        @InvokeOnAnyThread
        public Builder loco(@Nonnull SimulatedLoco loco) {
            locos.add(loco);
            return this;
        }

        /**
         * @param mm the train's length as JMRI is told it
         * @return this
         */
        @InvokeOnAnyThread
        public Builder length(double mm) {
            lengthMm = mm;
            return this;
        }

        /**
         * @param d how detectors see the train
         * @return this
         */
        @InvokeOnAnyThread
        public Builder detection(@Nonnull Detection d) {
            detection = d;
            return this;
        }

        /**
         * Truth offset: run faster (positive) or slower (negative) than the
         * locos' profiles.
         *
         * @param percent the difference, in percent
         * @return this
         */
        @InvokeOnAnyThread
        public Builder speedFactorPercent(double percent) {
            speedFactorPercent = percent;
            return this;
        }

        /**
         * Truth offset: be longer (positive) or shorter (negative) than the
         * length JMRI is told.
         *
         * @param mm the difference
         * @return this
         */
        @InvokeOnAnyThread
        public Builder lengthOffsetMm(double mm) {
            lengthOffsetMm = mm;
            return this;
        }

        /**
         * Truth option: the train's decoders apply CV23 and CV24 outside a
         * consist too, as decoders whose manuals don't say may do.
         *
         * @param applies true to apply them outside a consist
         * @return this
         */
        @InvokeOnAnyThread
        public Builder appliesAdjustmentOutsideConsist(boolean applies) {
            adjustmentOutsideConsist = applies;
            return this;
        }

        /**
         * @return the train
         * @throws IllegalArgumentException with a message for the user if it
         *                                  has no loco, no length, needs a loco
         *                                  length it lacks, or would run 100% or
         *                                  more slower than its profile
         */
        @InvokeOnAnyThread
        public VirtualTrain build() {
            if (locos.isEmpty()) {
                throw new IllegalArgumentException(Bundle.getMessage("TrainNeedsLoco", name));
            }
            if (lengthMm <= 0 || lengthMm + lengthOffsetMm <= 0) {
                throw new IllegalArgumentException(Bundle.getMessage("TrainNeedsLength", name));
            }
            if ((detection.getMode() == Detection.Mode.HEAD_ONLY || detection.getMode() == Detection.Mode.HEAD_AND_TAIL)
                    && locos.get(0).getLengthMm() <= 0) {
                throw new IllegalArgumentException(Bundle.getMessage("TrainNeedsLocoLength", name));
            }
            if (speedFactorPercent <= -100) {
                throw new IllegalArgumentException(Bundle.getMessage("SpeedFactorTooLow"));
            }
            return new VirtualTrain(this);
        }
    }

    /**
     * @return the train's name
     */
    @Nonnull
    @InvokeOnAnyThread
    public String getName() {
        return name;
    }

    /**
     * @return the train's locos, the first at the front
     */
    @Nonnull
    @InvokeOnAnyThread
    public List<SimulatedLoco> getLocos() {
        return locos;
    }

    /**
     * @return the length JMRI is told, mm
     */
    @InvokeOnAnyThread
    public double getLengthMm() {
        return lengthMm;
    }

    /**
     * @return the true length, mm: the length JMRI is told plus the offset
     */
    @InvokeOnAnyThread
    public double getTrueLengthMm() {
        return lengthMm + lengthOffsetMm;
    }

    /**
     * @return how detectors see the train
     */
    @Nonnull
    @InvokeOnAnyThread
    public Detection getDetection() {
        return detection;
    }

    /**
     * @return the truth offset: how much faster (positive) or slower (negative) than its
     *         profiles the train runs, in percent
     */
    @InvokeOnAnyThread
    public double getSpeedFactorPercent() {
        return speedFactorPercent;
    }

    /**
     * @return the truth offset: how much longer (positive) or shorter (negative) than JMRI is
     *         told the train is, mm
     */
    @InvokeOnAnyThread
    public double getLengthOffsetMm() {
        return lengthOffsetMm;
    }

    /**
     * @return true if the train's decoders apply CV23 and CV24 outside a
     *         consist (a truth option)
     */
    @InvokeOnAnyThread
    public boolean isAdjustmentAppliedOutsideConsist() {
        return adjustmentOutsideConsist;
    }

    /**
     * @return the train's name
     */
    @Override
    @InvokeOnAnyThread
    public String toString() {
        return name;
    }
}
