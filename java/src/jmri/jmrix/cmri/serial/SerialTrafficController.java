package jmri.jmrix.cmri.serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRNodeTrafficController;

import java.io.DataInputStream;
import java.util.ArrayList;
import jmri.jmrix.cmri.serial.cmrinetmetrics.CMRInetMetricsCollector;
import jmri.jmrix.cmri.serial.cmrinetmetrics.CMRInetMetricsData;

/**
 * Converts Stream-based I/O to/from C/MRI serial messages.
 * <P>
 * The "SerialInterface" side sends/receives message objects.
 * <P>
 * The connection to a SerialPortController is via a pair of *Streams, which
 * then carry sequences of characters for transmission. Note that this
 * processing is handled in an independent thread.
 * <P>
 * This handles the state transistions, based on the necessary state in each
 * message.
 * <P>
 * Handles initialization, polling, output, and input for multiple Serial Nodes.
 *
 * @author	Bob Jacobsen  Copyright (C) 2003
 * @author      Bob Jacobsen, Dave Duchamp, multiNode extensions, 2004
 * @author	Chuck Catania Copyright (C) 2014,2016 CMRInet extensions
 * @version	$Revision: 17977 $
 */
public class SerialTrafficController extends AbstractMRNodeTrafficController implements SerialInterface {

    CMRInetMetricsCollector metricsCollector;

    public SerialTrafficController() {
        super();

        // set node range
        init(0, 127);

        // entirely poll driven, so reduce interval
        mWaitBeforePoll = 5;  // default = 25

    }

    // The methods to implement the SerialInterface
    public synchronized void addSerialListener(SerialListener l) {
        this.addListener(l);
    }

    public synchronized void removeSerialListener(SerialListener l) {
        this.removeListener(l);
    }

    /**
     * Public method to set up for initialization of a Serial node
     */
    public void initializeSerialNode(SerialNode node) {
        synchronized (this) {
            // find the node in the registered node list
            for (int i = 0; i < getNumNodes(); i++) {
                if (getNode(i) == node) {
                    // found node - set up for initialization
                    setMustInit(i, true);
                    return;
                }
            }
        }
    }

    protected AbstractMRMessage enterProgMode() {
        log.warn("enterProgMode doesnt make sense for CMRI serial");
        return null;
    }

    protected AbstractMRMessage enterNormalMode() {
        // can happen during error recovery, null is OK
        return null;
    }

