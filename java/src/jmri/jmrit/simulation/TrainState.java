package jmri.jmrit.simulation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.CheckForNull;

import jmri.Block;
import jmri.DccLocoAddress;

/**
 * A placed train as the mover keeps it: where it is along the Blocks it
 * occupies, its decoders, and which of its detected stretches are in which
 * Block.
 * <p>
 * Positions are measured along the train's own way, increasing towards its
 * front. The way is a list of spans, one for each Block, from the rear of the
 * train to the front; spans are added ahead of the train (or behind, when it
 * runs rear first) as it is about to reach them, and dropped when it has left
 * them.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
final class TrainState {

    /** One Block along the train's way. */
    static final class Span {

        /** The way through the Block, followed front first. */
        final Passage passage;
        final double start;
        final double end;

        Span(Passage passage, double start, double end) {
            this.passage = passage;
            this.start = start;
            this.end = end;
        }

        Block block() {
            return passage.entry.getBlock();
        }

        double length() {
            return end - start;
        }
    }

    /** A detected stretch of the train being in a span. */
    static final class Membership {

        final int marker;
        final Span span;

        Membership(int marker, Span span) {
            this.marker = marker;
            this.span = span;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Membership && ((Membership) o).marker == marker && ((Membership) o).span == span;
        }

        @Override
        public int hashCode() {
            return Objects.hash(marker, System.identityHashCode(span));
        }
    }

    final VirtualTrain train;
    final double length;
    /** The detected stretches: {offset from front, length}. */
    final List<double[]> markers;
    final List<Span> spans = new ArrayList<>();
    final Set<Membership> in = new HashSet<>();
    final DecoderModel[] decoders;
    final SimulatedSpeedSource.Command[] latched;
    final long placedNanos;
    /** The front position. */
    double position;
    boolean crashed;
    @CheckForNull
    Message held;
    double speed;

    // limits worked out for the current step
    double frontLimit;
    double rearLimit;
    @CheckForNull
    Message frontReason;
    @CheckForNull
    Message rearReason;

    TrainState(VirtualTrain train, long placedNanos) {
        this.train = train;
        this.length = train.getTrueLengthMm();
        this.markers = train.getDetection().resolve(length, train.getLocos().get(0).getLengthMm());
        this.placedNanos = placedNanos;
        int n = train.getLocos().size();
        decoders = new DecoderModel[n];
        latched = new SimulatedSpeedSource.Command[n];
        for (int i = 0; i < n; i++) {
            decoders[i] = DecoderModel.of(train.getLocos().get(i), train.isAdjustmentAppliedOutsideConsist());
        }
    }

    Span front() {
        return spans.get(spans.size() - 1);
    }

    Span rear() {
        return spans.get(0);
    }

    double rearPosition() {
        return position - length;
    }

    /**
     * The decoder's signed target setting for a command.
     */
    private static double target(SimulatedLoco loco, @CheckForNull SimulatedSpeedSource.Command c) {
        if (c == null) {
            return 0.0;
        }
        boolean forward = c.isForward();
        if (loco.isInConsist() && (loco.getCv19() & 0x80) != 0) {
            forward = !forward; // S-9.2.1: bit 7 of CV19 reverses the loco within the consist
        }
        return forward ? c.getSetting() : -c.getSetting();
    }

    /**
     * Work out the train's velocity over a step, moving each decoder on.
     *
     * @param layout     where to find commands
     * @param stepStart  when the step starts, nanoseconds
     * @param dt         step length, seconds
     * @param locoSpeeds set to each loco's speed at the end of the step
     * @return the train's velocity, positive front first
     */
    Velocity velocity(SimulatedLayout layout, long stepStart, double dt, double[] locoSpeeds) {
        List<Velocity> each = new ArrayList<>();
        for (int i = 0; i < decoders.length; i++) {
            SimulatedLoco loco = train.getLocos().get(i);
            DccLocoAddress address = loco.getRespondingAddress();
            SimulatedSpeedSource.Command c = layout.commandFor(address);
            List<DecoderModel.Ramp> ramps = new ArrayList<>();
            DecoderModel d = decoders[i];
            if (c != null && !c.equals(latched[i])) {
                long effective = Math.max(c.getTimeNanos(), placedNanos);
                double tau = Math.max(0.0, (effective - stepStart) / 1e9);
                if (tau < dt) {
                    d.advance(target(loco, latched[i]), 0.0, tau, ramps);
                    latched[i] = c;
                    if (c.isEmergencyStop()) {
                        d.stopNow();
                    }
                    d.advance(target(loco, c), tau, dt, ramps);
                } else {
                    d.advance(target(loco, latched[i]), 0.0, dt, ramps); // not received yet
                }
            } else {
                d.advance(target(loco, latched[i]), 0.0, dt, ramps);
            }
            Velocity v = Velocity.ofLoco(ramps, loco.forwardTable(), loco.reverseTable(),
                    loco.isFacingRear() ? -1.0 : 1.0);
            each.add(v);
            locoSpeeds[i] = v.after(dt);
        }
        if (crashed) {
            return Velocity.still(0.0, dt);
        }
        Velocity v = Velocity.slowest(each, 0.0, dt);
        double factor = 1.0 + train.getSpeedFactorPercent() / 100.0;
        return factor == 1.0 ? v : v.times(factor);
    }

    /**
     * Is the train sure to stay still until a command changes?
     *
     * @param layout the layout, which holds the latest command for each
     *               decoder address
     * @return true if every decoder is stopped and commanded to stay stopped,
     *         so the train can't move until a command changes
     */
    boolean idle(SimulatedLayout layout) {
        for (int i = 0; i < decoders.length; i++) {
            SimulatedSpeedSource.Command c = layout.commandFor(train.getLocos().get(i).getRespondingAddress());
            if (decoders[i].getSetting() != 0.0 || (c != null && c.getSetting() > 0)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add the next Block ahead of the front span.
     *
     * @param models the track models, used to word the reason when the train
     *               can't go on
     * @return true if added; false with {@link #frontReason} set if the train
     *         can't go on
     */
    boolean extendFront(TrackModels models) {
        Span f = front();
        if (f.start >= position) {
            // planned ahead this step, not yet entered: check its way is still set
            Passage now = f.passage.entry.follow();
            if (now.stop == Passage.Stop.BLOCKED || !now.route.sameAs(f.passage.route)) {
                frontReason = (now.stop == Passage.Stop.BLOCKED) ? now.reason
                        : models.message("WayChangedUnderTrain", models.name(f.block()));
                return false;
            }
        }
        TrackEntry next = f.passage.next;
        if (next == null) {
            frontReason = f.passage.reason;
            return false;
        }
        Passage p = next.follow();
        if (p.stop == Passage.Stop.BLOCKED) {
            frontReason = p.reason;
            return false;
        }
        spans.add(new Span(p, f.end, f.end + p.length));
        return true;
    }

    /**
     * Add the Block behind the rear span.
     *
     * @param models the track models, used to word the reason when the train
     *               can't go back
     * @return true if added; false with {@link #rearReason} set if the train
     *         can't go back
     */
    boolean extendRear(TrackModels models) {
        Span r = rear();
        TrackEntry back = r.passage.entry.reverse();
        if (back == null) {
            rearReason = models.message("EndOfTrack", models.name(r.block()));
            return false;
        }
        Passage p = back.follow();
        TrackEntry beyond = p.back;
        if (p.stop == Passage.Stop.BLOCKED || beyond == null) {
            rearReason = p.reason;
            return false;
        }
        // the same way, followed front first
        Passage forward = new Passage(beyond, p.length, p.route.reversed(), r.passage.entry, p.entry,
                Passage.Stop.NONE, null);
        spans.add(0, new Span(forward, r.start - p.length, r.start));
        return true;
    }

    /**
     * Make sure the spans reach a distance beyond each end of the train,
     * setting the limits where they can't.
     *
     * @param models the track models
     * @param reach  how far the train might move this step
     */
    void planAhead(TrackModels models, double reach) {
        frontLimit = Double.POSITIVE_INFINITY;
        rearLimit = Double.NEGATIVE_INFINITY;
        frontReason = null;
        rearReason = null;
        // a turnout moved under the train: it can't move either way
        double r = rearPosition();
        for (Span s : spans) {
            if (s.start < position && s.end > r) {
                Passage now = s.passage.entry.follow();
                if (now.stop == Passage.Stop.BLOCKED || !now.route.sameAs(s.passage.route)) {
                    frontLimit = position;
                    rearLimit = r;
                    frontReason = models.message("WayChangedUnderTrain", models.name(s.block()));
                    rearReason = frontReason;
                    return;
                }
            }
        }
        while (front().end < position + reach) {
            if (!extendFront(models)) {
                frontLimit = front().end;
                break;
            }
        }
        while (rear().start > rearPosition() - reach) {
            if (!extendRear(models)) {
                rearLimit = rear().start;
                break;
            }
        }
    }

    /**
     * Drop the spans the train is not in.
     */
    void trim() {
        double r = rearPosition();
        spans.removeIf(s -> s.end <= r || s.start >= position);
    }

    /**
     * @return the Blocks the train is in, front first
     */
    List<Block> blocks() {
        List<Block> result = new ArrayList<>();
        double r = rearPosition();
        for (int i = spans.size() - 1; i >= 0; i--) {
            Span s = spans.get(i);
            if (s.start < position && s.end > r) {
                result.add(s.block());
            }
        }
        if (result.isEmpty()) {
            result.add(spanAt(position).block());
        }
        return result;
    }

    /**
     * @param x a position
     * @return the span containing it, preferring the one it is at the end of
     */
    Span spanAt(double x) {
        for (Span s : spans) {
            if (x > s.start && x <= s.end) {
                return s;
            }
        }
        return spans.get(0);
    }
}
