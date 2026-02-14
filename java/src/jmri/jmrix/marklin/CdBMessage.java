package jmri.jmrix.marklin;

import java.util.Objects;

/**
 * Factory utility for constructing CAN-digital-bahn (CdB) configuration frames.
 * <p>
 * Uses {@link MarklinCanCodec} to construct raw frames. Operates without requiring
 * full JMRI runtime instances and supports sending directly via {@link McanFrameSender}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de) Copyright (C) 2026
 */
public class CdBMessage {

    /** CdB Configuration command (0x11 / 17 in MCAN protocol) */
    public static final int CMD_CONFIG = 0x11;

    /**
     * Address encoding result for WeichenChef Kombi output address decoding.
     */
    public static class AddressEncoding {
        public final String format;
        public final int address;

        public AddressEncoding(String format, int address) {
            this.format = format;
            this.address = address;
        }
    }

    /**
     * Known CdB device classes (CAN class identifiers).
     * Each device class requires a separate discovery ping.
     */
    public enum CdBDeviceClass {
        GLEIS_REPORTER_JUMBO(0x03, "GleisReporter Jumbo", ProtocolGeneration.NEW),
        STROM_SNIFFER(0x0B, "StromSniffer", ProtocolGeneration.NEW),
        GLEIS_REPORTER(0x0D, "GleisReporter", ProtocolGeneration.NEW),
        REPORTER_CHEF_002(0x21, "ReporterChef", ProtocolGeneration.NEW),
        WEICHEN_CHEF_MAGNET(0x22, "WeichenChef Magnet M / deLuxe", ProtocolGeneration.NEW),
        WEICHEN_CHEF_AC(0x23, "WeichenChef AC", ProtocolGeneration.NEW),
        WEICHEN_CHEF_BASIC(0x26, "WeichenChef Basic", ProtocolGeneration.NEW),
        WEICHEN_CHEF_KOMBI(0x27, "WeichenChef Kombi", ProtocolGeneration.NEW),
        WEICHEN_CHEF_MOTOR(0x2A, "WeichenChef Motor", ProtocolGeneration.OLD),
        LAMPEN_CHEF(0x4A, "LampenChef", ProtocolGeneration.OLD);

        /**
         * Wire protocol generation for this device class.
         * OLD devices use hardcoded byte patterns for discovery (iterate 128 addresses).
         * NEW devices use a single zero-DLC class-specific ping.
         */
        public enum ProtocolGeneration {
            OLD,
            NEW
        }

        private final int classId;
        private final String displayName;
        private final ProtocolGeneration protocolGeneration;

        CdBDeviceClass(int classId, String displayName, ProtocolGeneration protocolGeneration) {
            this.classId = classId;
            this.displayName = displayName;
            this.protocolGeneration = protocolGeneration;
        }

        public int getClassId() {
            return classId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public ProtocolGeneration getProtocolGeneration() {
            return protocolGeneration;
        }

        /**
         * Return the {@code CdBDeviceClass} for the given class ID or {@code null} if unknown.
         * @param classId numeric CdB class ID from the CAN frame
         * @return matching enum constant or null
         */
        public static CdBDeviceClass forClassId(int classId) {
            for (CdBDeviceClass c : values()) {
                if (c.classId == classId) return c;
            }
            return null;
        }

        /**
         * Calculate the CdB ping identifier for this device class.
         * Formula: ping_id = ((C >> 4) << 21) + ((C & 0x0F) << 12) + 0x0A80
         *
         * @return 32-bit CAN identifier for zero-DLC ping
         */
        public int getPingId() {
            return ((classId >> 4) << 21) + ((classId & 0x0F) << 12) + 0x0A80;
        }
    }

    private CdBMessage() {
    }

