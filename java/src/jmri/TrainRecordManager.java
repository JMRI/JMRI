package jmri;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.beans.PropertyChangeProvider;

/**
 * Creates, finds and removes {@link TrainRecord}s. There is one, the default
 * from the {@link InstanceManager}.
 * <p>
 * A record's name, roster entry and ID tags each belong to one record only,
 * so a train can be found by any of them.
 * <p>
 * Used on the layout thread (Swing thread).
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public interface TrainRecordManager extends PropertyChangeProvider {

    /**
     * Property name for a record added or removed; the new value is the
     * record added, the old value the record removed.
     */
    String PROPERTY_RECORDS = "records"; // NOI18N

    /**
     * Create a record for a train. It is one component with no length,
     * detected as Entire Train, with no roster entries and no ID tags.
     *
     * @param name the train's name; leading and trailing spaces are removed
     *             as for a user name
     * @return the new record
     * @throws IllegalArgumentException if the name is empty or another
     *                                  record has it
     */
    @Nonnull
    @InvokeOnLayoutThread
    TrainRecord newRecord(@Nonnull String name);

    /**
     * Remove a record and dispose of it, so it can't be changed afterwards.
     * Removing a record that has already been removed does nothing.
     *
     * @param record the record
     * @throws IllegalArgumentException if the record isn't this manager's
     */
    @InvokeOnLayoutThread
    void remove(@Nonnull TrainRecord record);

    /**
     * @return the records, in the order they were created; a copy
     */
    @Nonnull
    List<TrainRecord> getRecords();

    /**
     * @param name the train's name; leading and trailing spaces are ignored
     * @return the record with this name, or null
     */
    @CheckForNull
    TrainRecord getByName(@Nonnull String name);

    /**
     * @param entry a roster entry
     * @return the record one of whose locos has this roster entry, or an
     *         entry with the same ID, or null
     */
    @CheckForNull
    TrainRecord getByRosterEntry(@Nonnull BasicRosterEntry entry);

    /**
     * @param tag an ID tag
     * @return the record this tag belongs to, or null
     */
    @CheckForNull
    TrainRecord getByIdTag(@Nonnull IdTag tag);

    /**
     * The record a Block value identifies: a roster entry's, an ID tag's,
     * or, for a String, the record of that name.
     *
     * @param value a Block value
     * @return the record, or null if the value identifies none
     */
    @CheckForNull
    TrainRecord getByValue(@CheckForNull Object value);

    /**
     * The trains a Block holds.
     *
     * @param block a Block
     * @return the records placed in it, in the order of {@link #getRecords()}
     */
    @Nonnull
    default List<TrainRecord> getRecordsIn(@Nonnull Block block) {
        List<TrainRecord> list = new java.util.ArrayList<>();
        for (TrainRecord r : getRecords()) {
            if (!r.isDisposed() && r.getBlocks().contains(block)) {
                list.add(r);
            }
        }
        return list;
    }

    /**
     * What has happened in a Block: the last {@value #BLOCK_HISTORY_LENGTH}
     * changes to the trains in it, from every record, most recent first.
     *
     * @param block a Block
     * @return the history; a copy
     */
    @Nonnull
    List<BlockEvent> getHistory(@Nonnull Block block);

    /** How many entries each Block's history keeps. */
    int BLOCK_HISTORY_LENGTH = 20;

    /**
     * Something that happened in a Block: the train's name at the time, and
     * what happened, with its reason.
     */
    @javax.annotation.concurrent.Immutable
    final class BlockEvent {

        private final String train;
        private final TrainRecord.Reason reason;

        /**
         * @param train  the train's name at the time
         * @param reason what happened, and why
         */
        public BlockEvent(@Nonnull String train, @Nonnull TrainRecord.Reason reason) {
            this.train = java.util.Objects.requireNonNull(train, "train");
            this.reason = java.util.Objects.requireNonNull(reason, "reason");
        }

        /**
         * @return the train's name at the time
         */
        @Nonnull
        public String getTrain() {
            return train;
        }

        /**
         * @return what happened, and why
         */
        @Nonnull
        public TrainRecord.Reason getReason() {
            return reason;
        }
    }
}
