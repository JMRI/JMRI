package jmri.jmrix.tmcc;

import jmri.util.StringUtil;

/**
 * Contains the data payload of a TMCC serial reply packet.
 * <p>
 * Note that <i>only</i> the payload, not the header or trailer, nor the padding
 * DLE characters are included. These are added during transmission.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2006
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
            s.append(StringUtil.twoHexFromInt(getElement(i)));
        }
        return s.toString();
    }

    public int getAsWord() {
        return (getElement(1) & 0xFF) * 256 + (getElement(2) & 0xFF);
    }

    @Override
    protected int skipPrefix(int index) {
        // doesn't have to do anything
        return index;
    }

}