    /**
     * Create an old-protocol addressed discovery ping for a specific address slot.
     * Used only for OLD-generation device classes (LampenChef, WeichenChef Motor).
     * Frame: {0x01, 0x40, class, 0x80+addr, 0x00, 0,0,0,0,0,0,0,0}
     *
     * @param canClass device class byte
     * @param address  address slot 0–127
     * @return 13 element raw CAN frame
     */
    public static int[] createOldProtocolPing(int canClass, int address) {
        int[] frame = new int[13];
        frame[0] = 0x01;
        frame[1] = 0x40;
        frame[2] = canClass & 0xFF;
        frame[3] = 0x80 | (address & 0x7F);
        frame[4] = 0x00;
        // bytes 5-12 remain zero
        return frame;
    }

    /**
     * Create a CdB PING query frame (class 0x00, module 0x00).
     *
     * @param hash 16-bit sender hash
     * @return 13 element raw CAN frame
     */
    public static int[] createPing(int hash) {
        return createConfigFrame(0x00, 0x00, 0x00, new int[0], hash);
    }

    /**
     * Create a CdB class-specific discovery ping (zero-DLC).
     *
     * @param deviceClass target CdB device class
     * @return 13 element raw CAN frame with zero DLC
     */
    public static int[] createClassPing(CdBDeviceClass deviceClass) {
        int pingId = deviceClass.getPingId();
        int[] rawFrame = new int[13];
        // CAN identifier (big-endian, 4 bytes)
        rawFrame[0] = (pingId >> 24) & 0xFF;
        rawFrame[1] = (pingId >> 16) & 0xFF;
        rawFrame[2] = (pingId >> 8) & 0xFF;
        rawFrame[3] = pingId & 0xFF;
        // DLC = 0
        rawFrame[4] = 0;
        // Data bytes 5-12 remain zero
        return rawFrame;
    }

    /**
     * Create a CdB parameter read frame.
     *
     * @param canClass CdB target CAN class
     * @param moduleId CdB target module ID
     * @param paramIndex parameter index to read
     * @param hash 16-bit sender hash
     * @return 13 element raw CAN frame
     */
    public static int[] createReadParam(int canClass, int moduleId, int paramIndex, int hash) {
        return createConfigFrame(canClass, moduleId, 0x01, new int[]{paramIndex & 0xFF}, hash);
    }

    /**
     * Create a CdB parameter write frame.
     *
     * @param canClass CdB target CAN class
     * @param moduleId CdB target module ID
     * @param paramIndex parameter index
     * @param paramValue parameter value byte
     * @param hash 16-bit sender hash
     * @return 13 element raw CAN frame
     */
    public static int[] createWriteParam(int canClass, int moduleId, int paramIndex, int paramValue, int hash) {
        return createConfigFrame(canClass, moduleId, 0x02, new int[]{paramIndex & 0xFF, paramValue & 0xFF}, hash);
    }

    /**
     * Create a CdB module reset frame.
     *
     * @param canClass CdB target CAN class
     * @param moduleId CdB target module ID
     * @param hash 16-bit sender hash
     * @return 13 element raw CAN frame
     */
    public static int[] createReset(int canClass, int moduleId, int hash) {
        return createConfigFrame(canClass, moduleId, 0x03, new int[0], hash);
    }

    /**
     * Create a general CdB configuration frame.
     *
     * @param canClass target CAN class
     * @param moduleId target module ID
     * @param cdbCommand CdB command code
     * @param payload extra payload bytes
     * @param hash 16-bit sender hash
     * @return 13 element raw CAN frame
     */
    public static int[] createConfigFrame(int canClass, int moduleId, int cdbCommand, int[] payload, int hash) {
        Objects.requireNonNull(payload, "payload cannot be null");
        int dlc = Math.min(3 + payload.length, 4); // Max DLC is 4 in 13-byte frame format
        int[] data = new int[dlc];
        data[0] = canClass & 0xFF;
        if (dlc > 1) data[1] = moduleId & 0xFF;
        if (dlc > 2) data[2] = cdbCommand & 0xFF;
        for (int i = 0; i < payload.length && (3 + i) < dlc; i++) {
            data[3 + i] = payload[i] & 0xFF;
        }

        int hash1 = (hash >> 8) & 0xFF;
        int hash2 = hash & 0xFF;

        return MarklinCanCodec.builder()
                .setPriority(MarklinConstants.PRIO_1)
                .setCommand(CMD_CONFIG)
                .setResponse(false)
                .setHash(hash1, hash2)
                .setData(data)
                .build();
    }

