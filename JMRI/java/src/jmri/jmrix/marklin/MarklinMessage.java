package jmri.jmrix.marklin;


/**
 * Encodes a message to a Marklin command station.
 * <p>
 * The {@link MarklinReply} class handles the response from the command station.
 *
 * @author Kevin Dickerson Copyright (C) 2001, 2008
 */
/*Packages of length 13 are interpreted as can-bus packages: 4 bytes
 Can-bus-ID (BigEndian or network order), 1-byte length and 8 bytes of data, if necessary with null bytes
 to fill in are.*/

/*The message ID is divided into the areas of lower priority (priority), command (command), response
 and hash. The communication is based on the following format:
 Prio - 2 +2bit
 Command 8 bit
 Resp - 1 bit
 Hash - 16bit
 DLC - 4bit (ie CAN message length)
 CAN message 8 BYTES
 Can Message Bytes 0 to 3 are the address bytes, with byte 0 High, byte 3 low
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
        for (int i = 0; i < d.length; i++) {
            _dataChars[i] = d[i];
        }
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

    // from String
    /*public  MarklinMessage(String m) {
     super(m);
     }*/
    // static methods to return a formatted message
    static public MarklinMessage getEnableMain() {
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

    static public MarklinMessage getKillMain() {
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

    //static public MarklinMessage get
    static public MarklinMessage getSetTurnout(int addr, int state, int power) {
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

    static public MarklinMessage getQryLocoSpeed(int addr) {
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

    static public MarklinMessage setLocoSpeed(int addr, int speed) {
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

    static public MarklinMessage setLocoEmergencyStop(int addr) {
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

    static public MarklinMessage setLocoSpeedSteps(int addr, int step) {
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

    static public MarklinMessage getQryLocoDirection(int addr) {
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

    static public MarklinMessage setLocoDirection(int addr, int dir) {
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

    static public MarklinMessage getQryLocoFunction(int addr, int funct) {
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

    static public MarklinMessage setLocoFunction(int addr, int funct, int state) {
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

    static public MarklinMessage sensorPollMessage(int module) {
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

    static public MarklinMessage getProgMode() {
        return new MarklinMessage();
    }

    static public MarklinMessage getExitProgMode() {
        return new MarklinMessage();
    }

    static public MarklinMessage getReadPagedCV(int cv) { //Rxxx
        return new MarklinMessage();
    }

    static public MarklinMessage getWritePagedCV(int cv, int val) { //Pxxx xxx
        return new MarklinMessage();
    }

    static public MarklinMessage getReadRegister(int reg) { //Vx
        return new MarklinMessage();
    }

    static public MarklinMessage getWriteRegister(int reg, int val) { //Sx xxx
        return new MarklinMessage();
    }

    static public MarklinMessage getReadDirectCV(int cv) { //Rxxx
        return new MarklinMessage();
    }

    static public MarklinMessage getWriteDirectCV(int cv, int val) { //Pxxx xxx
        return new MarklinMessage();
    }
}