    /**
     * Forward a SerialMessage to all registered SerialInterface listeners.
     */
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((SerialListener) client).message((SerialMessage) m);
    }

    /**
     * Forward a SerialReply to all registered SerialInterface listeners.
     */
    protected void forwardReply(AbstractMRListener client, AbstractMRReply m) {
        ((SerialListener) client).reply((SerialReply) m);
    }

    SerialSensorManager mSensorManager = null;

    int curSerialNodeIndex     = 0;     // cycles over defined nodes when pollMessage is called
    public boolean pollNetwork = true;  // true if network polling enabled
    
    private int initTimeout = 500;
    private int xmitTimeout = 2;
    private int pollTimeout = 2;
    
    // cpNode poll list 
    public ArrayList<Integer> cmriNetPollList = new ArrayList<Integer>();
   
    public void setPollNetwork( boolean OnOff ) { pollNetwork = OnOff; }
    public boolean getPollNetwork() { return pollNetwork; }

    public void setInitTimeout( int init_Timeout ) { initTimeout = init_Timeout; }
    public int getInitTimeout() { return initTimeout; }
    public void setXmitTimeout( int init_XmitTimeout ) { xmitTimeout = init_XmitTimeout; }
    public int getXmitTimeout() { return xmitTimeout; }
    
   /**
     *  Handles initialization, output and polling for C/MRI Serial Nodes
     *      from within the running thread
     */
    protected synchronized AbstractMRMessage pollMessage() {
        // ensure validity of call
        if (getNumNodes() <= 0) {
            return null;
        }

        int previousPollPointer = curSerialNodeIndex;
        updatePollPointer(); // service next node next

        //-------------------------------------
        // ensure that each node is initialized 
        //-------------------------------------
        SerialNode n = (SerialNode) SerialTrafficController.instance().getNode(curSerialNodeIndex);
        if (getMustInit(curSerialNodeIndex)) {
            setMustInit(curSerialNodeIndex, false);
            AbstractMRMessage m = getNode(curSerialNodeIndex).createInitPacket();
            log.debug("send init message: "+m);
            m.setTimeout( getInitTimeout() );  // wait for init to finish in the node (milliseconds)  //c2
//          m.setTimeout(500);  // wait for init to finish (milliseconds)
            n.setPollStatus(n.POLLSTATUS_INIT);
           return m;
        }
        
        //-------------------------------------
        // send Output packet if needed
        //-------------------------------------
        if (getNode(curSerialNodeIndex).mustSend()) {
            log.debug("request write command to send");
            getNode(curSerialNodeIndex).resetMustSend();
            AbstractMRMessage m = getNode(curSerialNodeIndex).createOutPacket();
            m.setTimeout( getXmitTimeout() );  // no need to wait for output to answer
//          m.setTimeout(xmitTimeout);  // no need to wait for output to answer
            // reset poll pointer update, so next increment will poll from here
             curSerialNodeIndex = previousPollPointer;
            return m;
        }
        
        //-------------------------------------
        // Poll node if polling enabled for this node  //c2
        // update polling status for the node
        //-------------------------------------
//      SerialNode n = (SerialNode) SerialTrafficController.instance().getNode(curSerialNodeIndex);
        if (!n.getPollingEnabled()) {
             n.setPollStatus(n.POLLSTATUS_IDLE);
              return null;
         }
        else 
         if ( getNode(curSerialNodeIndex).getSensorsActive() ) {
           if (n.getPollStatus() != n.POLLSTATUS_POLLING)
               n.setPollStatus(n.POLLSTATUS_POLLING);

           // Some sensors are active for this node, issue poll
           SerialMessage m = SerialMessage.getPoll(getNode(curSerialNodeIndex).getNodeAddress());
           return m;
         }
        else
        //------------------
        // no poll required
        //------------------
        {           
            return null;
        }
    }
    
    /**
     * Update the curSerialNodeIndex so next node polled next time
     */
    private void updatePollPointer() {
        curSerialNodeIndex++;
        if (curSerialNodeIndex >= getNumNodes()) {
            curSerialNodeIndex = 0;
        }
    }

    protected synchronized void handleTimeout(AbstractMRMessage m, AbstractMRListener l) {
        // don't use super behavior, as timeout to init, transmit message is normal
        SerialNode n = (SerialNode) SerialTrafficController.instance().getNode(curSerialNodeIndex);  //c2

        // inform node, and if it resets then reinitialize        
        if (n.handleTimeout(m,l)) {
//      if (getNode(curSerialNodeIndex).handleTimeout(m,l)) {
//         SerialNode n = (SerialNode) SerialTrafficController.instance().getNode(curSerialNodeIndex);
         if (n.getPollingEnabled())  //c2 
         {
             n.setPollStatus(n.POLLSTATUS_TIMEOUT);
             CMRInetMetricsData.incMetricErrValue(CMRInetMetricsData.CMRInetMetricTimeout);

         }
//        log.info("Node "+n.getNodeAddress()+" No Response");
           setMustInit(curSerialNodeIndex, true);
        }

    }

    protected synchronized void resetTimeout(AbstractMRMessage m) {
        // don't use super behavior, as timeout to init, transmit message is normal
        // and inform node
        getNode(curSerialNodeIndex).resetTimeout(m);

    }

    protected AbstractMRListener pollReplyHandler() {
        return mSensorManager;
    }

    /**
     * Forward a preformatted message to the actual interface.
     */
    public void sendSerialMessage(SerialMessage m, SerialListener reply) {
        sendMessage(m, reply);
    }

    /**
     * static function returning the SerialTrafficController instance to use.
     *
     * @return The registered SerialTrafficController instance for general use,
     *         if need be creating one.
     */
    static public SerialTrafficController instance() {
        if (self == null) {
            if (log.isDebugEnabled()) {
                log.debug("creating a new SerialTrafficController object");
            }
            self = new SerialTrafficController();
            
            // Start the CMRInet data collection listener
            //-------------------------------------------
            self.metricsCollector = new CMRInetMetricsCollector();
        }
        return self;
    }

    static volatile protected SerialTrafficController self = null;

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "temporary until mult-system; only set at startup")
    protected void setInstance() {
        self = this;
    }

    protected AbstractMRReply newReply() {
        return new SerialReply();
    }

    protected boolean endOfMessage(AbstractMRReply msg) {
        // our version of loadChars doesn't invoke this, so it shouldn't be called
        log.error("Not using endOfMessage, should not be called");
        return false;
    }

    protected void loadChars(AbstractMRReply msg, DataInputStream istream) throws java.io.IOException {
        int i;
        for (i = 0; i < msg.maxSize(); i++) {
            byte char1 = readByteProtected(istream);
            if (char1 == 0x03) {
                break;           // check before DLE handling
            }
            if (char1 == 0x10) {
                char1 = readByteProtected(istream);
            }
            msg.setElement(i, char1 & 0xFF);
        }
    }

    protected void waitForStartOfReply(DataInputStream istream) throws java.io.IOException {
        // loop looking for the start character
        while (readByteProtected(istream) != 0x02) {
        }
    }

    /**
     * Add header to the outgoing byte stream.
     *
     * @param msg The output byte stream
     * @return next location in the stream to fill
     */
    protected int addHeaderToOutput(byte[] msg, AbstractMRMessage m) {
        msg[0] = (byte) 0xFF;
        msg[1] = (byte) 0xFF;
        msg[2] = (byte) 0x02;  // STX
        return 3;
    }

    /**
     * Add trailer to the outgoing byte stream.
     *
     * @param msg    The output byte stream
     * @param offset the first byte not yet used
     */
    protected void addTrailerToOutput(byte[] msg, int offset, AbstractMRMessage m) {
        msg[offset] = 0x03;  // etx
    }

    /**
     * Determine how much many bytes the entire message will take, including
     * space for header and trailer
     *
     * @param m The message to be sent
     * @return Number of bytes
     */
    protected int lengthOfByteStream(AbstractMRMessage m) {
        int len = m.getNumDataElements();
        int cr = 4;
        return len + cr;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTrafficController.class.getName());
}
