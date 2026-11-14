package jmri.jmrix.marklin;

import jmri.jmrix.marklin.mcan.cs2.Cs2AccessoryCommands;
import jmri.jmrix.marklin.mcan.cs2.Cs2ConfigCommands;
import jmri.jmrix.marklin.mcan.cs2.Cs2DecoderCommands;
import jmri.jmrix.marklin.mcan.cs2.Cs2FeedbackCommands;
import jmri.jmrix.marklin.mcan.cs2.Cs2SystemCommands;

/**
 * Encodes a message to a Marklin command station.
 * <p>
 * The {@link MarklinReply} class handles the response from the command station.
 * Packages of length 13 are interpreted as can-bus packages:
 * 4 bytes Can-bus-ID (BigEndian or network order),
 * 1-byte length and
 * 8 bytes of data, if necessary with null bytes to fill in.
 * <p>
 * The message ID is divided into the areas of lower priority (priority),
 * command (command), response and hash.
 * The communication is based on the following format:
 * Prio - 2 +2bit
 * Command 8 bit
 * Resp - 1 bit
 * Hash - 16bit
 * DLC - 4bit (ie CAN message length)
 * CAN message 8 BYTES
 * Can Message Bytes 0 to 3 are the address bytes, with byte 0 High, byte 3 low
 * <p>
 * This class now leverages the mcan sub-package for message encoding.
 * @author Kevin Dickerson Copyright (C) 2001, 2008
 */
public class MarklinMessage extends jmri.jmrix.AbstractMRMessage {

    private final McanFrameCache frameCache = new McanFrameCache();

