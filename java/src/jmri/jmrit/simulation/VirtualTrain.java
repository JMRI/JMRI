package jmri.jmrit.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jmri.InvokeOnAnyThread;
import javax.annotation.CheckForNull;
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
    @CheckForNull
    private final String idTag;

    private VirtualTrain(Builder b) {
        name = b.name;
        locos = Collections.unmodifiableList(new ArrayList<>(b.locos));
        lengthMm = b.lengthMm;
        detection = b.detection;
        speedFactorPercent = b.speedFactorPercent;
        lengthOffsetMm = b.lengthOffsetMm;
        adjustmentOutsideConsist = b.adjustmentOutsideConsist;
        idTag = b.idTag;
    }

    /**
     * Unpowered stock: the part of a train left behind by a breakaway, with no
     * loco. Made only by {@link SimulatedFaults#breakaway}.
     *
     * @param name     its name
     * @param lengthMm its length, mm
     */
    private VirtualTrain(String name, double lengthMm) {
        this.name = name;
        locos = Collections.emptyList();
        this.lengthMm = lengthMm;
        detection = Detection.entireTrain();
        speedFactorPercent = 0;
        lengthOffsetMm = 0;
        adjustmentOutsideConsist = false;
        idTag = null;
    }

    /**
     * @param name     its name
     * @param lengthMm its length, mm
     * @return unpowered stock
     */
    static VirtualTrain stock(String name, double lengthMm) {
        return new VirtualTrain(name, lengthMm);
    }

    /**
     * @return true for unpowered stock, which has no loco: the rear of a
     *         train after a breakaway
     */
    @InvokeOnAnyThread
    public boolean isStock() {
        return locos.isEmpty();
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
        @CheckForNull
        private String idTag;
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
         * @param tagId the ID tag the train's front carries, such as an RFID
         *              tag's 10 hex digits or a RailCom address, read by the
         *              Blocks' reporters as it comes in; none unless set
         * @return this builder
         */
        @InvokeOnAnyThread
        public Builder idTag(@CheckForNull String tagId) {
            this.idTag = tagId;
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
     * @return the train's locos, the first at the front; none for stock
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
     * @return the ID tag the train's front carries, which Blocks' reporters
     *         read as it comes in, or null if it carries none
     */
    @CheckForNull
    @InvokeOnAnyThread
    public String getIdTag() {
        return idTag;
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
