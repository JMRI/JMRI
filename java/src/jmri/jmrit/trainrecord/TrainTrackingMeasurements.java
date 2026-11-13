package jmri.jmrit.trainrecord;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
 * It only measures: nothing uses the measurements yet.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public class TrainTrackingMeasurements extends PropertyChangeSupport implements InstanceManagerAutoDefault, Disposable {

    /** Property name for a new measurement; the new value is the {@link Measurement}. */
    public static final String PROPERTY_MEASUREMENT = "measurement"; // NOI18N

    private final TrainRecordManager manager;
    private final Map<Key, Measurement> measurements = new LinkedHashMap<>();
    private final Map<Block, Integer> dropOuts = new HashMap<>();
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
        double speed = length / seconds;
        Double profile = profileSpeed(r, fromNanos, toNanos);
        Measurement m = measurements.computeIfAbsent(new Key(crossed, from, to),
                k -> new Measurement(k.block, k.from, k.to));
        m.add(speed, profile);
        firePropertyChange(PROPERTY_MEASUREMENT, null, m);
        return m;
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
    }

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

        Measurement(Block block, Block from, Block to) {
            this.block = block;
            this.from = from;
            this.to = to;
        }

        void add(double speed, @CheckForNull Double profile) {
            crossings++;
            speedSum += speed;
            if (profile != null) {
                compared++;
                ratioSum += speed / profile;
            }
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
}
