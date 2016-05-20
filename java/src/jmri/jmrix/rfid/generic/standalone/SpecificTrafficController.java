// SpecificTrafficController.java

package jmri.jmrix.rfid.generic.standalone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.rfid.RfidMessage;
import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import jmri.jmrix.rfid.RfidTrafficController;

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
 * @author      Bob Jacobsen    Copyright (C) 2001, 2003, 2005, 2006, 2008
 * @author      Matthew Harris  Copyright (c) 2011
 * @version     $Revision$
 * @since       2.11.4
 */
public class SpecificTrafficController extends RfidTrafficController {

    private RfidSystemConnectionMemo memo = null;

    public SpecificTrafficController(RfidSystemConnectionMemo memo) {
        super();
        this.memo = memo;
        logDebug = log.isDebugEnabled();
        
        // not polled at all, so allow unexpected messages, and
        // use poll delay just to spread out startup
        setAllowUnexpectedReply(true);
        mWaitBeforePoll = 1000;  // can take a long time to send

    }

    @Override
    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        if (logDebug) log.debug("forward "+m);
        sendInterlock = ((RfidMessage)m).getInterlocked();
        super.forwardToPort(m, reply);
    }

    @Override
    public RfidMessage getRfidMessage(int length) {
        return new SpecificMessage(length);
    }

    @Override
    protected AbstractMRReply newReply() { 
        SpecificReply reply = new SpecificReply(memo.getTrafficController());
        return reply;
    }

    @Override
    public String getRange() {
        return "1";
    }

    @Override
    protected boolean endOfMessage(AbstractMRReply msg) {
        if (((SpecificReply)msg).getNumDataElements()==SpecificReply.SPECIFICMAXSIZE) {
            return true;
        }
        return false;
    }

    boolean sendInterlock = false; // send the 00 interlock when CRC received
    
    private static final Logger log = LoggerFactory.getLogger(SpecificTrafficController.class.getName());
}


/* @(#)SpecificTrafficController.java */
