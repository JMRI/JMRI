package jmri.jmrix.marklin;

/**
 * Hardware generation of a WeichenChef Magnet (CdB class 0x22) module.
 * <p>
 * Two hardware generations share the same CdB class ID and produce identical ping replies.
 * They can only be distinguished after discovery by issuing a firmware-version probe
 * (read slot=0, reg=1) and inspecting the reply header bytes.
 * <p>
 * <b>Gen-001</b> header bytes {@code [0xFF, 0x3C]}: supports 4 coil-fire channels.
 * <b>Gen-002+</b> header bytes {@code [0x03, 0x00]}: supports 8 coil-fire channels and
 * additional per-channel registers (slow-motion, secondary address, pulse timing).
 * <p>
 * See {@link jmri.jmrix.marklin.mcan.device.CdBDeviceClass.HardwareCapability#AMBIGUOUS}
 * and {@link jmri.jmrix.marklin.mcan.cdb.CdBWeichenChefCommands#buildCoilFireTest}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public enum WeichenChefMagnetGeneration {

    /**
     * Generation 001  -  coil-fire header {@code [0xFF, 0x3C]}, 4 channels.
     * Firmware version probe returns a reply whose first data byte is {@code 0xFF} (or
     * another sentinel value distinguishing it from gen-002+ firmware).
     */
    GEN_001,

    /**
     * Generation 002 and later  -  coil-fire header {@code [0x03, 0x00]}, 8 channels.
     * Supports additional per-channel registers: slow-motion time (reg=2), secondary
     * address (reg=8, reg=9), pulse period (reg=16), pulse width (reg=17), fixed coil
     * time (reg=18).
     */
    GEN_002_PLUS,

    /**
     * Generation not yet determined.
     * <p>
     * Assigned when the device has been discovered (PING_REPLY received) but the
     * firmware-version probe (read slot=0, reg=1) has not yet returned a classifiable
     * reply. The owning {@link WeichenChefMagnetService} transitions to a concrete
     * generation value once the probe reply arrives.
     */
    UNKNOWN
}