    /**
     * Send a CdB frame via a {@link McanFrameSender}.
     *
     * @param sender transport frame sender
     * @param frame raw frame to send
     */
    public static void send(McanFrameSender sender, int[] frame) {
        Objects.requireNonNull(sender, "sender cannot be null");
        Objects.requireNonNull(frame, "frame cannot be null");
        sender.sendFrame(frame);
    }

    // Raw CdB frame builders (bytes 0-3 = CAN ID, byte 4 = DLC, bytes 5+ = data)

    /**
     * Build a raw 13-byte CdB write frame for a specific module.
     * write_id = ping_id + 0x60000; module encoded in byte 3 as (moduleId + 0x80).
     */
    private static int[] buildWriteFrame(int canClass, int moduleId, int dlc, int[] data) {
        int pingId = ((canClass >> 4) << 21) | ((canClass & 0x0F) << 12) | 0x0A80;
        int writeId = pingId + 0x60000;
        // Set module in byte 3: (moduleId + 0x80)
        int canId = (writeId & 0xFFFFFF00) | ((moduleId + 0x80) & 0xFF);
        int[] frame = new int[13];
        frame[0] = (canId >> 24) & 0xFF;
        frame[1] = (canId >> 16) & 0xFF;
        frame[2] = (canId >> 8) & 0xFF;
        frame[3] = canId & 0xFF;
        frame[4] = dlc & 0xFF;
        for (int i = 0; i < data.length && i < 8; i++) {
            frame[5 + i] = data[i] & 0xFF;
        }
        return frame;
    }

    /**
     * Build a raw 13-byte CdB read frame for a specific module.
     * read_id = ping_id + 0x40000.
     */
    private static int[] buildReadFrame(int canClass, int moduleId, int dlc, int[] data) {
        int pingId = ((canClass >> 4) << 21) | ((canClass & 0x0F) << 12) | 0x0A80;
        int readId = pingId + 0x40000;
        int canId = (readId & 0xFFFFFF00) | ((moduleId + 0x80) & 0xFF);
        int[] frame = new int[13];
        frame[0] = (canId >> 24) & 0xFF;
        frame[1] = (canId >> 16) & 0xFF;
        frame[2] = (canId >> 8) & 0xFF;
        frame[3] = canId & 0xFF;
        frame[4] = dlc & 0xFF;
        for (int i = 0; i < data.length && i < 8; i++) {
            frame[5 + i] = data[i] & 0xFF;
        }
        return frame;
    }

    /**
     * Deselect (disconnect) a module: write frame with subcmd=0, reg=0xFE, DLC=2.
     *
     * @param canClass CdB device class
     * @param moduleId raw module ID (1..127)
     * @return 13 element raw CAN frame
     */
    public static int[] createDeselectModule(int canClass, int moduleId) {
        return buildWriteFrame(canClass, moduleId, 2, new int[]{0x00, 0xFE});
    }

    /**
     * Factory reset a selected module: write frame with subcmd=0, reg=0xFF, DLC=2.
     * Resets all module parameters to factory defaults. Module remains selected;
     * call startPropertyQuery() afterwards to reload the new values.
     *
     * @param canClass CdB device class
     * @param moduleId raw module ID (1..127)
     * @return 13 element raw CAN frame
     */
    public static int[] createFactoryReset(int canClass, int moduleId) {
        return buildWriteFrame(canClass, moduleId, 2, new int[]{0x00, 0xFF});
    }

    /**
     * Select (connect to) a module: write frame with subcmd=0, reg=0xF7, data=[0x00, 0x01], DLC=4.
     *
     * @param canClass CdB device class
     * @param moduleId raw module ID (1..127)
     * @return 13 element raw CAN frame
     */
    public static int[] createSelectModule(int canClass, int moduleId) {
        return buildWriteFrame(canClass, moduleId, 4, new int[]{0x00, 0xF7, 0x00, 0x01});
    }

