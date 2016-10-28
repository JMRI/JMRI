package jmri.jmrix.lenz;

import java.util.Hashtable;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for implementations of XNetInterface.
 * <P>
 * This provides just the basic interface, plus the "" static method for
 * locating the local implementation.
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 * @author	Paul Bender Copyright (C) 2004-2010
 *
 */
public abstract class XNetTrafficController extends AbstractMRTrafficController implements XNetInterface {

    protected Hashtable<XNetListener, Integer> mListenerMasks;

    /**
     * static function returning the TrafficController instance to use.
     *
     * @return The registered TrafficController instance for general use, if
     *         need be creating one.
     */
    @Deprecated
    static public XNetTrafficController instance() {
        return self;
    }

    /**
     * static function setting this object as the TrafficController instance to
     * use.
     */
    @Override
    @Deprecated
    protected void setInstance() {
        if (self == null) {
            self = this;
        }
    }

    static XNetTrafficController self = null;

    /**
     * Must provide a LenzCommandStation reference at creation time
     *
     * @param pCommandStation reference to associated command station object,
     *                        preserved for later.
     */
    XNetTrafficController(LenzCommandStation pCommandStation) {
        mCommandStation = pCommandStation;
        setAllowUnexpectedReply(true);
        mListenerMasks = new Hashtable<XNetListener, Integer>();
        HighPriorityQueue = new java.util.concurrent.LinkedBlockingQueue<XNetMessage>();
        HighPriorityListeners = new java.util.concurrent.LinkedBlockingQueue<XNetListener>();
    }

    // Abstract methods for the XNetInterface
    abstract public boolean status();

    /**
     * Forward a preformatted XNetMessage to the actual interface.
     *
     * @param m Message to send; will be updated with CRC
     */
    abstract public void sendXNetMessage(XNetMessage m, XNetListener reply);

    /**
     * Forward a preformatted XNetMessage to a specific listener interface.
     *
     * @param m Message to send;
     */
    public void forwardMessage(AbstractMRListener reply, AbstractMRMessage m) {
        if(!(reply instanceof XNetListener) || !(m instanceof XNetMessage)){
           throw new IllegalArgumentException("");
        }
        ((XNetListener) reply).message((XNetMessage) m);
    }

    /**
     * Forward a preformatted XNetMessage to the registered XNetListeners. NOTE:
     * this drops the packet if the checksum is bad.
     *
     * @param m Message to send # @param client is the client getting the
     *          message
     */
    public void forwardReply(AbstractMRListener client, AbstractMRReply m) {
        if(!(client instanceof XNetListener) || !(m instanceof XNetReply)){
           throw new IllegalArgumentException("");
        }
        // check parity
        if (!((XNetReply) m).checkParity()) {
            log.warn("Ignore packet with bad checksum: " + ((XNetReply) m).toString());
        } else {
            try {
                int mask = (mListenerMasks.get(client)).intValue();
                if (mask == XNetInterface.ALL) {
                    ((XNetListener) client).message((XNetReply) m);
                } else if ((mask & XNetInterface.COMMINFO)
                        == XNetInterface.COMMINFO
                        && (((XNetReply) m).getElement(0)
                        == XNetConstants.LI_MESSAGE_RESPONSE_HEADER)) {
                    ((XNetListener) client).message((XNetReply) m);
                } else if ((mask & XNetInterface.CS_INFO)
                        == XNetInterface.CS_INFO
                        && (((XNetReply) m).getElement(0)
                        == XNetConstants.CS_INFO
                        || ((XNetReply) m).getElement(0)
                        == XNetConstants.CS_SERVICE_MODE_RESPONSE
                        || ((XNetReply) m).getElement(0)
                        == XNetConstants.CS_REQUEST_RESPONSE
                        || ((XNetReply) m).getElement(0)
                        == XNetConstants.BC_EMERGENCY_STOP)) {
                    ((XNetListener) client).message((XNetReply) m);
                } else if ((mask & XNetInterface.FEEDBACK)
                        == XNetInterface.FEEDBACK
                        && (((XNetReply) m).isFeedbackMessage()
                        || ((XNetReply) m).isFeedbackBroadcastMessage())) {
                    ((XNetListener) client).message((XNetReply) m);
                } else if ((mask & XNetInterface.THROTTLE)
                        == XNetInterface.THROTTLE
                        && ((XNetReply) m).isThrottleMessage()) {
                    ((XNetListener) client).message((XNetReply) m);
                } else if ((mask & XNetInterface.CONSIST)
                        == XNetInterface.CONSIST
                        && ((XNetReply) m).isConsistMessage()) {
                    ((XNetListener) client).message((XNetReply) m);
                } else if ((mask & XNetInterface.INTERFACE)
                        == XNetInterface.INTERFACE
                        && (((XNetReply) m).getElement(0)
                        == XNetConstants.LI_VERSION_RESPONSE
                        || ((XNetReply) m).getElement(0)
                        == XNetConstants.LI101_REQUEST)) {
                    ((XNetListener) client).message((XNetReply) m);
                }
            } catch (NullPointerException e) {
                // catch null pointer exceptions, caused by a client
                // that sent a message without being a registered listener
                ((XNetListener) client).message((XNetReply) m);
            }
        }
    }

