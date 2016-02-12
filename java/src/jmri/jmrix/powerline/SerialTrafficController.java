// SerialTrafficController.java
package jmri.jmrix.powerline;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from messages. The "SerialInterface" side
 * sends/receives message objects.
 * <P>
 * The connection to a SerialPortController is via a pair of *Streams, which
 * then carry sequences of characters for transmission. Note that this
 * processing is handled in an independent thread.
 * <P>
 * This maintains a list of nodes, but doesn't currently do anything with it.
 * <p>
 * This implementation is complete and can be instantiated, but is not
 * functional. It will be created e.g. when a default object is needed for
 * configuring nodes, etc, during the initial configuration. A subclass must be
 * instantiated to actually communicate with an adapter.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2003, 2005, 2006, 2008 Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011
 * @version	$Revision$
 */
abstract public class SerialTrafficController extends AbstractMRTrafficController implements SerialInterface {

    public SerialTrafficController() {
        super();
        logDebug = log.isDebugEnabled();

        // not polled at all, so allow unexpected messages, and
        // use poll delay just to spread out startup
        setAllowUnexpectedReply(true);
        mWaitBeforePoll = 1000;  // can take a long time to send

    }

    /**
     * instance use of the traffic controller is no longer used for multiple
     * connections
     */
    @Deprecated
    public void setInstance() {
    }

    /**
     * Send a sequence of X10 messages to an adapter.
     * <p>
     * Makes them into the local messages and then queues in order.
     * <p>
     * This is a default, null implementation, which must be overridden in an
     * adapter-specific subclass.
     */
    public void sendX10Sequence(X10Sequence s, SerialListener l) {
    }

    /**
     * Send a sequence of Insteon messages to an adapter.
     * <p>
     * Makes them into the local messages and then queues in order.
     * <p>
     * This is a default, null implementation, which must be overridden in an
     * adapter-specific subclass.
     */
    public void sendInsteonSequence(InsteonSequence s, SerialListener l) {
    }

    /**
     * Provide the maximum number of dimming steps available.
     *
     * @return By default, dimming not available.
     */
    public int getNumberOfIntensitySteps() {
        return 0;
    }

    /**
     * Get a message of a specific length for filling in.
     * <p>
     * This is a default, null implementation, which must be overridden in an
     * adapter-specific subclass.
     */
    public SerialMessage getSerialMessage(int length) {
        return null;
    }

    // have several debug statements in tight loops, e.g. every character;
    // only want to check once
    protected boolean logDebug = false;

    // The methods to implement the SerialInterface
    public synchronized void addSerialListener(SerialListener l) {
        this.addListener(l);
    }

    public synchronized void removeSerialListener(SerialListener l) {
        this.removeListener(l);
    }

    protected int enterProgModeDelayTime() {
        // we should to wait at least a second after enabling the programming track
        return 1000;
    }

    /**
     * Forward a SerialMessage to all registered SerialInterface listeners.
     */
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((SerialListener) client).message((SerialMessage) m);
    }

    /**
     * Forward a reply to all registered SerialInterface listeners.
     */
    protected void forwardReply(AbstractMRListener client, AbstractMRReply r) {
        ((SerialListener) client).reply((SerialReply) r);
    }

    SerialSensorManager mSensorManager = null;

    public void setSensorManager(SerialSensorManager m) {
        mSensorManager = m;
    }

    public SerialSensorManager getSensorManager() {
        return mSensorManager;
    }

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
    public void sendSerialMessage(SerialMessage m, SerialListener reply) {
        sendMessage(m, reply);
    }

    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        if (logDebug) {
            log.debug("forward " + m);
        }
        sendInterlock = ((SerialMessage) m).getInterlocked();
        super.forwardToPort(m, reply);
    }

    protected AbstractMRMessage enterProgMode() {
        return null;
    }

    protected AbstractMRMessage enterNormalMode() {
        return null;
    }

//    /**
//     * static function returning the SerialTrafficController instance to use.
//     * @return The registered SerialTrafficController instance for general use,
//     *         if need be creating one.
//     */
//    @Deprecated
//    public SerialTrafficController instance() {
//        if (self == null) {
//            if (log.isDebugEnabled()) log.debug("Creating default SerialTrafficController instance");
//            self = new SerialTrafficController();
//        }
//        return self;
//    }
    public void setAdapterMemo(SerialSystemConnectionMemo adaptermemo) {
        memo = adaptermemo;
    }

    public SerialSystemConnectionMemo getAdapterMemo() {
        return memo;
    }

    private SerialSystemConnectionMemo memo = null;
    SerialTrafficController self = null;

    boolean sendInterlock = false; // send the 00 interlock when CRC received
    boolean expectLength = false;  // next byte is length of read
    boolean countingBytes = false; // counting remainingBytes into reply buffer
    int remainingBytes = 0;        // count of bytes _left_

    /**
     * <p>
     * This is a default, null implementation, which must be overridden in an
     * adapter-specific subclass.
     */
    protected boolean endOfMessage(AbstractMRReply msg) {
        return true;
    }

    /**
     * <p>
     * This is a default, null implementation, which must be overridden in an
     * adapter-specific subclass.
     */
    protected AbstractMRReply newReply() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTrafficController.class.getName());

}


/* @(#)SerialTrafficController.java */
