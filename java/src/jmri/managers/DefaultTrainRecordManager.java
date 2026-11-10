package jmri.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.BasicRosterEntry;
import jmri.Block;
import jmri.Disposable;
import jmri.IdTag;
import jmri.InvokeOnLayoutThread;
import jmri.NamedBean;
import jmri.TrainRecord;
import jmri.TrainRecordManager;
import jmri.beans.PropertyChangeSupport;
import jmri.implementation.DefaultTrainRecord;
import jmri.implementation.TrainRecordWatcher;

/**
 * The default {@link TrainRecordManager}, made by the
 * {@link jmri.InstanceManager}. Keeps each record's name, roster entry and
 * ID tags unique among the records.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public class DefaultTrainRecordManager extends PropertyChangeSupport implements TrainRecordManager, Disposable {

    private final List<DefaultTrainRecord> records = new ArrayList<>();
    private final TrainRecordWatcher watcher;
    private boolean disposed = false;
    private final java.util.Map<Block, java.util.Deque<BlockEvent>> blockHistories = new java.util.HashMap<>();
    private final java.beans.PropertyChangeListener historyListener = this::historyChanged;

    /**
     * Make the manager, which starts following the layout's Blocks: their
     * values always, their sensors while any record is placed.
     */
    public DefaultTrainRecordManager() {
        watcher = new TrainRecordWatcher(() -> new ArrayList<>(records), new TrainRecordWatcher.Placer() {
            @Override
            public void valueSet(@Nonnull Block block, @Nonnull Object value) {
                placeFromValue(block, value);
            }

            @Override
            public TrainRecord recordFor(Object value) {
                return getByValue(value);
            }
        });
        watcher.start();
    }

    /**
     * Stop following the layout. Called when the InstanceManager is cleared.
     */
    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            watcher.dispose();
        }
    }

    /**
     * A Block's value was set while it was occupied: place the record the
     * value identifies there if that record is not placed. A roster entry or
     * ID tag with no record gets one, named after it; a String names an
     * existing record only.
     */
    private void placeFromValue(Block block, Object value) {
        TrainRecord r = getByValue(value);
        if (r == null) {
            String name = null;
            if (value instanceof BasicRosterEntry) {
                name = ((BasicRosterEntry) value).getId();
            } else if (value instanceof IdTag) {
                name = ((IdTag) value).getDisplayName();
            }
            if (name == null || name.trim().isEmpty() || getByName(name) != null) {
                return;
            }
            r = newRecord(name);
            if (value instanceof BasicRosterEntry) {
                r.insertFirstRosterEntry((BasicRosterEntry) value);
            } else {
                r.addIdTag((IdTag) value);
            }
        }
        if (!r.isPlaced()) {
            r.place(List.of(block), false, TrainRecord.Reason.Rule.PLACED_BY_VALUE);
        }
    }

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
        public void placementChanged(@Nonnull TrainRecord record) {
            watcher.placementChanged();
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
        r.addPropertyChangeListener(TrainRecord.PROPERTY_HISTORY, historyListener);
        records.add(r);
        firePropertyChange(PROPERTY_RECORDS, null, r);
        return r;
    }

    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    public void remove(@Nonnull TrainRecord record) {
        Objects.requireNonNull(record, "record");
        if (record.isDisposed()) {
            return;
        }
        if (!(record instanceof DefaultTrainRecord) || !records.contains(record)) {
            throw new IllegalArgumentException("train record " + record.getName() + " isn't this manager's");
        }
        DefaultTrainRecord r = (DefaultTrainRecord) record;
        records.remove(r);
        r.removePropertyChangeListener(TrainRecord.PROPERTY_HISTORY, historyListener);
        r.dispose();
        watcher.placementChanged();
        firePropertyChange(PROPERTY_RECORDS, r, null);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public List<BlockEvent> getHistory(@Nonnull Block block) {
        java.util.Deque<BlockEvent> h = blockHistories.get(block);
        return h == null ? new ArrayList<>() : new ArrayList<>(h);
    }

    private void historyChanged(java.beans.PropertyChangeEvent e) {
        if (!(e.getSource() instanceof TrainRecord) || !(e.getNewValue() instanceof TrainRecord.Reason)) {
            return;
        }
        TrainRecord.Reason reason = (TrainRecord.Reason) e.getNewValue();
        String train = ((TrainRecord) e.getSource()).getName();
        List<Block> blocks = new ArrayList<>();
        if (reason.getBlock() != null) {
            blocks.add(reason.getBlock());
        }
        for (Block b : reason.getThrough()) {
            if (!blocks.contains(b)) {
                blocks.add(b);
            }
        }
        for (Block b : blocks) {
            java.util.Deque<BlockEvent> h = blockHistories.computeIfAbsent(b, k -> new java.util.ArrayDeque<>());
            h.addFirst(new BlockEvent(train, reason));
            while (h.size() > BLOCK_HISTORY_LENGTH) {
                h.removeLast();
            }
        }
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

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public TrainRecord getByValue(@CheckForNull Object value) {
        if (value instanceof BasicRosterEntry) {
            return getByRosterEntry((BasicRosterEntry) value);
        }
        if (value instanceof IdTag) {
            return getByIdTag((IdTag) value);
        }
        if (value instanceof String && !((String) value).trim().isEmpty()) {
            return getByName((String) value);
        }
        return null;
    }
}