    // We use the pollMessage routines for high priority messages.
    // This means responses to time critical messages (turnout off 
    // messages).  
    java.util.concurrent.LinkedBlockingQueue<XNetMessage> HighPriorityQueue = null;
    java.util.concurrent.LinkedBlockingQueue<XNetListener> HighPriorityListeners = null;

    public void sendHighPriorityXNetMessage(XNetMessage m, XNetListener reply) {
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

    public synchronized void addXNetListener(int mask, XNetListener l) {
        addListener(l);
        // This is adds all the mask information.  A better way to do
        // this would be to allow updating individual bits
        mListenerMasks.put(l, Integer.valueOf(mask));
    }

    public synchronized void removeXNetListener(int mask, XNetListener l) {
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
        return XNetMessage.getExitProgModeMsg();
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
        jmri.jmrix.lenz.XNetProgrammerManager pm = (XNetProgrammerManager) mMemo.getProgrammerManager();
        if (pm == null) {
            return true;
        }
        XNetProgrammer p = (XNetProgrammer) pm.getGlobalProgrammer();
        if(p == null) {
           return true;
        }
        return !(p.programmerBusy());
    }

    @Override
    protected boolean endOfMessage(AbstractMRReply msg) {
        int len = (((XNetReply) msg).getElement(0) & 0x0f) + 2;  // opCode+Nbytes+ECC
        log.debug("Message Length " + len + " Current Size " + msg.getNumDataElements());
        if (msg.getNumDataElements() < len) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected AbstractMRReply newReply() {
        return new XNetReply();
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
    @Override
    protected void loadChars(AbstractMRReply msg, java.io.DataInputStream istream) throws java.io.IOException {
        int i;
        for (i = 0; i < msg.maxSize(); i++) {
            byte char1 = readByteProtected(istream);
            msg.setElement(i, char1 & 0xFF);
            if (endOfMessage(msg)) {
                break;
            }
        }
        if (mCurrentState == IDLESTATE) {
            msg.setUnsolicited();
        }
    }

    @Override
    protected void handleTimeout(AbstractMRMessage msg, AbstractMRListener l) {
        super.handleTimeout(msg, l);
        if (l != null) {
            ((XNetListener) l).notifyTimeout((XNetMessage) msg);
        }
    }

    /**
     * Reference to the command station in communication here
     */
    LenzCommandStation mCommandStation;

    /**
     * Get access to communicating command station object
     *
     * @return associated Command Station object
     */
    public LenzCommandStation getCommandStation() {
        return mCommandStation;
    }

    /**
     * Reference to the system connection memo *
     */
    XNetSystemConnectionMemo mMemo = null;

    /**
     * Get access to the system connection memo associated with this traffic
     * controller
     *
     * @return associated systemConnectionMemo object
     */
    public XNetSystemConnectionMemo getSystemConnectionMemo() {
        return (mMemo);
    }

    /**
     * Set the system connection memo associated with this traffic controller
     *
     * @param m associated systemConnectionMemo object
     */
    public void setSystemConnectionMemo(XNetSystemConnectionMemo m) {
        mMemo = m;
    }

    private XNetFeedbackMessageCache _FeedbackCache = null;

    /**
     * return an XNetFeedbackMessageCache object associated with this traffic
     * controller.
     */
    public XNetFeedbackMessageCache getFeedbackMessageCache() {
        if (_FeedbackCache == null) {
            _FeedbackCache = new XNetFeedbackMessageCache(this);
        }
        return _FeedbackCache;
    }

    private final static Logger log = LoggerFactory.getLogger(XNetTrafficController.class.getName());
}
