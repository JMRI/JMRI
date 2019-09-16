package jmri.jmrix.easydcc;

import jmri.DccLocoAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encodes a message to an EasyDCC command station.
 * <p>
 * The {@link EasyDccReply} class handles the response from the command station.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2004
 */
public class EasyDccMessage extends jmri.jmrix.AbstractMRMessage {

    public EasyDccMessage() {
        super();
    }

    // create a new one
    public EasyDccMessage(int i) {
        super(i);
    }

    // copy one
    public EasyDccMessage(EasyDccMessage m) {
        super(m);
    }

    // from String
    public EasyDccMessage(String s) {
        super(s);
    }

    // diagnose format
    public boolean isKillMain() {
        return getOpCode() == 'K';
    }

    public boolean isEnableMain() {
        return getOpCode() == 'E';
    }

    // static methods to return a formatted message
    static public EasyDccMessage getEnableMain() {
        EasyDccMessage m = new EasyDccMessage(1);
        m.setBinary(false);
        m.setOpCode('E');
        return m;
    }

    static public EasyDccMessage getKillMain() {
        EasyDccMessage m = new EasyDccMessage(1);
        m.setBinary(false);
        m.setOpCode('K');
        return m;
    }

    /**
     * Get a static message to add a locomotive to a Standard Consist
     * in the normal direction.
     *
     * @param ConsistAddress  a consist address in the range 1-255
     * @param LocoAddress  a jmri.DccLocoAddress object representing the 
     * locomotive to add
     * @return an EasyDccMessage of the form GN cc llll 
     */
    static public EasyDccMessage getAddConsistNormal(int ConsistAddress, DccLocoAddress LocoAddress) {
        EasyDccMessage m = new EasyDccMessage(10);
        m.setBinary(false);
        m.setOpCode('G');
        m.setElement(1, 'N');
        m.setElement(2, ' ');
        m.addIntAsTwoHex(ConsistAddress, 3);
        m.setElement(5, ' ');
        m.addIntAsFourHex(LocoAddress.getNumber(), 6);
        return m;
    }

    /**
     * Get a static message to add a locomotive to a Standard Consist in
     * the reverse direction.
     *
     * @param ConsistAddress  a consist address in the range 1-255
     * @param LocoAddress  a jmri.DccLocoAddress object representing the 
     * locomotive to add
     * @return an EasyDccMessage of the form GS cc llll 
     */
    static public EasyDccMessage getAddConsistReverse(int ConsistAddress, DccLocoAddress LocoAddress) {
        EasyDccMessage m = new EasyDccMessage(10);
        m.setBinary(false);
        m.setOpCode('G');
        m.setElement(1, 'R');
        m.setElement(2, ' ');
        m.addIntAsTwoHex(ConsistAddress, 3);
        m.setElement(5, ' ');
        m.addIntAsFourHex(LocoAddress.getNumber(), 6);
        return m;
    }

    /**
     * Get a static message to subtract a locomotive from a Standard Consist.
     *
     * @param ConsistAddress  a consist address in the range 1-255
     * @param LocoAddress  a jmri.DccLocoAddress object representing the 
     * locomotive to remove
     * @return an EasyDccMessage of the form GS cc llll 
     */
    static public EasyDccMessage getSubtractConsist(int ConsistAddress, DccLocoAddress LocoAddress) {
        EasyDccMessage m = new EasyDccMessage(10);
        m.setBinary(false);
        m.setOpCode('G');
        m.setElement(1, 'S');
        m.setElement(2, ' ');
        m.addIntAsTwoHex(ConsistAddress, 3);
        m.setElement(5, ' ');
        m.addIntAsFourHex(LocoAddress.getNumber(), 6);
        return m;
    }

    /**
     * Get a static message to delete a Standard Consist.
     *
     * @param ConsistAddress  a consist address in the range 1-255
     * @return an EasyDccMessage of the form GK cc 
     */
    static public EasyDccMessage getKillConsist(int ConsistAddress) {
        EasyDccMessage m = new EasyDccMessage(5);
        m.setBinary(false);
        m.setOpCode('G');
        m.setElement(1, 'K');
        m.setElement(2, ' ');
        m.addIntAsTwoHex(ConsistAddress, 3);
        return m;
    }

    /**
     * Get a static message to display a Standard Consist.
     *
     * @param ConsistAddress  a consist address in the range 1-255
     * @return an EasyDccMessage of the form GD cc 
     */
    static public EasyDccMessage getDisplayConsist(int ConsistAddress) {
        EasyDccMessage m = new EasyDccMessage(5);
        m.setBinary(false);
        m.setOpCode('G');
        m.setElement(1, 'D');
        m.setElement(2, ' ');
        m.addIntAsTwoHex(ConsistAddress, 3);
        return m;
    }

    static public EasyDccMessage getProgMode() {
        EasyDccMessage m = new EasyDccMessage(1);
        m.setBinary(false);
        m.setOpCode('M');
        return m;
    }

    static public EasyDccMessage getExitProgMode() {
        EasyDccMessage m = new EasyDccMessage(1);
        m.setBinary(false);
        m.setOpCode('X');
        return m;
    }

    static public EasyDccMessage getReadPagedCV(int cv) { //R xxx
        EasyDccMessage m = new EasyDccMessage(5);
        m.setBinary(false);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(LONG_TIMEOUT);
        m.setOpCode('R');
        m.setElement(1, ' ');
        m.addIntAsThreeHex(cv, 2);
        return m;
    }

    static public EasyDccMessage getWritePagedCV(int cv, int val) { //P xxx xx
        EasyDccMessage m = new EasyDccMessage(8);
        m.setBinary(false);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(LONG_TIMEOUT);
        m.setOpCode('P');
        m.setElement(1, ' ');
        m.addIntAsThreeHex(cv, 2);
        m.setElement(5, ' ');
        m.addIntAsTwoHex(val, 6);
        return m;
    }

    static public EasyDccMessage getReadRegister(int reg) { //Vx
        if (reg > 8) {
            log.error("register number too large: " + reg);
        }
        EasyDccMessage m = new EasyDccMessage(2);
        m.setBinary(false);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(LONG_TIMEOUT);
        m.setOpCode('V');
        String s = "" + reg;
        m.setElement(1, s.charAt(s.length() - 1));
        return m;
    }

    static public EasyDccMessage getWriteRegister(int reg, int val) { //Sx xx
        if (reg > 8) {
            log.error("register number too large: " + reg);
        }
        EasyDccMessage m = new EasyDccMessage(5);
        m.setBinary(false);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(LONG_TIMEOUT);
        m.setOpCode('S');
        String s = "" + reg;
        m.setElement(1, s.charAt(s.length() - 1));
        m.setElement(2, ' ');
        m.addIntAsTwoHex(val, 3);
        return m;
    }

    static protected final int LONG_TIMEOUT = 180000; // e.g. for programming options

    private final static Logger log = LoggerFactory.getLogger(EasyDccMessage.class);

}
