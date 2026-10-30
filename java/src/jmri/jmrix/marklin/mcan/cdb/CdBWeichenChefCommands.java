package jmri.jmrix.marklin.mcan.cdb;

import java.util.ArrayList;
import java.util.List;

/**
 * Builders for WeichenChef device register operations.
 * CdB classes 0x22, 0x23, 0x26, 0x27, 0x2A
 */
public final class CdBWeichenChefCommands {

    private CdBWeichenChefCommands() {}

    /** Writes a per-channel switching time (slot=channel, reg=3, DLC=4). */
    public static int[] buildWriteChSwitchingTime(int classId, int moduleId, int channel, int timeMs) {
        return CdBConfigCommands.buildWrite(classId, moduleId, channel, 3, timeMs);
    }

    /** Writes a per-channel RMK feedback address (slot=channel, reg=5, DLC=4). */
    public static int[] buildWriteChRmk(int classId, int moduleId, int channel, int rmkAddr) {
        return CdBConfigCommands.buildWriteLE(classId, moduleId, channel, 5, rmkAddr);
    }

    /** Writes a per-channel CdB flag (slot=channel, reg=6, DLC=4). */
    public static int[] buildWriteChCdBFlag(int classId, int moduleId, int channel, int flagValue) {
        return CdBConfigCommands.buildWrite(classId, moduleId, channel, 6, flagValue);
    }

    /** Writes a per-channel output address (slot=channel, reg=4, DLC=4). */
    public static int[] buildWriteChAddress(int classId, int moduleId, int channel, int rawAddr) {
        return CdBConfigCommands.buildWriteLE(classId, moduleId, channel, 4, rawAddr);
    }

    /** Writes per-channel slow-motion time (slot=channel, reg=2, DLC=4). */
    public static int[] buildWriteChSlowMotion(int classId, int moduleId, int channel, int timeMs) {
        return CdBConfigCommands.buildWrite(classId, moduleId, channel, 2, timeMs);
    }

    /** Writes K84 base-position flag (slot=channel, reg=4, DLC=4).
     *  Produces the same wire frame as {@link #buildWriteChAddress} (both use reg=4);
     *  use whichever name matches the intent  -  K84 flag vs raw output address. */
    public static int[] buildWriteChK84(int classId, int moduleId, int channel, int flagValue) {
        return CdBConfigCommands.buildWrite(classId, moduleId, channel, 4, flagValue);
    }

    /** Writes secondary turnout address (gen-002+ only; slot=channel, reg=8, DLC=4). */
    public static int[] buildWriteChSecondaryAddress(int classId, int moduleId, int channel, int rawAddr) {
        return CdBConfigCommands.buildWrite(classId, moduleId, channel, 8, rawAddr);
    }

    /** Enables or disables secondary address (gen-002+ only; slot=channel, reg=9, DLC=4). */
    public static int[] buildWriteChSecondaryAddressEnable(int classId, int moduleId, int channel, int enable) {
        return CdBConfigCommands.buildWrite(classId, moduleId, channel, 9, enable);
    }

    /** Writes pulse period (slot=channel, reg=16, DLC=4). */
    public static int[] buildWriteChPeriode(int classId, int moduleId, int channel, int value) {
        return CdBConfigCommands.buildWrite(classId, moduleId, channel, 16, value);
    }

    /** Writes pulse width (slot=channel, reg=17, DLC=4). */
    public static int[] buildWriteChPulsbreite(int classId, int moduleId, int channel, int value) {
        return CdBConfigCommands.buildWrite(classId, moduleId, channel, 17, value);
    }

    /** Writes fixed coil-fire time (slot=channel, reg=18, DLC=4). */
    public static int[] buildWriteChFesteZeit(int classId, int moduleId, int channel, int value) {
        return CdBConfigCommands.buildWrite(classId, moduleId, channel, 18, value);
    }

    /** Triggers EEPROM commit (slot=0, reg=254, value=1; write-only). */
    public static int[] buildWriteEepromCommit(int classId, int moduleId) {
        return CdBConfigCommands.buildWrite(classId, moduleId, 0, 254, 1);
    }

