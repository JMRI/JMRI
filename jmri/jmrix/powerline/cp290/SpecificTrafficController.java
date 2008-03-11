// SpecificTrafficController.java

package jmri.jmrix.powerline.cp290;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.powerline.SerialTrafficController;
import jmri.jmrix.powerline.X10Sequence;
import jmri.jmrix.powerline.SerialListener;
import jmri.jmrix.powerline.SerialNode;
import jmri.jmrix.powerline.SerialMessage;
import jmri.jmrix.powerline.SerialSensorManager;

/**
 * Converts Stream-based I/O to/from messages.  The "SerialInterface"
 * side sends/receives message objects.
 * <P>
 * The connection to
 * a SerialPortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.     Note that this processing is
 * handled in an independent thread.
 * <P>
 * This maintains a list of nodes, but doesn't currently do anything
 * with it.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001, 2003, 2005, 2006, 2008
 * @version			$Revision: 1.3 $
 */
public class SpecificTrafficController extends SerialTrafficController {

	public SpecificTrafficController() {
        super();
        logDebug = log.isDebugEnabled();
        
        // not polled at all, so allow unexpected messages, and
        // use poll delay just to spread out startup
        setAllowUnexpectedReply(true);
        mWaitBeforePoll = 1000;  // can take a long time to send

        initNodes();
    }

    /**
     * Send a sequence of X10 messages
     * <p>
     * Makes them into the local messages and then queues in order
     */
    synchronized public void sendX10Sequence(X10Sequence s, SerialListener l) {
        s.reset();
        X10Sequence.Command c;
        // index through address commands
        int devicemask=0;
        // there should be at least one address
        c = s.getCommand();
        if (c==null) return;  // nothing!
        int housecode = c.getHouseCode();
        devicemask = setDeviceBit(devicemask, ((X10Sequence.Address)c).getAddress());
        
        // loop through other addresses, if any
        while ( ((c = s.getCommand() ) !=null) && (c.isAddress()) ) {
            if (housecode != ((X10Sequence.Address)c).getHouseCode()) {
                log.error("multiple housecodes found: "+housecode+", "+c.getHouseCode());
                return;
            }
            devicemask = setDeviceBit(devicemask, ((X10Sequence.Address)c).getAddress());
        }
        // at this point, we've picked up all the addresses, start
        // to process functions; there should be at least one
        if (c==null) {
            log.warn("no command");
            return;
        }
        formatAndSend(housecode, devicemask, (X10Sequence.Function)c, l);

        // loop through other functions, if any
        while ( ((c = s.getCommand() ) !=null) && (c.isFunction()) ) {
            if (housecode != ((X10Sequence.Function)c).getHouseCode()) {
                log.error("multiple housecodes found: "+housecode+", "+c.getHouseCode());
                return;
            }
            formatAndSend(housecode, devicemask, (X10Sequence.Function)c, l);
        }
    }
    
    /**
     * Turn a 1-16 device number into a mask bit
     */
    int setDeviceBit(int devicemask, int device){
        return devicemask | (0x10000 >> device);
    }

    /**
     * Format a message and send it
     */
    void formatAndSend(int housecode, int devicemask, 
                        X10Sequence.Function c, SerialListener l) {
        SpecificMessage m = new SpecificMessage(22);  // will be 22 bytes
        for (int i = 0; i< 16; i++) m.setElement(i, 0xFF);
        int level = c.getDimCount();
        if (level>15) {
            log.warn("can't handle dim counts > 15?");
            level = 15;
        }
        int function = c.getFunction();
        
        m.setElement(16, 1);
        m.setElement(17, level*16+function);
        m.setElement(18, housecode*16+0);
        m.setElement(19, devicemask&0xFF);
        m.setElement(20, (devicemask>>8)&0xFF);
        m.setElement(21, 0xFF&(m.getElement(17)+m.getElement(18)+m.getElement(19)+m.getElement(20)) ); // checksum
               
        sendSerialMessage(m, l);
        
    }
    
    /**
     * Get a message of a specific length for filling in.
     */
    public SerialMessage getSerialMessage(int length) {
        return new SpecificMessage(length);
    }

    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        if (logDebug) log.debug("forward "+m);
        super.forwardToPort(m, reply);
    }
        
    protected AbstractMRReply newReply() { 
        SpecificReply reply = new SpecificReply();
        return reply;
    }
    
    protected boolean endOfMessage(AbstractMRReply msg) {
        // overly simplistic, we just cut the message when
        // it gets a non-FF byte
        if ( (msg.getElement(msg.getNumDataElements()-1)&0xFF) == 0xFF) return false;
        log.debug("end of message: "+msg);
        return true;
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SpecificTrafficController.class.getName());
}


/* @(#)SpecificTrafficController.java */