    /**
     * Select a factory-fresh module (ID=0) using challenge/response handshake.
     * The module's ping reply (DLC=6) carries two challenge bytes at positions 6 and 7;
     * these must be echoed back in the select frame at data bytes 4 and 5.
     *
     * @param canClass   CdB device class
     * @param challenge0 byte at index 6 of the factory ping reply
     * @param challenge1 byte at index 7 of the factory ping reply
     * @return 13 element raw CAN frame
     */
    public static int[] createFactorySelectModule(int canClass, int challenge0, int challenge1) {
        return buildWriteFrame(canClass, 0, 6,
                new int[]{0x00, 0xF7, 0x00, 0x01, challenge0 & 0xFF, challenge1 & 0xFF});
    }

    /**
     * Write a per-channel switching time (reg 3).
     *
     * @param canClass CdB device class
     * @param moduleId raw module ID (1..127)
     * @param channel channel number (1-8)
     * @param timeMs switching time in milliseconds
     * @return 13 element raw CAN frame
     */
    public static int[] createWriteChSwitchingTime(int canClass, int moduleId, int channel, int timeMs) {
        return buildWriteFrame(canClass, moduleId, 4, new int[]{
            channel & 0xFF, 3, (timeMs >> 8) & 0xFF, timeMs & 0xFF
        });
    }

    /**
     * Write a per-channel output address (reg 4).
     * Address bytes are little-endian (lo then hi) per standalone tool protocol.
     *
     * @param canClass CdB device class
     * @param moduleId raw module ID (1..127)
     * @param channel channel number (1-5)
     * @param rawAddr encoded raw address value
     * @return 13 element raw CAN frame
     */
    public static int[] createWriteChAddress(int canClass, int moduleId, int channel, int rawAddr) {
        return buildWriteFrame(canClass, moduleId, 4, new int[]{
            channel & 0xFF, 4, rawAddr & 0xFF, (rawAddr >> 8) & 0xFF
        });
    }

    /**
     * Write a per-channel RMK (feedback) address (reg 5).
     * Address bytes are little-endian per standalone tool protocol.
     *
     * @param canClass CdB device class
     * @param moduleId raw module ID (1..127)
     * @param channel channel number (1-8)
     * @param rmkAddr feedback address value
     * @return 13 element raw CAN frame
     */
    public static int[] createWriteChRmk(int canClass, int moduleId, int channel, int rmkAddr) {
        return buildWriteFrame(canClass, moduleId, 4, new int[]{
            channel & 0xFF, 5, rmkAddr & 0xFF, (rmkAddr >> 8) & 0xFF
        });
    }

    /**
     * Write a per-channel CdB flag (reg 6).
     *
     * @param canClass CdB device class
     * @param moduleId raw module ID (1..127)
     * @param channel channel number (1-4)
     * @param flagValue flag byte value (0=off, 1 or 2=on per module type)
     * @return 13 element raw CAN frame
     */
    public static int[] createWriteChCdBFlag(int canClass, int moduleId, int channel, int flagValue) {
        return buildWriteFrame(canClass, moduleId, 4, new int[]{
            channel & 0xFF, 6, 0x00, flagValue & 0xFF
        });
    }

    /**
     * Write a new module ID (factory ID assignment, reg 2, subcmd 0).
     * The challenge bytes from the factory ping reply must be echoed in bytes 9-10,
     * matching the reference tool's implicit carry-over through the shared Data buffer.
     *
     * @param canClass   CdB device class
     * @param moduleId   current raw module ID (0 for unassigned module)
     * @param newId      new module ID to assign
     * @param challenge0 byte at index 6 of the factory ping reply
     * @param challenge1 byte at index 7 of the factory ping reply
     * @return 13 element raw CAN frame
     */
    public static int[] createWriteNewModuleId(int canClass, int moduleId, int newId,
            int challenge0, int challenge1) {
        return buildWriteFrame(canClass, moduleId, 6, new int[]{
            0x00, 2, (newId >> 8) & 0xFF, newId & 0xFF, challenge0 & 0xFF, challenge1 & 0xFF
        });
    }

