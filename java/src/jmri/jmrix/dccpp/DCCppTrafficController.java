// DCCppTrafficController.java
package jmri.jmrix.dccpp;

import java.util.Hashtable;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.DataInputStream;
import javax.swing.SwingUtilities;
import static jmri.jmrix.AbstractMRTrafficController.AUTORETRYSTATE;
import static jmri.jmrix.AbstractMRTrafficController.NORMALMODE;
import static jmri.jmrix.AbstractMRTrafficController.NOTIFIEDSTATE;
import static jmri.jmrix.AbstractMRTrafficController.OKSENDMSGSTATE;
import static jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE;
import static jmri.jmrix.AbstractMRTrafficController.WAITMSGREPLYSTATE;
import static jmri.jmrix.AbstractMRTrafficController.WAITREPLYINNORMMODESTATE;
import static jmri.jmrix.AbstractMRTrafficController.WAITREPLYINPROGMODESTATE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for implementations of DCCppInterface.
 * <P>
 * This provides just the basic interface, plus the "" static method for
 * locating the local implementation.
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 * @author	Paul Bender Copyright (C) 2004-2010
 * @author      Mark Underwood Copyright (C) 2015
 * @version $Revision$
 *
 * Based on XNetTrafficController by Bob Jacobsen and Paul Bender
 */
public abstract class DCCppTrafficController extends AbstractMRTrafficController implements DCCppInterface {

    protected Hashtable<DCCppListener, Integer> mListenerMasks;

    /**
     * static function returning the TrafficController instance to use.
     *
     * @return The registered TrafficController instance for general use, if
     *         need be creating one.
     */
    @Deprecated
    static public DCCppTrafficController instance() {
        return self;
    }

    /**
     * static function setting this object as the TrafficController instance to
     * use.
     */
    @Deprecated
    protected void setInstance() {
        if (self == null) {
            self = this;
        }
    }

    static DCCppTrafficController self = null;

    /**
     * Must provide a DCCppCommandStation reference at creation time
     *
     * @param pCommandStation reference to associated command station object,
     *                        preserved for later.
     */
    DCCppTrafficController(DCCppCommandStation pCommandStation) {
        mCommandStation = pCommandStation;
        setAllowUnexpectedReply(true);
        mListenerMasks = new Hashtable<DCCppListener, Integer>();
        HighPriorityQueue = new java.util.concurrent.LinkedBlockingQueue<DCCppMessage>();
        HighPriorityListeners = new java.util.concurrent.LinkedBlockingQueue<DCCppListener>();
	log.debug("DCCppTrafficController created.");
    }

    // Abstract methods for the DCCppInterface
    abstract public boolean status();

    /**
     * Forward a preformatted DCCppMessage to the actual interface.
     *
     * @param m Message to send; will be updated with CRC
     */
    abstract public void sendDCCppMessage(DCCppMessage m, DCCppListener reply);

    protected int lengthOfByteStream(AbstractMRMessage m) {
        int len = m.getNumDataElements();
        return len + 2;
    }

    /**
     * Forward a preformatted DCCppMessage to a specific listener interface.
     *
     * @param m Message to send;
     */
    public void forwardMessage(AbstractMRListener reply, AbstractMRMessage m) {
        ((DCCppListener) reply).message((DCCppMessage) m);
    }

    protected DCCppReply loadChars(DataInputStream istream) throws IOException {
        int i;
        String m;
        if (log.isDebugEnabled()) {
            log.debug("loading characters from port");
        }
        
        byte char1 = readByteProtected(istream);
        m = "";
        while (char1 != '<') {
            // Spin waiting for '<'
            char1 = readByteProtected(istream);
        }
        log.debug("Serial: Message started...");
        // Pick up the rest of the command
        char1 = readByteProtected(istream);
        i = 0; // NOTE: Just for debug printout 
        while (char1 != '>') {
            log.debug("msg char[{}]: {} ({})", i, char1, Character.toString((char)char1));
            m += Character.toString((char)char1);
            char1 = readByteProtected(istream);
            i++;
        }
        log.debug("Received: {}", m);
        // NOTE: Cast is OK because we checked runtime type of msg above.
        return(DCCppReplyParser.parseReply(m));
    }
    
