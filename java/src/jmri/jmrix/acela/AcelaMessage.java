// AcelaMessage.java
package jmri.jmrix.acela;

/**
 * Contains the data payload of an Acela packet.
 * <P>
 *
 * @author	Bob Jacobsen Copyright (C) 2001,2003
 * @version	$Revision$
 *
 * @author	Bob Coleman Copyright (C) 2007, 2008 Based on CMRI serial example,
 * modified to establish Acela support.
 */
public class AcelaMessage extends jmri.jmrix.AbstractMRMessage {
    // is this logically an abstract class?

    final static int POLL_TIMEOUT = 250;

    public AcelaMessage() {
        super();
    }

    // create a new one
    public AcelaMessage(int i) {
        super(i);
    }

    // copy one
    public AcelaMessage(AcelaMessage m) {
        super(m);
    }

    /**
     * This ctor interprets the String as the exact sequence to send,
     * byte-for-byte.
     *
     * @param m
     */
    public AcelaMessage(String m) {
        super(m);
    }

    /**
     * This ctor interprets the byte array as a sequence of characters to send.
     *
     * @param a Array of bytes to send
     */
    public AcelaMessage(byte[] a) {
        super(String.valueOf(a));
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION")
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
    public String toString() {
        String s = "";
        for (int i = 0; i < getNumDataElements(); i++) {
            if (i != 0) {
                s += " ";
            }
            s += jmri.util.StringUtil.twoHexFromInt(getElement(i));
        }
        return s;
    }

    // static methods to return a formatted message
    // used within AcelaTrafficController to initialize Acela system
    static public AcelaMessage getAcelaVersionMsg() {
        AcelaMessage m = new AcelaMessage(1);
        m.setBinary(true);
        m.setElement(0, 0x19);
        return m;
    }

    static public AcelaMessage getAcelaResetMsg() {
        // create a Acela message and add initialization bytes
        AcelaMessage m = new AcelaMessage(1);
        m.setBinary(true);
        m.setElement(0, 0x15);  //  Acela command to reset Acela network
        return m;
    }

    static public AcelaMessage getAcelaOnlineMsg() {
        // create a Acela message and add initialization bytes
        AcelaMessage m = new AcelaMessage(1);
        m.setBinary(true);
        m.setElement(0, 0x16);  //  Acela command to put Acela network ONLINE
        return m;
    }

    static public AcelaMessage getAcelaPollNodesMsg() {
        // create a Acela message and add initialization bytes
        AcelaMessage m = new AcelaMessage(1);
        m.setBinary(true);
        m.setElement(0, 0x18);  // Acela command to poll Acela network nodes
        return m;
    }

    static public AcelaMessage getAcelaPollSensorsMsg() {
        // create a Acela message and add initialization bytes
        AcelaMessage m = new AcelaMessage(1);
        m.setBinary(true);
        m.setElement(0, 0x14);  // Acela command to poll all sensors
        return m;
    }

    static public AcelaMessage getAcelaConfigSensorMsg() {
        // create a Acela message and add initialization bytes
        AcelaMessage m = new AcelaMessage(4);
        m.setBinary(true);
        m.setElement(0, 0x10);  // Acela command to configure one sensor
        m.setElement(1, 0x00);  // Address
        m.setElement(2, 0x00);  // Address
        m.setElement(3, 0x25);  // ending bits[2,1] == 10 means IR
        // ending bit[0] == 1 means invert output
        // bits [15,3] == sensitivity so 0010 0 is low
        return m;
    }
}

/* @(#)AcelaMessage.java */
