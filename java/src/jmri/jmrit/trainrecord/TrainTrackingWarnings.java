package jmri.jmrit.trainrecord;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import jmri.Block;
import jmri.Disposable;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.InvokeOnLayoutThread;
import jmri.TrainRecord;
import jmri.TrainRecord.Confidence;
import jmri.TrainRecord.SpeedCommand;
import jmri.TrainRecordManager;
import jmri.beans.PropertyChangeSupport;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.ThreadingUtil;

/**
 * The things train tracking has found wrong, as warnings for the operator:
 * a Block occupied when no train could have entered it, and a train stalled
 * or lost. It only warns: nothing on the layout changes, and no train is
 * stopped.
 * <p>
 * It also checks, once a second while any train record is placed, whether a
 * train is overdue: not seen to reach the next Block when even a train
 * {@value #SLOWER_THAN_PROFILE} times slower than its speed profile would
 * have had its first detected wheel there for {@value #OVERDUE_SECONDS}
 * seconds. A profile is measured on one stretch of track, and trains are
 * slower on grades and curves, with a load, or cold, so a train taking only a
 * little longer than its profile says is not overdue; and the detector sees
 * wheels, not buffers, so a train stopped with its front at the end of its
 * Block is not overdue either. Such a train is reported to the
 * {@link TrainRecordManager} as stalled, if a command to move is in force,
 * or lost. Only a train whose place is
 * {@link Confidence#CONFIRMED confirmed} is checked: one kept in a Block
 * after the Block went empty, or one of several that could have entered a
 * Block, already has its own reason, and was not "not seen to leave". Nor is
 * a train {@link TrainRecord#isIdentifiedByValue() identified by a Block's
 * value}: the value may have followed another train, so the record may not
 * be where its train is, and its loco's commands say nothing of the train
 * in that Block.
 * <p>
 * A warning stays until what caused it is over (the Block empties, the train
 * is seen again or placed) or the operator answers it. "Later" keeps it in
 * the list, for the Warnings window.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public class TrainTrackingWarnings extends PropertyChangeSupport implements InstanceManagerAutoDefault, Disposable {

    /** Property name for the list of warnings changing; no values. */
    public static final String PROPERTY_WARNINGS = "warnings"; // NOI18N

    /**
     * How long, in seconds, a train's slowest estimate must have been past
     * the end of its Block before it is overdue.
     */
    public static final int OVERDUE_SECONDS = 5;

    /**
     * How many times longer than its speed profile says a train may take to
     * reach the end of its Block before it can be overdue: trains run slower
     * than their profiles on grades and curves, with a load, or cold, and a
     * Block's length may be entered a little short.
     *
     * @since 5.19.2
     */
    public static final double SLOWER_THAN_PROFILE = 1.5;

    private final TrainRecordManager manager;
    private final List<Warning> warnings = new ArrayList<>();
    private final Map<TrainRecord, Long> pastEndSince = new HashMap<>();
    private final PropertyChangeListener managerListener = this::managerChanged;
    private final PropertyChangeListener recordListener = this::recordChanged;
    private final javax.swing.Timer timer = new javax.swing.Timer(1000, e -> checkOverdue(System.nanoTime()));
    private boolean disposed = false;

    /**
     * Follow the default {@link TrainRecordManager}.
     */
    public TrainTrackingWarnings() {
        this(InstanceManager.getDefault(TrainRecordManager.class));
    }

    /**
     * Follow a manager.
     *
     * @param manager the manager
     */
    public TrainTrackingWarnings(@Nonnull TrainRecordManager manager) {
        this.manager = Objects.requireNonNull(manager, "manager");
        manager.addPropertyChangeListener(managerListener);
        for (TrainRecord r : manager.getRecords()) {
            r.addPropertyChangeListener(TrainRecord.PROPERTY_CONFIDENCE, recordListener);
            r.addPropertyChangeListener(TrainRecord.PROPERTY_BLOCKS, recordListener);
        }
        for (Block b : manager.getUnexplained()) {
            warnings.add(new Warning(Warning.Kind.UNEXPLAINED, b, null));
        }
        for (TrainRecord r : manager.getRecords()) {
            Warning w = warningFor(r);
            if (w != null) {
                warnings.add(w);
            }
        }
        timer.setRepeats(true);
        ThreadingUtil.runOnGUIEventually(this::updateTimer);
    }

    /**
     * @return the warnings, oldest first; a copy
     */
    @Nonnull
    public List<Warning> getWarnings() {
        return new ArrayList<>(warnings);
    }

    /**
     * The operator says nothing is in a Block whose occupancy is
     * unexplained: its detector is marked untrusted, and the warning ends.
     * It can be trusted again from the Block dialog.
     *
     * @param block the Block
     */
    @InvokeOnLayoutThread
    public void detectorStuck(@Nonnull Block block) {
        manager.setUntrusted(block, true);
    }

    private void managerChanged(PropertyChangeEvent e) {
        ThreadingUtil.runOnLayoutEventually(() -> {
            if (TrainRecordManager.PROPERTY_UNEXPLAINED.equals(e.getPropertyName())) {
                if (e.getNewValue() instanceof Block) {
                    add(new Warning(Warning.Kind.UNEXPLAINED, (Block) e.getNewValue(), null));
                } else if (e.getOldValue() instanceof Block) {
                    Block b = (Block) e.getOldValue();
                    remove(w -> w.getKind() == Warning.Kind.UNEXPLAINED && w.getBlock() == b);
                }
            } else if (TrainRecordManager.PROPERTY_RECORDS.equals(e.getPropertyName())) {
                if (e.getNewValue() instanceof TrainRecord) {
                    TrainRecord r = (TrainRecord) e.getNewValue();
                    r.addPropertyChangeListener(TrainRecord.PROPERTY_CONFIDENCE, recordListener);
                    r.addPropertyChangeListener(TrainRecord.PROPERTY_BLOCKS, recordListener);
                }
                if (e.getOldValue() instanceof TrainRecord) {
                    TrainRecord r = (TrainRecord) e.getOldValue();
                    r.removePropertyChangeListener(TrainRecord.PROPERTY_CONFIDENCE, recordListener);
                    r.removePropertyChangeListener(TrainRecord.PROPERTY_BLOCKS, recordListener);
                    pastEndSince.remove(r);
                    remove(w -> w.getRecord() == r);
                }
                updateTimer();
            }
        });
    }

    private void recordChanged(PropertyChangeEvent e) {
        TrainRecord r = (TrainRecord) e.getSource();
        ThreadingUtil.runOnLayoutEventually(() -> {
            if (TrainRecord.PROPERTY_BLOCKS.equals(e.getPropertyName())) {
                pastEndSince.remove(r);
                updateTimer();
            }
            Warning now = warningFor(r);
            Warning old = null;
            for (Warning w : warnings) {
                if (w.getRecord() == r) {
                    old = w;
                }
            }
            if (old != null && (now == null || now.getKind() != old.getKind())) {
                Warning o = old;
                remove(w -> w == o);
            }
            if (now != null && (old == null || now.getKind() != old.getKind())) {
                add(now);
            }
        });
    }

    @CheckForNull
    private static Warning warningFor(TrainRecord r) {
        Confidence c = r.getConfidence();
        if (c != Confidence.STALLED && c != Confidence.LOST) {
            return null;
        }
        TrainRecord.Reason reason = r.getConfidenceReason();
        Block b = reason == null ? null : reason.getBlock();
        if (b == null) {
            List<Block> blocks = r.getBlocks();
            if (blocks.isEmpty()) {
                return null;
            }
            b = blocks.get(0);
        }
        return new Warning(c == Confidence.STALLED ? Warning.Kind.STALLED : Warning.Kind.LOST, b, r);
    }

    private void add(Warning w) {
        warnings.add(w);
        firePropertyChange(PROPERTY_WARNINGS, null, null);
    }

    private void remove(java.util.function.Predicate<Warning> which) {
        if (warnings.removeIf(which)) {
            firePropertyChange(PROPERTY_WARNINGS, null, null);
        }
    }

    private void updateTimer() {
        if (disposed) {
            return;
        }
        boolean any = false;
        for (TrainRecord r : manager.getRecords()) {
            if (r.isPlaced()) {
                any = true;
                break;
            }
        }
        if (any && !timer.isRunning()) {
            timer.start();
        } else if (!any && timer.isRunning()) {
            timer.stop();
        }
    }

    /**
     * Report each placed train, its place confirmed and not identified by a
     * Block's value, that even {@value #SLOWER_THAN_PROFILE} times slower than
     * its slowest estimate would have reached the next Block, and has for
     * {@value #OVERDUE_SECONDS} seconds.
     * <p>
     * What is compared with the Block's length is where its first detected
     * wheel is, which is what the next Block's detector sees, not its front:
     * a train standing with its buffers at the end of its Block, as Dispatcher
     * stops one at the end of a Section, hasn't reached the next Block. And
     * it is compared as a distance, not a time, so a train told to stop is
     * overdue only where even the slowest train would have stopped beyond the
     * next boundary, however long it stands.
     *
     * @param nowNanos the moment, on the {@link System#nanoTime()} scale
     */
    @InvokeOnLayoutThread
    void checkOverdue(long nowNanos) {
        for (TrainRecord r : manager.getRecords()) {
            Confidence c = r.getConfidence();
            if (!r.isPlaced() || c != Confidence.CONFIRMED || r.isIdentifiedByValue()) {
                pastEndSince.remove(r);
                continue;
            }
            TrainPositionEstimate e = TrainPositionEstimator.estimateDetected(r, momentumOf(r), nowNanos);
            List<TrainRecord.TrailEntry> trail = r.getTrail();
            double length = e == null || trail.isEmpty() ? 0
                    : TrainPositionEstimator.lengthOn(e.getBlock(), trail.get(trail.size() - 1).getFrom());
            if (e == null || length <= 0 || e.getUnlimitedMinMm() < SLOWER_THAN_PROFILE * length) {
                pastEndSince.remove(r);
                continue;
            }
            Long since = pastEndSince.get(r);
            if (since == null) {
                pastEndSince.put(r, nowNanos);
            } else if (nowNanos - since >= OVERDUE_SECONDS * 1_000_000_000L) {
                pastEndSince.remove(r);
                SpeedCommand last = leadCommand(r);
                boolean commanded = last != null && !last.isRelease() && !last.isEmergencyStop()
                        && last.getSpeedSetting() > 0;
                manager.reportOverdue(r, e.getBlock(), commanded);
            }
        }
    }

    /**
     * @return the last speed command for the train's lead loco, the one its
     *         position is estimated from, or null if there is none
     */
    @CheckForNull
    private static SpeedCommand leadCommand(TrainRecord r) {
        jmri.BasicRosterEntry entry = r.getFirstRosterEntry();
        jmri.LocoAddress lead = entry == null ? null : entry.getDccLocoAddress();
        return lead == null ? null : r.getLastCommand(lead);
    }

    private static TrainPositionEstimator.Momentum momentumOf(TrainRecord r) {
        jmri.BasicRosterEntry entry = r.getFirstRosterEntry();
        return entry instanceof RosterEntry ? TrainPositionEstimator.Momentum.fromRoster((RosterEntry) entry)
                : TrainPositionEstimator.Momentum.NONE;
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        disposed = true;
        timer.stop();
        manager.removePropertyChangeListener(managerListener);
        for (TrainRecord r : manager.getRecords()) {
            r.removePropertyChangeListener(TrainRecord.PROPERTY_CONFIDENCE, recordListener);
            r.removePropertyChangeListener(TrainRecord.PROPERTY_BLOCKS, recordListener);
        }
        warnings.clear();
        pastEndSince.clear();
    }

    /**
     * Something train tracking has found wrong.
     */
    @Immutable
    public static final class Warning {

        /**
         * What is wrong.
         */
        public enum Kind {
            /** A Block is occupied when no train could have entered it. */
            UNEXPLAINED,
            /** A train was told to move but not seen to reach the next Block. */
            STALLED,
            /** A train was expected somewhere and not found. */
            LOST
        }

        private final Kind kind;
        private final Block block;
        private final TrainRecord record;
        private final java.time.Instant time = java.time.Instant.now();

        /**
         * @param kind   what is wrong
         * @param block  where
         * @param record the train, or null for an unexplained occupancy
         */
        public Warning(@Nonnull Kind kind, @Nonnull Block block, @CheckForNull TrainRecord record) {
            this.kind = Objects.requireNonNull(kind, "kind");
            this.block = Objects.requireNonNull(block, "block");
            this.record = record;
        }

        /**
         * @return what is wrong
         */
        @Nonnull
        public Kind getKind() {
            return kind;
        }

        /**
         * @return where
         */
        @Nonnull
        public Block getBlock() {
            return block;
        }

        /**
         * @return the train, or null for an unexplained occupancy
         */
        @CheckForNull
        public TrainRecord getRecord() {
            return record;
        }

        /**
         * @return when it was found
         */
        @Nonnull
        public java.time.Instant getTime() {
            return time;
        }

        /**
         * @return the warning in words: its title
         */
        @Nonnull
        public String getTitle() {
            return Bundle.getMessage("Warning" + kind.name(), block.getDisplayName(),
                    record == null ? "" : record.getName());
        }

        @Override
        public String toString() {
            return "Warning[" + kind + " " + block.getDisplayName() + "]";
        }
    }
}