    /**
     * Handle each reply when complete.
     * <P>
     * (This is public for testing purposes) Runs in the "Receive" thread.
     *
     * @throws IOException
     */
    /*
    @Override
    public void handleOneIncomingReply() throws IOException {
            // we sit in this until the message is complete, relying on
        // threading to let other stuff happen

        // Create message off the right concrete class
        //AbstractMRReply msg = newReply();

        // wait for start if needed
        waitForStartOfReply(istream);

        // message exists, now fill it
        // NOTE: We do this a little differently from AbstractMRReply
        // This line right here is the whole reason we are overriding handleOneIncomingReply()
        AbstractMRReply msg = loadChars(istream);

        // WARNING: The code below is copied more-or-less wholesale from AbstractMRTrafficController
        // and should be maintained as such, until there's good reason not to.  Some if() statements
        // have been removed when they are always true or always false for DCC++.
 
        // message is complete, dispatch it !!
        replyInDispatch = true;
        if (log.isDebugEnabled()) {
            log.debug("dispatch reply of length {} contains {} state {}", msg.getNumDataElements(), msg.toString(), mCurrentState);
        }

        // forward the message to the registered recipients,
        // which includes the communications monitor
        // return a notification via the Swing event queue to ensure proper thread
        Runnable r = new RcvNotifier(msg, mLastSender, this);
        try {
            SwingUtilities.invokeAndWait(r);
        } catch (Exception e) {
            log.error("Unexpected exception in invokeAndWait: {}" + e.toString(), e);
        }
        log.debug("dispatch thread invoked");

        // NOTE: At present, DCC++ messages are /never/ unsolicited.  Or at least 
        // never treated as such.
        //
        if (!msg.isUnsolicited()) {
            // effect on transmit:
            switch (mCurrentState) {
                case WAITMSGREPLYSTATE: {
                    // update state, and notify to continue
                    synchronized (xmtRunnable) {
                        mCurrentState = NOTIFIEDSTATE;
                        replyInDispatch = false;
                        xmtRunnable.notify();
                        retransmitCount = 0;
                    }
                    break;
                }
                case WAITREPLYINPROGMODESTATE: {
                    // entering programming mode
                    mCurrentMode = PROGRAMINGMODE;
                    replyInDispatch = false;

                    // update state, and notify to continue
                    synchronized (xmtRunnable) {
                        mCurrentState = OKSENDMSGSTATE;
                        xmtRunnable.notify();
                    }
                    break;
                }
                case WAITREPLYINNORMMODESTATE: {
                    // entering normal mode
                    mCurrentMode = NORMALMODE;
                    replyInDispatch = false;
                    // update state, and notify to continue
                    synchronized (xmtRunnable) {
                        mCurrentState = OKSENDMSGSTATE;
                        xmtRunnable.notify();
                    }
                    break;
                }
                default: {
                    replyInDispatch = false;
                    if (allowUnexpectedReply == true) {
                        if (log.isDebugEnabled()) {
                            log.debug("Allowed unexpected reply received in state: {} was {}", mCurrentState, msg.toString());
                        }
                        synchronized (xmtRunnable) {
                            // The transmit thread sometimes gets stuck
                            // when unexpected replies are received.  Notify
                            // it to clear the block without a timeout.
                            // (do not change the current state)
                            //if(mCurrentState!=IDLESTATE)
                            xmtRunnable.notify();
                        }
                    } else {
                        log.error("reply complete in unexpected state: {} was {}", mCurrentState, msg.toString());
                    }
                }
            }
            // Unsolicited message
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Unsolicited Message Received {}", msg.toString());
            }

            replyInDispatch = false;
        }
    }
*/
    /**
     * Forward a preformatted DCCppMessage to the registered DCCppListeners. NOTE:
     * this drops the packet if the checksum is bad.
     *
     * @param client : Client to send message to
     * @param m Message to send # @param client is the client getting the
     *          message
     */
    @Override
    public void forwardReply(AbstractMRListener client, AbstractMRReply m) {
        // check parity
	try {
	    // NOTE: For now, just forward ALL messages without filtering
	    ((DCCppListener) client).message((DCCppReply) m);
	    // NOTE: For now, all listeners should register for DCCppInterface.ALL
	    /*
	    int mask = (mListenerMasks.get(client)).intValue();
	    if (mask == DCCppInterface.ALL) {
		((DCCppListener) client).message((DCCppReply) m);
	    } else if ((mask & DCCppInterface.COMMINFO)
		       == DCCppInterface.COMMINFO
		       && (((DCCppReply) m).getElement(0)
			   == DCCppConstants.LI_MESSAGE_RESPONSE_HEADER)) {
		((DCCppListener) client).message((DCCppReply) m);
	    } else if ((mask & DCCppInterface.CS_INFO)
		       == DCCppInterface.CS_INFO
		       && (((DCCppReply) m).getElement(0)
			   == DCCppConstants.CS_INFO
			   || ((DCCppReply) m).getElement(0)
			   == DCCppConstants.CS_SERVICE_MODE_RESPONSE
			   || ((DCCppReply) m).getElement(0)
			   == DCCppConstants.CS_REQUEST_RESPONSE
			   || ((DCCppReply) m).getElement(0)
			   == DCCppConstants.BC_EMERGENCY_STOP)) {
		((DCCppListener) client).message((DCCppReply) m);
	    } else if ((mask & DCCppInterface.FEEDBACK)
		       == DCCppInterface.FEEDBACK
		       && (((DCCppReply) m).isFeedbackMessage()
			   || ((DCCppReply) m).isFeedbackBroadcastMessage())) {
		((DCCppListener) client).message((DCCppReply) m);
	    } else if ((mask & DCCppInterface.THROTTLE)
		       == DCCppInterface.THROTTLE
		       && ((DCCppReply) m).isThrottleMessage()) {
		((DCCppListener) client).message((DCCppReply) m);
	    } else if ((mask & DCCppInterface.CONSIST)
		       == DCCppInterface.CONSIST
		       && ((DCCppReply) m).isConsistMessage()) {
		((DCCppListener) client).message((DCCppReply) m);
	    } else if ((mask & DCCppInterface.INTERFACE)
		       == DCCppInterface.INTERFACE
		       && (((DCCppReply) m).getElement(0)
			   == DCCppConstants.LI_VERSION_RESPONSE
			   || ((DCCppReply) m).getElement(0)
			   == DCCppConstants.LI101_REQUEST)) {
		((DCCppListener) client).message((DCCppReply) m);
	    }
		*/
	} catch (NullPointerException e) {
	    // catch null pointer exceptions, caused by a client
	    // that sent a message without being a registered listener
	    ((DCCppListener) client).message((DCCppReply) m);
	}
    }