    /**
     * Write one 6-character packet of the module name (reg 250).
     * data[0] = packet index, data[1] = 250 (reg), data[2..7] = 6 ASCII bytes (0-padded).
     *
     * @param canClass CdB device class
     * @param moduleId raw module ID (1..127)
     * @param packetIndex packet index (0-based)
     * @param chars up to 6 ASCII character values (shorter arrays are 0-padded)
     * @return 13 element raw CAN frame
     */
    public static int[] createWriteModulnamePacket(int canClass, int moduleId, int packetIndex, int[] chars) {
        int[] data = new int[8];
        data[0] = packetIndex & 0xFF;
        data[1] = 250;
        for (int i = 0; i < 6; i++) {
            data[2 + i] = (i < chars.length) ? (chars[i] & 0xFF) : 0;
        }
        return buildWriteFrame(canClass, moduleId, 8, data);
    }

    /**
     * Create a read request frame for a single register (DLC=2: subcmd + reg).
     *
     * @param canClass CdB device class
     * @param moduleId raw module ID (1..127)
     * @param subcmd sub-command byte (0 for global regs, 1-8 for per-channel)
     * @param reg register number
     * @return 13 element raw CAN frame
     */
    public static int[] createReadReg(int canClass, int moduleId, int subcmd, int reg) {
        return buildReadFrame(canClass, moduleId, 2, new int[]{subcmd & 0xFF, reg & 0xFF});
    }

    /**
     * Create a write request frame for a single register (DLC=4: subcmd + reg + value hi + value lo).
     *
     * @param canClass CdB device class
     * @param moduleId raw module ID (1..127)
     * @param subcmd sub-command byte (0 for global regs, 1-8 for per-channel)
     * @param reg register number
     * @param value 16-bit value to write
     * @return 13 element raw CAN frame
     */
    public static int[] createWriteReg(int canClass, int moduleId, int subcmd, int reg, int value) {
        return buildWriteFrame(canClass, moduleId, 4,
                new int[]{subcmd & 0xFF, reg & 0xFF, (value >> 8) & 0xFF, value & 0xFF});
    }

    /**
     * Create all property read frames for a WeichenChef Kombi module.
     * Reads regs {1,244,246,245}, per-channel reg 4 (ch 1-5), reg 5 (ch 1-8),
     * reg 3 and reg 6 (ch 1-4).
     *
     * @param canClass CdB device class (typically 0x27)
     * @param moduleId raw module ID (1..127)
     * @return array of 13 element raw CAN frames
     */
    public static int[][] createReadAllPropertiesWeichenChef(int canClass, int moduleId) {
        int[] globalRegs = {1, 244, 246, 245};
        int totalFrames = globalRegs.length + 5 + 8 + 4 + 4; // global + ch4(1-5) + ch5(1-8) + ch3(1-4) + ch6(1-4)
        int[][] frames = new int[totalFrames][];
        int idx = 0;
        for (int reg : globalRegs) {
            frames[idx++] = createReadReg(canClass, moduleId, 0, reg);
        }
        for (int ch = 1; ch <= 5; ch++) {
            frames[idx++] = createReadReg(canClass, moduleId, ch, 4);
        }
        for (int ch = 1; ch <= 8; ch++) {
            frames[idx++] = createReadReg(canClass, moduleId, ch, 5);
        }
        for (int ch = 1; ch <= 4; ch++) {
            frames[idx++] = createReadReg(canClass, moduleId, ch, 3);
        }
        for (int ch = 1; ch <= 4; ch++) {
            frames[idx++] = createReadReg(canClass, moduleId, ch, 6);
        }
        return frames;
    }

