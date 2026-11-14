package jmri.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.InvokeOnAnyThread;
import jmri.InvokeOnLayoutThread;
import jmri.TrainMakeupSource;
import jmri.TrainMakeupSource.TrainMakeup;
import jmri.TrainRecord.LengthSource;
import jmri.util.ThreadingUtil;

/**
 * Gives each train record what the train of the same name is made up of,
 * from the {@link TrainMakeupSource}s in the {@link InstanceManager}, such as
 * Operations: its components and its {@link LengthSource#OPERATIONS} length.
 * It does so when a record is made, and when a source says a train's makeup
 * has changed. When no source knows a train any more, its record's
 * Operations length is cleared; its components are left as they are.
 * <p>
 * Sources stored or removed later are followed. They are listened to only
 * while there is a record, so a source costs nothing while train tracking is
 * not in use. Changes arriving on another thread are handed to the layout
 * thread (Swing thread).
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public final class TrainRecordMakeupWatcher {

    private final Supplier<List<DefaultTrainRecord>> records;
    private final Set<TrainMakeupSource> attached = Collections.newSetFromMap(new IdentityHashMap<>());
    private final PropertyChangeListener sourcesChanged = e -> onLayoutThread(this::update);
    private final PropertyChangeListener makeupChanged = this::makeupChanged;
    private boolean watchingInstances = false;
    private boolean disposed = false;

    /**
     * @param records the records, read on the layout thread
     */
    public TrainRecordMakeupWatcher(@Nonnull Supplier<List<DefaultTrainRecord>> records) {
        this.records = records;
    }

    /**
     * Start or stop listening to the sources, as the records now need: some
     * record, and not disposed; then give every record its train's makeup.
     * Called by the manager when records are added or removed.
     */
    @InvokeOnLayoutThread
    public void update() {
        boolean wanted = !disposed && !records.get().isEmpty();
        if (wanted) {
            if (!watchingInstances) {
                InstanceManager.addPropertyChangeListener(
                        InstanceManager.getListPropertyName(TrainMakeupSource.class), sourcesChanged);
                watchingInstances = true;
            }
            List<TrainMakeupSource> now = InstanceManager.getList(TrainMakeupSource.class);
            for (TrainMakeupSource s : now) {
                if (attached.add(s)) {
                    s.addPropertyChangeListener(TrainMakeupSource.PROPERTY_MAKEUP, makeupChanged);
                }
            }
            for (Iterator<TrainMakeupSource> i = attached.iterator(); i.hasNext();) {
                TrainMakeupSource s = i.next();
                if (!now.contains(s)) {
                    s.removePropertyChangeListener(TrainMakeupSource.PROPERTY_MAKEUP, makeupChanged);
                    i.remove();
                }
            }
            for (DefaultTrainRecord r : records.get()) {
                apply(r);
            }
        } else {
            if (watchingInstances) {
                InstanceManager.removePropertyChangeListener(
                        InstanceManager.getListPropertyName(TrainMakeupSource.class), sourcesChanged);
                watchingInstances = false;
            }
            for (TrainMakeupSource s : attached) {
                s.removePropertyChangeListener(TrainMakeupSource.PROPERTY_MAKEUP, makeupChanged);
            }
            attached.clear();
        }
    }

    /**
     * Stop listening, for good.
     */
    @InvokeOnLayoutThread
    public void dispose() {
        disposed = true;
        update();
    }

    @InvokeOnAnyThread
    private void makeupChanged(@Nonnull PropertyChangeEvent e) {
        if (!(e.getNewValue() instanceof String)) {
            return;
        }
        String name = (String) e.getNewValue();
        onLayoutThread(() -> {
            if (disposed) {
                return;
            }
            for (DefaultTrainRecord r : records.get()) {
                if (r.getName().equals(name)) {
                    apply(r);
                }
            }
        });
    }

    /**
     * Give a record the components and Operations length of the train of the
     * same name, or clear its Operations length if no source knows it.
     *
     * @param r the record
     */
    private static void apply(@Nonnull DefaultTrainRecord r) {
        TrainMakeup m = TrainMakeupSource.find(r.getName());
        if (m == null) {
            if (r.getLength(LengthSource.OPERATIONS) != null) {
                r.setLength(LengthSource.OPERATIONS, null);
            }
            return;
        }
        if (!m.getTrainComponents().equals(r.getTrainComponents())) {
            r.setTrainComponents(m.getTrainComponents());
        }
        Double length = m.getLength();
        if (length == null ? r.getLength(LengthSource.OPERATIONS) != null
                : !length.equals(r.getLength(LengthSource.OPERATIONS))) {
            r.setLength(LengthSource.OPERATIONS, length);
        }
    }

    private static void onLayoutThread(@Nonnull Runnable r) {
        if (ThreadingUtil.isLayoutThread()) {
            r.run();
        } else {
            ThreadingUtil.runOnLayoutEventually(r::run);
        }
    }
}
