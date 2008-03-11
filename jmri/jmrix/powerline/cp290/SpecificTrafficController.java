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
 * @version			$Revision: 1.2 $
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
        while ( (c = s.getCommand() ) !=null) {
            SpecificMessage m;
            if (c.isAddress()) 
                m = SpecificMessage.getAddress(c.getHouseCode(), ((X10Sequence.Address)c).getAddress());
            else {
                X10Sequence.Function f = (X10Sequence.Function)c;
                if (f.getDimCount() > 0)
                    m = SpecificMessage.getFunctionDim(f.getHouseCode(), f.getFunction(), f.getDimCount());
                else
                    m = SpecificMessage.getFunction(f.getHouseCode(), f.getFunction());
            }
            sendSerialMessage(m, l);
        }
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
