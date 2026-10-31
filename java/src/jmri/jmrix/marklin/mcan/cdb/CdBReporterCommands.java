package jmri.jmrix.marklin.mcan.cdb;

/**
 * Builders for Reporter and StromSniffer device register operations
 * (CdB classes 0x03, 0x0B, 0x0D, 0x21).
 */
public final class CdBReporterCommands {

    private CdBReporterCommands() {}

    /** Writes a per-channel switch-on time (slot=channel, reg=2, DLC=4). */
    public static int[] buildWriteChEinSchaltzeit(int classId, int moduleId, int channel, int value) {
        return CdBConfigCommands.buildWrite(classId, moduleId, channel, 2, value);
    }

    /** Writes a per-channel switch-off time (slot=channel, reg=3, DLC=4). */
    public static int[] buildWriteChAusSchaltzeit(int classId, int moduleId, int channel, int value) {
        return CdBConfigCommands.buildWrite(classId, moduleId, channel, 3, value);
    }

    /** Writes the fault RMK address (slot=0, reg=10, DLC=4). */
    public static int[] buildWriteStorungsRmk(int classId, int moduleId, int rmkAddr) {
        return CdBConfigCommands.buildWrite(classId, moduleId, 0, 10, rmkAddr);
    }

    /** Writes the fault active flag (slot=0, reg=11, DLC=4). */
    public static int[] buildWriteStorungsFlag(int classId, int moduleId, int flag) {
        return CdBConfigCommands.buildWrite(classId, moduleId, 0, 11, flag);
    }

    /** Writes the special function value (slot=0, reg=12, DLC=4). */
    public static int[] buildWriteSonderfunktion(int classId, int moduleId, int value) {
        return CdBConfigCommands.buildWrite(classId, moduleId, 0, 12, value);
    }

    /** Writes a slave address (slot=0, reg=regNum, DLC=4). */
    public static int[] buildWriteSlaveAddr(int classId, int moduleId, int regNum, int addr) {
        return CdBConfigCommands.buildWrite(classId, moduleId, 0, regNum, addr);
    }

    /** Writes the freeze RMK address (slot=0, reg=18, DLC=4). StromSniffer only. */
    public static int[] buildWriteFreezeRmkAddr(int classId, int moduleId, int addr) {
        return CdBConfigCommands.buildWrite(classId, moduleId, 0, 18, addr);
    }

    /**
     * Builds all property read frames for a Reporter module (GleisReporter/StromSniffer).
     * Global regs {250,1,244,245,246,12,5,9,10,11,16,17,18,19,21,253},
     * per-channel regs 2 and 3 (ch 1–8).
     */
    public static int[][] buildReadAllProperties(int classId, int moduleId) {
        int[] globalRegs = {250, 1, 244, 245, 246, 12, 5, 9, 10, 11, 16, 17, 18, 19, 21, 253};
        int totalFrames = globalRegs.length + 8 + 8;
        int[][] frames = new int[totalFrames][];
        int idx = 0;
        for (int reg : globalRegs) frames[idx++] = CdBConfigCommands.buildRead(classId, moduleId, 0, reg);
        for (int ch = 1; ch <= 8; ch++) frames[idx++] = CdBConfigCommands.buildRead(classId, moduleId, ch, 2);
        for (int ch = 1; ch <= 8; ch++) frames[idx++] = CdBConfigCommands.buildRead(classId, moduleId, ch, 3);
        return frames;
    }
}
