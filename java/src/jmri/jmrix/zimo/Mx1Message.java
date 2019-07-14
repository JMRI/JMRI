package jmri.jmrix.zimo;

import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a single command or response to the Zimo Binary Protocol.
 * <p>
 * Content is represented with ints to avoid the problems with sign-extension
 * that bytes have, and because a Java char is actually a variable number of
 * bytes in Unicode.
 *
 * @author	Kevin Dickerson Copyright (C) 2014
 *
 * Adapted by Sip Bosch for use with zimo MX-1
 *
 */
public class Mx1Message extends jmri.jmrix.NetMessage {

    public Mx1Message(int len) {
        this(len, Mx1Packetizer.ASCII);
    }

    /**
     * Create a new object, representing a specific-length message.
     *
     * @param len      Total bytes in message, including opcode and
     *                 error-detection byte.
     * @param protocol one of {@link Mx1Packetizer#ASCII} or
     *                 {@link Mx1Packetizer#BINARY}
     */
    public Mx1Message(int len, boolean protocol) {
        super(len);
        this.protocol = protocol;
        if (!protocol) {
            if (len > 15 || len < 0) {
                log.error("Invalid length in ctor: " + len);
            }
        }
    }

    boolean protocol = Mx1Packetizer.ASCII;

    //Version 5 and above allow for a message size greater than 15 bytes
    public Mx1Message(Integer[] contents) {
        super(contents.length);
        protocol = Mx1Packetizer.BINARY;
        for (int i = 0; i < contents.length; i++) {
            this.setElement(i, contents[i]);
        }
    }

    //Version 5 and above allow for a message size greater than 15 bytes
    public Mx1Message(byte[] contents) {
        super(contents.length);
        protocol = Mx1Packetizer.BINARY;
        for (int i = 0; i < contents.length; i++) {
            this.setElement(i, (contents[i] & 0xff));
        }
    }

    public boolean getLongMessage() {
        if (protocol == Mx1Packetizer.BINARY) {
            if ((getElement(1) & 0x80) == 0x80) {
                return true;
            }
        }
        return false;
    }

    final static int PRIMARY = 0x00;
    final static int ACKREP1 = 0x40;
    final static int REPLY2 = 0x20;
    final static int ACK2 = 0x60;

    /**
     * Indicates where the message is to/from in the header byte.
     * <p>
     * Up to JMRI 4.3.5, this was doing {@code ((mod & MX1) == MX1)} for the
     * first test, which is really 0 == 0 and always true. At that point it was
     * changed to just check the bottom two bits.
     *
     * @return one of {@link #MX1}, {@link #MX8}, {@link #MX9} or 0x0F
     */
    public int getModule() {
        int mod = getElement(1) & 0x0F;
        if ((mod & 0x03) == MX1) {
            return MX1;
        }
        if ((mod & 0x03) == MX8) {
            return MX8;
        }
        if ((mod & 0x03) == MX9) {
            return MX9;
        }
        return mod;
    }

    public int getMessageType() {
        return getElement(1) & 0x60;
    }

    public int getPrimaryMessage() {
        return getElement(2);
    }

    /**
     * Message to/from Command Station MX1
     */
    static final int MX1 = 0x00;

    /**
     * Message to/from Accessory module MX8
     */
    static final int MX8 = 0x01;

    /**
     * Message to/from Track Section module MX9
     */
    static final int MX9 = 0x02;

    /**
     * Indicates the message source is a command station. {@value #CS}
     *
     * @see #messageSource()
     */
    final static boolean CS = true;
    /**
     * Indicates the message source is a command station. {@value #PC}
     *
     * @see #messageSource()
     */
    final static boolean PC = false;

    /**
     * Indicates the source of the message.
     *
     * @return {@link #PC} or {@link #CS}
     */
    public boolean messageSource() {
        if ((getElement(0) & 0x08) == 0x08) {
            return PC;
        }
        return CS;
    }

