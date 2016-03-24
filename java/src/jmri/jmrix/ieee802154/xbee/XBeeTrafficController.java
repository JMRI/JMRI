// XBeeTrafficController
package jmri.jmrix.ieee802154.xbee;

import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractPortController;
import jmri.jmrix.ieee802154.IEEE802154Listener;
import jmri.jmrix.ieee802154.IEEE802154Message;
import jmri.jmrix.ieee802154.IEEE802154Reply;
import jmri.jmrix.ieee802154.IEEE802154TrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Traffic Controller interface for communicating with XBee devices directly
 * using the XBee API.
 *
 * @author Paul Bender Copyright (C) 2013
 * @version $Revision$
 */
public class XBeeTrafficController extends IEEE802154TrafficController implements com.rapplogic.xbee.api.PacketListener, XBeeInterface {

    private XBee xbee = null;

    /**
     * Get a message of a specific length for filling in.
     * <p>
     * This is a default, null implementation, which must be overridden in an
     * adapter-specific subclass.
     */
    public IEEE802154Message getIEEE802154Message(int length) {
        return null;
    }

    /**
     * <p>
     * This is a default, null implementation, which must be overridden in an
     * adapter-specific subclass.
     */
    protected AbstractMRReply newReply() {
        return new XBeeReply();
    }

    /**
     * Make connection to existing PortController object.
     */
    @Override
    public void connectPort(AbstractPortController p) {
        //super.connectPort(p);
        // Attach XBee to the port
        if (xbee == null) {
            xbee = new XBee();
        }
        try {
            xbee.initProviderConnection(((XBeeAdapter) p));
            xbee.addPacketListener(this);
            // and start threads
            xmtThread = new Thread(xmtRunnable = new Runnable() {
                public void run() {
                    try {
                        transmitLoop();
                    } catch (Throwable e) {
                        log.error("Transmit thread terminated prematurely by: " + e.toString(), e);
                    }
                }
            });
            xmtThread.setName("Transmit");
            xmtThread.start();

        } catch (XBeeException xe) {
            log.error("Failed to make XBee connection " + xe);
        } catch (Exception e) {
            log.error("Failed to start up communications. Error was " + e);
        }
    }

    /**
     * Actually transmits the next message to the port
     */
    @Override
    synchronized protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        if (log.isDebugEnabled()) {
            log.debug("forwardToPort message: [" + m + "]");
        }
        // remember who sent this
        mLastSender = reply;

        // forward the message to the registered recipients,
        // which includes the communications monitor, except the sender.
        // Schedule notification via the Swing event queue to ensure order
        Runnable r = new XmtNotifier(m, mLastSender, this);
        javax.swing.SwingUtilities.invokeLater(r);

