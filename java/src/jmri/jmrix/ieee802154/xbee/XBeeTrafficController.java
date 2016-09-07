package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.models.ATCommandResponse;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.packet.XBeePacket;
import com.digi.xbee.api.listeners.IPacketReceiveListener;
import com.digi.xbee.api.listeners.IModemStatusReceiveListener;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.models.ModemStatusEvent;
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
 * @author Paul Bender Copyright (C) 2013,2016
 */
public class XBeeTrafficController extends IEEE802154TrafficController implements IPacketReceiveListener, IModemStatusReceiveListener, IDataReceiveListener, XBeeInterface {

    private XBeeDevice xbee = null;

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
        // Attach XBee to the port
        try {
            if( p instanceof XBeeAdapter) {
               XBeeAdapter xbp = (XBeeAdapter) p;
               xbee = new XBeeDevice(xbp);
               xbee.open();
               xbee.reset(); 
               try {
                  synchronized(this){
                     wait(2000);
                  }
               } catch (java.lang.InterruptedException e) {
               }
               xbee.addPacketListener(this);
               xbee.addModemStatusListener(this);
               xbee.addDataListener(this);

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

            } else {
               throw new java.lang.IllegalArgumentException("Wrong adapter type specified when connecting to the port.");
            }
        } catch (Exception e) {
            log.error("Failed to start up communications. Error was {} cause {} ",e,e.getCause());
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
            xbee.sendPacketAsync(((XBeeMessage) m).getXBeeRequest());
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
        if(node instanceof XBeeNode) {
           super.registerNode(node);
           XBeeNode xbnode= (XBeeNode) node;
           xbnode.setTrafficController(this);
        } else {
           throw new java.lang.IllegalArgumentException("Attempt to register node of incorrect type for this connection");
        }
    }

    // XBee IPacketReceiveListener interface methods
    public void packetReceived(XBeePacket response) {

        log.debug("packetReceived called with {}",response);
        dispatchResponse(response);
    }

    // XBee IModemStatusReceiveListener interface methods
    public void modemStatusEventReceived(ModemStatusEvent modemStatusEvent){
       log.debug("modemStatusEventReceived called with event {} ", modemStatusEvent);
    }

    // XBee IDataReceiveListener interface methods
    public void dataReceived(com.digi.xbee.api.models.XBeeMessage xbm){
       log.debug("dataReceived called with message {} ", xbm);
    }


    private void dispatchResponse(XBeePacket response){
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
        /*Runnable r = new RcvNotifier(reply, mLastSender, this);
        try {
            log.debug("invoking dispatch thread");
            javax.swing.SwingUtilities.invokeAndWait(r);
            log.debug("dispatch thread complete");
        } catch (Exception e) {
            log.error("Unexpected exception in invokeAndWait:" + e);
            log.error("cause:" + e.getCause());
            e.printStackTrace();
        }*/
        if (log.isDebugEnabled()) {
            log.debug("dispatch thread invoked");
        }

        replyInDispatch = false;
        log.debug("Dispatch Complete");
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

    /**
     * Public method to identify an XBeeNode from its node identifier
     *
     * @param Name the node identifier search string.
     * @return the node if found, or null otherwise.
     */
    synchronized public jmri.jmrix.AbstractNode getNodeFromName(String Name) {
        log.debug("getNodeFromName called with {}",Name);
        for (int i = 0; i < numNodes; i++) {
            XBeeNode node = (XBeeNode) getNode(i);
            if (node.getIdentifier().equals(Name)) {
                return node;
            }
        }
        return (null);
    }
 
   /**
     * Public method to identify an XBeeNode from its RemoteXBeeDevice object.
     *
     * @param device the RemoteXBeeDevice to search for.
     * @return the node if found, or null otherwise.
     */
    synchronized public jmri.jmrix.AbstractNode getNodeFromXBeeDevice(RemoteXBeeDevice device) {
        log.debug("getNodeFromXBeeDevice called with {}",device);
        for (int i = 0; i < numNodes; i++) {
            XBeeNode node = (XBeeNode) getNode(i);
            if (node.getXBee().equals(device)) {
                return node;
            }
        }
        return (null);
    }

    /*
     * @return the XBeeDevice associated with this traffic controller.
     */
    public XBeeDevice getXBee(){
        return xbee;
    }


    private final static Logger log = LoggerFactory.getLogger(XBeeTrafficController.class);

}
