// RfidTrafficController.java

package jmri.jmrix.rfid;

import org.apache.log4j.Logger;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;

/**
 * Converts Stream-based I/O to/from messages.  The "RfidInterface"
 * side sends/receives message objects.
 * <P>
 * The connection to
 * a SerialPortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.     Note that this processing is
 * handled in an independent thread.
 * <P>
 * This maintains a list of nodes, but doesn't currently do anything
 * with it.
 * <p>
 * This implementation is complete and can be instantiated, but
 * is not functional.  It will be created e.g. when a default
 * object is needed for configuring nodes, etc, during the initial
 * configuration.  A subclass must be instantiated to actually
 * communicate with an adapter.
 *
 * @author      Bob Jacobsen  Copyright (C) 2001, 2003, 2005, 2006, 2008
 * @author      Matthew Harris  Copyright (C) 2011
 * @version     $Revision$
 * @since       2.11.4
 */
public class RfidTrafficController extends AbstractMRTrafficController implements RfidInterface {

    public RfidTrafficController() {
        super();
        logDebug = log.isDebugEnabled();
        
        // not polled at all, so allow unexpected messages, and
        // use poll delay just to spread out startup
        setAllowUnexpectedReply(true);
        mWaitBeforePoll = 1000;  // can take a long time to send

    }

    RfidSystemConnectionMemo adapterMemo;

    public void setAdapterMemo(RfidSystemConnectionMemo memo) {
        adapterMemo = memo;
    }
    
    /**
     * Get a message of a specific length for filling in.
     * <p>
     * This is a default, null implementation, which must be overridden
     * in an adapter-specific subclass.
     */
    public RfidMessage getRfidMessage(int length) {return null;}
    
    // have several debug statements in tight loops, e.g. every character;
    // only want to check once
    protected boolean logDebug = false;


    // The methods to implement the RfidInterface

    public synchronized void addRfidListener(RfidListener l) {
        this.addListener(l);
    }

    public synchronized void removeRfidListener(RfidListener l) {
        this.removeListener(l);
    }

    /**
     * Forward a RfidMessage to all registered RfidInterface listeners.
     */
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((RfidListener)client).message((RfidMessage)m);
    }

    /**
     * Forward a reply to all registered RfidInterface listeners.
     */
    protected void forwardReply(AbstractMRListener client, AbstractMRReply r) {
        ((RfidListener)client).reply((RfidReply)r);
    }

    RfidSensorManager mSensorManager = null;
    public void setSensorManager(RfidSensorManager m) { mSensorManager = m; }
    public RfidSensorManager getSensorManager() { return mSensorManager; }
    
    RfidReporterManager mReporterManager = null;
    public void setReporterManager(RfidReporterManager m) { mReporterManager = m; }
    public RfidReporterManager getReporterManager() { return mReporterManager; }

    
    /**
     * Eventually, do initialization if needed
     */
    protected AbstractMRMessage pollMessage() {
        return null;
    }

    protected AbstractMRListener pollReplyHandler() {
        return null;
    }

    /**
     * Forward a preformatted message to the actual interface.
     */
    public void sendRfidMessage(RfidMessage m, RfidListener reply) {
        sendMessage(m, reply);
    }

    @Override
    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        if (logDebug) log.debug("forward "+m);
        sendInterlock = ((RfidMessage)m).getInterlocked();
        super.forwardToPort(m, reply);
    }
        
    protected AbstractMRMessage enterProgMode() {
        return null;
    }
    protected AbstractMRMessage enterNormalMode() {
        return null;
    }

//    /**
//     * static function returning the RfidTrafficController instance to use.
//     * @return The registered RfidTrafficController instance for general use,
//     *         if need be creating one.
//     */
//    static public RfidTrafficController instance() {
//        if (self == null) {
//            if (log.isDebugEnabled()) log.debug("Creating default SerialTrafficController instance");
//            self = new RfidTrafficController();
//        }
//        return self;
//    }

//    static volatile RfidTrafficController self;
    @Deprecated
    protected void setInstance() {
//        self = this;
    }

    
//    static public void checkInstance(RfidTrafficController tc) {
//        if (self != tc) {
//            log.error("mismatched TrafficController instance");
//            new Exception("").printStackTrace();
//        }
//    }

    boolean sendInterlock = false; // send the 00 interlock when CRC received
    boolean expectLength = false;  // next byte is length of read
    boolean countingBytes = false; // counting remainingBytes into reply buffer
    int remainingBytes = 0;        // count of bytes _left_
    
    /**
     * <p>
     * This is a default, null implementation, which must be overridden
     * in an adapter-specific subclass.
     */
    protected boolean endOfMessage(AbstractMRReply msg) { return true; }

    /**
     * <p>
     * This is a default, null implementation, which must be overridden
     * in an adapter-specific subclass.
     */
    protected AbstractMRReply newReply() { return null; }

    public String getRange() { return null; }
      
    private static final Logger log = Logger.getLogger(RfidTrafficController.class.getName());
}


/* @(#)RfidTrafficController.java */
