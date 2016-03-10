// z21Reply.java
package jmri.jmrix.roco.z21;

import jmri.jmrix.AbstractMRReply;

/**
 * class for replies in the z21/Z21 protocol.
 * <P>
 * Replies are of the format: 2 bytes length 2 bytes opcode n bytes data
 * <p>
 * numeric data is sent in little endian format.
 * <p>
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @author	Paul Bender Copyright (C) 2014
 * @version $Revision$
 */
public class z21Reply extends AbstractMRReply {

    // create a new one
    public z21Reply() {
        super();
        setBinary(true);
    }

    /**
     * This ctor interprets the byte array as a sequence of characters to send.
     *
     * @param a Array of bytes to send
     */
    public z21Reply(byte[] a, int l) {
        super();
        _nDataChars = l;
        setBinary(true);
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = a[i];
        }
    }

    // keep track of length
    public void setElement(int n, int v) {
        _dataChars[n] = (char) v;
        _nDataChars = Math.max(_nDataChars, n + 1);
    }

    public void setOpCode(int i) {
        _dataChars[2] = (char) (i & 0x00ff);
        _dataChars[3] = (char) ((i & 0xff00) >> 8);
    }

    public int getOpCode() {
        return _dataChars[2] + (_dataChars[3] << 8);
    }

    public void setLength(int i) {
        _dataChars[0] = (char) (i & 0x00ff);
        _dataChars[1] = (char) ((i & 0xff00) >> 8);
    }

    public int getLength() {
        return _dataChars[0] + (_dataChars[1] << 8);
    }

    protected int skipPrefix(int index) {
        return 0;
    }

    public String toMonitorString() {
        return toString();
    }

    // handle XPressNet replies tunneled in Z21 messages
    boolean isXPressNetTunnelMessage() {
        return (getOpCode() == 0x0040);
    }

    jmri.jmrix.lenz.XNetReply getXNetReply() {
        jmri.jmrix.lenz.XNetReply xnr = null;
        if (isXPressNetTunnelMessage()) {
            xnr = new jmri.jmrix.lenz.XNetReply();
            for (int i = 4; i < getLength(); i++) {
                xnr.setElement(i - 4, getElement(i));
            }
        }
        return xnr;
    }

}


/* @(#)z21Reply.java */
