package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.listeners.IModemStatusReceiveListener;
import com.digi.xbee.api.listeners.IPacketReceiveListener;
import com.digi.xbee.api.models.ModemStatusEvent;
import com.digi.xbee.api.packet.XBeeAPIPacket;
import com.digi.xbee.api.packet.XBeePacket;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
 * @author Paul Bender Copyright (C) 2013, 2016
 */
public class XBeeTrafficController extends IEEE802154TrafficController implements IPacketReceiveListener, IModemStatusReceiveListener, IDataReceiveListener, XBeeInterface {

    private XBeeDevice xbee = null;

    public XBeeTrafficController() {
        super();
    }

    /**
     * Get a message of a specific length for filling in.
     * <p>
     * This is a default, null implementation, which must be overridden in an
     * adapter-specific subclass.
     */
    @Override
    public IEEE802154Message getIEEE802154Message(int length) {
        return null;
    }

    /**
     * Get a message of zero length.
     */
    @Override
    protected AbstractMRReply newReply() {
        return new XBeeReply();
    }

    /**
     * Make connection to an existing PortController object.
     */
    @Override
    public void connectPort(AbstractPortController p) {
        // Attach XBee to the port
        try {
            if( p instanceof XBeeAdapter) {
               configureLocalXBee((XBeeAdapter) p);
               resetLocalXBee();
            } else {
               throw new java.lang.IllegalArgumentException("Wrong adapter type specified when connecting to the port.");
            }
        } catch (TimeoutException te) {
            log.error("Timeout during communication with Local XBee on communication start up. Error was {} ",te.getCause(), te);
        } catch (XBeeException xbe ) {
            log.error("Exception during XBee communication start up. Error was {} ",xbe.getCause(), xbe);
        }
        startTransmitThread();
    }

    private void startTransmitThread() {
        xmtThread = jmri.util.ThreadingUtil.newThread(
                xmtRunnable = () -> {
                    try {
                        transmitLoop();
                    } catch (Throwable e) {
                        if (!threadStopRequest) log.error("Transmit thread terminated prematurely by: {}", e, e);
                    }
                });

        String[] packages = this.getClass().getName().split("\\.");
        xmtThread.setName(
                (packages.length>=2 ? packages[packages.length-2]+"." :"")
                        +(packages.length>=1 ? packages[packages.length-1] :"")
                        +" Transmit thread");

        xmtThread.setDaemon(true);
        xmtThread.setPriority(Thread.MAX_PRIORITY-1);      //bump up the priority
        xmtThread.start();
    }

    private void configureLocalXBee(XBeeAdapter p) throws XBeeException {
        xbee = new XBeeDevice(p);
        xbee.open();
        xbee.setReceiveTimeout(200);
        xbee.addPacketListener(this);
        xbee.addModemStatusListener(this);
        xbee.addDataListener(this);
    }

    @SuppressFBWarnings(value = {"UW_UNCOND_WAIT", "WA_NOT_IN_LOOP"}, justification="The unconditional wait outside of a loop is used to allow the hardware to react to a reset request.")
    private void resetLocalXBee() throws XBeeException {
        xbee.reset();
        try {
           synchronized(this){
              wait(2000);
           }
        } catch (InterruptedException e) {
            log.debug("timeout interupted after reset request");
        }
    }

    /**
     * Actually transmit the next message to the port.
     */
    @Override
    synchronized protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        log.trace("forwardToPort message: [{}]", m);
        if (log.isDebugEnabled()) {
            log.debug("forwardToPort message: [{}]", m);
        }
        if (!(m instanceof XBeeMessage))
        {
            throw new IllegalArgumentException();
        }

        XBeeMessage xbm = (XBeeMessage) m;

        // remember who sent this
        mLastSender = reply;

        // forward the message to the registered recipients,
        // which includes the communications monitor, except the sender.
        // Schedule notification via the Swing event queue to ensure order
        Runnable r = new XmtNotifier(m, mLastSender, this);
        javax.swing.SwingUtilities.invokeLater(r);

