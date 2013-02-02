// SpecificTrafficController.java

package jmri.jmrix.rfid.merg.concentrator;

//import java.io.DataInputStream;
import org.apache.log4j.Logger;
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
 * @author      Matthew Harris  Copyright (C) 2011
 * @version     $Revision$
 * @since       2.11.4
 */
public class SpecificTrafficController extends RfidTrafficController {

    private String range;

    private RfidSystemConnectionMemo memo = null;
    
    public SpecificTrafficController(RfidSystemConnectionMemo memo, String range) {
        super();
        this.memo = memo;
        this.range = range;
        logDebug = log.isDebugEnabled();
        
        // not polled at all, so allow unexpected messages, and
        // use poll delay just to spread out startup
        setAllowUnexpectedReply(true);
        mWaitBeforePoll = 1000;  // can take a long time to send

    }

    @Override
    public RfidMessage getRfidMessage(int length) {
        return new SpecificMessage(length);
    }

    @Override
    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        if (logDebug) log.debug("forward "+m);
        sendInterlock = ((RfidMessage)m).getInterlocked();
        super.forwardToPort(m, reply);
    }

    @Override
    protected AbstractMRReply newReply() { 
        SpecificReply reply = new SpecificReply(memo.getTrafficController());
        return reply;
    }

    @Override
    public String getRange() {
        return range;
    }

    @Override
    protected boolean endOfMessage(AbstractMRReply msg) {
        SpecificReply sr = (SpecificReply) msg;
        if (sr.getNumDataElements()==SpecificReply.SPECIFICMAXSIZE) {
            if ((sr.getElement(SpecificReply.SPECIFICMAXSIZE-1)&0xFF)==0x3E &&
                (sr.getElement(SpecificReply.SPECIFICMAXSIZE-2)&0xFF)==0x0A &&
                (sr.getElement(SpecificReply.SPECIFICMAXSIZE-3)&0xFF)==0x0D) {
                return true;
            }
            if (logDebug) log.debug("Not a correctly formed message");
            return true;
        }
        return false;
    }

    boolean sendInterlock = false; // send the 00 interlock when CRC received
    
    private static final Logger log = Logger.getLogger(SpecificTrafficController.class.getName());
}


/* @(#)SpecificTrafficController.java */
