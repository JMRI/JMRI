package jmri.jmrix.tams;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import jmri.CommandStation;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;
import jmri.jmrix.tams.swing.monitor.TamsMonPane;
import jmri.util.StringUtil;
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
 * <p>
 * Based on work by Bob Jacobsen and Kevin Dickerson
 *
 * @author Jan Boen
 */
public class TamsTrafficController extends AbstractMRTrafficController implements TamsInterface, CommandStation {

    public TamsTrafficController() {
        super();
        log.debug("creating a new TamsTrafficController object");
        // set as command station too
        jmri.InstanceManager.setCommandStation(this);
        super.setAllowUnexpectedReply(false);

    }

    public void setAdapterMemo(TamsSystemConnectionMemo memo) {
        adaptermemo = memo;
        log.trace("setAdapterMemo method");
    }

    TamsSystemConnectionMemo adaptermemo;

    @Override
    public String getUserName() {
        if (adaptermemo == null) {
            return "Tams";
        }
        return adaptermemo.getUserName();
    }

    @Override
    public String getSystemPrefix() {
        if (adaptermemo == null) {
            return "TM";
        }
        return adaptermemo.getSystemPrefix();
    }

    // The methods to implement the TamsInterface
    @Override
    public synchronized void addTamsListener(TamsListener l) {
        this.addListener(l);
    }

    @Override
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
     * CommandStation implementation.
     *
     * @param packet ignored, but needed for API compatibility
     * @param count  ignored, but needed for API compatibility
     */
    @Override
    public void sendPacket(byte[] packet, int count) {
        log.trace("*** sendPacket ***");
    }

