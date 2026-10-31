package jmri.jmrix.marklin.mcan.cdb;

/**
 * Builders for LampenChef device register operations (CdB class 0x4A, OLD protocol).
 */
public final class CdBLampenChefCommands {

    private CdBLampenChefCommands() {}

    /** Writes a per-channel dimmer value (slot=channel, reg=4, DLC=4). */
    public static int[] buildWriteChDimmwert(int classId, int moduleId, int channel, int value) {
        return CdBConfigCommands.buildWrite(classId, moduleId, channel, 4, value);
    }

    /** Writes a per-channel Zeit_1 value (slot=channel, reg=2, DLC=4). */
    public static int[] buildWriteChZeit1(int classId, int moduleId, int channel, int value) {
        return CdBConfigCommands.buildWrite(classId, moduleId, channel, 2, value);
    }

    /** Writes a per-channel Zeit_2 value (slot=channel, reg=6, DLC=4). */
    public static int[] buildWriteChZeit2(int classId, int moduleId, int channel, int value) {
        return CdBConfigCommands.buildWrite(classId, moduleId, channel, 6, value);
    }

    /** Writes a per-channel start state (slot=channel, reg=7, DLC=4). */
    public static int[] buildWriteChStartzustand(int classId, int moduleId, int channel, int value) {
        return CdBConfigCommands.buildWrite(classId, moduleId, channel, 7, value);
    }

    /** Writes a per-channel output type (slot=channel, reg=9, DLC=4). */
    public static int[] buildWriteChAusgangType(int classId, int moduleId, int channel, int value) {
        return CdBConfigCommands.buildWrite(classId, moduleId, channel, 9, value);
    }

    /** Writes a per-channel Magnetadresse / output address (slot=channel, reg=3, DLC=4). */
    public static int[] buildWriteChMagnetadresse(int classId, int moduleId, int channel, int address) {
        return CdBConfigCommands.buildWrite(classId, moduleId, channel, 3, address);
    }

    /**
     * Builds all property read frames for a LampenChef / LampenChef Jumbo module.
     * Global regs {1,244,245,246,250,252,48}, per-channel regs {2,3,4,6,7,9} for channels 1–32.
     */
    public static int[][] buildReadAllProperties(int classId, int moduleId) {
        int[] globalRegs = {1, 244, 245, 246, 250, 252, 48};
        int[] channelRegs = {2, 3, 4, 6, 7, 9};
        int channels = 32;
        int totalFrames = globalRegs.length + channelRegs.length * channels;
        int[][] frames = new int[totalFrames][];
        int idx = 0;
        for (int reg : globalRegs) frames[idx++] = CdBConfigCommands.buildRead(classId, moduleId, 0, reg);
        for (int ch = 1; ch <= channels; ch++) {
            for (int reg : channelRegs) frames[idx++] = CdBConfigCommands.buildRead(classId, moduleId, ch, reg);
        }
        return frames;
    }

    /**
     * Builds an OLD-protocol addressed discovery ping frame (DLC=0).
     * Frame layout: byte[0]={@code 0x00}; byte[1]={@code 0x40}; byte[2]=classId; byte[3]=0x80|address.
     *
     * @param classId 8-bit CdB device class
     * @param address module address (0–127)
     * @return 13-byte frame
     */
    public static int[] buildOldProtocolPing(int classId, int address) {
        int[] frame = new int[13];
        frame[0] = 0x00;
        frame[1] = 0x40;
        frame[2] = classId & 0xFF;
        frame[3] = 0x80 | (address & 0x7F);
        frame[4] = 0;
        return frame;
    }
}
