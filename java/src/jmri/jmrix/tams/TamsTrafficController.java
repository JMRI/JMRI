// TamsTrafficController.java
package jmri.jmrix.tams;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import jmri.CommandStation;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;
import jmri.jmrix.tams.swing.monitor.TamsMonPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from Tams messages. The "TamsInterface" side
 * sends/receives message objects.
 * <P>
 * The connection to a TamsPortController is via a pair of Streams, which then
 * carry sequences of characters for transmission. Note that this processing is
 * handled in an independent thread.
 * <P>
 * This handles the state transitions, based on the necessary state in each
 * message.
 *
 * Based on work by Bob Jacobsen and Kevin Dickerson
 *
 * @authors Jan Boen
 * @version $Revision: 160524 $
 */
public class TamsTrafficController extends AbstractMRTrafficController implements TamsInterface, CommandStation {

    public TamsTrafficController() {
        super();
        if (log.isDebugEnabled()) {
            log.debug("creating a new TamsTrafficController object");
        }
        // set as command station too
        jmri.InstanceManager.setCommandStation(this);
        this.setAllowUnexpectedReply(false);

    }

    public void setAdapterMemo(TamsSystemConnectionMemo memo) {
        adaptermemo = memo;
        //log.debug("setAdapterMemo method");
    }

    TamsSystemConnectionMemo adaptermemo;

    public String getUserName() {
        if (adaptermemo == null) {
            return "Tams";
        }
        return adaptermemo.getUserName();
    }

    public String getSystemPrefix() {
        if (adaptermemo == null) {
            return "TM";
        }
        return adaptermemo.getSystemPrefix();
    }

    // The methods to implement the TamsInterface
    public synchronized void addTamsListener(TamsListener l) {
        this.addListener(l);
    }

    public synchronized void removeTamsListener(TamsListener l) {
        this.removeListener(l);
    }

    @Override
    protected int enterProgModeDelayTime() {
        // we should to wait at least a second after enabling the programming
        // track
        return 1000;
    }

    /**
     * CommandStation implementation
     */
    public void sendPacket(byte[] packet, int count) {
        //log.debug("*** sendPacket ***");
    }

