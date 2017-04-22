package jmri.jmrix.maple;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Contains the data payload of a serial reply packet. Note that _only_ the
 * payload, not the header or trailer, nor the padding DLE characters are
 * included. But it does include addressing characters, etc.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2008
 */
public class SerialReply extends jmri.jmrix.AbstractMRReply {

    // create a new one
    public SerialReply() {
        super();
    }

    public SerialReply(String s) {
        super(s);
    }

    public SerialReply(SerialReply l) {
        super(l);
    }

    @SuppressFBWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION")
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < getNumDataElements(); i++) {
            if (i != 0) {
                s += " ";
            }
            if (getElement(i) < 16) {
                s += "0";
            }
            s += Integer.toHexString(getElement(i) & 0xFF);
        }
        return s;
    }

    // recognize format
    public boolean isRcv() {
        return getElement(0) == 0x02;
    }

    public int getUA() {
        int addr = (getElement(1) - '0') * 10 + (getElement(2) - '0');
        return addr;
    }

    @Override
    protected int skipPrefix(int index) {
        // doesn't have to do anything
        return index;
    }

}