    long timeStamp = 0l;

    protected long getTimeStamp() {
        return timeStamp;
    }

    protected void setTimeStamp(long ts) {
        timeStamp = ts;
    }

    int retries = 3;

    public int getRetry() {
        return retries;
    }

    public void setRetries(int i) {
        retries = i;
    }

    //byte sequenceNo = 0x00;
    public boolean replyL1Expected() {
        return true;
    }

    byte[] rawPacket;

    public void setRawPacket(byte[] b) {
        rawPacket = b;
    }

    protected byte[] getRawPacket() {
        return rawPacket;
    }

    public void setSequenceNo(byte s) {
        setElement(0, (s & 0xff));
    }

    public int getSequenceNo() {
        return (getElement(0) & 0xff);
    }

    boolean crcError = false;

    public void setCRCError() {
        crcError = true;
    }

    public boolean isCRCError() {
        return crcError;
    }

    /**
     * Check if the message has a valid parity (actually check for CR or LF as
     * end of message).
     *
     * @return true if message has correct parity bit
     */
    @Override
    public boolean checkParity() {
        //javax.swing.JOptionPane.showMessageDialog(null, "A-Programma komt tot hier!");
        int len = getNumDataElements();
        return (getElement(len - 1) == (0x0D | 0x0A));
    }
    // programma komt hier volgens mij nooit
    // in fact set CR as end of message

    @Override
    public void setParity() {
        javax.swing.JOptionPane.showMessageDialog(null, "B-Programma komt tot hier!");
        int len = getNumDataElements();
        setElement(len - 1, 0x0D);
    }

