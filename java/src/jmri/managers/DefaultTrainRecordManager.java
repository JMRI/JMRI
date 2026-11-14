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
import jmri.implementation.TrainRecordMakeupWatcher;
import jmri.implementation.TrainRecordSpeedWatcher;
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
    private final TrainRecordSpeedWatcher speedWatcher = new TrainRecordSpeedWatcher(() -> new ArrayList<>(records));
    private final TrainRecordMakeupWatcher makeupWatcher = new TrainRecordMakeupWatcher(() -> new ArrayList<>(records));
    private boolean disposed = false;
    private final java.util.Map<Block, java.util.Deque<BlockEvent>> blockHistories = new java.util.HashMap<>();
    private final java.beans.PropertyChangeListener historyListener = this::historyChanged;
    private final java.util.Set<Block> unexplained = new java.util.LinkedHashSet<>();
    private final java.util.Set<Block> untrusted = new java.util.LinkedHashSet<>();
    private final java.beans.PropertyChangeListener blocksListener = e -> {
        if (e.getNewValue() instanceof List) {
            for (Object b : (List<?>) e.getNewValue()) {
                if (b instanceof Block) {
                    setUnexplained((Block) b, false); // a train is there now
                }
            }
        }
    };

    /**
     * Make the manager, which starts following the layout's Blocks: their
     * values always, their sensors while any record is placed. It follows
     * speed commands while any record has a loco address, and what trains
     * are made up of (from Operations, say) while there is any record.
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

            @Override
            public void unexplained(@Nonnull Block block, boolean unexplained) {
                setUnexplained(block, unexplained && getRecordsIn(block).isEmpty() && !isUntrusted(block));
            }

            @Override
            public boolean isUntrusted(@Nonnull Block block) {
                return untrusted.contains(block);
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
            speedWatcher.dispose();
            makeupWatcher.dispose();
        }
    }

    /**
     * A Block's value was set while it was occupied, or the Block went
     * occupied with a value it had just taken: place the record the
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
        public void rosterEntriesChanged(@Nonnull TrainRecord record) {
            speedWatcher.update();
        }

        @Override
        public void nameChanged(@Nonnull TrainRecord record) {
            makeupWatcher.update();
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
        r.addPropertyChangeListener(TrainRecord.PROPERTY_BLOCKS, blocksListener);
        records.add(r);
        makeupWatcher.update();
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
        r.removePropertyChangeListener(TrainRecord.PROPERTY_BLOCKS, blocksListener);
        r.dispose();
        watcher.placementChanged();
        speedWatcher.update();
        makeupWatcher.update();
        firePropertyChange(PROPERTY_RECORDS, r, null);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public List<BlockEvent> getHistory(@Nonnull Block block) {
        java.util.Deque<BlockEvent> h = blockHistories.get(block);
        return h == null ? new ArrayList<>() : new ArrayList<>(h);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public List<Block> getUnexplained() {
        return new ArrayList<>(unexplained);
    }

    private void setUnexplained(Block block, boolean is) {
        if (is ? unexplained.add(block) : unexplained.remove(block)) {
            firePropertyChange(PROPERTY_UNEXPLAINED, is ? null : block, is ? block : null);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isUntrusted(@Nonnull Block block) {
        return untrusted.contains(block);
    }

    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    public void setUntrusted(@Nonnull Block block, boolean untrust) {
        Objects.requireNonNull(block, "block");
        if (untrust ? untrusted.add(block) : untrusted.remove(block)) {
            if (untrust) {
                setUnexplained(block, false);
            }
            firePropertyChange(PROPERTY_UNTRUSTED, untrust ? null : block, untrust ? block : null);
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public List<Block> getUntrusted() {
        return new ArrayList<>(untrusted);
    }

    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    public void reportOverdue(@Nonnull TrainRecord record, @Nonnull Block block, boolean commanded) {
        if (record instanceof DefaultTrainRecord && records.contains(record)) {
            ((DefaultTrainRecord) record).overdue(block, commanded);
        }
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
