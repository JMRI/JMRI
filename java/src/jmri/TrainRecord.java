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
    /** Property name for a change of the lead loco's roster entry. */
    String PROPERTY_ROSTER_ENTRY = "rosterEntry"; // NOI18N
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
     * @return the train's name, unique among the records
     */
    @Nonnull
    String getName();

    /**
     * Rename the train.
     *
     * @param name the new name; leading and trailing spaces are removed as
     *             for a user name
     * @throws IllegalArgumentException if the name is empty or another record
     *                                  has it
     * @throws IllegalStateException    if the record has been removed
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
    BasicRosterEntry getRosterEntry();

    /**
     * Make a roster entry the lead loco's. If the entry is already one of the
     * train's locos it moves to the front; otherwise it is added at the front.
     * Null removes the lead loco's entry, and the next loco, if any, leads.
     * A roster entry belongs to at most one record.
     *
     * @param entry the roster entry, or null to remove the lead loco's
     * @throws IllegalArgumentException if another record has this entry
     * @throws IllegalStateException    if the record has been removed
     */
    @InvokeOnLayoutThread
    void setRosterEntry(@CheckForNull BasicRosterEntry entry);

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
     * @throws IllegalStateException    if the record has been removed
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
     * @throws IllegalStateException    if the record has been removed
     */
    @InvokeOnLayoutThread
    void addIdTag(@Nonnull IdTag tag);

    /**
     * Remove an ID tag from the train. Removing a tag the record doesn't have
     * does nothing.
     *
     * @param tag the tag
     * @throws IllegalStateException if the record has been removed
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
     * @throws IllegalStateException    if the record has been removed
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
     * @throws IllegalStateException    if the record has been removed
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
     * @throws IllegalStateException    if the record has been removed
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
     * @throws IllegalStateException    if the record has been removed
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
     * @throws IllegalStateException    if the record has been removed
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
     * @return true once the record has been removed from its manager; a
     *         removed record can't be changed
     */
    boolean isRemoved();

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

        /**
         * @param name     the component's name; null is taken as "", and
         *                 leading and trailing spaces are removed
         * @param lengthMm its length in millimetres, more than 0, or null if
         *                 not known
         * @throws IllegalArgumentException if the length is not more than 0
         */
        public TrainComponent(@CheckForNull String name, @CheckForNull Double lengthMm) {
            if (lengthMm != null && !(lengthMm > 0)) {
                throw new IllegalArgumentException("component length must be more than 0: " + lengthMm);
            }
            this.name = name == null ? "" : name.trim();
            this.lengthMm = lengthMm;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TrainComponent)) {
                return false;
            }
            TrainComponent c = (TrainComponent) o;
            return name.equals(c.name) && Objects.equals(lengthMm, c.lengthMm);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, lengthMm);
        }

        @Override
        public String toString() {
            return "TrainComponent[" + name + ", " + lengthMm + " mm]";
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
