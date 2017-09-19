package jmri.jmrix.maple;

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

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("");
        for (int i = 0; i < getNumDataElements(); i++) {
            if (i != 0) {
                s.append(" ");
            }
            if (getElement(i) < 16) {
                s.append("0");
            }
            s.append(Integer.toHexString(getElement(i) & 0xFF));
        }
        return s.toString();
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