    /**
     * Forward a TamsMessage to all registered TamsInterface listeners.
     */
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        //log.debug("*** forwardMessage ***");
        //This also forwards the messages to the Tams Monitor etc
        ((TamsListener) client).message((TamsMessage) m);
    }

    //Create a local TamsMessage Queue which we will use in combination with TamsReplies
    private static Queue<TamsMessage> tmq = new LinkedList<TamsMessage>();
        
   TamsMessage tm;//create a new local variable that will hold a copy of the latest TamsMessage

    /**
     * Forward a TamsReply to the appropriate TamsInterface listeners.
     */
    protected void forwardReply(AbstractMRListener client, AbstractMRReply tr) {
        //log.debug("*** forwardReply ***");
        //log.debug("Client = " + client);
        //log.debug("TamsMessage = " + jmri.util.StringUtil.appendTwoHexFromInt(tm.getElement(0) & 0xFF, "") + " " + jmri.util.StringUtil.appendTwoHexFromInt(tm.getElement(1) & 0xFF, "") + " and replyType = " + tm.getReplyType());
        //Only forward messages to the correct listener
        if ((client instanceof TamsPowerManager && tm.getReplyType() == 'P') ||
                (client instanceof TamsThrottle && tm.getReplyType() == 'L') ||
                (client instanceof TamsTurnout && tm.getReplyType() == 'T' && !tm.isBinary()) ||
                (client instanceof TamsTurnoutManager && tm.getReplyType() == 'T' && tm.isBinary()) ||
                (client instanceof TamsSensorManager && tm.getReplyType() == 'S') || 
                client instanceof TamsMonPane) {
            ((TamsListener)client).reply((TamsReply) tr);
        }
    }

    /**
     * Poll Message Handler
     */

    static class PollMessage {//also tried with removed static keyword, see no difference

        TamsListener tl;
        TamsMessage tm;

        PollMessage(TamsMessage tm, TamsListener tl) {
            //log.debug("*** PollMessage ***");
            this.tm = tm;
            this.tl = tl;
        }

        TamsListener getListener() {
            return tl;
        }

        TamsMessage getMessage() {
            return tm;
        }
    }

    ConcurrentLinkedQueue<PollMessage> pollQueue = new ConcurrentLinkedQueue<PollMessage>();

    boolean disablePoll = false;

    public boolean getPollQueueDisabled() {
        return disablePoll;
    }

    public void setPollQueueDisabled(boolean poll) {
        disablePoll = poll;
    }

    /**
     * As we have to poll the Tams MC system to get updates we put request into
     * a queue and allow the abstract traffic controller to handle them when it
     * is free
     */
    public void addPollMessage(TamsMessage tm, TamsListener tl) {
        //log.debug("*** addPollMessage ***");
        tm.setTimeout(100);
        boolean found = false;
        for (PollMessage pm : pollQueue) {
            //log.debug ("comparing poll messages: " + pm.getMessage().toString() + " " + tm.toString());
            if (pm.getListener() == tl && pm.getMessage().toString().equals(tm.toString())) {
                log.debug("Message is already in the poll queue so will not add");
                found = true;
            }
        }
        if (!found){
            //log.debug("Added to poll queue = " + tm.toString());
            PollMessage pm = new PollMessage(tm, tl);
            pollQueue.offer(pm);
        }
    }

    /**
     * Removes a message that is used for polling from the queue
     */
    public void removePollMessage(TamsMessage tm, TamsListener tl) {
        //log.debug("*** removePollMessage ***");
        for (PollMessage pm : pollQueue) {
            if (pm.getListener() == tl && pm.getMessage().toString().equals(tm.toString())) {
                pollQueue.remove(pm);
            }
        }
    }

    /**
     * Check Tams MC for status updates
     */

    protected TamsMessage pollMessage() {
        //log.debug("*** pollMessage ***");
        if (disablePoll) {
            //log.debug("Nothing in the Poll Queue");
            return null;
        }
        if (!pollQueue.isEmpty()) {
            PollMessage pm = pollQueue.peek();
            if (pm != null) {
                //log.debug("PollMessage = " + pm.getMessage().toString());
                tm = pm.getMessage();
                return pm.getMessage();
            }
        }
        return null;
    }

    protected AbstractMRListener pollReplyHandler() {
        //log.debug("*** pollReplyHandler ***");
        if (disablePoll) {
            return null;
        }
        if (!pollQueue.isEmpty()) {
            PollMessage pm = pollQueue.poll();
            if (pm != null) {
                pollQueue.offer(pm);
                tm = pm.getMessage();
                return pm.getListener();
            }
        }
        return null;
    }

    /**
     * Forward a pre-formatted message to the actual interface.
     */
    public void sendTamsMessage(TamsMessage m, TamsListener tl) {
        //log.debug("*** TamsMessage ***");
        tm = m;
        tmq.offer(tm);
        //log.debug("Length of TamsMessage Queue: " + tmq.size());
        if (tm.isBinary()) {
            //log.debug("Binary TamsMessage = " + jmri.util.StringUtil.appendTwoHexFromInt(tm.getElement(0) & 0xFF, "") + " " + jmri.util.StringUtil.appendTwoHexFromInt(tm.getElement(1) & 0xFF, "") + " and replyType = " + tm.getReplyType());
        } else {
            //log.debug("ASCII TamsMessage = " + tm.toString() + " and replyType = " + tm.getReplyType());
        }
        sendMessage(tm, tl);
    }

    protected boolean unsolicitedSensorMessageSeen = false;

    protected TamsMessage enterProgMode() {
        return null;
    }

    protected TamsMessage enterNormalMode() {
        return null;
    }

    //This can be removed once multi-connection is complete
    @Override
    public void setInstance() {
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "MS_PKGPROTECT")
    // FindBugs wants this package protected, but we're removing it when multi-connection
    // migration is complete
    final static protected TamsTrafficController self = null;

    /**
     * Add trailer to the outgoing byte stream.
     *
     * @param msg The output byte stream
     * @param offset the first byte not yet used
     */
    protected void addTrailerToOutput(byte[] msg, int offset, TamsMessage m) {
        //log.debug("*** addTrailerToOutput ***");
        if (!m.isBinary()) {// Activated this in case the output is not binary
            msg[offset] = 0x0d;
        }
    }

    /**
     * Determine how many bytes the entire message will take, including space
     * for header and trailer
     *
     * @param m The message to be sent
     * @return Number of bytes
     */
    protected int lengthOfByteStream(TamsMessage m) {
        //log.debug("*** lengthOfByteStream ***");
        int len = m.getNumDataElements();
        int cr = 0;
        if (!m.isBinary())
            cr = 1; // space for return
        //log.debug("length ByteStream = " + (len + cr) + ", message = |" + m + "|");
        return len + cr;
    }

    // The reply part
    protected int myCounter = 0; //Helper variable used to count the number of iterations
    protected int groupSize = 0; //Helper variable used to determine how many bytes are present in each reply nibble
    protected boolean endReached = false; //Helper variable used to indicate we reached the end of the message
    protected int numberOfNibbles = 0; //Helper variable used to calculate how many message nibbles there are in the reply
    protected int messageLength = 0; //Helper variable used hold the length of the message
    protected int index = 0; //Helper variable used keep track of where we are in the message
    
    protected TamsReply newReply() {
        //log.debug("*** TamsReply ***");
        //log.debug("myCounter = " + myCounter);
        TamsReply reply = new TamsReply();
        if (!tmq.isEmpty()){
            tm = tmq.peek();
            if (tm.isBinary()) {
                //log.debug("Binary TamsMessage = " + jmri.util.StringUtil.appendTwoHexFromInt(tm.getElement(0) & 0xFF, "") + " " + jmri.util.StringUtil.appendTwoHexFromInt(tm.getElement(1) & 0xFF, "") + " and replyType = " + tm.getReplyType());
            } else {
                //log.debug("ASCII TamsMessage = " + tm.toString() + " and replyType = " + tm.getReplyType());
            }
        } else {
            //log.debug("No TamsMessages in the queue!");
        }
        if (tm != null){//Only when there is a valid TamsMessage
            if (tm.isBinary()) {//Binary reply so must makes sure the reply get initialized as ArrayList of integers
                //log.debug("Binary TamsMessage = " + jmri.util.StringUtil.appendTwoHexFromInt(tm.getElement(0) & 0xFF, "") + " " + jmri.util.StringUtil.appendTwoHexFromInt(tm.getElement(1) & 0xFF, "") + " and replyType = " + tm.getReplyType() + " and isBinary = " + tm.isBinary());
                //reply.setBinary(true);;
            } else {//ASCII reply so just return the string
                //log.debug("ASCII TamsMessage = " + tm.toString() + " and replyType = " + tm.getReplyType() + " and isBinary = " + tm.isBinary());
                //reply.setBinary(false);;
            }
        }
        return reply;
    }

    // Has the message been completely received?
    // The length depends on the message type
    protected boolean endOfMessage(AbstractMRReply reply) {
        TamsReply tr = (TamsReply)reply;
        //log.debug("*** endOfMessage ***");
        if (!tmq.isEmpty()){
            tm = tmq.peek();
            if (tm != null){//Only when there is a valid TamsMessage
                if (tm.isBinary()) {//Binary reply so must makes sure the reply get initialized as ArrayList of integers
                    //log.debug("Binary TamsMessage = " + jmri.util.StringUtil.appendTwoHexFromInt(tm.getElement(0) & 0xFF, "") + " " + jmri.util.StringUtil.appendTwoHexFromInt(tm.getElement(1) & 0xFF, "") + " and replyType = " + tm.getReplyType() + " and isBinary = " + tm.isBinary());
                } else {//ASCII reply so just return the string
                    //log.debug("ASCII TamsMessage = " + tm.toString() + " and replyType = " + tm.getReplyType() + " and isBinary = " + tm.isBinary());
                }
            }
        } else {
            //log.debug("No TamsMessages in the queue!");
        }
        // Input is a continuous stream of characters and we must chop them up into separate messages
        index = tr.getNumDataElements() - 1;
        //log.debug("Reading byte number = " + tr.getNumDataElements() + ", value = " + jmri.util.StringUtil.appendTwoHexFromInt(tr.getElement(index) & 0xFF, ""));
        if (tm.isBinary()) {// Binary reply
            if (tm.getReplyOneByte()) {// Single byte reply
                if (tr.getNumDataElements() < 1) {// Read one byte reply
                    endReached = false;
                } else {
                    //log.debug("One byte binary reply = " + jmri.util.StringUtil.appendTwoHexFromInt(tr.getElement(index) & 0xFF, ""));
                    myCounter = 0;
                    endReached = true;
                }
            } else {// Multi byte reply
                // Read multiple byte reply, until expected last byte
                // Sensor reply
                if (tm.getReplyType() == 'S'){
                    // Sensor replies are grouped per 3 (AA BB CC) when a new group has 0x00 as AA then this is the end of the message
                    // BUT 0x00 is also a valid byte in the 2 data bytes (BB CC) of a sensor read
                    //log.debug("*** Receiving Sensor Reply ***");
                    groupSize = 3;
                    //log.debug("Looking for byte# = " + (groupSize * myCounter + 1) + " and index = " + index + " and expect as last byte = " + tm.getReplyLastByte());
                    if (tr.getNumDataElements() == (groupSize * myCounter + 1) && tr.getElement(index) == tm.getReplyLastByte()){
                        myCounter = 0;
                        endReached = true;
                        //log.debug("S - End reached!");
                        
                    } else {
                        if (tr.getNumDataElements() == (groupSize * myCounter + 1)){
                            myCounter++;
                        }
                        endReached = false;
                    }
                }
                // Turnout reply
                if (tm.getReplyType() == 'T'){
                    // The first byte of a reply can be 0x00 or hold the value of the number messages that will follow
                    // Turnout replies are grouped per 2 (AA BB) 
                    // 0x00 is also a valid byte in the 2 data bytes (AA BB) of a turnout read
                    //log.debug("*** Receiving Turnout Reply ***");
                    numberOfNibbles = tr.getElement(0);
                    if (numberOfNibbles > 50) {
                        numberOfNibbles = 50;
                    }
                    messageLength = numberOfNibbles * 2;
                    //log.debug("Number of turnout events# = " + numberOfNibbles);
                    if (myCounter < messageLength){
                        //log.debug("myCounter = " + myCounter + ", reply length= " + tr.getNumDataElements());
                        myCounter++;
                        endReached = false;
                    } else {
                        myCounter = 0;
                        endReached = true;
                        //log.debug("myCounter = " + myCounter);
                        //log.debug("T - End reached!");
                    }
                }
                // Loco reply
                if (tm.getReplyType() == 'L'){
                    // The first byte of a reply can be 0x80 or if different messages will follow, 0x80 will be the last byte
                    // Loco replies are grouped per 5 (AA BB CC DD EE) 
                    // Anything is a valid byte in the 5 data bytes (AA BB CC DD EE) of a Loco read
                    //log.debug("*** Receiving Loco Reply ***");
                    //log.debug("Current byte = " + jmri.util.StringUtil.appendTwoHexFromInt(tr.getElement(index) & 0xFF, ""));
                    groupSize = 5;
                    if (((tr.getElement(index) & 0xFF) == TamsConstants.EOM80)){
                        myCounter = 0;
                        endReached = true;
                        if (index > 1){//OK we have a real message
                            //log.debug("reply = " + jmri.util.StringUtil.appendTwoHexFromInt(tr.getElement(0) & 0xFF, "") + " " + jmri.util.StringUtil.appendTwoHexFromInt(tr.getElement(1) & 0xFF, "") + " " + jmri.util.StringUtil.appendTwoHexFromInt(tr.getElement(2) & 0xFF, "") + " " + jmri.util.StringUtil.appendTwoHexFromInt(tr.getElement(3) & 0xFF, "") + " " + jmri.util.StringUtil.appendTwoHexFromInt(tr.getElement(4) & 0xFF, ""));
                        }
                        //log.debug("L - End reached!");
                    } else {
                        if (tr.getNumDataElements() == (groupSize * myCounter + 1)){
                            myCounter++;
                        }
                        endReached = false;
                    }
                }
            }
        } else {// ASCII reply
            if (tr.getNumDataElements() > 0 && tr.getElement(index) != 0x5d) {// Read ASCII reply, last is [
                //log.debug("Building ASCII reply = " + tr);
                //myCounter++;
                endReached = false;
            } else {
                _isBinary = tr.isBinary();
                //log.debug("ASCII reply = " + tr.toString() + " isBinary = " + _isBinary);
                //tm = tr.getSource();
                //log.debug("Source = " + tm.toString() + " isBinary = " + tm.isBinary());
                myCounter = 0;
                endReached = true;
            }
        }
        //log.debug("End of Message = " + endReached);
        if (endReached){
            if(!tmq.isEmpty()){
                //log.debug("Going to remove this message: " + tmq.peek().toString());
                tmq.poll();
            }
            if(!tmq.isEmpty()){
                //log.debug("This message is at the head: " + tmq.peek().toString());
            } else {
                //log.debug("The queue is empty");
            }
        }
        return endReached;
    }

    // Override the finalize method for this class
    public boolean sendWaitMessage(TamsMessage m, AbstractMRListener reply) {
        //log.debug("*** sendWaitMessage ***");
        if (log.isDebugEnabled()) {
            //log.debug("Send a message and wait for the response");
        }
        if (ostream == null) {
            return false;
        }
        m.setTimeout(500);// was 500
        m.setRetries(10);// was 10
        synchronized (this) {
            forwardToPort(m, reply);
            // wait for reply
            try {
                if (xmtRunnable != null) {
                    synchronized (xmtRunnable) {
                        xmtRunnable.wait(m.getTimeout());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // retain if needed later
                log.error("transmit interrupted");
                return false;
            }
        }
        return true;
    }

    // mode accessors
    private boolean _isBinary;
    
    // display format
    protected int[] _dataChars = null;

    // display format
    // contents (private)
    protected int _nDataChars = 0;

    // display format
    public String toString() {
        String s = "";
        for (int i = 0; i < _nDataChars; i++) {
            if (_isBinary) {
                if (i != 0) {
                    s += " ";
                }
                s = jmri.util.StringUtil.appendTwoHexFromInt(_dataChars[i] & 0xFF, s);
            } else {
                s += (char) _dataChars[i];
            }
        }
        return s;
    }


    static Logger log = LoggerFactory.getLogger(TamsTrafficController.class.getName());

}

/* @(#)TamsTrafficController.java */
