package jmri.jmrix.marklin;

import java.time.Instant;
import java.util.Objects;
import jmri.implementation.AbstractNamedBean;
import jmri.util.ThreadingUtil;

/**
 * Represents an MCAN device (Native CS2/MS2 or CdB module) as a JMRI NamedBean entity.
 * <p>
 * Property changes (such as state updates or firmware version changes) are dispatched
 * on the Swing EDT via {@link ThreadingUtil#runOnGUI(ThreadingUtil.ThreadAction)} for thread safety.
 * <p>
 * Each device may carry an {@link McanDeviceService}  -  a type-specific service object
 * instantiated by the classifier layer after discovery. The service holds all state and
 * logic that only makes sense for one particular device class (e.g. WeichenChef Magnet
 * generation, Reporter occupancy channels). {@code McanDevice} itself stays generic and
 * never imports device-class-specific types.
 * <p>
 * TODO: once a classifier layer exists for all device classes,
 * the existing {@code WeichenChefKombiParameterService} and {@code ReporterParameterService}
 * should implement {@code McanDeviceService} and be attached here automatically instead of
 * being wired manually by UI panels.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de) */
public class McanDevice extends AbstractNamedBean {

    public static final String PROPERTY_DEVICE_STATE    = "deviceState";
    public static final String PROPERTY_SOFTWARE_VERSION = "softwareVersion";
    public static final String PROPERTY_HARDWARE_VERSION = "hardwareVersion";
    public static final String PROPERTY_FIRST_SEEN      = "firstSeen";
    public static final String PROPERTY_LAST_SEEN       = "lastSeen";
    public static final String PROPERTY_HW_FINGERPRINT  = "hwFingerprint";
    public static final String PROPERTY_DEVICE_SERVICE  = "deviceService";

    private final McanDeviceIdentity identity;
    private McanDeviceState deviceState;
    private int softwareVersion = -1;
    private int hardwareVersion = -1;
    private final Instant firstSeen;
    private Instant lastSeen;
    private int[] factoryChallengeBytes = null;
    private long hwFingerprint = -1L; // bytes 9-12 of ping reply, -1 = unknown
    private McanDeviceService deviceService = null;

    /**
     * Create an McanDevice bean using system prefix and identity.
     *
     * @param systemPrefix system prefix (e.g. "M")
     * @param identity device identity (Native or CdB)
     */
    public McanDevice(String systemPrefix, McanDeviceIdentity identity) {
        super(Objects.requireNonNull(identity, "identity cannot be null").getSystemName(systemPrefix));
        this.identity = identity;
        this.deviceState = McanDeviceState.DISCOVERED;
        this.firstSeen = Instant.now();
        this.lastSeen = this.firstSeen;
        setUserName(identity.getDisplayName());
    }

    /**
     * Get first time this device was seen active on the bus.
     * @return timestamp
     */
    public synchronized Instant getFirstSeen() {
        return firstSeen;
    }

    /**
     * Get device identity.
     * @return device identity
     */
    public McanDeviceIdentity getIdentity() {
        return identity;
    }

    /**
     * Get current device lifecycle state.
     * @return device state
     */
    public synchronized McanDeviceState getDeviceState() {
        return deviceState;
    }

    /**
     * Set device lifecycle state, dispatching PropertyChangeEvent on EDT if changed.
     * @param newState new device state
     */
    public void setDeviceState(McanDeviceState newState) {
        Objects.requireNonNull(newState, "newState cannot be null");
        McanDeviceState oldState;
        synchronized (this) {
            if (this.deviceState == newState) {
                return;
            }
            oldState = this.deviceState;
            this.deviceState = newState;
        }
        ThreadingUtil.runOnGUI(() -> firePropertyChange(PROPERTY_DEVICE_STATE, oldState, newState));
    }

    /**
     * Get software version code or -1 if unknown.
     * @return software version
     */
    public synchronized int getSoftwareVersion() {
        return softwareVersion;
    }

    /**
     * Set software version, dispatching PropertyChangeEvent on EDT if changed.
     * @param version software version code
     */
    public void setSoftwareVersion(int version) {
        int oldVersion;
        synchronized (this) {
            if (this.softwareVersion == version) {
                return;
            }
            oldVersion = this.softwareVersion;
            this.softwareVersion = version;
        }
        ThreadingUtil.runOnGUI(() -> firePropertyChange(PROPERTY_SOFTWARE_VERSION, oldVersion, version));
    }