    /**
     * Create all property read frames for a Reporter module (GleisReporter/StromSniffer).n
     * Reads regs {250,1,244,245,246,12,5,9,10,11,16,17,18,19,21,253},
     * per-channel regs 2 and 3 (ch 1-8).
     *
     * @param canClass CdB device class (0x0D for GleisReporter, 0x0B for StromSniffer)
     * @param moduleId raw module ID (1..127)
     * @return array of 13 element raw CAN frames
     */
    public static int[][] createReadAllPropertiesReporter(int canClass, int moduleId) {
        int[] globalRegs = {250, 1, 244, 245, 246, 12, 5, 9, 10, 11, 16, 17, 18, 19, 21, 253};
        int totalFrames = globalRegs.length + 8 + 8; // global + ch2(1-8) + ch3(1-8)
        int[][] frames = new int[totalFrames][];
        int idx = 0;
        for (int reg : globalRegs) {
            frames[idx++] = createReadReg(canClass, moduleId, 0, reg);
        }
        for (int ch = 1; ch <= 8; ch++) {
            frames[idx++] = createReadReg(canClass, moduleId, ch, 2);
        }
        for (int ch = 1; ch <= 8; ch++) {
            frames[idx++] = createReadReg(canClass, moduleId, ch, 3);
        }
        return frames;
    }

    /**
     * Create all property read frames for a LampenChef / LampenChef Jumbo module.
     * Global regs: {1, 244, 245, 246, 250, 252, 48}.
     * Per-channel regs {2, 3, 4, 6, 7, 9} for all 32 channels (slots 1–32).
     *
     * @param canClass CdB device class (typically 0x4A)
     * @param moduleId raw module ID (1..127)
     * @return array of 13 element raw CAN frames
     */
    public static int[][] createReadAllPropertiesLampenChef(int canClass, int moduleId) {
        int[] globalRegs = {1, 244, 245, 246, 250, 252, 48};
        int[] channelRegs = {2, 3, 4, 6, 7, 9};
        int channels = 32;
        int totalFrames = globalRegs.length + channelRegs.length * channels;
        int[][] frames = new int[totalFrames][];
        int idx = 0;
        for (int reg : globalRegs) {
            frames[idx++] = createReadReg(canClass, moduleId, 0, reg);
        }
        for (int ch = 1; ch <= channels; ch++) {
            for (int reg : channelRegs) {
                frames[idx++] = createReadReg(canClass, moduleId, ch, reg);
            }
        }
        return frames;
    }

    /**
     * Write a per-channel dimmer value (reg 4) for LampenChef.
     *
     * @param canClass CdB device class
     * @param moduleId raw module ID (1..127)
     * @param channel  channel number (1–32)
     * @param value    dimmer value (0–255)
     * @return 13 element raw CAN frame
     */
    public static int[] createWriteChDimmwert(int canClass, int moduleId, int channel, int value) {
        return buildWriteFrame(canClass, moduleId, 4, new int[]{
            channel & 0xFF, 4, (value >> 8) & 0xFF, value & 0xFF
        });
    }

    /**
     * Write a per-channel Zeit_1 value (reg 2) for LampenChef.
     *
     * @param canClass CdB device class
     * @param moduleId raw module ID (1..127)
     * @param channel  channel number (1–32)
     * @param value    time value
     * @return 13 element raw CAN frame
     */
    public static int[] createWriteChZeit1(int canClass, int moduleId, int channel, int value) {
        return buildWriteFrame(canClass, moduleId, 4, new int[]{
            channel & 0xFF, 2, (value >> 8) & 0xFF, value & 0xFF
        });
    }

    /**
     * Write a per-channel Zeit_2 value (reg 6) for LampenChef.
     *
     * @param canClass CdB device class
     * @param moduleId raw module ID (1..127)
     * @param channel  channel number (1–32)
     * @param value    time value
     * @return 13 element raw CAN frame
     */
    public static int[] createWriteChZeit2(int canClass, int moduleId, int channel, int value) {
        return buildWriteFrame(canClass, moduleId, 4, new int[]{
            channel & 0xFF, 6, (value >> 8) & 0xFF, value & 0xFF
        });
    }

    /**
     * Write a per-channel start state (reg 7) for LampenChef.
     *
     * @param canClass CdB device class
     * @param moduleId raw module ID (1..127)
     * @param channel  channel number (1–32)
     * @param value    start state value
     * @return 13 element raw CAN frame
     */
    public static int[] createWriteChStartzustand(int canClass, int moduleId, int channel, int value) {
        return buildWriteFrame(canClass, moduleId, 4, new int[]{
            channel & 0xFF, 7, (value >> 8) & 0xFF, value & 0xFF
        });
    }