    /**
     * Forward a TamsMessage to all registered TamsInterface listeners.
     *
     * @param client the listener, may throw an uncaught exception if not a
     *               TamsListner
     * @param m      the message, may throw an uncaught exception if not a
     *               TamsMessage
     */
    @Override
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        log.trace("*** forwardMessage ***");
        //This also forwards the messages to the Tams Monitor etc
        ((TamsListener) client).message((TamsMessage) m);
    }

    //Create a local TamsMessage Queue which we will use in combination with TamsReplies
    private static final Queue<TamsMessage> tmq = new LinkedList<TamsMessage>();

    TamsMessage tm;//create a new local variable that will hold a copy of the latest TamsMessage

    /**
     * Forward a TamsReply to the appropriate TamsInterface listeners.
     *
     * @param client the listener for the TamsInterface
     * @param tr     the message to forward
     */
    @Override
    protected void forwardReply(AbstractMRListener client, AbstractMRReply tr) {
        log.trace("*** forwardReply ***");
        log.debug("Client = {}", client);
        if (log.isTraceEnabled()) {
            log.trace("TamsMessage = {} {} and replyType = {}", StringUtil.appendTwoHexFromInt(tm.getElement(0) & 0xFF, ""), StringUtil.appendTwoHexFromInt(tm.getElement(1) & 0xFF, ""), tm.getReplyType());
        }
        //Only forward binary messages to the correct listener
        if (tm.isBinary()
                && (client instanceof TamsPowerManager && tm.getReplyType() == 'P')
                || (client instanceof TamsThrottle && tm.getReplyType() == 'L')
                || (client instanceof TamsTurnoutManager && tm.getReplyType() == 'T')
                || (client instanceof TamsSensorManager && tm.getReplyType() == 'S')) {
            log.debug("Forward binary message");
            ((TamsListener) client).reply((TamsReply) tr);
        }
        //Forward ASCII messages to the TamsTurnout
        if (tm.isBinary() && (client instanceof TamsTurnout && tm.getReplyType() == 'T')) {
            ((TamsListener) client).reply((TamsReply) tr);
            log.debug("Forward ASCII Turnout message");
        }
        //Forward ASCII messages to all listeners except those define above nor the TamsMonPane
        if (!(client instanceof TamsMonPane) && !tm.isBinary()
                && !((tm.getReplyType() == 'P') || (tm.getReplyType() == 'L') || (tm.getReplyType() == 'S') || (tm.getReplyType() == 'T'))) {
            log.debug("Forward ACSII message to other listeners");
            ((TamsListener) client).reply((TamsReply) tr);
        }
        //Forward all messages to Monitor Panel
        if (client instanceof TamsMonPane) {
            log.debug("Forward any message to Monitor Panel");
            ((TamsListener) client).reply((TamsReply) tr);
        }

    }

    /**
     * Poll Message Handler.
     */
    static class PollMessage {

        TamsListener tl;
        TamsMessage tm;

        PollMessage(TamsMessage tm, TamsListener tl) {
            log.trace("*** PollMessage ***");
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

    ConcurrentLinkedQueue<PollMessage> pollQueue = new ConcurrentLinkedQueue<>();

    boolean disablePoll = false;

    public boolean getPollQueueDisabled() {
        return disablePoll;
    }

    public void setPollQueueDisabled(boolean poll) {
        disablePoll = poll;
    }

    /**
     * As we have to poll the Tams MC system to get updates, we put request into
     * a queue and allow the abstract traffic controller to handle requests when
     * it is free.
     *
     * @param tm the message to queue
     * @param tl the listener to monitor the message and its reply
     */
    public void addPollMessage(TamsMessage tm, TamsListener tl) {
        log.trace("*** addPollMessage ***");
        tm.setTimeout(100);
        boolean found = false;
        for (PollMessage pm : pollQueue) {
            log.trace("comparing poll messages: {} {}", pm.getMessage(), tm);
            if (pm.getListener() == tl && pm.getMessage().toString().equals(tm.toString())) {
                log.debug("Message is already in the poll queue so will not add");
                found = true;
            }
        }
        if (!found) {
            log.trace("Added to poll queue = {}", tm);
            PollMessage pm = new PollMessage(tm, tl);
            pollQueue.offer(pm);
        }
    }

    /**
     * Remove a message that is used for polling from the queue.
     *
     * @param tm the message to remove
     * @param tl the listener waiting for the reply to the message
     */
    public void removePollMessage(TamsMessage tm, TamsListener tl) {
        log.trace("*** removePollMessage ***");
        for (PollMessage pm : pollQueue) {
            if (pm.getListener() == tl && pm.getMessage().toString().equals(tm.toString())) {
                pollQueue.remove(pm);
            }
        }
    }

    /**
     * Check Tams MC for status updates.
     *
     * @return the next available message
     */
    @Override
    protected TamsMessage pollMessage() {
        log.trace("*** pollMessage ***");
        if (disablePoll) {
            log.trace("Nothing in the Poll Queue");
            return null;
        }
        if (!pollQueue.isEmpty()) {
            PollMessage pm = pollQueue.peek();
            if (pm != null) {
                log.trace("PollMessage = {}", pm.getMessage());
                tm = pm.getMessage();
                return pm.getMessage();
            }
        }
        return null;
    }

    @Override
    protected AbstractMRListener pollReplyHandler() {
        log.trace("*** pollReplyHandler ***");
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
     *
     * @param m  the message to forward
     * @param tl the listener for the reply to the messageF
     */
    @Override
    public void sendTamsMessage(TamsMessage m, TamsListener tl) {
        log.trace("*** TamsMessage ***");
        tm = m;
        tmq.offer(tm);
        log.trace("Length of TamsMessage Queue: {}", tmq.size());
        if (log.isTraceEnabled()) {
            if (tm.isBinary()) {
                log.trace("Binary TamsMessage = {} {} and replyType = {}", StringUtil.appendTwoHexFromInt(tm.getElement(0) & 0xFF, ""), StringUtil.appendTwoHexFromInt(tm.getElement(1) & 0xFF, ""), tm.getReplyType());
            } else {
                log.trace("ASCII TamsMessage = {} and replyType = {}", tm, tm.getReplyType());
            }
        }
        sendMessage(tm, tl);
    }

    protected boolean unsolicitedSensorMessageSeen = false;

    @Override
    protected TamsMessage enterProgMode() {
        return null;
    }

    @Override
    protected TamsMessage enterNormalMode() {
        return null;
    }

    //This can be removed once multi-connection is complete
    @Override
    public void setInstance() {
    }

    @SuppressFBWarnings(value = "MS_PKGPROTECT")
    // FindBugs wants this package protected, but we're removing it when multi-connection
    // migration is complete
    final static protected TamsTrafficController self = null;

    /**
     * Add trailer to the outgoing byte stream.
     *
     * @param msg    the output byte stream
     * @param offset the first byte not yet used
     * @param m      the message in the byte stream
     */
    protected void addTrailerToOutput(byte[] msg, int offset, TamsMessage m) {
        log.trace("*** addTrailerToOutput ***");
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
        log.trace("*** lengthOfByteStream ***");
        int len = m.getNumDataElements();
        int cr = 0;
        if (!m.isBinary()) {
            cr = 1; // space for return
        }
        log.trace("length ByteStream = {}, message = |{}|", len + cr, m);
        return len + cr;
    }

    // The reply part
    protected int myCounter = 0; //Helper variable used to count the number of iterations
    protected int groupSize = 0; //Helper variable used to determine how many bytes are present in each reply nibble
    protected boolean endReached = false; //Helper variable used to indicate we reached the end of the message
    protected int numberOfNibbles = 0; //Helper variable used to calculate how many message nibbles there are in the reply
    protected int messageLength = 0; //Helper variable used hold the length of the message
    protected int index = 0; //Helper variable used keep track of where we are in the message

    @Override
    protected TamsReply newReply() {
        log.trace("*** TamsReply ***");
        log.trace("myCounter = {}", myCounter);
        TamsReply reply = new TamsReply();
        if (!tmq.isEmpty()) {
            tm = tmq.peek();
            if (log.isTraceEnabled()) {
                if (tm.isBinary()) {
                    log.trace("Binary TamsMessage = " + StringUtil.appendTwoHexFromInt(tm.getElement(0) & 0xFF, "") + " " + StringUtil.appendTwoHexFromInt(tm.getElement(1) & 0xFF, "") + " and replyType = " + tm.getReplyType());
                } else {
                    log.trace("ASCII TamsMessage = " + tm.toString() + " and replyType = " + tm.getReplyType());
                }
            }
        } else {
            log.trace("No TamsMessages in the queue!");
        }
        if (tm != null && log.isTraceEnabled()) { //Only when there is a valid TamsMessage
            if (tm.isBinary()) { //Binary reply so must makes sure the reply get initialized as ArrayList of integers
                log.trace("Binary TamsMessage = {} {} and replyType = {} and isBinary = {}", StringUtil.appendTwoHexFromInt(tm.getElement(0) & 0xFF, ""), StringUtil.appendTwoHexFromInt(tm.getElement(1) & 0xFF, ""), tm.getReplyType(), tm.isBinary());
                //reply.setBinary(true);;
            } else { //ASCII reply so just return the string
                log.trace("ASCII TamsMessage = {} and replyType = {} and isBinary = {}", tm, tm.getReplyType(), tm.isBinary());
                //reply.setBinary(false);;
            }
        }
        return reply;
    }

    // Has the message been completely received?
    // The length depends on the message type
    @Override
    protected boolean endOfMessage(AbstractMRReply reply) {
        TamsReply tr = (TamsReply) reply;
        log.trace("*** endOfMessage ***");
        if (!tmq.isEmpty()) {
            tm = tmq.peek();
            if (tm != null && log.isTraceEnabled()) { //Only when there is a valid TamsMessage
                if (tm.isBinary()) { //Binary reply so must makes sure the reply get initialized as ArrayList of integers
                    log.trace("Binary TamsMessage = {} {} and replyType = {} and isBinary = {}", StringUtil.appendTwoHexFromInt(tm.getElement(0) & 0xFF, ""), StringUtil.appendTwoHexFromInt(tm.getElement(1) & 0xFF, ""), tm.getReplyType(), tm.isBinary());
                } else { //ASCII reply so just return the string
                    log.trace("ASCII TamsMessage = {} and replyType = {} and isBinary = {}", tm, tm.getReplyType(), tm.isBinary());
                }
            }
        } else {
            log.trace("No TamsMessages in the queue!");
        }
        // Input is a continuous stream of characters and we must chop them up into separate messages
        index = tr.getNumDataElements() - 1;
        if (log.isTraceEnabled()) {
            log.trace("Reading byte number = {}, value = {}", tr.getNumDataElements(), StringUtil.appendTwoHexFromInt(tr.getElement(index) & 0xFF, ""));
        }
        if (tm.isBinary()) {// Binary reply
            if (tm.getReplyOneByte()) {// Single byte reply
                if (tr.getNumDataElements() < 1) {// Read one byte reply
                    endReached = false;
                } else {
                    if (log.isTraceEnabled()) {
                        log.trace("One byte binary reply = {}", StringUtil.appendTwoHexFromInt(tr.getElement(index) & 0xFF, ""));
                    }
                    myCounter = 0;
                    endReached = true;
                }
            } else {// Multi byte reply
                // Read multiple byte reply, until expected last byte
                // Sensor reply
                if (tm.getReplyType() == 'S') {
                    // Sensor replies are grouped per 3 (AA BB CC) when a new group has 0x00 as AA then this is the end of the message
                    // BUT 0x00 is also a valid byte in the 2 data bytes (BB CC) of a sensor read
                    log.trace("*** Receiving Sensor Reply ***");
                    groupSize = 3;
                    log.trace("Looking for byte# = {} and index = {} and expect as last byte = {}", groupSize * myCounter + 1, index, tm.getReplyLastByte());
                    if (tr.getNumDataElements() == (groupSize * myCounter + 1) && tr.getElement(index) == tm.getReplyLastByte()) {
                        myCounter = 0;
                        endReached = true;
                        log.trace("S - End reached!");
                    } else {
                        if (tr.getNumDataElements() == (groupSize * myCounter + 1)) {
                            myCounter++;
                        }
                        endReached = false;
                    }
                }
                // Turnout reply
                if (tm.getReplyType() == 'T') {
                    // The first byte of a reply can be 0x00 or hold the value of the number messages that will follow
                    // Turnout replies are grouped per 2 (AA BB)
                    // 0x00 is also a valid byte in the 2 data bytes (AA BB) of a turnout read
                    log.trace("*** Receiving Turnout Reply ***");
                    numberOfNibbles = tr.getElement(0);
                    if (numberOfNibbles > 50) {
                        numberOfNibbles = 50;
                    }
                    messageLength = numberOfNibbles * 2;
                    log.trace("Number of turnout events# = {}", numberOfNibbles);
                    if (myCounter < messageLength) {
                        log.trace("myCounter = {}, reply length= {}", myCounter, tr.getNumDataElements());
                        myCounter++;
                        endReached = false;
                    } else {
                        myCounter = 0;
                        endReached = true;
                        log.trace("myCounter = {}", myCounter);
                        log.trace("T - End reached!");
                    }
                }
                // Loco reply
                if (tm.getReplyType() == 'L') {
                    // The first byte of a reply can be 0x80 or if different messages will follow, 0x80 will be the last byte
                    // Loco replies are grouped per 5 (AA BB CC DD EE)
                    // Anything is a valid byte in the 5 data bytes (AA BB CC DD EE) of a Loco read
                    log.trace("*** Receiving Loco Reply ***");
                    if (log.isTraceEnabled()) {
                        log.trace("Current byte = {}", StringUtil.appendTwoHexFromInt(tr.getElement(index) & 0xFF, ""));
                    }
                    groupSize = 5;
                    if (((tr.getElement(index) & 0xFF) == TamsConstants.EOM80)) {
                        myCounter = 0;
                        endReached = true;
                        if (index > 1) {//OK we have a real message
                            if (log.isTraceEnabled()) {
                                log.trace("reply = {} {} {} {} {}", StringUtil.appendTwoHexFromInt(tr.getElement(0) & 0xFF, ""), StringUtil.appendTwoHexFromInt(tr.getElement(1) & 0xFF, ""), StringUtil.appendTwoHexFromInt(tr.getElement(2) & 0xFF, ""), StringUtil.appendTwoHexFromInt(tr.getElement(3) & 0xFF, ""), StringUtil.appendTwoHexFromInt(tr.getElement(4) & 0xFF, ""));
                            }
                        }
                        log.trace("L - End reached!");
                    } else {
                        if (tr.getNumDataElements() == (groupSize * myCounter + 1)) {
                            myCounter++;
                        }
                        endReached = false;
                    }
                }
            }
        } else {// ASCII reply
            if (tr.getNumDataElements() > 0 && tr.getElement(index) != 0x5d) {// Read ASCII reply, last is [
                log.trace("Building ASCII reply = {}", tr);
                //myCounter++;
                endReached = false;
            } else {
                _isBinary = tr.isBinary();
                log.trace("ASCII reply = {} isBinary = {}", tr, _isBinary);
                //tm = tr.getSource();
                log.trace("Source = {} isBinary = {}", tm, tm.isBinary());
                myCounter = 0;
                endReached = true;
            }
        }
        log.trace("End of Message = {}", endReached);
        if (endReached) {
            if (!tmq.isEmpty()) {
                TamsMessage m = tmq.poll();
                log.trace("Removed this message: {}", m);
            }
            if (!tmq.isEmpty()) {
                log.trace("This message is at the head: {}", tmq.peek());
            } else {
                log.trace("The queue is empty");
            }
        }
        return endReached;
    }

    // Override the finalize method for this class
    public boolean sendWaitMessage(TamsMessage m, AbstractMRListener reply) {
        log.trace("*** sendWaitMessage ***");
        log.trace("Send a message and wait for the response");
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
    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < _nDataChars; i++) {
            if (_isBinary) {
                if (i != 0) {
                    s += " ";
                }
                s = StringUtil.appendTwoHexFromInt(_dataChars[i] & 0xFF, s);
            } else {
                s += (char) _dataChars[i];
            }
        }
        return s;
    }
    private final static Logger log = LoggerFactory.getLogger(TamsTrafficController.class);

}