    /**
     * Classified view of this message. The result is cached and reused until the
     * message's bytes change.
     *
     * @return a {@link jmri.jmrix.marklin.mcan.cs2.Cs2Frame},
     *         {@link jmri.jmrix.marklin.mcan.cdb.CdBFrameParser.CdBFrame} or
     *         {@link jmri.jmrix.marklin.mcan.UnknownFrame}
     */
    public jmri.jmrix.marklin.mcan.McanFrame getFrame() {
        int[] bytes = new int[13];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = getElement(i);
        }
        return frameCache.get(bytes);
    }

    static int MY_UID = 0x12345678;

    private boolean replyExpected = true;

    public void setReplyExpected(boolean expected) {
        replyExpected = expected;
    }

    @Override
    public boolean replyExpected() {
        return replyExpected;
    }

    MarklinMessage() {
        _dataChars = new int[13];
        _nDataChars = 13;
        setBinary(true);
        for (int i = 0; i < 13; i++) {
            _dataChars[i] = 0x00;
        }
    }

    // create a new one from an array
    public MarklinMessage(int[] d) {
        this();
        System.arraycopy(d, 0, _dataChars, 0, d.length);
    }

    // create a new one from a byte array, as a service
    public MarklinMessage(byte[] d) {
        this();
        for (int i = 0; i < d.length; i++) {
            _dataChars[i] = d[i] & 0xFF;
        }
    }

    // create a new one
    public MarklinMessage(int i) {
        this();
    }

    // copy one
    public MarklinMessage(MarklinMessage m) {
        super(m);
    }

    // static methods to return a formatted message
    public static MarklinMessage getEnableMain() {
        return new MarklinMessage(Cs2SystemCommands.buildSystemGo());
    }

    public static MarklinMessage getKillMain() {
        return new MarklinMessage(Cs2SystemCommands.buildSystemStop());
    }

    /**
     * Generate CAN BOOT command (0x1B) for Gleisbox activation.
     * <p>
     * This command resets the Gleisbox/trackbox and initiates it to start
     * passing commands to locos and accessories on the rails. Without this
     * command on startup, the hardware does not respond to subsequent commands.
     * This variant is used for normal operational startup of standalone
     * Gleisbox devices when no CS2/MS2 is attached.
     * <p>
     * The packet uses DLC=5 with data byte 0 set to 0x11, which is the
     * "magic value" that activates the Gleisbox for normal operations.
     * This matches the behavior of Rocrail and the can2udp reference
     * implementation (M_GLEISBOX_MAGIC_START_SEQUENCE).
     *
     * @return MarklinMessage containing the CAN BOOT activation command
     * @see <a href="https://github.com/GBert/railroad/can2udp">can2udp reference implementation</a>
     * @see #getCanBootloaderMode()
     */
    public static MarklinMessage getCanBoot() {
        return new MarklinMessage(Cs2ConfigCommands.buildCanBoot());
    }

    /**
     * Generate CAN BOOT command (0x1B) for entering bootloader mode.
     * <p>
     * This variant of the CAN BOOT command invokes the bootloader update
     * sequence on Märklin devices for firmware updates. It is sent after
     * a system reset with approximately 400ms wait time, putting the device
     * into bootloader mode ready to receive firmware data.
     * <p>
     * This command uses DLC=0 (no data bytes) and should be followed by
     * firmware data transfer packets if performing an actual firmware update.
     * This is different from {@link #getCanBoot()} which activates the device
     * for normal operations.
     * <p>
     * <strong>Note:</strong> This command is intended for firmware update
     * operations. For normal Gleisbox activation to run trains, use
     * {@link #getCanBoot()} instead.
     *
     * @return MarklinMessage containing the CAN BOOT bootloader invocation command
     * @see <a href="https://www.stummiforum.de/t122854f7-M-rklin-CAN-Protokoll-x-B-commands-updates.html">Märklin CAN Protokoll 0x1B commands documentation</a>
     * @see #getCanBoot()
     */
    public static MarklinMessage getCanBootloaderMode() {
        return new MarklinMessage(Cs2ConfigCommands.buildBootloaderMode());
    }

    public static MarklinMessage getSetTurnout(int addr, int state, int power) {
        return new MarklinMessage(
                Cs2AccessoryCommands.buildSetAccessory(addr & 0xFFFFFFFFL, state, power != 0));
    }

    public static MarklinMessage getQryLocoSpeed(int addr) {
        return new MarklinMessage(Cs2DecoderCommands.buildQueryLocoSpeed(addr & 0xFFFFFFFFL));
    }

    public static MarklinMessage setLocoSpeed(int addr, int speed) {
        return new MarklinMessage(Cs2DecoderCommands.buildLocoSpeed(addr & 0xFFFFFFFFL, speed));
    }

    public static MarklinMessage setLocoEmergencyStop(int addr) {
        return new MarklinMessage(Cs2DecoderCommands.buildLocoEmergencyStop(addr & 0xFFFFFFFFL));
    }

    public static MarklinMessage setLocoSpeedSteps(int addr, int step) {
        return new MarklinMessage(
                Cs2SystemCommands.buildLocoDataProtocol(addr & 0xFFFFFFFFL, step));
    }

    public static MarklinMessage getQryLocoDirection(int addr) {
        return new MarklinMessage(Cs2DecoderCommands.buildQueryLocoDirection(addr & 0xFFFFFFFFL));
    }

    public static MarklinMessage setLocoDirection(int addr, int dir) {
        return new MarklinMessage(Cs2DecoderCommands.buildLocoDirection(addr & 0xFFFFFFFFL, dir));
    }

    public static MarklinMessage getQryLocoFunction(int addr, int funct) {
        return new MarklinMessage(
                Cs2DecoderCommands.buildLocoFunctionQuery(addr & 0xFFFFFFFFL, funct));
    }

    public static MarklinMessage setLocoFunction(int addr, int funct, int state) {
        return new MarklinMessage(
                Cs2DecoderCommands.buildLocoFunction(addr & 0xFFFFFFFFL, funct, state != 0));
    }

    public static MarklinMessage sensorPollMessage(int module) {
        return new MarklinMessage(Cs2FeedbackCommands.buildS88Poll(module));
    }

    public long getAddress() {
        long addr = getElement(MarklinConstants.CANADDRESSBYTE1);
        addr = (addr << 8) + getElement(MarklinConstants.CANADDRESSBYTE2);
        addr = (addr << 8) + getElement(MarklinConstants.CANADDRESSBYTE3);
        addr = (addr << 8) + getElement(MarklinConstants.CANADDRESSBYTE4);

        return addr;
    }

    public static MarklinMessage getProgMode() {
        return null;
    }

    public static MarklinMessage getExitProgMode() {
        return null;
    }

    public static MarklinMessage getReadPagedCV(int cv) { //Rxxx
        return new MarklinMessage();
    }

    public static MarklinMessage getWritePagedCV(int cv, int val) { //Pxxx xxx
        return new MarklinMessage();
    }

    public static MarklinMessage getReadRegister(int reg) { //Vx
        return new MarklinMessage();
    }

    public static MarklinMessage getWriteRegister(int reg, int val) { //Sx xxx
        return new MarklinMessage();
    }

    public static MarklinMessage getReadDirectCV(int cv) { //Rxxx
        return new MarklinMessage();
    }

    public static MarklinMessage getWriteDirectCV(int cv, int val) { //Pxxx xxx
        return new MarklinMessage();
    }
}
