package jmri.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.BasicRosterEntry;
import jmri.IdTag;
import jmri.InvokeOnLayoutThread;
import jmri.NamedBean;
import jmri.TrainRecord;
import jmri.TrainRecordManager;
import jmri.beans.PropertyChangeSupport;
import jmri.implementation.DefaultTrainRecord;

/**
 * The default {@link TrainRecordManager}, made by the
 * {@link jmri.InstanceManager}. Keeps each record's name, roster entry and
 * ID tags unique among the records.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public class DefaultTrainRecordManager extends PropertyChangeSupport implements TrainRecordManager {

    private final List<DefaultTrainRecord> records = new ArrayList<>();

    private final DefaultTrainRecord.Registry registry = new DefaultTrainRecord.Registry() {
        @Override
        public void checkName(@Nonnull TrainRecord record, @Nonnull String name) {
            TrainRecord other = getByName(name);
            if (other != null && other != record) {
                throw new IllegalArgumentException("a train record named " + name + " already exists");
            }
        }

        @Override
        public void checkRosterEntry(@Nonnull TrainRecord record, @Nonnull BasicRosterEntry entry) {
            TrainRecord other = getByRosterEntry(entry);
            if (other != null && other != record) {
                throw new IllegalArgumentException("roster entry " + entry.getId()
                        + " already belongs to train " + other.getName());
            }
        }

        @Override
        public void checkIdTag(@Nonnull TrainRecord record, @Nonnull IdTag tag) {
            TrainRecord other = getByIdTag(tag);
            if (other != null && other != record) {
                throw new IllegalArgumentException("ID tag " + tag.getSystemName()
                        + " already belongs to train " + other.getName());
            }
        }
    };

    /** {@inheritDoc} */
    @Override
    @Nonnull
    @InvokeOnLayoutThread
    public TrainRecord newRecord(@Nonnull String name) {
        String n = DefaultTrainRecord.normalizeName(name);
        if (getByName(n) != null) {
            throw new IllegalArgumentException("a train record named " + n + " already exists");
        }
        DefaultTrainRecord r = new DefaultTrainRecord(n, registry);
        records.add(r);
        firePropertyChange(PROPERTY_RECORDS, null, r);
        return r;
    }

    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    public void remove(@Nonnull TrainRecord record) {
        Objects.requireNonNull(record, "record");
        if (record.isRemoved()) {
            return;
        }
        if (!(record instanceof DefaultTrainRecord) || !records.contains(record)) {
            throw new IllegalArgumentException("train record " + record.getName() + " isn't this manager's");
        }
        DefaultTrainRecord r = (DefaultTrainRecord) record;
        records.remove(r);
        r.markRemoved();
        firePropertyChange(PROPERTY_RECORDS, r, null);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public List<TrainRecord> getRecords() {
        return new ArrayList<>(records);
    }

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public TrainRecord getByName(@Nonnull String name) {
        String n = NamedBean.normalizeUserName(Objects.requireNonNull(name, "name"));
        for (TrainRecord r : records) {
            if (r.getName().equals(n)) {
                return r;
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public TrainRecord getByRosterEntry(@Nonnull BasicRosterEntry entry) {
        String id = Objects.requireNonNull(entry, "entry").getId();
        for (TrainRecord r : records) {
            for (BasicRosterEntry e : r.getRosterEntries()) {
                if (e == entry || (id != null && id.equals(e.getId()))) {
                    return r;
                }
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public TrainRecord getByIdTag(@Nonnull IdTag tag) {
        Objects.requireNonNull(tag, "tag");
        for (TrainRecord r : records) {
            if (r.getIdTags().contains(tag)) {
                return r;
            }
        }
        return null;
    }
}
