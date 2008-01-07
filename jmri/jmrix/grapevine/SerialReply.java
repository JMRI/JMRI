// SerialReply.java

package jmri.jmrix.grapevine;


/**
 * Contains the data payload of a serial reply
 * packet.  Note that its _only_ the payload.
 *
 * @author	Bob Jacobsen  Copyright (C) 2002, 2006, 2007
 * @version     $Revision: 1.2 $
 */
public class SerialReply extends jmri.jmrix.AbstractMRReply {

    // create a new one
    public  SerialReply() {
        super();
        setBinary(true);
    }
    public SerialReply(String s) {
        super(s);
        setBinary(true);
    }
    public SerialReply(SerialReply l) {
        super(l);
        setBinary(true);
    }

    /**
     * Is reply to poll message
     */
    public int getAddr() { return getElement(0)&0x7F; }

    public boolean isUnsolicited() { return true; } //always unsolicited!
    
    protected int skipPrefix(int index) {
        // doesn't have to do anything
        return index;
    }

    public int getBank() {
        return ( (getElement(3)&0x70)>>4);
    }
    
    public boolean isError() {
        return (getElement(0)&0x7F) == 0;
    }
    
    public boolean isFromParallelSensor() {
        // bank 5?
        if ( (getElement(3) & 0x70) != 0x50) return false;
        if ( (getElement(1) & 0xA0) != 0x00) return false;
        return true;
    }
    
    public boolean isFromOldSerialSensor() {
        // bank 5?
        if ( (getElement(3) & 0x70) != 0x50) return false;
        if ( (getElement(1) & 0xA0) != 0x20) return false;
        return true;
    }
    
    public boolean isFromNewSerialSensor() {
        // bank 4?
        if ( (getElement(3) & 0x70) != 0x40) return false;
        return true;
    }
    
    /**
     * Format the reply as human-readable text.
     * <P>
     * Since Grapevine doesn't distinguish between message 
     * and reply, this uses the Message method.
     */
    public String format() {
        return SerialMessage.staticFormat(getElement(0)&0xff, getElement(1)&0xff, getElement(2)&0xff, getElement(3)&0xff);
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialReply.class.getName());

}

/* @(#)SerialReply.java */