        /* TODO: Check to see if we need to do any of the error handling
         in AbstractMRTrafficController here */
        // forward using XBee Specific message format
        try {
            xbee.sendAsynchronous(((XBeeMessage) m).getXBeeRequest());
        } catch (XBeeException xbe) {
            log.error("Error Sending message to XBee: " + xbe);
        }
    }

    /**
     * Invoked if it's appropriate to do low-priority polling of the command
     * station, this should return the next message to send, or null if the TC
     * should just sleep.
     */
    @Override
    protected AbstractMRMessage pollMessage() {
        if (numNodes <= 0) {
            return null;
        }
        XBeeMessage msg = null;
        if (getNode(curSerialNodeIndex).getSensorsActive()) {
            msg = XBeeMessage.getForceSampleMessage(((XBeeNode) getNode(curSerialNodeIndex)).getPreferedTransmitAddress());
        }
        curSerialNodeIndex = (curSerialNodeIndex + 1) % numNodes;
        return msg;
    }

    @Override
    protected AbstractMRListener pollReplyHandler() {
        return null;
    }

    /*
     * enterProgMode() and enterNormalMode() return any message that
     * needs to be returned to the command station to change modes.
     *
     * If no message is needed, you may return null.
     *
     * If the programmerIdle() function returns true, enterNormalMode() is
     * called after a timeout while in IDLESTATE during programming to
     * return the system to normal mode.
     *
     */
    @Override
    protected AbstractMRMessage enterProgMode() {
        return null;
    }

    @Override
    protected AbstractMRMessage enterNormalMode() {
        return null;
    }

    /*
     * For this implementation, the receive is handled by the
     * XBee Library, so we are suppressing the standard receive
     * loop.
     */
    @Override
    public void receiveLoop() {
    }

    /**
     * Public method to register a node
     */
    @Override
    public void registerNode(jmri.jmrix.AbstractNode node) {
        super.registerNode(node);
        ((XBeeNode) node).setTrafficController(this);
    }

    // XBee Packet Listener interface methods
    // NOTE: Many of the details of this function are derived
    // from the the handleOneIncomingReply() in 
    // AbstractMRTrafficController.
    public void processResponse(XBeeResponse response) {

        // before we forward this on to the listeners, handle
        // responses that may modify how the message is interpreted.
        try {
            // set the hardware type from a response to an "HV" AtCommandResponse
            if (response instanceof AtCommandResponse
                    && ((AtCommandResponse) response).getCommand().equals("HV")) {
                setSeries(com.rapplogic.xbee.api.HardwareVersion.parse(
                        (com.rapplogic.xbee.api.AtCommandResponse) response));
            }
        } catch (com.rapplogic.xbee.api.XBeeException xbe) {
            setSeries(com.rapplogic.xbee.api.HardwareVersion.RadioType.UNKNOWN);
        }

        // set the firmware version after a "VR" AtCommandResponse
        if (response instanceof AtCommandResponse
                && ((AtCommandResponse) response).getCommand().equals("VR")) {
            setVersion(((com.rapplogic.xbee.api.AtCommandResponse) response).getValue());
        }

        //if(response.isError()) {
        //    log.error("XBee API Reports error in parsing reply");
        //    return;
        //}
        XBeeReply reply = new XBeeReply(response);

        // message is complete, dispatch it !!
        replyInDispatch = true;
        if (log.isDebugEnabled()) {
            log.debug("dispatch reply of length " + reply.getNumDataElements()
                    + " contains " + reply.toString() + " state " + mCurrentState);
        }

        // forward the message to the registered recipients,
        // which includes the communications monitor
        // return a notification via the Swing event queue to ensure proper thread
        Runnable r = new RcvNotifier(reply, mLastSender, this);
        try {
            javax.swing.SwingUtilities.invokeAndWait(r);
        } catch (Exception e) {
            log.error("Unexpected exception in invokeAndWait:" + e);
            log.error("cause:" + e.getCause());
            e.printStackTrace();
        }
        if (log.isDebugEnabled()) {
            log.debug("dispatch thread invoked");
        }

        // from here to the end of the function was copied verbatim from 
        // handleOneIncomingReply.  We may not need it after the send code
        // is put into place.
        if (!reply.isUnsolicited()) {
            // effect on transmit:
            switch (mCurrentState) {
                case WAITMSGREPLYSTATE: {
                    // check to see if the response was an error message we want
                    // to automatically handle by re-queueing the last sent
                    // message, otherwise go on to the next message
                    if (reply.isRetransmittableErrorMsg()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Automatic Recovery from Error Message: +reply.toString()");
                        }
                        synchronized (xmtRunnable) {
                            mCurrentState = AUTORETRYSTATE;
                            replyInDispatch = false;
                            xmtRunnable.notify();
                        }
                    } else {
                        // update state, and notify to continue
                        synchronized (xmtRunnable) {
                            mCurrentState = NOTIFIEDSTATE;
                            replyInDispatch = false;
                            xmtRunnable.notify();
                        }
                    }
                    break;
                }
                case WAITREPLYINPROGMODESTATE: {
                    // entering programming mode
                    mCurrentMode = PROGRAMINGMODE;
                    replyInDispatch = false;

                    // check to see if we need to delay to allow decoders to become
                    // responsive
                    int warmUpDelay = enterProgModeDelayTime();
                    if (warmUpDelay != 0) {
                        try {
                            synchronized (xmtRunnable) {
                                xmtRunnable.wait(warmUpDelay);
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // retain if needed later
                        }
                    }
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
                            log.debug("Allowed unexpected reply received in state: "
                                    + mCurrentState + " was " + reply.toString());
                        }
                        //synchronized (xmtRunnable) {
                        // The transmit thread sometimes gets stuck
                        // when unexpected replies are received.  Notify
                        // it to clear the block without a timeout.
                        // (do not change the current state)
                        //if(mCurrentState!=IDLESTATE)
                        //        xmtRunnable.notify();
                        // }
                    } else {
                        log.error("reply complete in unexpected state: "
                                + mCurrentState + " was " + reply.toString());
                    }
                }
            }
            // Unsolicited message
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Unsolicited Message Received "
                        + reply.toString());
            }

            replyInDispatch = false;
        }
    }

    /*
     * Build a new IEEE802154 Node.
     * @return new IEEE802154Node.
     */
    public jmri.jmrix.ieee802154.IEEE802154Node newNode() {
        return new XBeeNode();
    }

    public void addXBeeListener(XBeeListener l) {
        this.addListener(l);
    }

    public void removeXBeeListener(XBeeListener l) {
        this.addListener(l);
    }

    public void sendXBeeMessage(XBeeMessage m, XBeeListener l) {
        sendMessage(m, l);
    }

    /**
     * This is invoked with messages to be forwarded to the port. It queues
     * them, then notifies the transmission thread.
     */
    @Override
    synchronized protected void sendMessage(AbstractMRMessage m, AbstractMRListener reply) {
        msgQueue.addLast(m);
        listenerQueue.addLast(reply);
        if (m != null) {
            log.debug("just notified transmit thread with message " + m.toString());
        }
    }

    /**
     * Forward a XBeeMessage to all registered XBeeInterface listeners.
     */
    @Override
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {

        try {
            ((XBeeListener) client).message((XBeeMessage) m);
        } catch (java.lang.ClassCastException cce) {
            // try sending as an IEEE message.
            ((IEEE802154Listener) client).message((IEEE802154Message) m);
        }
    }

    /**
     * Forward a reply to all registered IEEE802154Interface listeners.
     */
    @Override
    protected void forwardReply(AbstractMRListener client, AbstractMRReply r) {
        if (client instanceof XBeeListener) {
            ((XBeeListener) client).reply((XBeeReply) r);
        } else {
            // we're using some non-XBee specific code, like the monitor
            // that only registeres as an IEEE802154Listener.
            ((IEEE802154Listener) client).reply((IEEE802154Reply) r);
        }
    }

    // keep track of the XBee Series
    private com.rapplogic.xbee.api.HardwareVersion.RadioType series = com.rapplogic.xbee.api.HardwareVersion.RadioType.UNKNOWN;

    private void setSeries(com.rapplogic.xbee.api.HardwareVersion.RadioType type) {
        series = type;
    }

    // if we are using Series 1 XBees, use wpan classes from the XBee API library
    public boolean isSeries1() {
        return ((!((firmwareVersion[0] & 0xF0) == 0x80))
                && (series == com.rapplogic.xbee.api.HardwareVersion.RadioType.SERIES1
                || series == com.rapplogic.xbee.api.HardwareVersion.RadioType.SERIES1_PRO
                || series == com.rapplogic.xbee.api.HardwareVersion.RadioType.UNKNOWN));
    }

    // if we are using Series 2 XBees, use zigbee classes from the XBee API library
    public boolean isSeries2() {
        return ((firmwareVersion[0] & 0xF0) == 0x80
                || series == com.rapplogic.xbee.api.HardwareVersion.RadioType.SERIES2
                || series == com.rapplogic.xbee.api.HardwareVersion.RadioType.SERIES2_PRO
                || series == com.rapplogic.xbee.api.HardwareVersion.RadioType.SERIES2B_PRO);
    }

    // keep track of the XBee Firmware Version
    private int firmwareVersion[] = {0xFF, 0xFF};

    private void setVersion(int version[]) {
        firmwareVersion[0] = version[0];
        firmwareVersion[1] = version[1];
    }

    /**
     * Public method to identify an XBeeNode from its node identifier
     *
     * @param Name the node identifier search string.
     * @return the node if found, or null otherwise.
     */
    synchronized public jmri.jmrix.AbstractNode getNodeFromName(String Name) {
        log.debug("getNodeFromName called with " + Name);
        for (int i = 0; i < numNodes; i++) {
            XBeeNode node = (XBeeNode) getNode(i);
            if (node.getIdentifier().equals(Name)) {
                return node;
            }
        }
        return (null);
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeTrafficController.class);

}
