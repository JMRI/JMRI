package jmri.jmrix.cmri.serial;


/**
 * Contains the data payload of a CMRI serial reply packet. Note that _only_ the
 * payload, not the header or trailer, nor the padding DLE characters are
 * included. But it does include addressing characters, etc.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Chuck Catania Copyright (C) 2014, 2015, 2016  CMRInet changes

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
        StringBuilder s = new StringBuilder();
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
        return getElement(1) == 0x52;
    }

    public int getUA() {
        return getElement(0) - 65;
    }
    
    // CMRI-E  Extended Protocol Messages  c2
    public boolean isEOT()     { return (getElement(1)==0x45); }   // 'E'
    public boolean isQUERY()   { return (getElement(1)==0x51); }   // 'Q'
    public boolean isDGREAD()  { return (getElement(1)==0x44); }   // 'D'
    public boolean isDGWRITE() { return (getElement(1)==0x57); }   // 'W'
    public boolean isDGACK()   { return (getElement(1)==0x41) &&   // 'A'
                                        (getElement(2)==0x06); }   //  ACK
    public boolean isDGNAK()   { return (getElement(1)==0x41) &&   // 'A'
                                        (getElement(2)==0x15); }   //  NAK

    @Override
    protected int skipPrefix(int index) {
        // doesn't have to do anything
        return index;
    }

}