    // decode messages of a particular form
    public String getStringMsg() {
        StringBuilder txt = new StringBuilder();
        if (protocol == Mx1Packetizer.BINARY) {
            if (isCRCError()) {
                txt.append(" === CRC ERROR === ");
            }
            if (getNumDataElements() <= 3) {
                txt.append("Short Packet ");
                return txt.toString();
            }
            if ((getElement(1) & 0x10) == 0x10) {
                txt.append("From PC");
            } else {
                txt.append("From CS");
            }
            txt.append(" Seq ").append(getElement(0) & 0xff);
            if (getLongMessage()) {
                txt.append(" (L)");
            } else {
                txt.append(" (S)");
            }
            int offset;
            switch (getMessageType()) {
                case PRIMARY:
                    txt.append(" Prim");
                    break;
                case ACKREP1:
                    txt.append(" Ack/Reply 1");
                    break;
                case REPLY2:
                    txt.append(" Reply 2");
                    break;
                case ACK2:
                    txt.append(" Ack 2");
                    break;
                default:
                    txt.append(" Unknown msg");
                    break;
            }
            if (getModule() == MX1) {  //was (getElement(1)&0x00) == 0x00
                txt.append(" to/from CS (MX1)");
                switch (getPrimaryMessage()) {  //was getElement(2)
                    case TRACKCTL:
                        offset = 0;
                        if (getMessageType() == ACKREP1) {
                            offset++;
                        }
                        txt.append(" Track Control ");
                        if ((getElement(3 + offset) & 0x03) == 0x03) {
                            txt.append(" Query Track Status ");
                        } else if ((getElement(3 + offset) & 0x01) == 0x01) {
                            txt.append(" Turn Track Off ");
                        } else if ((getElement(3 + offset) & 0x02) == 0x02) {
                            txt.append(" Turn Track On ");
                        } else {
                            txt.append(" Stop All Locos ");
                        }
                        break;
                    case 3:
                        txt.append(" Loco Control : ");
                        if (getMessageType() == PRIMARY) {
                            txt.append(getLocoAddress(getElement((3)), getElement(4)));
                            txt.append(((getElement(6) & 0x20) == 0x20) ? " Fwd " : " Rev ");
                            txt.append(((getElement(6) & 0x10) == 0x10) ? " F0: On " : " F0: Off ");
                            txt.append(decodeFunctionStates(getElement(7), getElement(8)));
                        }
                        break;
                    case 4:
                        txt.append(" Loco Funct ");
                        break;
                    case 5:
                        txt.append(" Loco Acc/Dec ");
                        break;
                    case 6:
                        txt.append(" Shuttle ");
                        break;
                    case 7:
                        txt.append(" Accessory ");
                        if (getMessageType() == PRIMARY) {
                            txt.append(getLocoAddress(getElement((3)), getElement(4)));
                            txt.append(((getElement(5) & 0x04) == 0x04) ? " Thrown " : " Closed ");
                        }
                        break;
                    case 8:
                        txt.append(" Loco Status ");
                        break;
                    case 9:
                        txt.append(" Acc Status ");
                        break;
                    case 10:
                        txt.append(" Address Control ");
                        break;
                    case 11:
                        txt.append(" CS State ");
                        break;
                    case 12:
                        txt.append(" Read/Write CS CV ");
                        break;
                    case 13:
                        txt.append(" CS Equip Query ");
                        break;
                    case 17:
                        txt.append(" Tool Type ");
                        break;
                    case PROGCMD:
                        offset = 0;
                        if (getMessageType() == ACKREP1) {
                            txt.append(" Prog CV ");
                            break;
                        }
                        if (getMessageType() == REPLY2) {
                            offset++;
                        }
                        if (getMessageType() == ACK2) {
                            txt.append("Ack to CS Message");
                            break;
                        }
                        if (getNumDataElements() == 7 && getMessageType() == ACKREP1) {
                            txt.append(" Error Occured ");
                            txt.append(getErrorCode(getElement(6)));
                            txt.append(" Loco: ").append(getLocoAddress(getElement((3 + offset)), getElement(4 + offset)));
                            break;
                        }
                        /*if(getNumDataElements()<7){
                         txt.append(" Ack L1 ");
                         break;
                         }*/
                        if ((getMessageType() == PRIMARY && getNumDataElements() == 8)) {
                            txt.append(" Write CV ");
                        } else {
                            txt.append(" Read CV ");
                        }
                        txt.append("Loco: ").append(getLocoAddress(getElement((3 + offset)), getElement(4 + offset)));
                        if ((getElement(3 + offset) & 0x80) == 0x80) {
                            txt.append(" DCC");
                        }
                        int cv = (((getElement(5 + offset) & 0xff) << 8) + (getElement(6 + offset) & 0xff));
                        txt.append(" CV: ").append(cv);
                        if (getNumDataElements() >= (8 + offset)) {  //Version 61.26 and later includes an extra error bit at the end of the packet
                            txt.append(" Set To: ").append(getElement(7 + offset) & 0xff);
                        }
                        break;
                    case 254:
                        txt.append(" Cur Acc Memory ");
                        break;
                    case 255:
                        txt.append(" Cur Loco Memory ");
                        break;
                    default:
                        txt.append(" Unknown ");
                }
            } else if ((getElement(1) & 0x01) == 0x01) {
                txt.append(" to/from Accessory Mod (MX8)");
            } else if ((getElement(1) & 0x02) == 0x02) {
                txt.append(" to/from Track Section (MX9)");
            } else {
                txt.append(" unknown");
            }
        }
        //int type = getElement(2);

        return txt.toString();
    }

