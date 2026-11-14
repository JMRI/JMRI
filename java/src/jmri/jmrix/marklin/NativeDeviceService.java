package jmri.jmrix.marklin;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.OptionalInt;
import java.util.Optional;
import jmri.util.ThreadingUtil;

/**
 * Common base for native-device services, separate from immutable identity.
 * The default hash is predictable, but collision-selected hashes must be observed.
 */
public class NativeDeviceService implements McanDeviceService {
    public static final String PROPERTY_OBSERVED_HASH = "nativeHash";

    private final int defaultHash;
    private final McanDevice device;
    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
    private int observedHash = -1;

    public NativeDeviceService(McanDevice device) {
        if (!(device.getIdentity() instanceof McanDeviceIdentity.Native)) {
            throw new IllegalArgumentException("Native service requires a native device");
        }
        this.device = device;
        McanDeviceIdentity.Native identity = (McanDeviceIdentity.Native) device.getIdentity();
        defaultHash = (identity.getUid().calculateHash() & 0xFC7F) | 0x0300;
    }

    /** Find the native base service, including future model-specific subclasses. */
    public static Optional<NativeDeviceService> get(McanDevice device) {
        McanDeviceService service = device.getDeviceService();
        return service instanceof NativeDeviceService
                ? Optional.of((NativeDeviceService) service) : Optional.empty();
    }

    /** UID-derived hash with CS2 fingerprint bits; not evidence of a sender. */
    public int getDefaultHash() {
        return defaultHash;
    }

    /** Last hash positively associated with this UID by discovery. */
    public synchronized OptionalInt getObservedHash() {
        return observedHash < 0 ? OptionalInt.empty() : OptionalInt.of(observedHash);
    }

    public void setObservedHash(int hash) {
        if (hash < 0 || hash > 0xFFFF || (hash & 0x0380) != 0x0300) {
            throw new IllegalArgumentException("Not a native CAN sender hash: " + hash);
        }
        int old;
        synchronized (this) {
            old = observedHash;
            if (old == hash) {
                return;
            }
            observedHash = hash;
        }
        ThreadingUtil.runOnGUI(() -> {
            changes.firePropertyChange(PROPERTY_OBSERVED_HASH, old, hash);
            if (device.getDeviceService() == this) {
                device.notifyDeviceServiceChanged();
            }
        });
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changes.removePropertyChangeListener(listener);
    }
}
