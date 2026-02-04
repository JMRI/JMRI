package jmri.jmrix.marklin;

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
 * @author Kevin Dickerson Copyright (C) 2001, 2008
 */
public class MarklinMessage extends jmri.jmrix.AbstractMRMessage {

    static int MY_UID = 0x12345678;

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
        MarklinMessage m = new MarklinMessage();
        m.setElement(0, MarklinConstants.SYSCOMMANDSTART & 0xFF);
        m.setElement(1, 0x00 & 0xFF);
        m.setElement(2, MarklinConstants.HASHBYTE1 & 0xFF);
        m.setElement(3, MarklinConstants.HASHBYTE2 & 0xFF);
        m.setElement(4, 0x05 & 0xFF); //five bytes;
        //5, 6, 7, 8 Address but this is a global command
        m.setElement(9, MarklinConstants.CMDGOSYS & 0xFF); //Turn main on 0x01
        return m;
    }

    public static MarklinMessage getKillMain() {
        MarklinMessage m = new MarklinMessage();
        m.setElement(0, MarklinConstants.SYSCOMMANDSTART & 0xFF);
        m.setElement(1, 0x00 & 0xFF);
        m.setElement(2, MarklinConstants.HASHBYTE1 & 0xFF);
        m.setElement(3, MarklinConstants.HASHBYTE2 & 0xFF);
        m.setElement(4, 0x05 & 0xFF); //five bytes;
        //5, 6, 7, 8 Address but this is a global command
        m.setElement(9, MarklinConstants.CMDSTOPSYS & 0xFF); //Turn main off 0x00
        return m;
    }

    /**
     * Generate CAN BOOT command (0xB1).
     * <p>
     * This command is used to invoke the bootloader update sequence
     * for Märklin devices. According to German language forum research,
     * this is part of the software/bootloader command range used for
     * firmware updates and device initialization.
     * 
     * @return MarklinMessage containing the CAN BOOT command
     * @see <a href="https://www.stummiforum.de/t122854f7-M-rklin-CAN-Protokoll-x-B-commands-updates.html">Märklin CAN Protokoll 0x1B commands documentation</a>
     */
    public static MarklinMessage getCanBoot() {
        MarklinMessage m = new MarklinMessage();
        m.setElement(0, (0xB1 >> 7) & 0xFF);  // Command 0xB1 high bits
        m.setElement(1, (0xB1 << 1) & 0xFF);  // Command 0xB1 low bits
        m.setElement(2, MarklinConstants.HASHBYTE1 & 0xFF);
        m.setElement(3, MarklinConstants.HASHBYTE2 & 0xFF);
        m.setElement(4, 0x00 & 0xFF); // DLC = 0 for basic boot command
        // Elements 5-12 are left as default 0x00 for this command
        return m;
    }

    //static public MarklinMessage get
    public static MarklinMessage getSetTurnout(int addr, int state, int power) {
        MarklinMessage m = new MarklinMessage();
        m.setElement(0, (MarklinConstants.ACCCOMMANDSTART >> 7) & 0xFF);
        m.setElement(1, (MarklinConstants.ACCCOMMANDSTART << 1) & 0xFF);
        m.setElement(2, MarklinConstants.HASHBYTE1 & 0xFF);
        m.setElement(3, MarklinConstants.HASHBYTE2 & 0xFF);
        m.setElement(4, 0x06 & 0xFF); //five bytes;
        m.setElement(MarklinConstants.CANADDRESSBYTE1, (addr >> 24) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE2, (addr >> 16) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE3, (addr >> 8) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE4, (addr) & 0xFF);
        m.setElement(9, state & 0xff);
        m.setElement(10, power & 0xff);
        return m;
    }

    public static MarklinMessage getQryLocoSpeed(int addr) {
        MarklinMessage m = new MarklinMessage();
        m.setElement(0, (MarklinConstants.LOCOSPEED >> 7) & 0xFF);
        m.setElement(1, (MarklinConstants.LOCOSPEED << 1) & 0xFF);
        m.setElement(2, MarklinConstants.HASHBYTE1 & 0xFF);
        m.setElement(3, MarklinConstants.HASHBYTE2 & 0xFF);
        m.setElement(4, 0x04 & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE1, (addr >> 24) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE2, (addr >> 16) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE3, (addr >> 8) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE4, (addr) & 0xFF);
        return m;
    }

    public static MarklinMessage setLocoSpeed(int addr, int speed) {
        MarklinMessage m = new MarklinMessage();
        m.setElement(0, (MarklinConstants.LOCOSPEED >> 7) & 0xFF);
        m.setElement(1, (MarklinConstants.LOCOSPEED << 1) & 0xFF);
        m.setElement(2, MarklinConstants.HASHBYTE1 & 0xFF);
        m.setElement(3, MarklinConstants.HASHBYTE2 & 0xFF);
        m.setElement(4, 0x06 & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE1, (addr >> 24) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE2, (addr >> 16) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE3, (addr >> 8) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE4, (addr) & 0xFF);
        m.setElement(9, (speed >> 8) & 0xff);
        m.setElement(10, speed & 0xff);
        return m;
    }

    public static MarklinMessage setLocoEmergencyStop(int addr) {
        MarklinMessage m = new MarklinMessage();
        m.setElement(0, MarklinConstants.SYSCOMMANDSTART & 0xFF);
        m.setElement(1, 0x00 & 0xFF);
        m.setElement(2, MarklinConstants.HASHBYTE1 & 0xFF);
        m.setElement(3, MarklinConstants.HASHBYTE2 & 0xFF);
        m.setElement(4, 0x05 & 0xFF); //five bytes;
        m.setElement(MarklinConstants.CANADDRESSBYTE1, (addr >> 24) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE2, (addr >> 16) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE3, (addr >> 8) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE4, (addr) & 0xFF);
        m.setElement(9, MarklinConstants.LOCOEMERGENCYSTOP & 0xFF);
        return m;
    }

    public static MarklinMessage setLocoSpeedSteps(int addr, int step) {
        MarklinMessage m = new MarklinMessage();
        m.setElement(0, MarklinConstants.SYSCOMMANDSTART & 0xFF);
        m.setElement(1, 0x00 & 0xFF);
        m.setElement(2, MarklinConstants.HASHBYTE1 & 0xFF);
        m.setElement(3, MarklinConstants.HASHBYTE2 & 0xFF);
        m.setElement(4, 0x05 & 0xFF); //five bytes;
        m.setElement(MarklinConstants.CANADDRESSBYTE1, (addr >> 24) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE2, (addr >> 16) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE3, (addr >> 8) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE4, (addr) & 0xFF);
        m.setElement(9, 0x05 & 0xFF);
        m.setElement(10, step & 0xFF);
        return m;
    }

    public static MarklinMessage getQryLocoDirection(int addr) {
        MarklinMessage m = new MarklinMessage();
        m.setElement(0, (MarklinConstants.LOCODIRECTION >> 7) & 0xFF);
        m.setElement(1, (MarklinConstants.LOCODIRECTION << 1) & 0xFF);
        m.setElement(2, MarklinConstants.HASHBYTE1 & 0xFF);
        m.setElement(3, MarklinConstants.HASHBYTE2 & 0xFF);
        m.setElement(4, 0x04 & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE1, (addr >> 24) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE2, (addr >> 16) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE3, (addr >> 8) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE4, (addr) & 0xFF);
        return m;
    }

    public static MarklinMessage setLocoDirection(int addr, int dir) {
        MarklinMessage m = new MarklinMessage();
        m.setElement(0, (MarklinConstants.LOCODIRECTION >> 7) & 0xFF);
        m.setElement(1, (MarklinConstants.LOCODIRECTION << 1) & 0xFF);
        m.setElement(2, MarklinConstants.HASHBYTE1 & 0xFF);
        m.setElement(3, MarklinConstants.HASHBYTE2 & 0xFF);
        m.setElement(4, 0x05 & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE1, (addr >> 24) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE2, (addr >> 16) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE3, (addr >> 8) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE4, (addr) & 0xFF);
        m.setElement(9, dir & 0xff);
        return m;
    }

    public static MarklinMessage getQryLocoFunction(int addr, int funct) {
        MarklinMessage m = new MarklinMessage();
        m.setElement(0, (MarklinConstants.LOCOFUNCTION >> 7) & 0xFF);
        m.setElement(1, (MarklinConstants.LOCOFUNCTION << 1) & 0xFF);
        m.setElement(2, MarklinConstants.HASHBYTE1 & 0xFF);
        m.setElement(3, MarklinConstants.HASHBYTE2 & 0xFF);
        m.setElement(4, 0x05 & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE1, (addr >> 24) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE2, (addr >> 16) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE3, (addr >> 8) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE4, (addr) & 0xFF);
        m.setElement(9, (funct) & 0xFF);
        return m;
    }

    public static MarklinMessage setLocoFunction(int addr, int funct, int state) {
        MarklinMessage m = new MarklinMessage();
        m.setElement(0, (MarklinConstants.LOCOFUNCTION >> 7) & 0xFF);
        m.setElement(1, (MarklinConstants.LOCOFUNCTION << 1) & 0xFF);
        m.setElement(2, MarklinConstants.HASHBYTE1 & 0xFF);
        m.setElement(3, MarklinConstants.HASHBYTE2 & 0xFF);
        m.setElement(4, 0x06 & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE1, (addr >> 24) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE2, (addr >> 16) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE3, (addr >> 8) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE4, (addr) & 0xFF);
        m.setElement(9, funct & 0xff);
        m.setElement(10, state & 0xff);
        m.getAddress();
        return m;
    }

    public static MarklinMessage sensorPollMessage(int module) {
        MarklinMessage m = new MarklinMessage();
        m.setElement(0, (MarklinConstants.FEECOMMANDSTART >> 7) & 0xFF);
        m.setElement(1, (MarklinConstants.FEECOMMANDSTART << 1) & 0xFF);
        m.setElement(2, MarklinConstants.HASHBYTE1 & 0xFF);
        m.setElement(3, MarklinConstants.HASHBYTE2 & 0xFF);
        m.setElement(4, 0x05 & 0xFF); //five bytes;
        m.setElement(MarklinConstants.CANADDRESSBYTE1, (MY_UID >> 24) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE2, (MY_UID >> 16) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE3, (MY_UID >> 8) & 0xFF);
        m.setElement(MarklinConstants.CANADDRESSBYTE4, (MY_UID) & 0xFF);
        m.setElement(9, module & 0xFF);
        return m;
    }

    public long getAddress() {
        long addr = getElement(MarklinConstants.CANADDRESSBYTE1);
        addr = (addr << 8) + getElement(MarklinConstants.CANADDRESSBYTE2);
        addr = (addr << 8) + getElement(MarklinConstants.CANADDRESSBYTE3);
        addr = (addr << 8) + getElement(MarklinConstants.CANADDRESSBYTE4);

        return addr;
    }

    public static MarklinMessage getProgMode() {
        return new MarklinMessage();
    }

    public static MarklinMessage getExitProgMode() {
        return new MarklinMessage();
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
