package jmri.jmrit.simulation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.Immutable;

import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.InvokeOnAnyThread;
import jmri.SpeedStepMode;
import jmri.Throttle;
import jmri.ThrottleManager;
import jmri.jmrix.debugthrottle.DebugThrottleManager;

/**
 * Speed commands from the Internal connection's throttles, so that
 * Dispatcher, Warrants, throttle windows and scripts using the Internal
 * connection drive simulated trains.
 * <p>
 * The Internal connection hands out debug throttles, from a
 * {@link DebugThrottleManager}; they keep their settings and send nothing.
 * For each address the placed trains' locos obey, this source attaches a
 * listener to every debug throttle manager in the {@link InstanceManager},
 * through {@link ThrottleManager#attachListener}, the throttle managers' own
 * way of watching an address without controlling it. A throttle manager
 * stored later, as the Internal connection makes its own when first asked
 * for one, is watched too. Nothing is attached while no train is placed.
 * <p>
 * A loco's command is the throttle's speed setting, direction and speed-step
 * mode, received when the throttle was told (when it fires its property
 * change). The speed setting is turned into a speed step of the throttle's
 * mode by rounding, any speed above 0 being at least step 1. A negative
 * setting is an emergency stop. A throttle in a mode with fewer than two
 * steps (unknown or incremental) is taken as 128-step. When a train is
 * placed, a throttle that already exists for its address is read at once,
 * so a loco obeys a speed set before it was placed, as a decoder put on the
 * track obeys the command station's refreshed speed.
 * <p>
 * While a listener is attached, the throttle manager does not dispose of
 * the throttle when its users release it, so the next user of the address is
 * handed the same throttle with its last settings, much as a command station
 * goes on refreshing a released loco's last speed. When the last train is
 * removed the listeners are removed; the debug throttle itself stays with
 * the manager until it is next released.
 * <p>
 * Consists: a loco obeys the throttle for the address it responds to, its own
 * address or, when its CV19 (given when the train is built, or read from its
 * roster entry) puts it in a consist, the consist address. A consist JMRI
 * builds while running, through the consist manager, is not followed: on the
 * Internal connection that writes CV19 through ops-mode programming, which
 * the simulator does not see. Functions are ignored.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
@javax.annotation.concurrent.ThreadSafe
public class InternalThrottleSpeedSource implements SimulatedSpeedSource {

    /** Name of the throttle managers' event when a listener is attached. */
    static final String THROTTLE_ASSIGNED = "throttleAssigned";
    /** Name of the throttle managers' event when an address's throttle is replaced. */
    static final String THROTTLE_CHANGED = "throttleAssignmentChanged";

    private final Map<DccLocoAddress, Command> commands = new ConcurrentHashMap<>();
    /** Each address's last known throttle settings, changed only under {@link #updating}. */
    private final Map<DccLocoAddress, Setting> settings = new ConcurrentHashMap<>();
    private final List<CommandListener> listeners = new CopyOnWriteArrayList<>();
    /** Held only while working out and storing a command, never while calling out. */
    private final Object updating = new Object();

    @GuardedBy("this")
    private final Set<DccLocoAddress> watched = new HashSet<>();
    @GuardedBy("this")
    private final Map<ThrottleManager, Map<DccLocoAddress, Watch>> attached = new HashMap<>();
    @GuardedBy("this")
    private boolean listeningForManagers;
    private final PropertyChangeListener managerListener = this::managerStored;

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    @InvokeOnAnyThread
    public Command getCommand(@Nonnull DccLocoAddress address) {
        return commands.get(address);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @InvokeOnAnyThread
    public void addCommandListener(@Nonnull CommandListener listener) {
        listeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @InvokeOnAnyThread
    public void removeCommandListener(@Nonnull CommandListener listener) {
        listeners.remove(listener);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Attaches to each new address on every debug throttle manager, and
     * removes the listeners of addresses no longer obeyed.
     */
    @Override
    @InvokeOnAnyThread
    public synchronized void watch(@Nonnull Set<DccLocoAddress> addresses) {
        watched.clear();
        watched.addAll(addresses);
        for (Map<DccLocoAddress, Watch> map : attached.values()) {
            Iterator<Map.Entry<DccLocoAddress, Watch>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<DccLocoAddress, Watch> e = it.next();
                if (!watched.contains(e.getKey())) {
                    it.remove();
                    e.getValue().manager.removeListener(e.getKey(), e.getValue());
                }
            }
        }
        attached.values().removeIf(Map::isEmpty);
        commands.keySet().retainAll(watched);
        settings.keySet().retainAll(watched);
        if (watched.isEmpty()) {
            if (listeningForManagers) {
                InstanceManager.removePropertyChangeListener(
                        InstanceManager.getListPropertyName(ThrottleManager.class), managerListener);
                listeningForManagers = false;
            }
            return;
        }
        if (!listeningForManagers) {
            InstanceManager.addPropertyChangeListener(
                    InstanceManager.getListPropertyName(ThrottleManager.class), managerListener);
            listeningForManagers = true;
        }
        for (ThrottleManager m : new ArrayList<>(InstanceManager.getList(ThrottleManager.class))) {
            attachAll(m);
        }
    }

    private void managerStored(PropertyChangeEvent e) {
        if (e.getNewValue() instanceof ThrottleManager) {
            synchronized (this) {
                if (!watched.isEmpty()) {
                    attachAll((ThrottleManager) e.getNewValue());
                }
            }
        }
    }

    @GuardedBy("this")
    private void attachAll(ThrottleManager m) {
        if (!(m instanceof DebugThrottleManager)) {
            return;
        }
        Map<DccLocoAddress, Watch> map = attached.computeIfAbsent(m, k -> new HashMap<>());
        for (DccLocoAddress a : watched) {
            if (!map.containsKey(a)) {
                Watch w = new Watch(m, a);
                map.put(a, w);
                // a debug throttle manager makes the throttle at once if there isn't one, and says it is assigned
                m.attachListener(a, w);
            }
        }
    }

    /**
     * Store a new setting for an address, and tell the listeners.
     */
    private void update(DccLocoAddress address, java.util.function.UnaryOperator<Setting> change, long timeNanos) {
        synchronized (updating) {
            Setting s = change.apply(settings.getOrDefault(address, Setting.INITIAL));
            settings.put(address, s);
            commands.put(address, s.command(timeNanos));
        }
        for (CommandListener l : listeners) {
            l.commandChanged(address);
        }
    }

    /**
     * A speed step of a mode from a throttle's speed setting.
     *
     * @param speed the speed setting, 0 to 1
     * @param mode  the speed-step mode
     * @return the step, 0 only for a setting of 0
     */
    static int step(float speed, SpeedStepMode mode) {
        int step = Math.round(speed * mode.numSteps);
        if (step == 0 && speed > 0) {
            step = 1;
        }
        return Math.min(Math.max(step, 0), mode.numSteps);
    }

    /** A throttle's settings as last known. */
    @Immutable
    private static final class Setting {

        static final Setting INITIAL = new Setting(0.0f, true, SpeedStepMode.NMRA_DCC_128);

        final float speed;
        final boolean forward;
        final SpeedStepMode mode;

        Setting(float speed, boolean forward, SpeedStepMode mode) {
            this.speed = speed;
            this.forward = forward;
            this.mode = mode.numSteps < 2 ? SpeedStepMode.NMRA_DCC_128 : mode;
        }

        Command command(long timeNanos) {
            if (speed < 0) {
                return new Command(0, mode, forward, true, timeNanos);
            }
            return new Command(step(speed, mode), mode, forward, false, timeNanos);
        }
    }

    /** The listener attached for one address on one throttle manager. */
    private final class Watch implements PropertyChangeListener {

        final ThrottleManager manager;
        final DccLocoAddress address;

        Watch(ThrottleManager manager, DccLocoAddress address) {
            this.manager = manager;
            this.address = address;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            long now = System.nanoTime(); // when the throttle was told
            Object v = e.getNewValue();
            String name = e.getPropertyName();
            if (name == null) {
                return;
            }
            switch (name) {
                case THROTTLE_ASSIGNED:
                case THROTTLE_CHANGED:
                    Object speed = manager.getThrottleInfo(address, Throttle.SPEEDSETTING);
                    Object forward = manager.getThrottleInfo(address, Throttle.ISFORWARD);
                    Object mode = manager.getThrottleInfo(address, Throttle.SPEEDSTEPMODE);
                    update(address, s -> new Setting(speed instanceof Float ? (Float) speed : s.speed,
                            forward instanceof Boolean ? (Boolean) forward : s.forward,
                            mode instanceof SpeedStepMode ? (SpeedStepMode) mode : s.mode), now);
                    break;
                case Throttle.SPEEDSETTING:
                    if (v instanceof Float) {
                        update(address, s -> new Setting((Float) v, s.forward, s.mode), now);
                    }
                    break;
                case Throttle.ISFORWARD:
                    if (v instanceof Boolean) {
                        update(address, s -> new Setting(s.speed, (Boolean) v, s.mode), now);
                    }
                    break;
                case Throttle.SPEEDSTEPS:
                case Throttle.SPEEDSTEPMODE:
                    if (v instanceof SpeedStepMode) {
                        update(address, s -> new Setting(s.speed, s.forward, (SpeedStepMode) v), now);
                    }
                    break;
                default:
                    // functions and the managers' other notices don't move a train
                    break;
            }
        }
    }
}