    /**
     * Builds a coil-fire test command (OLD-wire opcode 0x16, DLC=6).
     * <p>
     * The two hardware generations of WeichenChef Magnet are distinguished by the
     * coil-header bytes at frame[2–3]:
     * <ul>
     *   <li>gen-001: {@code [0xFF, 0x3C]}  -  4 pulse channels</li>
     *   <li>gen-002+: {@code [0x03, 0x00]}  -  8 pulse channels</li>
     * </ul>
     * The caller must already know the generation (e.g. from a firmware-version probe)
     * before invoking this builder. See {@link jmri.jmrix.marklin.mcan.device.CdBDeviceClass.HardwareCapability}.
     *
     * @param classId   8-bit CdB device class (0x22 for WeichenChef Magnet)
     * @param moduleId  module slot
     * @param channel   1-based channel (1–4 for gen-001; 1–8 for gen-002+)
     * @param rawAddr   16-bit encoded turnout address
     * @param direction 0 or 1
     * @param active    0 (deactivate) or 1 (activate)
     * @param gen001    {@code true} = gen-001 header {@code [0xFF, 0x3C]};
     *                  {@code false} = gen-002+ header {@code [0x03, 0x00]}
     * @return 13-byte frame
     */
    public static int[] buildCoilFireTest(int classId, int moduleId,
                                          int channel, int rawAddr,
                                          int direction, int active,
                                          boolean gen001) {
        int[] frame = new int[13];
        frame[0] = 0x00;
        frame[1] = 0x16;
        frame[2] = gen001 ? 0xFF : 0x03;
        frame[3] = gen001 ? 0x3C : 0x00;
        frame[4] = 6;
        frame[5] = channel   & 0xFF;
        frame[6] = 0x00;
        frame[7] = (rawAddr >> 8) & 0xFF;
        frame[8] =  rawAddr       & 0xFF;
        frame[9] = direction & 0xFF;
        frame[10] = active   & 0xFF;
        return frame;
    }

    /**
     * Builds all property read frames for a WeichenChef module.
     * Global regs {1,244,246,245}, per-channel reg 4 (ch 1–5), reg 5 (ch 1–8),
     * reg 3 and reg 6 (ch 1–4).
     */
    public static int[][] buildReadAllProperties(int classId, int moduleId) {
        int[] globalRegs = {1, 244, 246, 245};
        int totalFrames = globalRegs.length + 5 + 8 + 4 + 4;
        int[][] frames = new int[totalFrames][];
        int idx = 0;
        for (int reg : globalRegs) frames[idx++] = CdBConfigCommands.buildRead(classId, moduleId, 0, reg);
        for (int ch = 1; ch <= 5; ch++) frames[idx++] = CdBConfigCommands.buildRead(classId, moduleId, ch, 4);
        for (int ch = 1; ch <= 8; ch++) frames[idx++] = CdBConfigCommands.buildRead(classId, moduleId, ch, 5);
        for (int ch = 1; ch <= 4; ch++) frames[idx++] = CdBConfigCommands.buildRead(classId, moduleId, ch, 3);
        for (int ch = 1; ch <= 4; ch++) frames[idx++] = CdBConfigCommands.buildRead(classId, moduleId, ch, 6);
        return frames;
    }

    /**
     * Magnet query plan. Optionally includes extended timing slots of newer generation hardware.
     * Addresses use reg=6 and automation feedback uses reg=7.
     * @param moduleId target module
     * @param extended include timing slots 5-8
     * @return ordered read frames
     */
    public static List<int[]> buildMagnetReads(int moduleId, boolean extended) {
        List<int[]> frames = new ArrayList<>();
        frames.add(CdBConfigCommands.buildRead(0x22, moduleId, 0, 1));
        int[] registers = {7, 2, 3, 4, 5, 6, 224, 18, 16, 17, 9, 8};
        for (int reg : registers) {
            int count = reg == 224 ? 0 : reg == 7 ? 8 : reg == 6 ? 5
                    : extended && isMagnetTimingRegister(reg) ? 8 : 4;
            if (count == 0) {
                frames.add(CdBConfigCommands.buildRead(0x22, moduleId, 0, reg));
            } else {
                for (int slot = 1; slot <= count; slot++) {
                    frames.add(CdBConfigCommands.buildRead(0x22, moduleId, slot, reg));
                }
            }
        }
        return frames;
    }

    /**
     * Whether a Magnet register has direction-specific timing slots.
     *
     * The older generation doesn't have this feature.
     *
     * @param reg register
     * @return true for timeout, period and pulse width
     */
    public static boolean isMagnetTimingRegister(int reg) {
        return reg == 2 || reg == 16 || reg == 17;
    }

    /**
     * Magnet accessory test. Byte 5 deliberately reproduces the
     * low byte of the first turnout's address, not the tested channel number.
     *
     * @param firstAddress stored raw address of turnout 1
     * @param address stored raw address of the tested turnout
     * @param green true for green, false for red
     * @param active true to activate, false to deactivate
     * @param older true for the 001 tool's sender hash
     * @return reference-compatible frame
     */
    public static int[] buildMagnetCoilTest(int firstAddress, int address,
            boolean green, boolean active, boolean older) {
        return buildCoilFireTest(0x22, 0, firstAddress & 0xFF,
                address, green ? 1 : 0, active ? 1 : 0, older);
    }

    /**
     * End an address or feedback save exactly as in the Magnet reference tools.
     *
     * @param moduleId target module
     * @return DLC=2, slot=0, register=254 frame (no value payload)
     */
    public static int[] buildMagnetEndSave(int moduleId) {
        return CdBConfigCommands.buildDeselect(0x22, moduleId);
    }
}
