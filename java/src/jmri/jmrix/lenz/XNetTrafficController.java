package jmri.jmrix.lenz;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
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
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender Copyright (C) 2004-2010
 */
public abstract class XNetTrafficController extends AbstractMRTrafficController implements XNetInterface {

    protected HashMap<XNetListener, Integer> mListenerMasks;

    /**
     * Static function returning the TrafficController instance to use.
     *
     * @return The registered TrafficController instance for general use, if
     *         need be creating one.
     */
    @Deprecated
    static public XNetTrafficController instance() {
        return self;
    }

    /**
     * Static function setting this object as the TrafficController instance to
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
     * Must provide a LenzCommandStation reference at creation time.
     *
     * @param pCommandStation reference to associated command station object,
     *                        preserved for later.
     */
    XNetTrafficController(LenzCommandStation pCommandStation) {
        mCommandStation = pCommandStation;
        setAllowUnexpectedReply(true);
        mListenerMasks = new HashMap<>();
        highPriorityQueue = new LinkedBlockingQueue<>();
        highPriorityListeners = new LinkedBlockingQueue<>();
    }

    // Abstract methods for the XNetInterface

    /**
     * Forward a preformatted XNetMessage to the actual interface.
     *
     * @param m Message to send; will be updated with CRC
     */
    @Override
    abstract public void sendXNetMessage(XNetMessage m, XNetListener reply);

    /**
     * Make connection to existing PortController object.
     */
    @Override
    public void connectPort(jmri.jmrix.AbstractPortController p) {
        super.connectPort(p);
        if (p instanceof XNetPortController) {
            this.addXNetListener(XNetInterface.COMMINFO, new XNetTimeSlotListener((XNetPortController) p));
        }
    }

    /**
     * Forward a preformatted XNetMessage to a specific listener interface.
     *
     * @param m Message to send;
     */
    @Override
    public void forwardMessage(AbstractMRListener reply, AbstractMRMessage m) {
        if (!(reply instanceof XNetListener) || !(m instanceof XNetMessage)) {
            throw new IllegalArgumentException("");
        }
        ((XNetListener) reply).message((XNetMessage) m);
    }

    /**
     * Forward a preformatted XNetMessage to the registered XNetListeners.
     * <p>
     * NOTE: this drops the packet if the checksum is bad.
     *
     * @param client is the client getting the message
     * @param m      Message to send
     */
    @Override
    public void forwardReply(AbstractMRListener client, AbstractMRReply m) {
        if (!(client instanceof XNetListener) || !(m instanceof XNetReply)) {
            throw new IllegalArgumentException("");
        }
        // check parity
        if (!((XNetReply) m).checkParity()) {
            log.warn("Ignore packet with bad checksum: {}", (m));
        } else {
            try {
                int mask = (mListenerMasks.get(client));
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
    // This means responses to time critical messages (turnout off messages).
    LinkedBlockingQueue<XNetMessage> highPriorityQueue = null;
    LinkedBlockingQueue<XNetListener> highPriorityListeners = null;

    public void sendHighPriorityXNetMessage(XNetMessage m, XNetListener reply) {
        try {
            highPriorityQueue.put(m);
            highPriorityListeners.put(reply);
        } catch (java.lang.InterruptedException ie) {
            log.error("Interupted while adding High Priority Message to Queue");
        }
    }

    @Override
    protected AbstractMRMessage pollMessage() {
        try {
            if (highPriorityQueue.peek() == null) {
                return null;
            } else {
                return highPriorityQueue.take();
            }
        } catch (java.lang.InterruptedException ie) {
            log.error("Interupted while removing High Priority Message from Queue");
        }
        return null;
    }

    @Override
    protected AbstractMRListener pollReplyHandler() {
        try {
            if (highPriorityListeners.peek() == null) {
                return null;
            } else {
                return highPriorityListeners.take();
            }
        } catch (java.lang.InterruptedException ie) {
            log.error("Interupted while removing High Priority Message Listener from Queue");
        }
        return null;
    }

    @Override
    public synchronized void addXNetListener(int mask, XNetListener l) {
        addListener(l);
        // This is adds all the mask information.  A better way to do
        // this would be to allow updating individual bits
        mListenerMasks.put(l, mask);
    }

    @Override
    public synchronized void removeXNetListener(int mask, XNetListener l) {
        removeListener(l);
        // This is removes all the mask information.  A better way to do
        // this would be to allow updating of individual bits
        mListenerMasks.remove(l);
    }

    /**
     * This method has to be available, even though it doesn't do anything on
     * lenz.
     */
    @Override
    protected AbstractMRMessage enterProgMode() {
        return null;
    }

    /**
     * Return the value of getExitProgModeMsg().
     */
    @Override
    protected AbstractMRMessage enterNormalMode() {
        return XNetMessage.getExitProgModeMsg();
    }

    /**
     * Check to see if the programmer associated with this interface is idle or
     * not.
     */
    @Override
    protected boolean programmerIdle() {
        if (mMemo == null) {
            return true;
        }
        jmri.jmrix.lenz.XNetProgrammerManager pm = mMemo.getProgrammerManager();
        if (pm == null) {
            return true;
        }
        XNetProgrammer p = (XNetProgrammer) pm.getGlobalProgrammer();
        if (p == null) {
            return true;
        }
        return !(p.programmerBusy());
    }

    @Override
    protected boolean endOfMessage(AbstractMRReply msg) {
        int len = (((XNetReply) msg).getElement(0) & 0x0f) + 2;  // opCode+Nbytes+ECC
        log.debug("Message Length {} Current Size {}", len, msg.getNumDataElements());
        return msg.getNumDataElements() >= len;
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
     * Reference to the command station in communication here.
     */
    LenzCommandStation mCommandStation;

    /**
     * Get access to communicating command station object.
     *
     * @return associated Command Station object
     */
    public LenzCommandStation getCommandStation() {
        return mCommandStation;
    }

    /**
     * Reference to the system connection memo.
     */
    XNetSystemConnectionMemo mMemo = null;

    /**
     * Get access to the system connection memo associated with this traffic
     * controller.
     *
     * @return associated systemConnectionMemo object
     */
    public XNetSystemConnectionMemo getSystemConnectionMemo() {
        return (mMemo);
    }

    /**
     * Set the system connection memo associated with this traffic controller.
     *
     * @param m associated systemConnectionMemo object
     */
    public void setSystemConnectionMemo(XNetSystemConnectionMemo m) {
        mMemo = m;
    }

    private XNetFeedbackMessageCache _FeedbackCache = null;

    /**
     * Return an XNetFeedbackMessageCache object associated with this traffic
     * controller.
     */
    public XNetFeedbackMessageCache getFeedbackMessageCache() {
        if (_FeedbackCache == null) {
            _FeedbackCache = new XNetFeedbackMessageCache(this);
        }
        return _FeedbackCache;
    }

    private final static Logger log = LoggerFactory.getLogger(XNetTrafficController.class);

}
