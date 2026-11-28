package jmri.jmrit.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jmri.InvokeOnAnyThread;
import javax.annotation.Nonnull;

/**
 * Which parts of a train a detector sees: the whole train, only the loco,
 * the loco and a marker at the rear (a resistor wheelset, say), or a list of
 * detected stretches each given by its offset from the front of the train and
 * its length.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
@javax.annotation.concurrent.Immutable
public final class Detection {

    /** The kinds of detection. */
    public enum Mode {
        /** Every vehicle is detected. */
        ENTIRE_TRAIN,
        /** Only the lead loco is detected. */
        HEAD_ONLY,
        /** The lead loco, and a marker at the rear of the train. */
        HEAD_AND_TAIL,
        /** A list of detected stretches. */
        MARKERS
    }

    /** A detected stretch of a train. */
    @javax.annotation.concurrent.Immutable
    public static final class Marker {

        private final double offsetMm;
        private final double lengthMm;

        /**
         * @param offsetMm distance of its front from the front of the train, mm
         * @param lengthMm its length, mm, more than 0
         * @throws IllegalArgumentException with a message for the user if the
         *                                  offset is negative or the length
         *                                  is not more than 0
         */
        @InvokeOnAnyThread
        public Marker(double offsetMm, double lengthMm) {
            if (offsetMm < 0 || lengthMm <= 0) {
                throw new IllegalArgumentException(Bundle.getMessage("MarkerValues"));
            }
            this.offsetMm = offsetMm;
            this.lengthMm = lengthMm;
        }

        /**
         * @return distance of the marker's front from the front of the train, mm
         */
        @InvokeOnAnyThread
        public double getOffsetMm() {
            return offsetMm;
        }

        /**
         * @return the marker's length, mm
         */
        @InvokeOnAnyThread
        public double getLengthMm() {
            return lengthMm;
        }
    }

    private final Mode mode;
    private final double tailMarkerMm;
    private final List<Marker> markers;

    private Detection(Mode mode, double tailMarkerMm, List<Marker> markers) {
        this.mode = mode;
        this.tailMarkerMm = tailMarkerMm;
        this.markers = markers;
    }

    /**
     * @return detection of the whole train, the default
     */
    @InvokeOnAnyThread
    public static Detection entireTrain() {
        return new Detection(Mode.ENTIRE_TRAIN, 0, Collections.emptyList());
    }

    /**
     * @return detection of the lead loco only
     */
    @InvokeOnAnyThread
    public static Detection headOnly() {
        return new Detection(Mode.HEAD_ONLY, 0, Collections.emptyList());
    }

    /**
     * @param tailMarkerMm length of the marker at the rear, mm
     * @return detection of the lead loco and a marker at the rear
     * @throws IllegalArgumentException with a message for the user if the
     *                                  length is not more than 0
     */
    @InvokeOnAnyThread
    public static Detection headAndTail(double tailMarkerMm) {
        if (tailMarkerMm <= 0) {
            throw new IllegalArgumentException(Bundle.getMessage("TailMarkerLength"));
        }
        return new Detection(Mode.HEAD_AND_TAIL, tailMarkerMm, Collections.emptyList());
    }

    /**
     * @param markers the detected stretches
     * @return detection of those stretches
     * @throws IllegalArgumentException with a message for the user if there
     *                                  is none
     */
    @InvokeOnAnyThread
    public static Detection markers(@Nonnull List<Marker> markers) {
        if (markers.isEmpty()) {
            throw new IllegalArgumentException(Bundle.getMessage("MarkerNeeded"));
        }
        return new Detection(Mode.MARKERS, 0, Collections.unmodifiableList(new ArrayList<>(markers)));
    }

    /**
     * @return the kind of detection
     */
    @InvokeOnAnyThread
    public Mode getMode() {
        return mode;
    }

    /**
     * The detected stretches of a train, each cut to the train's true length.
     *
     * @param trainLengthMm true length of the train
     * @param locoLengthMm  length of the lead loco
     * @return the stretches, as {offset from front, length}
     */
    List<double[]> resolve(double trainLengthMm, double locoLengthMm) {
        List<double[]> result = new ArrayList<>();
        double loco = Math.min(locoLengthMm, trainLengthMm);
        switch (mode) {
            case ENTIRE_TRAIN:
                result.add(new double[]{0, trainLengthMm});
                break;
            case HEAD_ONLY:
                result.add(new double[]{0, loco});
                break;
            case HEAD_AND_TAIL: {
                result.add(new double[]{0, loco});
                double tail = Math.min(tailMarkerMm, trainLengthMm);
                result.add(new double[]{trainLengthMm - tail, tail});
                break;
            }
            default:
                for (Marker m : markers) {
                    double length = Math.min(m.getLengthMm(), trainLengthMm - m.getOffsetMm());
                    if (length > 0) {
                        result.add(new double[]{m.getOffsetMm(), length});
                    }
                }
                break;
        }
        return result;
    }
}
