package jmri.jmrit.trainrecord;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.BasicRosterEntry;
import jmri.Block;
import jmri.Disposable;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.LocoAddress;
import jmri.TrainRecord;
import jmri.TrainRecord.Reason;
import jmri.TrainRecord.TrailEntry;
import jmri.TrainRecordManager;
import jmri.beans.PropertyChangeSupport;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;

/**
 * What train tracking measures as trains run, for each Block and each way
 * through it (the Blocks it was entered from and left for): how many times
 * a train's leading end has crossed it from one boundary to the other, how
 * fast, how that compares with the train's speed profile, and how often the
 * Block's detector dropped out under a train it was sure of.
 * <p>
 * A crossing is measured when a train's leading end enters a Block and then
 * the next: the time between the two, and the length of that way through
 * the Block (its route length, or the Block's length where none is set),
 * give its average speed. Where the train's lead loco has a speed profile
 * and its speed command didn't change during the crossing, the speed the
 * profile gives for that command is kept too, so the two can be compared.
 * <p>
 * Only detectors are measured from (see {@link TrainTrackingDetectors}): a
 * crossing needs one on the Block crossed and on the Block left for, and a
 * drop-out is a detector's. With no layout hardware, what is measured is of
 * simulated trains.
 * <p>
 * A drop-out is counted when train tracking keeps a train in a Block that
 * went empty under it because the train can't have left it. Where the train
 * could have gone on into a Block with no detector, it is only counted once
 * the train is seen again in the Block that went empty.
 * <p>
 * Where a way through has been crossed at different steady speeds, the
 * measured times are fitted against the times the profile gives, which
 * separates a fixed time added to every crossing, such as a detector that is
 * slow to report or a debounce delay, from a profile that is out by a
 * proportion at every speed (see {@link Measurement#getDetectorOffset()}).
 * <p>
 * When a train's rear end leaves a Block, the time since its leading end
 * crossed the same boundary, less the time the train's detected part took
 * to pass it at the speed its profile gives, is how much later JMRI knew
 * the Block behind was empty than it knew the Block ahead was occupied (see
 * {@link Boundary}). Detectors usually report empty later than occupied,
 * and many delay it on purpose, so this is the time a Block is held after a
 * train has gone.
 * <p>
 * Each crossing compared with a profile is also a prediction: the time the
 * profile gives for it against the time it took. Kept for each train (see
 * {@link Arrivals}), this says how far that train's speed model can be
 * trusted.
 * <p>
 * It only measures: nothing uses the measurements yet.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public class TrainTrackingMeasurements extends PropertyChangeSupport implements InstanceManagerAutoDefault, Disposable {

    /** Property name for a new measurement; the new value is the {@link Measurement}. */
    public static final String PROPERTY_MEASUREMENT = "measurement"; // NOI18N

    private final TrainRecordManager manager;
    private final Map<Key, Measurement> measurements = new LinkedHashMap<>();
    private final Map<Block, Integer> dropOuts = new HashMap<>();
    private final Map<Key, Boundary> boundaries = new LinkedHashMap<>();
    private final Map<TrainRecord, Arrivals> arrivals = new LinkedHashMap<>();
    /** The Block each train was last not seen in, until it is seen again or does something else. */
    private final Map<TrainRecord, Block> notSeenIn = new HashMap<>();
    private final PropertyChangeListener managerListener = this::managerChanged;
    private final PropertyChangeListener recordListener = this::recordChanged;

    /**
     * Measure the default {@link TrainRecordManager}'s records.
     */
    public TrainTrackingMeasurements() {
        this(InstanceManager.getDefault(TrainRecordManager.class));
    }

    /**
     * Measure a manager's records.
     *
     * @param manager the manager
     */
    public TrainTrackingMeasurements(@Nonnull TrainRecordManager manager) {
        this.manager = Objects.requireNonNull(manager, "manager");
        manager.addPropertyChangeListener(TrainRecordManager.PROPERTY_RECORDS, managerListener);
        for (TrainRecord r : manager.getRecords()) {
            listenTo(r);
        }
    }

    private void listenTo(TrainRecord r) {
        r.addPropertyChangeListener(TrainRecord.PROPERTY_TRAIL, recordListener);
        r.addPropertyChangeListener(TrainRecord.PROPERTY_HISTORY, recordListener);
    }

    private void managerChanged(PropertyChangeEvent e) {
        if (e.getNewValue() instanceof TrainRecord) {
            listenTo((TrainRecord) e.getNewValue());
        }
        if (e.getOldValue() instanceof TrainRecord) {
            TrainRecord r = (TrainRecord) e.getOldValue();
            notSeenIn.remove(r);
            arrivals.remove(r);
            r.removePropertyChangeListener(TrainRecord.PROPERTY_TRAIL, recordListener);
            r.removePropertyChangeListener(TrainRecord.PROPERTY_HISTORY, recordListener);
        }
    }

    // records change, and tell their listeners, on the layout thread
    private void recordChanged(PropertyChangeEvent e) {
        TrainRecord r = (TrainRecord) e.getSource();
        Object v = e.getNewValue();
        if (v instanceof TrailEntry) {
            entered(r, (TrailEntry) v);
        } else if (v instanceof Reason) {
            reason(r, (Reason) v);
        }
    }

    /**
     * Something happened to a train. A drop-out is a Block that went empty
     * under a train that can't have left it, or, where it could have gone
     * on into a Block with no detector, one that it is then seen in again.
     *
     * @param r      the train's record
     * @param reason what happened, as its record's history has it
     */
    void reason(@Nonnull TrainRecord r, @Nonnull Reason reason) {
        Block b = reason.getBlock();
        Block notSeen = notSeenIn.remove(r);
        switch (reason.getRule()) {
            case KEPT_AFTER_DROPOUT:
                droppedOut(b);
                break;
            case NOT_SEEN:
                if (b != null) {
                    notSeenIn.put(r, b);
                }
                break;
            case SEEN_AGAIN:
                if (b != null && b == notSeen) {
                    droppedOut(b);
                }
                break;
            case LEFT:
                if (b != null) {
                    left(r, b, reason.getTime());
                }
                break;
            default:
                break;
        }
    }

    private void droppedOut(@CheckForNull Block b) {
        if (b != null && TrainTrackingDetectors.isDetector(b)) {
            dropOuts.merge(b, 1, Integer::sum);
            firePropertyChange(PROPERTY_MEASUREMENT, null, null);
        }
    }

    /**
     * A train's end entered a Block: if the same end entered the Block it
     * came from at that Block's other boundary, it has crossed that Block.
     */
    private void entered(TrainRecord r, TrailEntry now) {
        Block crossed = now.getFrom();
        if (crossed == null || now.getEnd() == null) {
            return;
        }
        List<TrailEntry> trail = r.getTrail();
        TrailEntry before = null;
        for (int i = trail.size() - 1; i >= 0; i--) {
            TrailEntry t = trail.get(i);
            if (t == now) {
                continue;
            }
            if (t.getBlock() == crossed && t.getEnd() == now.getEnd()) {
                before = t;
                break;
            }
        }
        Block from = before == null ? null : before.getFrom();
        if (before == null || from == null || from == now.getBlock()) {
            return;
        }
        measure(r, crossed, from, now.getBlock(), before.getTimeNanos(), now.getTimeNanos());
    }

    /**
     * A train's leading end crossed a Block.
     *
     * @param r         the train's record
     * @param crossed   the Block
     * @param from      the Block it entered it from
     * @param to        the Block it left it for
     * @param fromNanos when it entered it
     * @param toNanos   when it entered the next
     * @return the Block's measurement for that way through it, or null if
     *         nothing could be measured: no time passed, the way through has
     *         no length, or the Block or the next has no detector
     */
    @CheckForNull
    Measurement measure(@Nonnull TrainRecord r, @Nonnull Block crossed, @Nonnull Block from, @Nonnull Block to,
            long fromNanos, long toNanos) {
        if (!TrainTrackingDetectors.isDetector(crossed) || !TrainTrackingDetectors.isDetector(to)) {
            return null; // one of the two moments wasn't a detector's
        }
        double seconds = (toNanos - fromNanos) / 1e9;
        double length = crossed.getRouteLengthMm(from, to);
        if (seconds <= 0 || length <= 0) {
            return null;
        }
        Double profile = profileSpeed(r, fromNanos, toNanos);
        Measurement m = measurements.computeIfAbsent(new Key(crossed, from, to),
                k -> new Measurement(k.block, k.from, k.to));
        m.add(seconds, length, profile);
        if (profile != null) {
            arrivals.computeIfAbsent(r, Arrivals::new).add(length / profile, seconds);
        }
        firePropertyChange(PROPERTY_MEASUREMENT, null, m);
        return m;
    }

    /**
     * A train's rear end left a Block: if its leading end crossed the same
     * boundary out of it, measure how much later the Block went empty than
     * the train's passing explains.
     *
     * @param r        the train's record
     * @param behind   the Block left
     * @param leftTime when JMRI knew it was left
     * @return the boundary's measurement, or null if nothing could be
     *         measured: the leading end wasn't seen to cross out of the
     *         Block, one of the two Blocks has no detector, the train's
     *         detected length isn't known, or its speed can't be had from
     *         a steady command and its profile
     */
    @CheckForNull
    Boundary left(@Nonnull TrainRecord r, @Nonnull Block behind, @Nonnull Instant leftTime) {
        TrailEntry crossing = null;
        List<TrailEntry> trail = r.getTrail();
        for (int i = trail.size() - 1; i >= 0; i--) {
            TrailEntry t = trail.get(i);
            if (t.getFrom() == behind && t.getEnd() != null) {
                crossing = t;
                break;
            }
        }
        if (crossing == null) {
            return null;
        }
        Block ahead = crossing.getBlock();
        if (!TrainTrackingDetectors.isDetector(behind) || !TrainTrackingDetectors.isDetector(ahead)) {
            return null;
        }
        long passNanos = Duration.between(crossing.getTime(), leftTime).toNanos();
        Double detected = detectedLength(r);
        if (passNanos <= 0 || detected == null) {
            return null;
        }
        Double speed = profileSpeed(r, crossing.getTimeNanos(), crossing.getTimeNanos() + passNanos);
        if (speed == null) {
            return null;
        }
        Boundary m = boundaries.computeIfAbsent(new Key(null, behind, ahead), k -> new Boundary(k.from, k.to));
        m.add(passNanos / 1e9 - detected / speed);
        firePropertyChange(PROPERTY_MEASUREMENT, null, m);
        return m;
    }

    /**
     * The length of a train's detected part, from its first detected wheel to
     * its last: its length less the overhang at each end, where both
     * overhangs are known exactly. That needs the end components' own wheel
     * overhangs: an overhang that rests on the default for one not entered
     * isn't known, nor is one that is only a range, as for a train detected
     * only at its head.
     *
     * @param r the train's record
     * @return the length in millimetres, or null if not known
     */
    @CheckForNull
    static Double detectedLength(@Nonnull TrainRecord r) {
        Double length = r.getPlacingLength();
        Double front = exactOverhang(r, TrainRecord.End.FRONT);
        Double rear = exactOverhang(r, TrainRecord.End.REAR);
        if (length == null || front == null || rear == null) {
            return null;
        }
        double detected = length - front - rear;
        return detected > 0 ? detected : null;
    }

    @CheckForNull
    private static Double exactOverhang(TrainRecord r, TrainRecord.End end) {
        // the same with no default and with a large one: it doesn't rest on the default
        TrainOverhang without = TrainOverhang.of(r, end, 0);
        TrainOverhang with = TrainOverhang.of(r, end, NOT_A_DEFAULT_MM);
        if (without == null || with == null || Math.abs(without.getMaxMm() - with.getMaxMm()) > EXACT_MM
                || without.getMaxMm() - without.getMinMm() > EXACT_MM) {
            return null;
        }
        return without.getMaxMm();
    }

    /**
     * The speed the lead loco's profile gives for the one command to it in
     * force from one moment to another.
     *
     * @param r         the train's record
     * @param fromNanos the first moment
     * @param toNanos   the second
     * @return the speed in millimetres per second, or null if the lead loco
     *         has no profile or address, no command to it was in force, or
     *         its command changed
     */
    @CheckForNull
    static Double profileSpeed(TrainRecord r, long fromNanos, long toNanos) {
        BasicRosterEntry entry = r.getFirstRosterEntry();
        if (!(entry instanceof RosterEntry)) {
            return null;
        }
        RosterSpeedProfile profile = ((RosterEntry) entry).getSpeedProfile();
        LocoAddress lead = entry.getDccLocoAddress();
        if (profile == null || lead == null) {
            return null;
        }
        TrainRecord.SpeedCommand inForce = null;
        for (TrainRecord.SpeedCommand c : r.getCommands(lead)) {
            if (c.getTimeNanos() <= fromNanos) {
                inForce = c;
            } else if (c.getTimeNanos() < toNanos) {
                return null; // it changed during the crossing
            }
        }
        if (inForce == null || inForce.isRelease() || inForce.getSpeedSetting() <= 0) {
            return null;
        }
        float speed = profile.getSpeed(inForce.getSpeedSetting(), inForce.isForward());
        return speed > 0 ? (double) speed : null;
    }

    /**
     * @return every Block and way through it measured so far, in the order
     *         first measured; a copy
     */
    @Nonnull
    public List<Measurement> getMeasurements() {
        return new ArrayList<>(measurements.values());
    }

    /**
     * @param block a Block
     * @return its measurements, one for each way through it; a copy
     */
    @Nonnull
    public List<Measurement> getMeasurements(@Nonnull Block block) {
        List<Measurement> list = new ArrayList<>();
        for (Measurement m : measurements.values()) {
            if (m.getBlock() == block) {
                list.add(m);
            }
        }
        return list;
    }

    /**
     * @return every boundary whose emptying delay has been measured, in the
     *         order first measured; a copy
     * @since 5.19.2
     */
    @Nonnull
    public List<Boundary> getBoundaries() {
        return new ArrayList<>(boundaries.values());
    }

    /**
     * @return each train whose arrivals have been compared with its speed
     *         profile, in the order first compared; a copy
     * @since 5.19.2
     */
    @Nonnull
    public List<Arrivals> getArrivals() {
        return new ArrayList<>(arrivals.values());
    }

    /**
     * @param r a train's record
     * @return its arrivals compared with its speed profile, or null if none
     * @since 5.19.2
     */
    @CheckForNull
    public Arrivals getArrivals(@Nonnull TrainRecord r) {
        return arrivals.get(r);
    }

    /**
     * @param block a Block
     * @return how many times its detector has dropped out under a train
     */
    public int getDropOuts(@Nonnull Block block) {
        return dropOuts.getOrDefault(block, 0);
    }

    /**
     * @return the Blocks whose detector has dropped out under a train, in no
     *         particular order; a copy
     */
    @Nonnull
    public List<Block> getDropOutBlocks() {
        return new ArrayList<>(dropOuts.keySet());
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        manager.removePropertyChangeListener(TrainRecordManager.PROPERTY_RECORDS, managerListener);
        for (TrainRecord r : manager.getRecords()) {
            r.removePropertyChangeListener(TrainRecord.PROPERTY_TRAIL, recordListener);
            r.removePropertyChangeListener(TrainRecord.PROPERTY_HISTORY, recordListener);
        }
        measurements.clear();
        dropOuts.clear();
        boundaries.clear();
        arrivals.clear();
        notSeenIn.clear();
    }

    /** How close, in millimetres, the two ends of an overhang's range are for it to count as known. */
    static final double EXACT_MM = 1.0;
    /** A default wheel overhang no component has, to tell whether an overhang rests on the default. */
    private static final double NOT_A_DEFAULT_MM = 1e6;

    /** The fewest crossings compared with a profile to fit a detector offset from. */
    static final int MIN_FIT_CROSSINGS = 3;
    /** How many times the longest profile time must be of the shortest, to fit from. */
    static final double MIN_FIT_SPREAD = 1.5;

    private static final class Key {

        private final Block block;
        private final Block from;
        private final Block to;

        Key(Block block, Block from, Block to) {
            this.block = block;
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Key && ((Key) o).block == block && ((Key) o).from == from && ((Key) o).to == to;
        }

        @Override
        public int hashCode() {
            return (System.identityHashCode(block) * 31 + System.identityHashCode(from)) * 31
                    + System.identityHashCode(to);
        }
    }

    /**
     * The crossings of one Block, entered from one other and left for a
     * third.
     */
    public static final class Measurement {

        private final Block block;
        private final Block from;
        private final Block to;
        private int crossings = 0;
        private double speedSum = 0;
        private int compared = 0;
        private double ratioSum = 0;
        // for the fit of measured time against the profile's time
        private double sumX = 0;
        private double sumY = 0;
        private double sumXX = 0;
        private double sumXY = 0;
        private double minX = Double.MAX_VALUE;
        private double maxX = 0;

        Measurement(Block block, Block from, Block to) {
            this.block = block;
            this.from = from;
            this.to = to;
        }

        /**
         * One crossing.
         *
         * @param seconds  how long it took
         * @param lengthMm the length of the way through
         * @param profile  the speed the lead loco's profile gives for its
         *                 command, in millimetres per second, or null if
         *                 none can be compared
         */
        void add(double seconds, double lengthMm, @CheckForNull Double profile) {
            double speed = lengthMm / seconds;
            crossings++;
            speedSum += speed;
            if (profile != null) {
                compared++;
                ratioSum += speed / profile;
                double x = lengthMm / profile; // the time the profile gives
                sumX += x;
                sumY += seconds;
                sumXX += x * x;
                sumXY += x * seconds;
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
            }
        }

        /**
         * Whether the crossings compared with a profile were at speeds far
         * enough apart, and enough of them, to tell a fixed time offset
         * from a speed that differs from the profile.
         *
         * @return true if {@link #getDetectorOffset()} and
         *         {@link #getAgainstProfileWithoutOffset()} have values
         * @since 5.19.2
         */
        public boolean isFitted() {
            return compared >= MIN_FIT_CROSSINGS && maxX >= MIN_FIT_SPREAD * minX
                    && compared * sumXX - sumX * sumX > 0;
        }

        private double slope() {
            return (compared * sumXY - sumX * sumY) / (compared * sumXX - sumX * sumX);
        }

        /**
         * How much later the detector of the Block left for reports a train
         * than the detector of this Block did, beyond the time the train
         * took: a fixed time added to every crossing, whatever the speed, as
         * a slow detector or a debounce delay adds. Found by fitting the
         * measured times against the times the speed profile gives, over
         * crossings at different speeds: the measured time is taken to be
         * the profile's time, scaled, plus this offset.
         *
         * @return the offset in seconds, positive where the far detector is
         *         the later; NaN unless {@link #isFitted()}
         * @since 5.19.2
         */
        public double getDetectorOffset() {
            if (!isFitted()) {
                return Double.NaN;
            }
            return (sumY - slope() * sumX) / compared;
        }

        /**
         * The trains' speed through the Block, as a fraction of the speed
         * the profile gives, with {@link #getDetectorOffset()} taken out: how
         * far the profile itself is out here.
         *
         * @return the fraction, such as 0.95 for 5% slower than the
         *         profile; NaN unless {@link #isFitted()}
         * @since 5.19.2
         */
        public double getAgainstProfileWithoutOffset() {
            if (!isFitted()) {
                return Double.NaN;
            }
            double slope = slope();
            return slope > 0 ? 1 / slope : Double.NaN;
        }

        /**
         * @return the Block crossed
         */
        @Nonnull
        public Block getBlock() {
            return block;
        }

        /**
         * @return the Block it was entered from
         */
        @Nonnull
        public Block getFrom() {
            return from;
        }

        /**
         * @return the Block it was left for
         */
        @Nonnull
        public Block getTo() {
            return to;
        }

        /**
         * @return how many crossings were measured
         */
        public int getCrossings() {
            return crossings;
        }

        /**
         * @return the average speed of the crossings, in millimetres per
         *         second
         */
        public double getAverageSpeed() {
            return crossings == 0 ? 0 : speedSum / crossings;
        }

        /**
         * @return how many crossings could be compared with a speed profile
         */
        public int getCompared() {
            return compared;
        }

        /**
         * @return the average of measured speed divided by the profile's
         *         speed, over the crossings compared; 0 if none
         */
        public double getAgainstProfile() {
            return compared == 0 ? 0 : ratioSum / compared;
        }
    }

    /**
     * The spread of a set of values: their standard deviation.
     */
    private static double spread(int n, double sum, double sumSquares) {
        if (n < 2) {
            return 0;
        }
        double mean = sum / n;
        return Math.sqrt(Math.max(0, (sumSquares - n * mean * mean) / (n - 1)));
    }

    /**
     * How much later a Block was known to be empty, after trains left it for
     * one neighbour, than the train's passing explains: the time from its
     * leading end entering the neighbour to the Block going empty, less the
     * time its detected part took to pass the boundary at the speed its
     * profile gives.
     * <p>
     * What remains is how much later the Block's detector reports empty than
     * the neighbour's reports occupied, debounce included, plus any error in
     * the train's length or profile; over several trains and speeds those
     * errors show as the spread.
     *
     * @since 5.19.2
     */
    public static final class Boundary {

        private final Block behind;
        private final Block ahead;
        private int count = 0;
        private double sum = 0;
        private double sumSquares = 0;

        Boundary(Block behind, Block ahead) {
            this.behind = behind;
            this.ahead = ahead;
        }

        void add(double seconds) {
            count++;
            sum += seconds;
            sumSquares += seconds * seconds;
        }

        /**
         * @return the Block left, whose going empty is timed
         */
        @Nonnull
        public Block getBehind() {
            return behind;
        }

        /**
         * @return the Block it was left for, whose going occupied is timed
         */
        @Nonnull
        public Block getAhead() {
            return ahead;
        }

        /**
         * @return how many times it was measured
         */
        public int getCount() {
            return count;
        }

        /**
         * @return the average delay, in seconds; positive where the Block
         *         behind is known empty later than the passing explains
         */
        public double getEmptyDelay() {
            return count == 0 ? 0 : sum / count;
        }

        /**
         * @return how much the delay varies: its standard deviation in
         *         seconds, 0 with fewer than two measurements
         */
        public double getSpread() {
            return spread(count, sum, sumSquares);
        }
    }

    /**
     * A train's crossings compared with its speed profile, as predictions:
     * how much longer each crossing took than the profile gives, as a
     * fraction of the time it gives.
     *
     * @since 5.19.2
     */
    public static final class Arrivals {

        private final TrainRecord record;
        private int count = 0;
        private double sum = 0;
        private double sumSquares = 0;

        Arrivals(TrainRecord record) {
            this.record = record;
        }

        /**
         * One crossing.
         *
         * @param predicted the time the profile gives, in seconds
         * @param actual    the time it took, in seconds
         */
        void add(double predicted, double actual) {
            double late = actual / predicted - 1;
            count++;
            sum += late;
            sumSquares += late * late;
        }

        /**
         * @return the train's record
         */
        @Nonnull
        public TrainRecord getRecord() {
            return record;
        }

        /**
         * @return how many crossings were compared
         */
        public int getCount() {
            return count;
        }

        /**
         * @return how late the train arrived on average, as a fraction of
         *         the time predicted: 0.1 for 10% later, -0.1 for 10%
         *         earlier
         */
        public double getLate() {
            return count == 0 ? 0 : sum / count;
        }

        /**
         * @return how much the lateness varies, its standard deviation as a
         *         fraction; 0 with fewer than two crossings. A train whose
         *         arrivals vary widely is one whose speed model can't be
         *         trusted far between fixes
         */
        public double getSpread() {
            return spread(count, sum, sumSquares);
        }
    }
}
