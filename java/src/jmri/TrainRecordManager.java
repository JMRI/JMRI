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
}
