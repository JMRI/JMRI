package jmri.implementation;

import java.beans.PropertyChangeListener;
import java.time.Instant;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.InvokeOnAnyThread;
import jmri.InvokeOnLayoutThread;
import jmri.LocoAddress;
import jmri.LocoSpeedSource;
import jmri.ThrottleManager;
import jmri.TrainRecord.SpeedCommand;
import jmri.util.ThreadingUtil;

/**
 * Gives each train record the speed commands for its locos: those of every
 * {@link LocoSpeedSource}, matched to a record by the loco addresses of its
 * roster entries, every loco of the train, not only the lead. It only
 * watches.
 * <p>
 * The sources are every {@link ThrottleManager} that is a
 * {@link LocoSpeedSource}, which covers every throttle JMRI holds on any
 * connection, and every {@link LocoSpeedSource} in the
 * {@link InstanceManager}, such as a connection that sees other cabs'
 * commands on its bus. Sources stored or removed later are followed. They
 * are listened to only while some record has a loco address.
 * <p>
 * Reports arriving on another thread are handed to the layout thread (Swing
 * thread) in order. Each command keeps the time its source gave it.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public final class TrainRecordSpeedWatcher implements LocoSpeedSource.Listener {

    private final Supplier<List<DefaultTrainRecord>> records;
    private final Set<LocoSpeedSource> attached = Collections.newSetFromMap(new IdentityHashMap<>());
    private final PropertyChangeListener sourcesChanged = e -> onLayoutThread(this::update);
    private boolean watchingInstances = false;
    private boolean disposed = false;

    /**
     * @param records the records, read on the layout thread
     */
    public TrainRecordSpeedWatcher(@Nonnull Supplier<List<DefaultTrainRecord>> records) {
        this.records = records;
    }

    /**
     * Start or stop listening to the sources, as the records now need: some
     * record with a loco address, and not disposed. Called by the manager
     * when records are added or removed, or a record's roster entries change.
     */
    @InvokeOnLayoutThread
    public void update() {
        boolean wanted = !disposed && records.get().stream().anyMatch(r -> !r.getLocoAddresses().isEmpty());
        if (wanted) {
            if (!watchingInstances) {
                InstanceManager.addPropertyChangeListener(InstanceManager.getListPropertyName(ThrottleManager.class),
                        sourcesChanged);
                InstanceManager.addPropertyChangeListener(InstanceManager.getListPropertyName(LocoSpeedSource.class),
                        sourcesChanged);
                watchingInstances = true;
            }
            Set<LocoSpeedSource> now = sources();
            for (LocoSpeedSource s : now) {
                if (attached.add(s)) {
                    s.addLocoSpeedListener(this);
                }
            }
            for (Iterator<LocoSpeedSource> i = attached.iterator(); i.hasNext();) {
                LocoSpeedSource s = i.next();
                if (!now.contains(s)) {
                    s.removeLocoSpeedListener(this);
                    i.remove();
                }
            }
        } else {
            if (watchingInstances) {
                InstanceManager.removePropertyChangeListener(
                        InstanceManager.getListPropertyName(ThrottleManager.class), sourcesChanged);
                InstanceManager.removePropertyChangeListener(
                        InstanceManager.getListPropertyName(LocoSpeedSource.class), sourcesChanged);
                watchingInstances = false;
            }
            for (LocoSpeedSource s : attached) {
                s.removeLocoSpeedListener(this);
            }
            attached.clear();
        }
    }

    /**
     * @return the sources now in the InstanceManager, each once
     */
    @Nonnull
    private static Set<LocoSpeedSource> sources() {
        Set<LocoSpeedSource> found = Collections.newSetFromMap(new IdentityHashMap<>());
        for (ThrottleManager tm : InstanceManager.getList(ThrottleManager.class)) {
            if (tm instanceof LocoSpeedSource) {
                found.add((LocoSpeedSource) tm);
            }
        }
        found.addAll(InstanceManager.getList(LocoSpeedSource.class));
        return found;
    }

    /**
     * @return true if listening to the sources
     */
    boolean isListening() {
        return watchingInstances;
    }

    /**
     * Stop listening, for good.
     */
    @InvokeOnLayoutThread
    public void dispose() {
        disposed = true;
        update();
    }

    /** {@inheritDoc} */
    @Override
    @InvokeOnAnyThread
    public void speedCommanded(@Nonnull LocoAddress address, float speed, boolean forward, long timeNanos) {
        Instant time = wallClock(timeNanos);
        onLayoutThread(() -> deliver(address,
                last -> new SpeedCommand(address, speed, forward, false, timeNanos, time)));
    }

    /** {@inheritDoc} */
    @Override
    @InvokeOnAnyThread
    public void released(@Nonnull LocoAddress address, long timeNanos) {
        Instant time = wallClock(timeNanos);
        onLayoutThread(() -> deliver(address, last -> new SpeedCommand(address,
                last == null ? 0f : last.getSpeedSetting(), last == null || last.isForward(), true, timeNanos, time)));
    }

    /** Makes a command for a record, from the record's last command for the same loco. */
    private interface CommandFor {

        @Nonnull
        SpeedCommand make(@CheckForNull SpeedCommand last);
    }

    private void deliver(@Nonnull LocoAddress address, @Nonnull CommandFor command) {
        if (disposed) {
            return;
        }
        for (DefaultTrainRecord r : records.get()) {
            if (!r.isRemoved() && r.getLocoAddresses().stream().anyMatch(a -> sameAddress(address, a))) {
                r.commanded(command.make(r.getLastCommand(address)));
            }
        }
    }

    /**
     * Loco addresses are the same if their numbers and protocols are: a
     * short and a long address with the same number are different locos.
     *
     * @param a an address
     * @param b another, or null
     * @return true if they are the same
     */
    static boolean sameAddress(@Nonnull LocoAddress a, @CheckForNull LocoAddress b) {
        return b != null && a.getNumber() == b.getNumber() && a.getProtocol() == b.getProtocol();
    }

    /**
     * @param timeNanos a time on the {@link System#nanoTime()} scale
     * @return the same time as a wall-clock time
     */
    @Nonnull
    private static Instant wallClock(long timeNanos) {
        return Instant.now().minusNanos(System.nanoTime() - timeNanos);
    }

    private static void onLayoutThread(@Nonnull Runnable r) {
        if (ThreadingUtil.isLayoutThread()) {
            r.run();
        } else {
            ThreadingUtil.runOnLayoutEventually(r::run);
        }
    }
}