    // We use the pollMessage routines for high priority messages.
    // This means responses to time critical messages (turnout off 
    // messages).  
    java.util.concurrent.LinkedBlockingQueue<DCCppMessage> HighPriorityQueue = null;
    java.util.concurrent.LinkedBlockingQueue<DCCppListener> HighPriorityListeners = null;

    public void sendHighPriorityDCCppMessage(DCCppMessage m, DCCppListener reply) {
        try {
            HighPriorityQueue.put(m);
            HighPriorityListeners.put(reply);
        } catch (java.lang.InterruptedException ie) {
            log.error("Interupted while adding High Priority Message to Queue");
        }
    }

    protected AbstractMRMessage pollMessage() {
        try {
            if (HighPriorityQueue.peek() == null) {
                return null;
            } else {
                return HighPriorityQueue.take();
            }
        } catch (java.lang.InterruptedException ie) {
            log.error("Interupted while removing High Priority Message from Queue");
        }
        return null;
    }

    protected AbstractMRListener pollReplyHandler() {
        try {
            if (HighPriorityListeners.peek() == null) {
                return null;
            } else {
                return HighPriorityListeners.take();
            }
        } catch (java.lang.InterruptedException ie) {
            log.error("Interupted while removing High Priority Message Listener from Queue");
        }
        return null;
    }

    public synchronized void addDCCppListener(int mask, DCCppListener l) {
        addListener(l);
        // This is adds all the mask information.  A better way to do
        // this would be to allow updating individual bits
        mListenerMasks.put(l, Integer.valueOf(mask));
    }

    public synchronized void removeDCCppListener(int mask, DCCppListener l) {
        removeListener(l);
        // This is removes all the mask information.  A better way to do 
        // this would be to allow updating of individual bits
        mListenerMasks.remove(l);
    }

    /**
     * enterProgMode(); has to be available, even though it doesn't do anything
     * on lenz
     */
    @Override
    protected AbstractMRMessage enterProgMode() {
        return null;
    }

