package jmri.jmrix.dcc4pc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encodes a message to the DCC4PC Interface.
 * <p>
 * The {@link Dcc4PcReply} class handles the response from the command station.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Kevin Dickerson Copyright (C) 2012
 * 
 */
public class Dcc4PcMessage extends jmri.jmrix.AbstractMRMessage {

    public static final int MAXSIZE = 515;

    // create a new one
    public Dcc4PcMessage(int i) {
        if (i < 1) {
            log.error("invalid length in call to ctor");
        }
        _nDataChars = i;
        _dataChars = new int[i];
        setBinary(true);
        setRetries(3);
    }

    /**
     * Creates a new Dcc4PcMessage containing a byte array to represent a packet
     * to output
     *
     * @param packet The contents of the packet
     */
    public Dcc4PcMessage(byte[] packet) {
        this((packet.length));
        int i = 0; // counter of byte in output message
        int j; // counter of byte in input packet
        setBinary(true);
        // add each byte of the input message
        for (j = 0; j < packet.length; j++) {
            this.setElement(i, packet[i]);
            i++;
        }
        setRetries(1);
    }

    // from String
    public Dcc4PcMessage(String s) {
        _nDataChars = s.length();
        _dataChars = new int[_nDataChars];
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = s.charAt(i);
        }
        setBinary(true);
        setRetries(3);
    }

    // copy one
    public Dcc4PcMessage(Dcc4PcMessage m) {
        if (m == null) {
            log.error("copy ctor of null message");
            return;
        }
        _nDataChars = m._nDataChars;
        _dataChars = new int[_nDataChars];
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = m._dataChars[i];
        }
        setBinary(true);
        setRetries(1);
    }

    public void setForChildBoard(boolean boo) {
        childBoard = boo;
    }

    boolean childBoard = false;

    public boolean isForChildBoard() {
        return childBoard;
    }

    /** {@inheritDoc} */
    @Override
    public void setElement(int n, int v) {
        _dataChars[n] = v;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("0x");
        buf.append(Integer.toHexString(0xFF & _dataChars[0]));
        for (int i = 1; i < _nDataChars; i++) {
            buf.append(".0x");
            buf.append(Integer.toHexString(0xff & _dataChars[i]));
            //buf.append(", 0x" + Integer.toHexString(0xFF & _dataChars[i]));
        }
        return buf.toString();
    }

    /**
     * Get formatted message for direct output to stream - this is the final
     * format of the message as a byte array
     *
     * @return the formatted message as a byte array
     */
    public byte[] getFormattedMessage() {
        int len = this.getNumDataElements();
        // space for carriage return if required
        int cr = 0;

        byte msg[] = new byte[len + cr];

        for (int i = 0; i < len; i++) {
            msg[i] = (byte) this.getElement(i);
        }
        return msg;
    }

    //Not supported
    static public Dcc4PcMessage getProgMode() {
        Dcc4PcMessage m = new Dcc4PcMessage(1);
        //m.setOpCode('+');
        return m;
    }

    //Not supported
    static public Dcc4PcMessage getExitProgMode() {
        Dcc4PcMessage m = new Dcc4PcMessage(1);
        // m.setOpCode(' ');
        return m;
    }

    boolean isResponse = false;

    public boolean isGetResponse() {
        return isResponse;
    }
    
    int board = -1;
    
    public int getBoard(){
        return board;
    }
    
    int messageType = -1;
    public int getMessageType(){
        return messageType;
    }
    
    static final int INFO = 0x00;
    static final int DESC = 0x01;
    static final int SERIAL = 0x02;
    static final int CHILDENABLEDINPUTS = 0x07;
    static final int CHILDRESET = 0x09;
    static final int CHILDPOLL = 0x0a;
    static final int RESPONSE = 0x0c;
    
    static public Dcc4PcMessage getInfo(int address) {
        Dcc4PcMessage m = new Dcc4PcMessage(new byte[]{(byte) 0x0b, (byte) address, (byte) INFO});
        m.childBoard = true;
        m.board = address;
        m.messageType = INFO;
        m.setRetries(2);
        return m;
    }

    static public Dcc4PcMessage getDescription(int address) {
        Dcc4PcMessage m = new Dcc4PcMessage(new byte[]{(byte) 0x0b, (byte) address, (byte) DESC});
        m.childBoard = true;
        m.board = address;
        m.messageType = DESC;
        return m;
    }

    static public Dcc4PcMessage getSerialNumber(int address) {
        Dcc4PcMessage m = new Dcc4PcMessage(new byte[]{(byte) 0x0b, (byte) address, (byte) SERIAL});
        m.childBoard = true;
        m.board = address;
        m.messageType = SERIAL;
        return m;
    }

    static public Dcc4PcMessage resetBoardData(int address) {
        Dcc4PcMessage m = new Dcc4PcMessage(new byte[]{(byte) 0x0b, (byte) address, (byte) CHILDRESET});
        m.childBoard = true;
        m.board = address;
        m.messageType = CHILDRESET;
        return m;
    }
       
    static public Dcc4PcMessage pollBoard(int address){
        Dcc4PcMessage m = new Dcc4PcMessage(new byte[]{(byte) 0x0b, (byte) address, (byte) CHILDPOLL});
        m.childBoard = true;
        m.setTimeout(500);
        m.board = address;
        m.messageType = CHILDPOLL;
        return m;
    }

    static public Dcc4PcMessage getEnabledInputs(int address) {
        Dcc4PcMessage m = new Dcc4PcMessage(new byte[]{(byte) 0x0b, (byte) address, (byte) CHILDENABLEDINPUTS});
        m.childBoard = true;
        m.board = address;
        m.messageType = CHILDENABLEDINPUTS;
        return m;
    }

    static public Dcc4PcMessage getInfo() {
        Dcc4PcMessage m = new Dcc4PcMessage(new byte[]{(byte) INFO});
        m.messageType = INFO;
        return m;
    }

    static public Dcc4PcMessage getResponse() {
        Dcc4PcMessage m = new Dcc4PcMessage(new byte[]{(byte) RESPONSE});
        m.isResponse = true;
        m.messageType = RESPONSE;
        return m;
    }

    static public Dcc4PcMessage getDescription() {
        Dcc4PcMessage m = new Dcc4PcMessage(new byte[]{(byte) DESC});
        m.messageType = DESC;
        return m;
    }

    static public Dcc4PcMessage getSerialNumber() {
        Dcc4PcMessage m = new Dcc4PcMessage(new byte[]{(byte) SERIAL});
        m.messageType = SERIAL;
        return m;
    }
    
    private final static Logger log = LoggerFactory.getLogger(Dcc4PcMessage.class);
}