    /**
     * Write a per-channel output type (reg 9) for LampenChef.
     *
     * @param canClass CdB device class
     * @param moduleId raw module ID (1..127)
     * @param channel  channel number (1–32)
     * @param value    output type value
     * @return 13 element raw CAN frame
     */
    public static int[] createWriteChAusgangType(int canClass, int moduleId, int channel, int value) {
        return buildWriteFrame(canClass, moduleId, 4, new int[]{
            channel & 0xFF, 9, (value >> 8) & 0xFF, value & 0xFF
        });
    }

    /**
     * Write a per-channel Magnetadresse / output address (reg 3) for LampenChef.
     * Address bytes are stored hi then lo.
     *
     * @param canClass CdB device class
     * @param moduleId raw module ID (1..127)
     * @param channel  channel number (1–32)
     * @param address  output address value
     * @return 13 element raw CAN frame
     */
    public static int[] createWriteChMagnetadresse(int canClass, int moduleId, int channel, int address) {
        return buildWriteFrame(canClass, moduleId, 4, new int[]{
            channel & 0xFF, 3, (address >> 8) & 0xFF, address & 0xFF
        });
    }

    // ---- GleisReporter / StromSniffer write methods ----

    /**
     * Write a per-channel switch-on time (reg 2) for GleisReporter/StromSniffer.
     *
     * @param canClass CdB device class (0x0D GleisReporter, 0x0B StromSniffer)
     * @param moduleId raw module ID (1..127)
     * @param channel  channel number (1–8)
     * @param value    time value
     * @return 13 element raw CAN frame
     */
    public static int[] createWriteChEinSchaltzeit(int canClass, int moduleId, int channel, int value) {
        return buildWriteFrame(canClass, moduleId, 4, new int[]{
            channel & 0xFF, 2, (value >> 8) & 0xFF, value & 0xFF
        });
    }

    /**
     * Write a per-channel switch-off time (reg 3) for GleisReporter/StromSniffer.
     *
     * @param canClass CdB device class
     * @param moduleId raw module ID (1..127)
     * @param channel  channel number (1–8)
     * @param value    time value
     * @return 13 element raw CAN frame
     */
    public static int[] createWriteChAusSchaltzeit(int canClass, int moduleId, int channel, int value) {
        return buildWriteFrame(canClass, moduleId, 4, new int[]{
            channel & 0xFF, 3, (value >> 8) & 0xFF, value & 0xFF
        });
    }

    /**
     * Write the fault (Störungs) RMK address (reg 10).
     *
     * @param canClass CdB device class
     * @param moduleId raw module ID (1..127)
     * @param rmkAddr  fault feedback address
     * @return 13 element raw CAN frame
     */
    public static int[] createWriteStorungsRmk(int canClass, int moduleId, int rmkAddr) {
        return buildWriteFrame(canClass, moduleId, 4, new int[]{
            0x00, 10, (rmkAddr >> 8) & 0xFF, rmkAddr & 0xFF
        });
    }

    /**
     * Write the fault (Störungs) active flag (reg 11).
     *
     * @param canClass CdB device class
     * @param moduleId raw module ID (1..127)
     * @param flag     flag value (0=off, 1=on)
     * @return 13 element raw CAN frame
     */
    public static int[] createWriteStorungsFlag(int canClass, int moduleId, int flag) {
        return buildWriteFrame(canClass, moduleId, 4, new int[]{
            0x00, 11, 0x00, flag & 0xFF
        });
    }

    /**
     * Write the special function (Sonderfunktion) value (reg 12).
     *
     * @param canClass CdB device class
     * @param moduleId raw module ID (1..127)
     * @param value    function value
     * @return 13 element raw CAN frame
     */
    public static int[] createWriteSonderfunktion(int canClass, int moduleId, int value) {
        return buildWriteFrame(canClass, moduleId, 4, new int[]{
            0x00, 12, 0x00, value & 0xFF
        });
    }

