package jmri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import jmri.beans.PropertyChangeProvider;

/**
 * What is known about one train, kept from one run to the next.
 * <p>
 * A train record is one object per train, so that every part of JMRI that
 * needs to know about a train can refer to the same one. It holds:
 * <ul>
 * <li>the train's identity: its name, the roster entries of its locos (lead
 * loco first) where they have them, and any ID tags known to belong to it;</li>
 * <li>what it is made of: its components, the parts it is split into or
 * joined from, each with a length where one is known;</li>
 * <li>which parts of it detection can see: Entire Train, Head Only, Head and
 * Tail, or a custom list of detection markers;</li>
 * <li>its length from each source that gives one, and the rule for which is
 * used.</li>
 * </ul>
 * A record outlives any one run of its train: what was entered for the train
 * is kept with the record, not with the run.
 * <p>
 * A record is not the Block value. A Block's value is unchanged (a String, a
 * RosterEntry or an IdTag, as before); the record sits beside it.
 * <p>
 * Records are created, found and removed through the
 * {@link TrainRecordManager}. They are read and changed on the layout thread
 * (Swing thread); listeners are told on the thread that made the change.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public interface TrainRecord extends PropertyChangeProvider {

    /** Property name for a change of name. */
    String PROPERTY_NAME = "name"; // NOI18N
    /**
     * Property name for a change in the locos' roster entries; the old and
     * new values are the lists, as {@link #getRosterEntries()} gives them.
     */
    String PROPERTY_ROSTER_ENTRIES = "rosterEntries"; // NOI18N
    /** Property name for a change in the ID tags. */
    String PROPERTY_ID_TAGS = "idTags"; // NOI18N
    /** Property name for a change in the train's components. */
    String PROPERTY_TRAIN_COMPONENTS = "trainComponents"; // NOI18N
    /**
     * Property name for a change of detection or detection markers; the old
     * and new values are the markers, as {@link #getMarkers()} gives them.
     */
    String PROPERTY_DETECTION = "detection"; // NOI18N
    /**
     * Property name for a change in any length, the components' length included;
     * the old and new values are null, so read the lengths from the record.
     */
    String PROPERTY_LENGTHS = "lengths"; // NOI18N
    /**
     * Property name for a change in the Blocks the train is known to be in;
     * the old and new values are the Blocks, as {@link #getBlocks()} gives
     * them.
     */
    String PROPERTY_BLOCKS = "blocks"; // NOI18N
    /** Property name for a new entry in the trail; the new value is the entry. */
    String PROPERTY_TRAIL = "trail"; // NOI18N
    /** Property name for a change of confidence. */
    String PROPERTY_CONFIDENCE = "confidence"; // NOI18N
    /** Property name for a change of the leading end or of whether the front is known. */
    String PROPERTY_DIRECTION = "direction"; // NOI18N
    /**
     * Property name for a new speed command for one of the train's locos, or
     * its release; the new value is the command, the old value the last
     * command for the same loco, or null.
     */
    String PROPERTY_COMMAND = "command"; // NOI18N

    /**
     * @return the train's name, unique among the records
     */
    @Nonnull
    String getName();
    /**
     * Property name for a new entry in the history; the new value is the
     * {@link Reason}.
     */
    String PROPERTY_HISTORY = "history"; // NOI18N

    /**
     * Rename the train.
     *
     * @param name the new name; leading and trailing spaces are removed as
     *             for a user name
     * @throws IllegalArgumentException if the name is empty or another record
     *                                  has it
     * @throws IllegalStateException    if the record has been disposed
     */
    @InvokeOnLayoutThread
    void setName(@Nonnull String name);

    /**
     * The roster entry of the train's lead loco: the loco Dispatcher and
     * Warrants drive.
     *
     * @return the first of {@link #getRosterEntries()}, or null if the train
     *         has no loco with a roster entry
     */
    @CheckForNull
    BasicRosterEntry getFirstRosterEntry();

    /**
     * Add a roster entry at the front of the train's locos, so that it is the
     * lead loco's. A roster entry belongs to at most one record, and appears
     * in its list at most once.
     *
     * @param entry the roster entry
     * @throws IllegalArgumentException if this record or another already has
     *                                  the entry
     * @throws IllegalStateException    if the record has been disposed
     */
    @InvokeOnLayoutThread
    void insertFirstRosterEntry(@Nonnull BasicRosterEntry entry);

    /**
     * Remove the first of the train's roster entries, the lead loco's; the
     * next loco, if any, then leads. Does nothing if the train has none.
     *
     * @throws IllegalStateException if the record has been disposed
     */
    @InvokeOnLayoutThread
    void removeFirstRosterEntry();

    /**
     * The roster entries of the train's locos, such as both locos of a
     * double-headed train, in order from the lead loco.
     *
     * @return the entries, lead loco first, possibly empty; a copy
     */
    @Nonnull
    List<BasicRosterEntry> getRosterEntries();

    /**
     * Set the roster entries of the train's locos. Each roster entry belongs
     * to at most one record, and appears in the list at most once.
     *
     * @param entries the entries, lead loco first; empty for none
     * @throws IllegalArgumentException if an entry appears twice, or another
     *                                  record has one of them
     * @throws IllegalStateException    if the record has been disposed
     */
    @InvokeOnLayoutThread
    void setRosterEntries(@Nonnull List<BasicRosterEntry> entries);

    /**
     * @return the ID tags known to belong to the train, in the order added;
     *         a copy
     */
    @Nonnull
    Set<IdTag> getIdTags();

    /**
     * Add an ID tag known to belong to the train. A tag belongs to at most
     * one record. Adding a tag the record already has does nothing.
     *
     * @param tag the tag
     * @throws IllegalArgumentException if another record has this tag
     * @throws IllegalStateException    if the record has been disposed
     */
    @InvokeOnLayoutThread
    void addIdTag(@Nonnull IdTag tag);

    /**
     * Remove an ID tag from the train. Removing a tag the record doesn't have
     * does nothing.
     *
     * @param tag the tag
     * @throws IllegalStateException if the record has been disposed
     */
    @InvokeOnLayoutThread
    void removeIdTag(@Nonnull IdTag tag);

    /**
     * The train's components, front first: the parts it is split into or
     * joined from. A train that is never split is one component however many
     * vehicles it has, and that is the default, so nobody declares components
     * in advance.
     *
     * @return the components, front first, never empty; a copy
     */
    @Nonnull
    List<TrainComponent> getTrainComponents();

    /**
     * Set the train's components, front first.
     *
     * @param components the components, front first, at least one
     * @throws IllegalArgumentException if the list is empty
     * @throws IllegalStateException    if the record has been disposed
     */
    @InvokeOnLayoutThread
    void setTrainComponents(@Nonnull List<TrainComponent> components);

    /**
     * @return how the train is detected
     */
    @Nonnull
    Detection getDetection();

    /**
     * Set how the train is detected, as one of the three standard settings.
     * Any custom list of detection markers is forgotten.
     *
     * @param detection {@link Detection#ENTIRE_TRAIN},
     *                  {@link Detection#HEAD_ONLY} or
     *                  {@link Detection#HEAD_AND_TAIL}
     * @throws IllegalArgumentException if detection is
     *                                  {@link Detection#CUSTOM}; use
     *                                  {@link #setCustomMarkers} instead
     * @throws IllegalStateException    if the record has been disposed
     */
    @InvokeOnLayoutThread
    void setDetection(@Nonnull Detection detection);

    /**
     * Give the train a custom list of detection markers, for a train the
     * three standard settings can't describe, such as one detected only in
     * places. Detection becomes {@link Detection#CUSTOM}.
     *
     * @param markers the markers, at least one
     * @throws IllegalArgumentException if the list is empty, or a marker is
     *                                  {@link DetectionMarker.Placement#WHOLE_TRAIN},
     *                                  which only Entire Train uses
     * @throws IllegalStateException    if the record has been disposed
     */
    @InvokeOnLayoutThread
    void setCustomMarkers(@Nonnull List<DetectionMarker> markers);

    /**
     * The train's detection markers. For the standard settings they are
     * worked out from the setting, not stored: Entire Train is one marker
     * covering the whole train; Head Only one marker at the front; Head and
     * Tail one at the front and one at the rear. For
     * {@link Detection#CUSTOM} they are the list given.
     *
     * @return the markers, front first where their order is known; never
     *         empty
     */
    @Nonnull
    List<DetectionMarker> getMarkers();

    /**
     * The train's length from one source.
     *
     * @param source the source
     * @return the length in millimetres, or null if this source gives none
     */
    @CheckForNull
    Double getLength(@Nonnull LengthSource source);

    /**
     * Set or clear the train's length from one source. For
     * {@link LengthSource#TRAIN_COMPONENTS} use {@link #setTrainComponents}:
     * that length is worked out from the components. For {@link LengthSource#MEASURED} use
     * {@link #setMeasuredLength}.
     *
     * @param source the source
     * @param mm     the length in millimetres, more than 0, or null to clear
     * @throws IllegalArgumentException if the source is
     *                                  {@link LengthSource#TRAIN_COMPONENTS} or
     *                                  {@link LengthSource#MEASURED}, or the
     *                                  length is not more than 0
     * @throws IllegalStateException    if the record has been disposed
     */
    @InvokeOnLayoutThread
    void setLength(@Nonnull LengthSource source, @CheckForNull Double mm);

    /**
     * Set or clear the train's detected length as measured from its
     * crossings, and whether the measurements so far are consistent.
     *
     * @param mm         the length in millimetres, more than 0, or null to
     *                   clear
     * @param consistent true if the measurements are consistent enough to be
     *                   used for placing; ignored when clearing
     * @throws IllegalArgumentException if the length is not more than 0
     * @throws IllegalStateException    if the record has been disposed
     */
    @InvokeOnLayoutThread
    void setMeasuredLength(@CheckForNull Double mm, boolean consistent);

    /**
     * @return true if there is a measured length and its measurements are
     *         consistent
     */
    boolean isMeasuredLengthConsistent();

    /**
     * The length used for holding Blocks: the longest length any source gives,
     * measured included, so that nothing is released earlier than the
     * longest length says.
     *
     * @return the length in millimetres, or null if no source gives one
     */
    @CheckForNull
    Double getHoldingLength();

    /**
     * The length used for placing markers within the train: the measured
     * length once its measurements are consistent, since it places them more
     * exactly than an entered length; otherwise the holding length.
     *
     * @return the length in millimetres, or null if no source gives one
     */
    @CheckForNull
    Double getPlacingLength();

    /**
     * @return true once the record has been removed from its manager, which
     *         disposes of it; a disposed record can't be changed
     */
    boolean isDisposed();

    /**
     * @return true if the record knows where on the layout the train is
     */
    boolean isPlaced();

    /**
     * Say where the train is: the Blocks it is in. Its trail starts again
     * from here, and its confidence becomes {@link Confidence#CONFIRMED}.
     *
     * @param blocks     the Blocks, at least one, front first if the front
     *                   is known
     * @param frontKnown true if the first Block given holds the front of
     *                   the train
     * @throws IllegalArgumentException if the list is empty or holds a
     *                                  Block twice
     * @throws IllegalStateException    if the record has been disposed
     */
    @InvokeOnLayoutThread
    default void place(@Nonnull List<Block> blocks, boolean frontKnown) {
        place(blocks, frontKnown, Reason.Rule.PLACED);
    }

    /**
     * Say where the train is, and why: as {@link #place(List, boolean)}, with
     * the rule that placed it kept as the reason.
     *
     * @param blocks     the Blocks, at least one, front first if the front
     *                   is known
     * @param frontKnown true if the first Block given holds the front of
     *                   the train
     * @param rule       {@link Reason.Rule#PLACED},
     *                   {@link Reason.Rule#PLACED_BY_VALUE},
     *                   {@link Reason.Rule#PLACED_BY_DISPATCHER} or
     *                   {@link Reason.Rule#PLACED_BY_OPERATOR}
     * @throws IllegalArgumentException if the list is empty or holds a
     *                                  Block twice
     * @throws IllegalStateException    if the record has been disposed
     */
    @InvokeOnLayoutThread
    void place(@Nonnull List<Block> blocks, boolean frontKnown, @Nonnull Reason.Rule rule);

    /**
     * Forget where the train is, as when it is taken off the layout.
     *
     * @throws IllegalStateException if the record has been disposed
     */
    @InvokeOnLayoutThread
    void unplace();

    /**
     * The Blocks the train is known to be in, as detection has shown them:
     * for a train detected only in part, the Blocks its detected parts are
     * in.
     *
     * @return the Blocks, front first if the front is known; empty if the
     *         record is not placed; a copy
     */
    @Nonnull
    List<Block> getBlocks();

    /**
     * @return true if it is known which end of {@link #getBlocks()} holds
     *         the front of the train
     */
    boolean isFrontKnown();

    /**
     * @return the end of the train that led when it last moved, or null if
     *         it hasn't been seen to move since it was placed
     */
    @CheckForNull
    End getLeadingEnd();

    /**
     * The Blocks the train has entered, oldest first: the most recent
     * {@value #TRAIL_LENGTH} entries.
     *
     * @return the trail; a copy
     */
    @Nonnull
    List<TrailEntry> getTrail();

    /** How many entries the trail keeps. */
    int TRAIL_LENGTH = 64;

    /**
     * @return how sure JMRI is of where the train is; null if it is not
     *         placed
     */
    @CheckForNull
    Confidence getConfidence();

    /**
     * The addresses of the train's locos, from their roster entries, lead
     * loco first. Speed commands for each of these addresses, from any
     * {@link LocoSpeedSource}, are kept with the record.
     *
     * @return the addresses, lead loco first, possibly empty; a copy
     */
    @Nonnull
    List<LocoAddress> getLocoAddresses();

    /**
     * The speed commands for the train's locos, oldest first: the most recent
     * {@value #COMMANDS_LENGTH}, for all of its locos together. A command
     * repeating the speed and direction of the one before it for the same
     * loco is not kept: when several sources see the same command, the first
     * to report it is kept.
     *
     * @return the commands; a copy
     */
    @Nonnull
    List<SpeedCommand> getCommands();

    /**
     * The speed commands kept for one of the train's locos, oldest first.
     *
     * @param address the loco's address
     * @return the commands for that address; a copy, empty if there are none
     */
    @Nonnull
    List<SpeedCommand> getCommands(@Nonnull LocoAddress address);

    /** How many speed commands a record keeps, for all of its locos. */
    int COMMANDS_LENGTH = 256;

    /**
     * @return the most recent speed command for any of the train's locos, or
     *         null if none has been seen
     */
    @CheckForNull
    SpeedCommand getLastCommand();

    /**
     * @param address the address of one of the train's locos
     * @return the most recent speed command for that loco, or null if none
     *         has been seen
     */
    @CheckForNull
    SpeedCommand getLastCommand(@Nonnull LocoAddress address);

    /**
     * A speed and direction commanded for one of a train's locos, or the
     * release of its address.
     */
    @Immutable
    final class SpeedCommand {

        private final LocoAddress address;
        private final float speedSetting;
        private final boolean forward;
        private final boolean release;
        private final long timeNanos;
        private final java.time.Instant time;

        /**
         * @param address      the loco's address
         * @param speedSetting the speed setting, as
         *                     {@link Throttle#getSpeedSetting()} gives it: 0
         *                     to 1, or less than 0 for an emergency stop
         * @param forward      true if the direction is forward
         * @param release      true if this is the release of the loco's
         *                     address, which leaves it at the speed and
         *                     direction given
         * @param timeNanos    when, on the {@link System#nanoTime()} scale
         * @param time         when, as a wall-clock time
         */
        public SpeedCommand(@Nonnull LocoAddress address, float speedSetting, boolean forward, boolean release,
                long timeNanos, @Nonnull java.time.Instant time) {
            this.address = Objects.requireNonNull(address, "address");
            this.speedSetting = speedSetting;
            this.forward = forward;
            this.release = release;
            this.timeNanos = timeNanos;
            this.time = Objects.requireNonNull(time, "time");
        }

        /**
         * @return the address of the loco this was commanded for
         */
        @Nonnull
        public LocoAddress getAddress() {
            return address;
        }

        /**
         * Loco addresses are the same if their numbers and protocols are: a
         * short and a long address with the same number are different locos.
         *
         * @param other a loco address
         * @return true if this was commanded for that address
         */
        public boolean isFor(@Nonnull LocoAddress other) {
            return address.getNumber() == other.getNumber() && address.getProtocol() == other.getProtocol();
        }

        /**
         * @return the speed setting: 0 to 1, or less than 0 for an emergency
         *         stop
         */
        public float getSpeedSetting() {
            return speedSetting;
        }

        /**
         * @return true for an emergency stop
         */
        public boolean isEmergencyStop() {
            return speedSetting < 0;
        }

        /**
         * @return true if the direction is forward
         */
        public boolean isForward() {
            return forward;
        }

        /**
         * @return true if this is the release of the loco's address: nothing
         *         was commanded, and the loco is left at the speed and
         *         direction given
         */
        public boolean isRelease() {
            return release;
        }

        /**
         * @param other the other command
         * @return true if this commands the same speed and direction as the
         *         other
         */
        public boolean sameSpeedAs(@Nonnull SpeedCommand other) {
            return Float.compare(speedSetting, other.speedSetting) == 0 && forward == other.forward;
        }

        /**
         * @return when, on the {@link System#nanoTime()} scale: when the
         *         source reported it
         */
        public long getTimeNanos() {
            return timeNanos;
        }

        /**
         * @return when, as a wall-clock time
         */
        @Nonnull
        public java.time.Instant getTime() {
            return time;
        }

        @Override
        public String toString() {
            return "SpeedCommand[" + address + ", " + (release ? "released at " : "") + speedSetting
                    + (forward ? " forward]" : " reverse]");
        }
    }

    /**
     * An end of a train.
     */
    enum End {
        /** The front of the train. */
        FRONT,
        /** The rear of the train. */
        REAR
    }

    /**
     * How sure JMRI is of where a train is.
     */
    enum Confidence {
        /** Just seen by a detector or a reporter, or placed. */
        CONFIRMED,
        /** Not seen where it was last known to be, as after a drop-out. */
        ESTIMATED,
        /** One of several trains that could have entered a Block. */
        AMBIGUOUS,
        /** Told to move but not seen to. */
        STALLED,
        /** Expected somewhere and not found. */
        LOST
    }

    /**
     * What has happened to the train, most recent first: the last
     * {@value #HISTORY_LENGTH} changes to the Blocks it is in or to how sure
     * JMRI is of where it is, each with its reason.
     *
     * @return the history; a copy
     */
    @Nonnull
    List<Reason> getHistory();

    /** How many entries the history keeps. */
    int HISTORY_LENGTH = 20;

    /**
     * Why the train is in a Block.
     *
     * @param block one of {@link #getBlocks()}
     * @return the reason it was put there, or null if it isn't there
     */
    @CheckForNull
    Reason getReason(@Nonnull Block block);

    /**
     * Why JMRI is as sure, or as unsure, of where the train is as
     * {@link #getConfidence()} says.
     *
     * @return the reason for the present confidence, or null if the train
     *         is not placed
     */
    @CheckForNull
    Reason getConfidenceReason();

    /**
     * A Block a train entered.
     */
    @Immutable
    final class TrailEntry {

        private final Block block;
        private final Block from;
        private final End end;
        private final long timeNanos;
        private final java.time.Instant time;

        /**
         * @param block     the Block entered
         * @param from      the Block it was entered from, or null if the
         *                  train was placed there
         * @param end       the end of the train that entered it, or null if
         *                  not known
         * @param timeNanos when, on the {@link System#nanoTime()} scale
         * @param time      when, as a wall-clock time
         */
        public TrailEntry(@Nonnull Block block, @CheckForNull Block from, @CheckForNull End end, long timeNanos,
                @Nonnull java.time.Instant time) {
            this.block = Objects.requireNonNull(block, "block");
            this.from = from;
            this.end = end;
            this.timeNanos = timeNanos;
            this.time = Objects.requireNonNull(time, "time");
        }

        /**
         * @return the Block entered
         */
        @Nonnull
        public Block getBlock() {
            return block;
        }

        /**
         * @return the Block it was entered from, or null if the train was
         *         placed there
         */
        @CheckForNull
        public Block getFrom() {
            return from;
        }

        /**
         * @return the end of the train that entered, or null if not known
         */
        @CheckForNull
        public End getEnd() {
            return end;
        }

        /**
         * @return when, on the {@link System#nanoTime()} scale: when the
         *         Block's sensor first reported it
         */
        public long getTimeNanos() {
            return timeNanos;
        }

        /**
         * @return when, as a wall-clock time
         */
        @Nonnull
        public java.time.Instant getTime() {
            return time;
        }

        @Override
        public String toString() {
            return "TrailEntry[" + block.getDisplayName() + " from "
                    + (from == null ? "nowhere" : from.getDisplayName()) + ", " + end + "]";
        }
    }

    /**
     * Why something about a train changed: the rule that applied and what
     * set it off, held as references to things that already exist (a Block,
     * a sensor, a time). It is put into words only when someone looks
     * ({@code jmri.jmrit.trainrecord.TrainRecordReasons}).
     */
    @Immutable
    final class Reason {

        /**
         * The rule that applied.
         */
        public enum Rule {
            /** Placed in its Blocks, by code or a script. */
            PLACED,
            /** Placed where a Block's value names it. */
            PLACED_BY_VALUE,
            /** Placed in its start Block by Dispatcher. */
            PLACED_BY_DISPATCHER,
            /** Placed by the operator: "train is here". */
            PLACED_BY_OPERATOR,
            /** No longer placed anywhere. */
            UNPLACED,
            /** Entered a Block next to one of its ends: its sensor went occupied. */
            ENTERED,
            /** Entered a Block through Blocks with no detector. */
            ENTERED_THROUGH_DARK,
            /** One of several trains that could have entered a Block. */
            AMBIGUOUS,
            /**
             * Entered a Block next to both of its ends while which end leads
             * isn't known: which end entered isn't known until the train
             * leaves one of them.
             */
            ENTERED_END_UNKNOWN,
            /** The Block's value named it as the train that entered. */
            SETTLED_BY_VALUE,
            /** Another train was named as the one that entered: no longer in doubt. */
            AMBIGUITY_CLEARED,
            /** Left a Block at one of its ends: its sensor went unoccupied. */
            LEFT,
            /** Left the Blocks with no detector behind a Block it was seen to leave. */
            LEFT_DARK_BEHIND,
            /** A Block it is in went unoccupied, but it can't have left it. */
            KEPT_AFTER_DROPOUT,
            /** None of its Blocks with a detector shows it. */
            NOT_SEEN,
            /** A Block it is in went occupied again. */
            SEEN_AGAIN
        }

        private final Rule rule;
        private final Block block;
        private final Block from;
        private final Sensor sensor;
        private final List<Block> through;
        private final java.time.Instant time;

        /**
         * @param rule    the rule that applied
         * @param block   the Block it is about, or null
         * @param from    the Block it came from, or null
         * @param sensor  the sensor whose change set it off, or null
         * @param through Blocks with no detector passed through, in order;
         *                empty if none
         * @param time    when
         */
        public Reason(@Nonnull Rule rule, @CheckForNull Block block, @CheckForNull Block from,
                @CheckForNull Sensor sensor, @Nonnull List<Block> through, @Nonnull java.time.Instant time) {
            this.rule = Objects.requireNonNull(rule, "rule");
            this.block = block;
            this.from = from;
            this.sensor = sensor;
            this.through = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(through, "through")));
            this.time = Objects.requireNonNull(time, "time");
        }

        /**
         * A reason with no other Blocks or sensor, dated now.
         *
         * @param rule  the rule that applied
         * @param block the Block it is about, or null
         * @return the reason
         */
        @Nonnull
        public static Reason of(@Nonnull Rule rule, @CheckForNull Block block) {
            return new Reason(rule, block, null, null, List.of(), java.time.Instant.now());
        }

        /**
         * @return the rule that applied
         */
        @Nonnull
        public Rule getRule() {
            return rule;
        }

        /**
         * @return the Block it is about, or null
         */
        @CheckForNull
        public Block getBlock() {
            return block;
        }

        /**
         * @return the Block the train came from, or null
         */
        @CheckForNull
        public Block getFrom() {
            return from;
        }

        /**
         * @return the sensor whose change set it off, or null
         */
        @CheckForNull
        public Sensor getSensor() {
            return sensor;
        }

        /**
         * @return the Blocks with no detector passed through, in order
         */
        @Nonnull
        public List<Block> getThrough() {
            return through;
        }

        /**
         * @return when
         */
        @Nonnull
        public java.time.Instant getTime() {
            return time;
        }

        @Override
        public String toString() {
            return "Reason[" + rule + (block == null ? "" : " " + block.getDisplayName()) + "]";
        }
    }

    /**
     * How a train is detected. The first three are Dispatcher's Train
     * Detection settings.
     */
    enum Detection {
        /** Detection sees the whole train. */
        ENTIRE_TRAIN,
        /** Detection sees only the front, usually the loco. */
        HEAD_ONLY,
        /** Detection sees the front and the rear. */
        HEAD_AND_TAIL,
        /** A list of detection markers given for this train. */
        CUSTOM
    }

    /**
     * Where a train's length comes from. The holding length is the longest
     * of them; see {@link TrainRecord#getHoldingLength()}.
     */
    enum LengthSource {
        /** Entered for the train, as Dispatcher's Max Train Length. */
        ENTERED,
        /** The locos' roster entries. */
        ROSTER,
        /** Operations car and engine lengths. */
        OPERATIONS,
        /** The sum of the components' lengths, when every component has one. */
        TRAIN_COMPONENTS,
        /** Measured from the train's crossings. */
        MEASURED
    }

    /**
     * A part a train is split into or joined from as one piece, such as a
     * rake of wagons left at a station, or the portion that goes on. It is
     * not every vehicle that could be uncoupled: a train that is never split
     * is one component however many vehicles it has, and one that leaves a
     * rake of wagons at a station is two, the rake and the rest.
     */
    @Immutable
    final class TrainComponent {

        private final String name;
        private final Double lengthMm;
        private final Double frontOverhangMm;
        private final Double rearOverhangMm;

        /**
         * A component whose wheel overhangs are not known.
         *
         * @param name     the component's name; null is taken as "", and
         *                 leading and trailing spaces are removed
         * @param lengthMm its length in millimetres, more than 0, or null if
         *                 not known
         * @throws IllegalArgumentException if the length is not more than 0
         */
        public TrainComponent(@CheckForNull String name, @CheckForNull Double lengthMm) {
            this(name, lengthMm, null, null);
        }

        /**
         * A component with its wheel overhangs. Detection sees wheels, never
         * buffers: the front overhang is how far the component reaches ahead
         * of its first wheels, and the rear overhang how far behind its last.
         *
         * @param name            the component's name; null is taken as "",
         *                        and leading and trailing spaces are removed
         * @param lengthMm        its length in millimetres, more than 0, or
         *                        null if not known
         * @param frontOverhangMm its front wheel overhang in millimetres, 0
         *                        or more, or null if not known
         * @param rearOverhangMm  its rear wheel overhang in millimetres, 0 or
         *                        more, or null if not known
         * @throws IllegalArgumentException if the length is not more than 0,
         *                                  or an overhang is less than 0
         * @since 5.19.1
         */
        public TrainComponent(@CheckForNull String name, @CheckForNull Double lengthMm,
                @CheckForNull Double frontOverhangMm, @CheckForNull Double rearOverhangMm) {
            if (lengthMm != null && !(lengthMm > 0)) {
                throw new IllegalArgumentException("component length must be more than 0: " + lengthMm);
            }
            if (frontOverhangMm != null && !(frontOverhangMm >= 0)) {
                throw new IllegalArgumentException("front overhang must not be negative: " + frontOverhangMm);
            }
            if (rearOverhangMm != null && !(rearOverhangMm >= 0)) {
                throw new IllegalArgumentException("rear overhang must not be negative: " + rearOverhangMm);
            }
            this.name = name == null ? "" : name.trim();
            this.lengthMm = lengthMm;
            this.frontOverhangMm = frontOverhangMm;
            this.rearOverhangMm = rearOverhangMm;
        }

        /**
         * @return the component's name, or "" if it has none; never null
         */
        @Nonnull
        public String getName() {
            return name;
        }

        /**
         * @return the component's length in millimetres, or null if not known
         */
        @CheckForNull
        public Double getLength() {
            return lengthMm;
        }

        /**
         * @return how far the component reaches ahead of its first wheels, in
         *         millimetres, or null if not known
         * @since 5.19.1
         */
        @CheckForNull
        public Double getFrontOverhang() {
            return frontOverhangMm;
        }

        /**
         * @return how far the component reaches behind its last wheels, in
         *         millimetres, or null if not known
         * @since 5.19.1
         */
        @CheckForNull
        public Double getRearOverhang() {
            return rearOverhangMm;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TrainComponent)) {
                return false;
            }
            TrainComponent c = (TrainComponent) o;
            return name.equals(c.name) && Objects.equals(lengthMm, c.lengthMm)
                    && Objects.equals(frontOverhangMm, c.frontOverhangMm)
                    && Objects.equals(rearOverhangMm, c.rearOverhangMm);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, lengthMm, frontOverhangMm, rearOverhangMm);
        }

        @Override
        public String toString() {
            return "TrainComponent[" + name + ", " + lengthMm + " mm, overhang " + frontOverhangMm + " / "
                    + rearOverhangMm + " mm]";
        }
    }

    /**
     * A stretch of a train that detection can see, such as a loco, a lit
     * coach, or wagons with resistor wheelsets. Offsets are measured from
     * the train's front, not from its direction of travel.
     */
    @Immutable
    final class DetectionMarker {

        /**
         * Where a marker is on the train.
         */
        public enum Placement {
            /** The whole train; used only for Entire Train. */
            WHOLE_TRAIN,
            /** At the front of the train. */
            FRONT,
            /** At the rear of the train. */
            REAR,
            /**
             * Its front at an offset from the train's front; an unknown
             * offset means it may be anywhere along the train.
             */
            OFFSET
        }

        private final Placement placement;
        private final Double offsetMm;
        private final Double lengthMm;

        private DetectionMarker(Placement placement, Double offsetMm, Double lengthMm) {
            if (lengthMm != null && !(lengthMm > 0)) {
                throw new IllegalArgumentException("marker length must be more than 0: " + lengthMm);
            }
            if (offsetMm != null && !(offsetMm >= 0)) {
                throw new IllegalArgumentException("marker offset must not be negative: " + offsetMm);
            }
            this.placement = placement;
            this.offsetMm = offsetMm;
            this.lengthMm = lengthMm;
        }

        /**
         * @return a marker covering the whole train
         */
        @Nonnull
        public static DetectionMarker wholeTrain() {
            return new DetectionMarker(Placement.WHOLE_TRAIN, null, null);
        }

        /**
         * @param lengthMm its length in millimetres, more than 0, or null if
         *                 not known
         * @return a marker at the front of the train
         * @throws IllegalArgumentException if the length is not more than 0
         */
        @Nonnull
        public static DetectionMarker front(@CheckForNull Double lengthMm) {
            return new DetectionMarker(Placement.FRONT, null, lengthMm);
        }

        /**
         * @param lengthMm its length in millimetres, more than 0, or null if
         *                 not known
         * @return a marker at the rear of the train
         * @throws IllegalArgumentException if the length is not more than 0
         */
        @Nonnull
        public static DetectionMarker rear(@CheckForNull Double lengthMm) {
            return new DetectionMarker(Placement.REAR, null, lengthMm);
        }

        /**
         * @param offsetMm how far its front is from the train's front in
         *                 millimetres, not negative, or null if not known
         * @param lengthMm its length in millimetres, more than 0, or null if
         *                 not known
         * @return a marker at an offset from the train's front
         * @throws IllegalArgumentException if the offset is negative or the
         *                                  length is not more than 0
         */
        @Nonnull
        public static DetectionMarker atOffset(@CheckForNull Double offsetMm, @CheckForNull Double lengthMm) {
            return new DetectionMarker(Placement.OFFSET, offsetMm, lengthMm);
        }

        /**
         * @return where the marker is on the train
         */
        @Nonnull
        public Placement getPlacement() {
            return placement;
        }

        /**
         * @return for {@link Placement#OFFSET}, how far the marker's front is
         *         from the train's front in millimetres, or null if not known;
         *         null for the other placements
         */
        @CheckForNull
        public Double getOffset() {
            return offsetMm;
        }

        /**
         * @return the marker's length in millimetres, or null if not known
         *         (always null for {@link Placement#WHOLE_TRAIN})
         */
        @CheckForNull
        public Double getLength() {
            return lengthMm;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DetectionMarker)) {
                return false;
            }
            DetectionMarker m = (DetectionMarker) o;
            return placement == m.placement && Objects.equals(offsetMm, m.offsetMm)
                    && Objects.equals(lengthMm, m.lengthMm);
        }

        @Override
        public int hashCode() {
            return Objects.hash(placement, offsetMm, lengthMm);
        }

        @Override
        public String toString() {
            return "DetectionMarker[" + placement + ", offset " + offsetMm + " mm, length " + lengthMm + " mm]";
        }

        /**
         * The markers for one of the standard detection settings.
         *
         * @param detection {@link Detection#ENTIRE_TRAIN},
         *                  {@link Detection#HEAD_ONLY} or
         *                  {@link Detection#HEAD_AND_TAIL}
         * @return the markers, front first
         * @throws IllegalArgumentException for {@link Detection#CUSTOM}, which
         *                                  has no standard markers
         */
        @Nonnull
        public static List<DetectionMarker> forDetection(@Nonnull Detection detection) {
            List<DetectionMarker> list = new ArrayList<>(2);
            switch (detection) {
                case ENTIRE_TRAIN:
                    list.add(wholeTrain());
                    break;
                case HEAD_ONLY:
                    list.add(front(null));
                    break;
                case HEAD_AND_TAIL:
                    list.add(front(null));
                    list.add(rear(null));
                    break;
                case CUSTOM:
                default:
                    throw new IllegalArgumentException("no standard markers for " + detection);
            }
            return Collections.unmodifiableList(list);
        }
    }
}
