package jmri.jmrix.marklin;

/**
 * Lifecycle states for an MCAN (Marklin CAN) bus device or module.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public enum McanDeviceState {
    /** Ping reply or message received, device presence and identity established. */
    DISCOVERED,

    /** Follow-up parameter reads in progress (e.g. CdB firmware/hardware version or CS2 config). */
    INITIALIZING,

    /** Fully initialized and actively responding on the bus. */
    ONLINE,

    /** No response received within the configured threshold; status uncertain. */
    STALE,

    /** Explicitly disconnected or lost. */
    OFFLINE
}
