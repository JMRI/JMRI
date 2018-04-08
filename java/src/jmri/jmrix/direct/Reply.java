package jmri.jmrix.direct;

/**
 * Contains the data payload of a serial reply packet. Note that _only_ the
 * payload, not the header or trailer, nor the padding DLE characters are
 * included. But it does include addressing characters, etc.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2008
 */
public class Reply extends jmri.jmrix.AbstractMRReply {

    // create a new one
    public Reply() {
        super();
    }

    public Reply(String s) {
        super(s);
    }

    public Reply(Reply l) {
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

    @Override
    protected int skipPrefix(int index) {
        // doesn't have to do anything
        return index;
    }

}
