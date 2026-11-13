package jmri.jmrit.trainrecord;

import java.util.List;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import jmri.InstanceManager;
import jmri.TrainRecord;
import jmri.TrainRecord.DetectionMarker;
import jmri.TrainRecord.TrainComponent;
import jmri.implementation.SignalSpeedMap;

/**
 * How far a train reaches beyond its detected part at one end: its overhang
 * there, as a range of distances.
 * <p>
 * Detection sees wheels, never buffers: a detection marker runs from the
 * first detected wheel to the last. Whatever lies ahead of a train's first
 * marker is its leading overhang, and whatever lies behind its last marker is
 * its trailing overhang. An Entire Train train has small overhangs, its end
 * components' wheel overhangs; a Head Only train has a small overhang at its
 * front and a large one at its rear, the rest of the train; a train detected
 * only by a loco in its middle has a large overhang at both ends.
 * <p>
 * The overhangs follow from the train's markers and components, so nothing
 * about them is entered separately. An end component whose wheel overhang is
 * not known is given a generous default, so that a missing value holds a
 * little more track, never less. Where a marker's offset or length is not
 * known, the overhang is the range of what it could be, which needs the
 * train's length: without it there is no overhang to give.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
@Immutable
public final class TrainOverhang {

    /**
     * The default wheel overhang, in prototype millimetres: generous for most
     * locos and coaches, from buffer to outer axle.
     */
    public static final double DEFAULT_PROTOTYPE_MM = 4000;

    private final double minMm;
    private final double maxMm;

    TrainOverhang(double minMm, double maxMm) {
        if (!(minMm >= 0) || !(maxMm >= minMm)) {
            throw new IllegalArgumentException("not a range of overhangs: " + minMm + " to " + maxMm);
        }
        this.minMm = minMm;
        this.maxMm = maxMm;
    }

    /**
     * @return the least the train may reach beyond its detected part, in
     *         millimetres
     */
    public double getMinMm() {
        return minMm;
    }

    /**
     * @return the most the train may reach beyond its detected part, in
     *         millimetres
     */
    public double getMaxMm() {
        return maxMm;
    }

    /**
     * The default wheel overhang on this layout: {@link #DEFAULT_PROTOTYPE_MM}
     * at the layout scale set in the Warrant preferences.
     *
     * @return the overhang in millimetres
     */
    public static double defaultMm() {
        float scale = InstanceManager.getDefault(SignalSpeedMap.class).getLayoutScale();
        return scale > 0 ? DEFAULT_PROTOTYPE_MM / scale : DEFAULT_PROTOTYPE_MM / 87.1;
    }

    /**
     * A train's overhang at one end, with the layout's default for end
     * components whose wheel overhang isn't known.
     *
     * @param record the train's record
     * @param end    the end
     * @return the overhang, or null if it can't be known (see the class
     *         description)
     */
    @CheckForNull
    public static TrainOverhang of(@Nonnull TrainRecord record, @Nonnull TrainRecord.End end) {
        return of(record, end, defaultMm());
    }

    /**
     * A train's overhang at one end.
     *
     * @param record    the train's record
     * @param end       the end
     * @param defaultMm the wheel overhang to take for an end component whose
     *                  own isn't known, in millimetres, 0 or more
     * @return the overhang, or null if it can't be known (see the class
     *         description)
     */
    @CheckForNull
    public static TrainOverhang of(@Nonnull TrainRecord record, @Nonnull TrainRecord.End end, double defaultMm) {
        Objects.requireNonNull(end, "end");
        List<DetectionMarker> markers = record.getMarkers();
        List<TrainComponent> components = record.getTrainComponents();
        boolean front = end == TrainRecord.End.FRONT;
        TrainComponent endComponent = components.isEmpty() ? null
                : components.get(front ? 0 : components.size() - 1);
        Double own = endComponent == null ? null
                : (front ? endComponent.getFrontOverhang() : endComponent.getRearOverhang());
        double wheels = own != null ? own : defaultMm;
        if (markers.isEmpty()) {
            return null;
        }
        DetectionMarker m = markers.get(front ? 0 : markers.size() - 1);
        Double length = record.getHoldingLength();
        Double markerLength = m.getLength();
        switch (m.getPlacement()) {
            case WHOLE_TRAIN:
                return new TrainOverhang(wheels, wheels);
            case FRONT:
                if (front) {
                    return new TrainOverhang(wheels, wheels);
                }
                // the rest of the train lies behind a marker at its front
                return beyond(length, markerLength, wheels);
            case REAR:
                if (!front) {
                    return new TrainOverhang(wheels, wheels);
                }
                return beyond(length, markerLength, wheels);
            case OFFSET:
            default:
                Double offset = m.getOffset();
                if (offset != null && front) {
                    return new TrainOverhang(offset, offset);
                }
                if (offset != null) {
                    // behind a marker whose front is at a known offset
                    return beyond(length == null ? null : length - offset, markerLength, wheels);
                }
                // a marker somewhere in the train, not known where
                if (length == null) {
                    return null;
                }
                return new TrainOverhang(wheels,
                        Math.max(wheels, length - (markerLength != null ? markerLength : 0)));
        }
    }

    /**
     * The overhang at an end beyond a marker that reaches the other end, or
     * whose start is known: the length left, less the marker's. A marker whose
     * length isn't known may end anywhere, so the overhang is then a range,
     * from the wheel overhang up.
     *
     * @param left         the length of train from the marker's start to
     *                     this end, or null if not known
     * @param markerLength the marker's length, or null if not known
     * @param wheels       the wheel overhang at this end
     */
    @CheckForNull
    private static TrainOverhang beyond(@CheckForNull Double left, @CheckForNull Double markerLength,
            double wheels) {
        if (left == null) {
            return null;
        }
        if (markerLength != null) {
            double behind = Math.max(wheels, left - markerLength);
            return new TrainOverhang(behind, behind);
        }
        return new TrainOverhang(wheels, Math.max(wheels, left));
    }

    @Override
    public String toString() {
        return String.format("TrainOverhang[%.0f to %.0f mm]", minMm, maxMm);
    }
}