    private String decodeFunctionStates(int cData2, int cData3) {
        StringBuilder txt = new StringBuilder();

        txt.append(((cData2 & 0x1) == 0x1) ? " F1: On " : " F1: Off ");
        txt.append(((cData2 & 0x2) == 0x2) ? " F2: On " : " F2: Off ");
        txt.append(((cData2 & 0x4) == 0x4) ? " F3: On " : " F3: Off ");
        txt.append(((cData2 & 0x8) == 0x8) ? " F4: On " : " F4: Off ");
        txt.append(((cData2 & 0x10) == 0x10) ? " F5: On " : " F5: Off ");
        txt.append(((cData2 & 0x20) == 0x20) ? " F6: On " : " F6: Off ");
        txt.append(((cData2 & 0x40) == 0x40) ? " F7: On " : " F7: Off ");
        txt.append(((cData2 & 0x80) == 0x80) ? " F8: On " : " F8: Off ");

        txt.append(((cData3 & 0x1) == 0x1) ? " F9: On " : " F9: Off ");
        txt.append(((cData3 & 0x2) == 0x2) ? " F10: On " : " F10: Off ");
        txt.append(((cData3 & 0x4) == 0x4) ? " F11: On " : " F11: Off ");
        txt.append(((cData3 & 0x8) == 0x8) ? " F12: On " : " F12: Off ");

        return txt.toString();
    }

    public int getLocoAddress() {
        int offset = 0;
        if (getMessageType() == REPLY2) {
            offset++;
        } else if (getMessageType() == ACKREP1) {
            offset = +2;
        }
        if (getNumDataElements() == (4 + offset)) {
            return getLocoAddress(getElement(3 + offset), getElement(4 + offset));
        }
        return -1;
    }

    public int getCvValue() {
        int offset = 0;
        if (getMessageType() == REPLY2) {
            offset++;
        } else if (getMessageType() == ACKREP1) {
            offset = +2;
        }
        if (getNumDataElements() >= (8 + offset)) { //Version 61.26 and later includes an extra error bit at the end of the packet
            return (getElement(7 + offset) & 0xff);
        }
        return -1;
    }

    int getLocoAddress(int hi, int lo) {
        hi = hi & 0x3F;
        if (hi == 0) {
            return lo;
        } else {
            hi = (((hi & 255) - 192) << 8);
            hi = hi + (lo & 255);
            return hi;
        }
    }

    String getErrorCode(int i) {
        switch (i) {
            case NO_ERROR:
                return "No Error";
            case ERR_ADDRESS:
                return "Invalid Address";
            case ERR_INDEX:
                return "Invalid Index";
            case ERR_FORWARD:
                return "Could not be Forwarded";
            case ERR_BUSY:
                return "CMD Busy";
            case ERR_NO_MOT:
                return "Motorola Jump Off";
            case ERR_NO_DCC:
                return "DCC Jump Off";
            case ERR_CV_ADDRESS:
                return "Invalid CV";
            case ERR_SECTION:
                return "Invalid Section";
            case ERR_NO_MODUL:
                return "No Module Found";
            case ERR_MESSAGE:
                return "Error in Message";
            case ERR_SPEED:
                return "Invalid Speed";
            default:
                return "Unknown Error";
        }
    }

    final static int NO_ERROR = 0x00;
    final static int ERR_ADDRESS = 0x01;
    final static int ERR_INDEX = 0x02;
    final static int ERR_FORWARD = 0x03;
    final static int ERR_BUSY = 0x04;
    final static int ERR_NO_MOT = 0x05;
    final static int ERR_NO_DCC = 0x06;
    final static int ERR_CV_ADDRESS = 0x07;
    final static int ERR_SECTION = 0x08;
    final static int ERR_NO_MODUL = 0x09;
    final static int ERR_MESSAGE = 0x0a;
    final static int ERR_SPEED = 0x0b;

    final static int TRACKCTL = 0x02;
    final static int PROGCMD = 0x13;
    final static int LOCOCMD = 0x03;
    final static int ACCCMD = 0x07;

    static public Mx1Message getCmdStnDetails() {
        Mx1Message m = new Mx1Message(4);
        m.setElement(1, 0x10);
        m.setElement(2, 0x13);
        m.setElement(3, 0x00);
        return m;
    }

    /**
     * Set Track Power Off/Emergency Stop
     *
     * @return MrcMessage
     */
    static public Mx1Message setPowerOff() {
        Mx1Message m = new Mx1Message(4, Mx1Packetizer.BINARY);
        m.setElement(1, 0x10); // PC control short message

        m.setElement(2, TRACKCTL);
        m.setElement(3, 0x01);
        return m;
    }