        sendWithErrorHandling(xbm);
    }

    private void sendWithErrorHandling(XBeeMessage xbm) {
       /* TODO: Check to see if we need to do any of the error handling
          in AbstractMRTrafficController here */
       // forward using XBee Specific message format
       try {
           log.trace("Sending message {}", xbm);
           sendXBeePacketAsync(xbm.getXBeeRequest());
       } catch (XBeeException xbe) {
           log.error("Error Sending message to XBee {}", xbe,xbe);
       }
    }

    private void sendXBeePacketAsync(XBeeAPIPacket xBeeAPIPacket) throws XBeeException {
        log.trace("Sending XBeeAPIPacket +{}",xBeeAPIPacket.toPrettyString());
        xbee.sendPacketAsync(xBeeAPIPacket);
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
     * Register a node.
     */
    @Override
    public void registerNode(jmri.jmrix.AbstractNode node) {
        if(node instanceof XBeeNode) {
           super.registerNode(node);
           XBeeNode xbnode = (XBeeNode) node;
           xbnode.setTrafficController(this);
        } else {
           throw new java.lang.IllegalArgumentException("Attempt to register node of incorrect type for this connection");
        }
    }

    @SuppressFBWarnings(value="VO_VOLATILE_INCREMENT", justification="synchronized method provides locking")
    public synchronized void deleteNode(XBeeNode node) {
        // find the serial node
        int index = 0;
        for (int i = 0; i < numNodes; i++) {
            if (nodeArray[i] == node) {
                index = i;
            }
        }
        if (index == curSerialNodeIndex) {
            log.warn("Deleting the serial node active in the polling loop");
        }
        // Delete the node from the node list
        numNodes--;
        if (index < numNodes) {
            // did not delete the last node, shift
            for (int j = index; j < numNodes; j++) {
                nodeArray[j] = nodeArray[j + 1];
            }
        }
        nodeArray[numNodes] = null;
        // remove this node from the network too.
        getXBee().getNetwork().addRemoteDevice(node.getXBee());
    }

    // XBee IPacketReceiveListener interface methods

    @Override
    public void packetReceived(XBeePacket response) {
        // because of the XBee library architecture, we don't
        // do anything here with the responses.
        log.debug("packetReceived called with {}", response);
    }

    // XBee IModemStatusReceiveListener interface methods

    @Override
    public void modemStatusEventReceived(ModemStatusEvent modemStatusEvent){
        // because of the XBee library architecture, we don't
        // do anything here with the responses.
        log.debug("modemStatusEventReceived called with event {} ", modemStatusEvent);
    }

    // XBee IDataReceiveListener interface methods

    @Override
    public void dataReceived(com.digi.xbee.api.models.XBeeMessage xbm){
        // because of the XBee library architecture, we don't
        // do anything here with the responses.
        log.debug("dataReceived called with message {} ", xbm);
    }

    /*
     * Build a new IEEE802154 Node.
     *
     * @return new IEEE802154Node
     */
    @Override
    public jmri.jmrix.ieee802154.IEEE802154Node newNode() {
        return new XBeeNode();
    }

    @Override
    public void addXBeeListener(XBeeListener l) {
        this.addListener(l);
    }

    @Override
    public void removeXBeeListener(XBeeListener l) {
        this.addListener(l);
    }

    @Override
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
            log.debug("just notified transmit thread with message {}", m);
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
     * Forward a reply to all registered XBeeInterface listeners.
     */
    @Override
    protected void forwardReply(AbstractMRListener client, AbstractMRReply r) {
        if (client instanceof XBeeListener) {
            ((XBeeListener) client).reply((XBeeReply) r);
        } else {
            // we're using some non-XBee specific code, like the monitor
            // that only registers as an IEEE802154Listener.
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
            // examine the addresses of the two XBee Devices to see
            // if they are the same.
            RemoteXBeeDevice nodeXBee = node.getXBee();
            if(nodeXBee.get16BitAddress().equals(device.get16BitAddress())
               && nodeXBee.get64BitAddress().equals(device.get64BitAddress())) {
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

    @Override
    protected void terminate(){
       if(xbee!=null) {
          terminateThreads();
          xbee.close();
          xbee=null;
       }
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeTrafficController.class);

}