    /**
     * enterNormalMode() returns the value of getExitProgModeMsg();
     */
    @Override
    protected AbstractMRMessage enterNormalMode() {
        //return DCCppMessage.getExitProgModeMsg();
	return null;
    }

    /**
     * programmerIdle() checks to see if the programmer associated with this
     * interface is idle or not.
     */
    @Override
    protected boolean programmerIdle() {
        if (mMemo == null) {
            return true;
        }
        return !(((jmri.jmrix.dccpp.DCCppProgrammer) mMemo.getProgrammerManager().getGlobalProgrammer()).programmerBusy());
    }

    @Override
    // endOfMessage() not really used in DCC++ .. it's handled in the Packetizer.
    protected boolean endOfMessage(AbstractMRReply msg) {
	if (msg.getElement(msg.getNumDataElements()-1) == '>')
	    return true;
	else
	    return false;
    }

    protected AbstractMRReply newReply() {
        return new DCCppReply();
    }

    /**
     * Get characters from the input source, and file a message.
     * <P>
     * Returns only when the message is complete.
     * <P>
     * Only used in the Receive thread.
     *
     * @param msg     message to fill
     * @param istream character source.
     * @throws java.io.IOException when presented by the input source.
     */
    /*
        protected void loadChars(AbstractMRReply msg, java.io.DataInputStream istream) throws java.io.IOException {
	// Spin waiting for start-of-frame '<' character (and toss it)
	String s = new String();
	byte char1;
	boolean found_start = false;
        
        log.debug("Calling DCCppTrafficController.loadChars()");

	while (!found_start) {
	    char1 = readByteProtected(istream);
	    log.debug("Char1: {}", char1);
	    if ((char1 & 0xFF) == '<') {
		found_start = true;
		log.debug("Found starting < ");
		break; // A bit redundant with setting the loop condition true (false)
	    } else {
		//char1 = readByteProtected(istream);
	    }
	}
	
	// Now, suck in the rest of the message...
        for (int i = 0; i < DCCppConstants.MAX_MESSAGE_SIZE; i++) {
            char1 = readByteProtected(istream);
	    if (char1 == '>') {
		log.debug("msg found > ");
		// Don't store the >
		break;
	    } else {
		log.debug("msg read byte {}", char1);
		char c = (char) (char1 & 0x00FF);
		s += Character.toString(c);
	    }
	}
	// TODO: Still need to strip leading and trailing whitespace.
	log.debug("Complete message = {}", s);
        ((DCCppReply)msg).parseReply(s);
    }
*/
    protected void handleTimeout(AbstractMRMessage msg, AbstractMRListener l) {
        super.handleTimeout(msg, l);
        if (l != null) {
            ((DCCppListener) l).notifyTimeout((DCCppMessage) msg);
        }
    }

    /**
     * Reference to the command station in communication here
     */
    DCCppCommandStation mCommandStation;

    /**
     * Get access to communicating command station object
     *
     * @return associated Command Station object
     */
    public DCCppCommandStation getCommandStation() {
        return mCommandStation;
    }

    /**
     * Reference to the system connection memo *
     */
    DCCppSystemConnectionMemo mMemo = null;

    /**
     * Get access to the system connection memo associated with this traffic
     * controller
     *
     * @return associated systemConnectionMemo object
     */
    public DCCppSystemConnectionMemo getSystemConnectionMemo() {
        return (mMemo);
    }

    /**
     * Set the system connection memo associated with this traffic controller
     *
     * @param m associated systemConnectionMemo object
     */
    public void setSystemConnectionMemo(DCCppSystemConnectionMemo m) {
        mMemo = m;
    }

    private DCCppTurnoutReplyCache _TurnoutReplyCache = null;

    /**
     * return an DCCppTurnoutReplyCache object associated with this traffic
     * controller.
     */
    public DCCppTurnoutReplyCache getTurnoutReplyCache() {
        if (_TurnoutReplyCache == null) {
            _TurnoutReplyCache = new DCCppTurnoutReplyCache(this);
        }
        return _TurnoutReplyCache;
    }
    static Logger log = LoggerFactory.getLogger(DCCppTrafficController.class.getName());
}


/* @(#)DCCppTrafficController.java */