    /**
     * Write a slave address (reg 16 or 21 depending on firmware) for GleisReporter/StromSniffer.
     *
     * @param canClass CdB device class
     * @param moduleId raw module ID (1..127)
     * @param reg      register number (16 or 21)
     * @param addr     slave address value
     * @return 13 element raw CAN frame
     */
    public static int[] createWriteSlaveAddr(int canClass, int moduleId, int reg, int addr) {
        return buildWriteFrame(canClass, moduleId, 4, new int[]{
            0x00, reg & 0xFF, (addr >> 8) & 0xFF, addr & 0xFF
        });
    }

    /**
     * Write the freeze RMK address (reg 18) for StromSniffer only.
     *
     * @param canClass CdB device class (0x0B StromSniffer)
     * @param moduleId raw module ID (1..127)
     * @param addr     freeze feedback address
     * @return 13 element raw CAN frame
     */
    public static int[] createWriteFreezeRmkAddr(int canClass, int moduleId, int addr) {
        return buildWriteFrame(canClass, moduleId, 4, new int[]{
            0x00, 18, (addr >> 8) & 0xFF, addr & 0xFF
        });
    }

    // ---- Address encoding ----

    /**
     * Encode an output address for a WeichenChef output channel configured as a solenoid drive
     * (output type 2, as stored in per-channel configuration register 245).
     * DCC -> raw = 14336 + address - 1
     * MM  -> raw = 12288 + address - 1
     * CAN -> raw = address
     *
     * This is the encoding for the CdB Config protocol.
     * Do not confuse with operational command protocol in MarklinCanCodec.getProtocolFromAddress() which uses different offsets.
     *
     * @param format "DCC", "MM" or "CAN"
     * @param address 1-based address
     * @return raw wire address value
     */
    public static int encodeAddress(String format, int address) {
        switch (format) {
            case "DCC": return 14336 + address - 1;
            case "MM":  return 12288 + address - 1;
            default:    return address; // CAN
        }
    }

    /**
     * Decode a raw wire address for a WeichenChef output channel configured as a solenoid drive
     * (output type 2, as stored in per-channel configuration register 245).
     *
     * @param rawAddr raw wire address
     * @return AddressEncoding with format and 1-based address
     */
    public static AddressEncoding decodeAddress(int rawAddr) {
        if (rawAddr >= 14336) {
            return new AddressEncoding("DCC", rawAddr - 14336 + 1);
        } else if (rawAddr >= 12288) {
            return new AddressEncoding("MM", rawAddr - 12288 + 1);
        } else {
            return new AddressEncoding("CAN", rawAddr);
        }
    }

    /**
     * Encode an output address for a WeichenChef output channel configured as a motor drive
     * with end-stop evaluation (output type 3, as stored in per-channel configuration register 245).
     * Motor-drive channels only support DCC addressing in two sub-formats:
     * DCC     -> raw = 12288 + address - 1
     * DCC ext -> raw = 12800 + address + 3
     * <p>
     * For solenoid-drive channels (type 2), use {@link #encodeAddress(String, int)} instead.
     *
     * @param format "DCC" or "DCC ext."
     * @param address 1-based address
     * @return raw wire address value
     */
    public static int encodeAddressType3(String format, int address) {
        if ("DCC ext.".equals(format)) {
            return 12800 + address + 3;
        }
        return 12288 + address - 1; // DCC
    }

    /**
     * Decode a raw wire address for a WeichenChef output channel configured as a motor drive
     * with end-stop evaluation (output type 3, as stored in per-channel configuration register 245).
     * <p>
     * For solenoid-drive channels (type 2), use {@link #decodeAddress(int)} instead.
     *
     * @param rawAddr raw wire address
     * @return AddressEncoding with format and 1-based address
     */
    public static AddressEncoding decodeAddressType3(int rawAddr) {
        if (rawAddr >= 12800) {
            return new AddressEncoding("DCC ext.", rawAddr - 12800 - 3);
        }
        return new AddressEncoding("DCC", rawAddr - 12288 + 1);
    }
}