    /**
     * Get hardware version code or -1 if unknown.
     * @return hardware version
     */
    public synchronized int getHardwareVersion() {
        return hardwareVersion;
    }

    /**
     * Set hardware version, dispatching PropertyChangeEvent on EDT if changed.
     * @param version hardware version code
     */
    public void setHardwareVersion(int version) {
        int oldVersion;
        synchronized (this) {
            if (this.hardwareVersion == version) {
                return;
            }
            oldVersion = this.hardwareVersion;
            this.hardwareVersion = version;
        }
        ThreadingUtil.runOnGUI(() -> firePropertyChange(PROPERTY_HARDWARE_VERSION, oldVersion, version));
    }

    /**
     * Get last time this device was seen active on the bus.
     * @return timestamp
     */
    public synchronized Instant getLastSeen() {
        return lastSeen;
    }

    /**
     * Touch device last seen timestamp to current time.
     */
    public void touch() {
        Instant oldTime;
        Instant newTime = Instant.now();
        synchronized (this) {
            oldTime = this.lastSeen;
            this.lastSeen = newTime;
        }
        ThreadingUtil.runOnGUI(() -> firePropertyChange(PROPERTY_LAST_SEEN, oldTime, newTime));
    }

    @Override
    public synchronized int getState() {
        return deviceState.ordinal();
    }

    @Override
    public void setState(int state) {
        if (state >= 0 && state < McanDeviceState.values().length) {
            setDeviceState(McanDeviceState.values()[state]);
        }
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameMcanDevice");
    }

    /**
     * Store the two challenge bytes from a factory ping reply (DLC=6, bytes 6–7).
     * Used by the configure panel to send the correct challenge/response select frame.
     * @param b0 byte at index 6 of the ping reply
     * @param b1 byte at index 7 of the ping reply
     */
    public synchronized void setFactoryChallengeBytes(int b0, int b1) {
        factoryChallengeBytes = new int[]{b0 & 0xFF, b1 & 0xFF};
    }

    /**
     * Return the stored factory challenge bytes or null if none recorded.
     * @return two-element array [b0, b1] or null
     */
    public synchronized int[] getFactoryChallengeBytes() {
        return factoryChallengeBytes != null ? factoryChallengeBytes.clone() : null;
    }

    /**
     * Get the hardware fingerprint (bytes 9–12 of a WeichenChef Kombi ping reply).
     * Strictly valid only for class 0x27. Returns -1 if not yet observed.
     * @return fingerprint as long or -1
     */
    public synchronized long getHwFingerprint() {
        return hwFingerprint;
    }

    /**
     * Set the hardware fingerprint, dispatching PropertyChangeEvent on EDT if changed.
     * @param fp fingerprint value (bytes 9–12 packed into a long)
     */
    public void setHwFingerprint(long fp) {
        long old;
        synchronized (this) {
            if (this.hwFingerprint == fp) return;
            old = this.hwFingerprint;
            this.hwFingerprint = fp;
        }
        ThreadingUtil.runOnGUI(() -> firePropertyChange(PROPERTY_HW_FINGERPRINT, old, fp));
    }

    /**
     * Return the type-specific service attached to this device by the classifier layer,
     * or {@code null} if none has been attached yet.
     *
     * @return service object or null
     */
    public synchronized McanDeviceService getDeviceService() {
        return deviceService;
    }

    /**
     * Attach a type-specific service to this device, dispatching a PropertyChangeEvent
     * on the EDT. The service holds all state and logic specific to one device class
     * (e.g. {@code WeichenChefMagnetService}); {@code McanDevice} treats it as opaque.
     *
     * @param service service object to attach; may be null to detach
     */
    public void setDeviceService(McanDeviceService service) {
        McanDeviceService old;
        synchronized (this) {
            if (this.deviceService == service) return;
            old = this.deviceService;
            this.deviceService = service;
        }
        ThreadingUtil.runOnGUI(() -> firePropertyChange(PROPERTY_DEVICE_SERVICE, old, service));
    }
}
