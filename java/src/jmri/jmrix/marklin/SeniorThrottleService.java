package jmri.jmrix.marklin;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import javax.swing.Timer;
import jmri.jmrix.marklin.mcan.McanUid;
import jmri.util.ThreadingUtil;

/**
 * Passive calculation of the most senior throttle/master device on the bus.
 *
 * Devices are considered stale if their LastSeen is older than 60 seconds
 * TODO: Make this configurable for unit tests or special setups
 *
 * Among MS2, the non-stale device with the smallest UID is considered the most senior.
 *
 * If a CS2, CS3 or CS3+ is present, it is senior to all MS2.
 *
 * The exact seniority rules among CS2, CS3, CS3+ are not yet modeled here.
 * Public documentation is contradictory.
 *
 * For JMRI purposes, a CS2 cannot be senior if a CS3 or CS3+ is present.
 * More complete rules on multiple CS2/CS3/CS3+ must come from observation.
 */
public final class SeniorThrottleService {
    public static final Duration STALE_AFTER = Duration.ofSeconds(60);
    public static final String PROPERTY_MASTER = "master";
    public enum Role { MASTER, JUNIOR, UNKNOWN }

    private final McanDeviceManager manager;
    private final Clock clock;
    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
    private final Timer timer;
    private volatile McanDevice master;
    private volatile boolean disposed;

    public SeniorThrottleService(McanDeviceManager manager) {
        this(manager, Clock.systemUTC());
    }

    SeniorThrottleService(McanDeviceManager manager, Clock clock) {
        this.manager = Objects.requireNonNull(manager);
        this.clock = Objects.requireNonNull(clock);
        timer = new Timer(1_000, e -> refresh());
        timer.start();
    }

    public static boolean isMs2(McanDevice device) {
        if (!(device.getIdentity() instanceof McanDeviceIdentity.Native)) {
            return false;
        }
        McanDeviceIdentity.Native identity = (McanDeviceIdentity.Native) device.getIdentity();
        return identity.getDeviceType() >= 0x0030 && identity.getDeviceType() <= 0x0039;
    }

    private boolean isFresh(McanDevice device) {
        Instant now = clock.instant();
        Instant seen = device.getLastSeen();
        return isMs2(device) && device.getDeviceState() == McanDeviceState.ONLINE
                && seen.isAfter(now.minus(STALE_AFTER));
    }

    /** Re-evaluate. Also called periodically for expiry. */
    public void refresh() {
        if (disposed) {
            return;
        }
        McanDevice candidate = null;
        for (McanDevice device : manager.getNamedBeanSet()) {
            if (isFresh(device) && (candidate == null || uid(device).compareTo(uid(candidate)) < 0)) {
                candidate = device;
            }
        }
        if (candidate != master) {
            McanDevice old = master;
            master = candidate;
            McanDevice value = candidate;
            ThreadingUtil.runOnGUI(() -> {
                if (!disposed) {
                    changes.firePropertyChange(PROPERTY_MASTER, old, value);
                }
            });
        }
    }

    public Optional<McanDevice> getMaster() {
        refresh();
        return disposed ? Optional.empty() : Optional.ofNullable(master);
    }

    public Role getRole(McanDevice device) {
        refresh();
        if (disposed || !manager.getNamedBeanSet().contains(device) || !isFresh(device) || master == null) {
            return Role.UNKNOWN;
        }
        return device == master ? Role.MASTER : Role.JUNIOR;
    }

    private static McanUid uid(McanDevice device) {
        return ((McanDeviceIdentity.Native) device.getIdentity()).getUid();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changes.removePropertyChangeListener(listener);
    }

    public void dispose() {
        ThreadingUtil.runOnGUI(() -> {
            disposed = true;
            timer.stop();
            master = null;
            for (PropertyChangeListener listener : changes.getPropertyChangeListeners()) {
                changes.removePropertyChangeListener(listener);
            }
        });
    }
}