    static public Mx1Message setPowerOn() {
        Mx1Message m = new Mx1Message(4, Mx1Packetizer.BINARY);
        m.setElement(1, 0x10); // PC control short message

        m.setElement(2, TRACKCTL);
        m.setElement(3, 0x02);
        return m;
    }

    static public Mx1Message getTrackStatus() {
        Mx1Message m = new Mx1Message(4, Mx1Packetizer.BINARY);
        m.setElement(1, 0x10); // PC control short message

        m.setElement(2, TRACKCTL);
        m.setElement(3, 0x03);
        return m;
    }

    /**
     * Create a message to read or write a CV.
     *
     * @param locoAddress address of the loco
     * @param cv          CV to read or write
     * @param value       value to write to CV, if -1 CV is read
     * @param dcc         true if decoder is DCC; false if decoder is Motorola
     * @return a message to read or write a CV
     */
    // javadoc did indicate locoAddress could be blank to use programming track, but that's not possible with an int
    static public Mx1Message getDecProgCmd(int locoAddress, int cv, int value, boolean dcc) {
        Mx1Message m;
        if (value == -1) {
            m = new Mx1Message(7, Mx1Packetizer.BINARY);
        } else {
            m = new Mx1Message(8, Mx1Packetizer.BINARY);
        }
        m.setElement(0, 0x00);
        m.setElement(1, 0x10); // PC control short message

        m.setElement(2, PROGCMD);
        int locoHi = locoAddress >> 8;
        if (dcc) {
            locoHi = locoHi + 128;
        } else {
            locoHi = locoHi + 64;
        }
        m.setElement(3, (locoHi));
        m.setElement(4, (locoAddress & 0xff));
        m.setElement(5, cv >> 8);
        m.setElement(6, cv & 0xff);
        if (value != -1) {
            m.setElement(7, value);
        }
        return m;
    }

    /**
     * Create a locomotive control message.
     *
     * @param locoAddress address of the loco
     * @param speed       Speed Step in the actual Speed Step System
     * @param dcc         true if decoder is DCC; false if decoder is Motorola
     * @param cData1      ???
     * @param cData2      functions output 0-7
     * @param cData3      functions output 9-12
     * @return message controlling a locomotive
     */
    static public Mx1Message getLocoControl(int locoAddress, int speed, boolean dcc, int cData1, int cData2, int cData3) {
        Mx1Message m = new Mx1Message(9, Mx1Packetizer.BINARY);
        m.setElement(0, 0x00);
        m.setElement(1, 0x10); // PC control short message

        m.setElement(2, LOCOCMD);
        //High add 80 to indicate DCC
        int locoHi = locoAddress >> 8;
        if (dcc) {
            locoHi = locoHi + 128;
        } else {
            locoHi = locoHi + 64;
        }
        m.setElement(3, (locoHi));
        m.setElement(4, (locoAddress & 0xff));
        m.setElement(5, speed);
        m.setElement(6, cData1);
        m.setElement(7, cData2);
        m.setElement(8, cData3);
        return m;
    }

    static public Mx1Message getSwitchMsg(int accAddress, int setting, boolean dcc) {
        Mx1Message m = new Mx1Message(6, Mx1Packetizer.BINARY);
        m.setElement(0, 0x00);
        m.setElement(1, 0x10); // PC control short message

        m.setElement(2, ACCCMD);
        //High add 80 to indicate DCC
        int accHi = accAddress >> 8;
        if (dcc) {
            accHi = accHi + 128;
        } else {
            accHi = accHi + 64;
        }
        m.setElement(3, (accHi));
        m.setElement(4, (accAddress & 0xff));
        m.setElement(5, 0x00);
        if (setting == jmri.Turnout.THROWN) {
            m.setElement(5, 0x04);
        }
        return m;
    }
    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(Mx1Message.class);

}
