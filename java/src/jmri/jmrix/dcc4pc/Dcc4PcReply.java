// Dcc4PcReply.java
package jmri.jmrix.dcc4pc;

import jmri.jmrix.AbstractMRReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dcc4PcReply.java
 *
 * Description:	Carries the reply to a Dcc4PcMessage
 *
 * @author Kevin Dickerson Copyright (C) 2012
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision: 18133 $
 */
public class Dcc4PcReply extends AbstractMRReply {

    static public final int maxSize = 2048;

    // create a new one
    public Dcc4PcReply() {
        super();
    }

    public Dcc4PcReply(byte[] packet) {
        this();
        int i = 0; // counter of byte in output message
        int j = 0; // counter of byte in input packet
        setBinary(true);
        // add each byte of the input message
        for (j = 0; j < packet.length; j++) {
            this.setElement(i, packet[i]);
            i++;
        }
    }

    // no need to do anything
    protected int skipPrefix(int index) {
        return index;
    }

    /**
     * Create a new Dcc4PcReply as a deep copy of an existing Dcc4PcReply
     *
     * @param m the Dcc4PcReply to copy
     */
    @SuppressWarnings("null")
    public Dcc4PcReply(Dcc4PcReply m) {
        this();
        if (m == null) {
            log.error("copy ctor of null message");
            return;
        }
        _nDataChars = m._nDataChars;
        if (m.isUnsolicited()) {
            super.setUnsolicited();
        }
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = m._dataChars[i];
        }
    }

    public Dcc4PcReply(String replyString) {
        super(replyString);
    }

    boolean error = false;

    public void setError(boolean boo) {
        error = boo;
    }

    /**
     * Is this reply indicating that a general error has occurred?
     */
    public boolean isError() {
        return error;
    }

    public static final int FAILED = 0x02;
    public static final int INCOMPLETE = 0x01;
    public static final int SUCCESS = 0x00;

    boolean stripRun = false;

    // Check and strip 
    public void strip() {
        //we only want to run the strip once
        if (stripRun) {
            return;
        }
        char tmp[] = new char[_nDataChars];
        int j = 0;

        // Check framing characters
        if ((_dataChars[0] == FAILED) || (_dataChars[0] == INCOMPLETE) || (_dataChars[0] == SUCCESS)) {
            for (int i = 1; i < _nDataChars; i++) {
                tmp[j++] = (char) _dataChars[i];
            }

            // Copy back to original Dcc4PcReply
            for (int i = 0; i < j; i++) {
                _dataChars[i] = tmp[i];
            }
            _nDataChars = j;
            stripRun = true;
            return;
        }
    }

    /**
     * Returns a string representation of this Dcc4PcReply
     */
    public String toHexString() {

        StringBuffer buf = new StringBuffer();
        buf.append("0x" + Integer.toHexString(0xFF & _dataChars[0]));
        for (int i = 1; i < _nDataChars; i++) {
            buf.append(", 0x" + Integer.toHexString(0xFF & _dataChars[i]));
        }
        return buf.toString();
    }

    /**
     * Returns the index of String s in the reply
     */
    public int match(String s) {
        // find a specific string in the reply
        String rep = new String(_dataChars, 0, _nDataChars);
        return rep.indexOf(s);
    }

    public int[] getDataAsArray() {
        return _dataChars.clone();
    }

    public byte[] getFormattedReply() {
        int len = this.getNumDataElements();
        int cr = 0;

        byte msg[] = new byte[len + cr];

        for (int i = 0; i < len; i++) {
            msg[i] = (byte) (0xFF & this.getElement(i));
        }
        return msg;
    }

    public int maxSize() {
        return maxSize;
    }

    private final static Logger log = LoggerFactory.getLogger(Dcc4PcReply.class.getName());

}


/* @(#)Dcc4PcReply.java */
