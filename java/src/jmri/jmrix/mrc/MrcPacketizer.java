package jmri.jmrix.mrc;

import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from Mrc messages. The "MrcInterface" side
 * sends/receives MrcMessage objects. The connection to a MrcPortController is
 * via a pair of *Streams, which then carry sequences of characters for
 * transmission.
 * <p>
 * This is based upon the Packetizer used for LocoNet Connections due to its
 * speed and efficiency to handle messages. This also takes some code from the
 * AbstractMRTrafficController, when dealing with handling replies to messages
 * sent.
 *
 * The MRC Command Station sends out a poll message to each handset which then
 * has approximately 20ms to initially respond with a request. Otherwise the
 * Command Station will poll the next handset.
 *
 * <p>
 * Messages come to this via the main GUI thread, and are forwarded back to
 * listeners in that same thread. Reception and transmission are handled in
 * dedicated threads by RcvHandler and XmtHandler objects. Those are internal
 * classes defined here. The thread priorities are:
 * <ul>
 * <li> RcvHandler - at highest available priority
 * <li> XmtHandler - down one, which is assumed to be above the GUI
 * <li> (everything else)
 * </ul>
 * <p>
 * Some of the message formats used in this class are Copyright MRC, Inc. and
 * used with permission as part of the JMRI project. That permission does not
 * extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Mrc Inc
 * for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Kevin Dickerson Copyright (C) 2014
 * @author Ken Cameron Copyright (C) 2014
 *
 */
public class MrcPacketizer extends MrcTrafficController {

    /**
     * true if the external hardware is not echoing messages, so we must
     */
    protected boolean echo = true;  // echo messages here, instead of in hardware

    public MrcPacketizer() {
    }

    // The methods to implement the MrcInterface
    @Override
    public boolean status() {
        return (ostream != null && istream != null);
    }

    /**
     * Synchronized list used as a transmit queue.
     * <p>
     * This is public to allow access from the internal class(es) when compiling
     * with Java 1.1
     */
    public LinkedList<MrcMessage> xmtList = new LinkedList<MrcMessage>();

    /**
     * XmtHandler (a local class) object to implement the transmit thread
     */
    protected Runnable xmtHandler;

    /**
     * RcvHandler (a local class) object to implement the receive thread
     */
    protected Runnable rcvHandler;

    /**
     * Forward a preformatted MrcMessage to the actual interface.
     *
     * The message is converted to a byte array and queue for transmission
     *
     * @param m Message to send;
     */
    @Override
    public void sendMrcMessage(MrcMessage m) {
        // update statistics
        transmittedMsgCount++;

        //Convert the message to a byte stream, to save doing this when the message
        //is picked out
        m.setByteStream();

        if (log.isDebugEnabled()) { // avoid String building if not needed
            log.debug("queue Mrc packet: {}", m.toString());
        }
        // in an atomic operation, queue the request and wake the xmit thread
        try {
            synchronized (xmtHandler) {
                xmtList.addLast(m);
                if (log.isDebugEnabled()) { // avoid String building if not needed
                    log.debug("xmt list size " + xmtList.size()); //IN18N
                    Iterator<MrcMessage> iterator = xmtList.iterator();
                    while (iterator.hasNext()) {
                        log.debug(iterator.next().toString());
                    }
                    log.debug("==");
                }
            }
        } catch (RuntimeException e) {
            log.warn("passing to xmit: unexpected exception: {0}", e); //IN18N
        }
    }

    /**
     * Implement abstract method to signal if there's a backlog of information
     * waiting to be sent.
     *
     * @return true if busy, false if nothing waiting to send
     */
    @Override
    public boolean isXmtBusy() {
        if (controller == null) {
            return false;
        }

        return (!controller.okToSend());
    }

    // methods to connect/disconnect to a source of data in a MrcPortController
    // This is public to allow access from the internal class(es) when compiling with Java 1.1
    public MrcPortController controller = null;

    /**
     * Make connection to existing MrcPortController object.
     *
     * @param p Port controller for connected. Save this for a later disconnect
     *          call
     */
    public void connectPort(MrcPortController p) {
        istream = p.getInputStream();
        ostream = p.getOutputStream();
        if (controller != null) {
            log.warn("connectPort: connect called while connected"); //IN18N
        }
        controller = p;
    }

    /**
     * Break connection to existing MrcPortController object. Once broken,
     * attempts to send via "message" member will fail.
     *
     * @param p previously connected port
     */
    public void disconnectPort(MrcPortController p) {
        istream = null;
        ostream = null;
        if (controller != p) {
            log.warn("disconnectPort: disconnect called from non-connected MrcPortController"); //IN18N
        }
        controller = null;
    }

    // data members to hold the streams. These are public so the inner classes defined here
    // can access whem with a Java 1.1 compiler
    public DataInputStream istream = null;
    public OutputStream ostream = null;

    //We keep a copy of the lengths here to save on time on each request later.
    final private static int THROTTLEPACKETLENGTH = MrcPackets.getThrottlePacketLength();
    final private static int FUNCTIONGROUPLENGTH = MrcPackets.getFunctionPacketLength();
    final private static int READCVLENGTH = MrcPackets.getReadCVPacketLength();
    final private static int readCVReplyLength = MrcPackets.getReadCVPacketReplyLength();
    final private static int readDecoderAddressLength = MrcPackets.getReadDecoderAddressLength();
    final private static int WRITECVPROGLENGTH = MrcPackets.getWriteCVPROGPacketLength();
    final private static int WRITECVPOMLENGTH = MrcPackets.getWriteCVPOMPacketLength();
    final private static int SETCLOCKRATIOLENGTH = MrcPackets.getSetClockRatioPacketLength();
    final private static int SETCLOCKTIMELENGTH = MrcPackets.getSetClockTimePacketLength();
    final private static int setClockAMPMLength = MrcPackets.getSetClockAmPmPacketLength();
    final private static int powerOnLength = MrcPackets.getPowerOnPacketLength();
    final private static int powerOffLength = MrcPackets.getPowerOffPacketLength();

    final private static int addToConsistLength = MrcPackets.getClearConsistPacketLength();
    final private static int clearConsistLength = MrcPackets.getClearConsistPacketLength();
    final private static int routeControlLength = MrcPackets.getRouteControlPacketLength();
    final private static int clearRouteLength = MrcPackets.getClearRoutePacketLength();
    final private static int addToRouteLength = MrcPackets.getAddToRoutePacketLength();
    final private static int accessoryLength = MrcPackets.getAccessoryPacketLength();

    /**
     * Read a single byte, protecting against various timeouts, etc.
     * <p>
     * When a port is set to have a receive timeout (via the
     * enableReceiveTimeout() method), some will return zero bytes or an
     * EOFException at the end of the timeout. In that case, the read should be
     * repeated to get the next real character.
     * @param istream data input stream from layout
     * @return byte stream from interface
     * @throws java.io.IOException from read errors
     *
     */
    protected byte readByteProtected(DataInputStream istream) throws java.io.IOException {
        while (true) { // loop will repeat until character found
            int nchars;
            nchars = istream.read(rcvBuffer, 0, 1);
            if (nchars > 0) {
                return rcvBuffer[0];
            }
        }
    }
    // Defined this way to reduce new object creation
    private byte[] rcvBuffer = new byte[1];
    //boolean xmtWindow = false;

    /**
     * Captive class to handle incoming characters. This is a permanent loop,
     * looking for input messages in character form on the stream connected to
     * the MrcPortController via <code>connectPort</code>.
     */
    class RcvHandler implements Runnable {

        /**
         * Remember the MrcPacketizer object
         */
        MrcPacketizer trafficController;

        public RcvHandler(MrcPacketizer lt) {
            trafficController = lt;
        }

        @Override
        public void run() {
            int firstByte;
            int secondByte;
            int thirdByte;
            while (true) {   // loop permanently, program close will exit
                try {
                    firstByte = readByteProtected(istream) & 0xFF;
                    secondByte = readByteProtected(istream) & 0xFF;
                    thirdByte = readByteProtected(istream) & 0xFF;
                    // start by looking for command -  skip if bit not set or byte 1 & 3 don't match.
                    while (secondByte != 0x00 && secondByte != 0x01 || firstByte != thirdByte) {
                        if (firstByte == 0x00 && secondByte == 0x01) {
                            //Only a clock message has the first & thirdbyte different
                            break;
                        }
                        log.debug("Skipping: {} {} {}", Integer.toHexString(firstByte), Integer.toHexString(secondByte), Integer.toHexString(thirdByte));
                        firstByte = secondByte;
                        secondByte = thirdByte;
                        thirdByte = readByteProtected(istream) & 0xFF;
                    }
                    final Date time = new Date();
                    log.trace(" (RcvHandler) Start message with message: {} {}", Integer.toHexString(firstByte), Integer.toHexString(secondByte));
                    MrcMessage msg = null;
                    boolean pollForUs = false;

                    if (secondByte == 0x01) {

                        msg = new MrcMessage(6);
                        msg.setMessageClass(MrcInterface.POLL);
                        //msg.setPollMessage();
                        if (firstByte == cabAddress) {
                            pollForUs = true;
                        } else if (mCurrentState == WAITFORCMDRECEIVED) {
                            log.debug("Missed our poll slot");
                            synchronized (transmitLock) {
                                mCurrentState = MISSEDPOLL;
                                transmitLock.notify();
                            }
                        }
                        if (firstByte == 0x00) {
                            msg.setMessageClass(MrcInterface.CLOCK + MrcInterface.POLL);
                        }
                    } else {
                        switch (firstByte) {
                            case 0:/* 2 No Data Poll */

                                msg = new MrcMessage(4);
                                msg.setMessageClass(MrcInterface.POLL);
                                break;
                            case MrcPackets.THROTTLEPACKETCMD:
                                msg = new MrcMessage(THROTTLEPACKETLENGTH);
                                msg.setMessageClass(MrcInterface.THROTTLEINFO);
                                break;
                            //$FALL-THROUGH$
                            case MrcPackets.FUNCTIONGROUP1PACKETCMD:
                            case MrcPackets.FUNCTIONGROUP2PACKETCMD:
                            case MrcPackets.FUNCTIONGROUP3PACKETCMD:
                            case MrcPackets.FUNCTIONGROUP4PACKETCMD:
                            case MrcPackets.FUNCTIONGROUP5PACKETCMD:
                            case MrcPackets.FUNCTIONGROUP6PACKETCMD:
                                msg = new MrcMessage(FUNCTIONGROUPLENGTH);
                                msg.setMessageClass(MrcInterface.THROTTLEINFO);
                                break;
                            case MrcPackets.READCVCMD:
                                msg = new MrcMessage(READCVLENGTH);
                                msg.setMessageClass(MrcInterface.PROGRAMMING);
                                log.debug("Read CV Cmd");
                                break;
                            case MrcPackets.READDECODERADDRESSCMD:
                                msg = new MrcMessage(readDecoderAddressLength);
                                msg.setMessageClass(MrcInterface.PROGRAMMING);
                                break;
                            case MrcPackets.WRITECVPROGCMD:
                                msg = new MrcMessage(WRITECVPROGLENGTH);
                                msg.setMessageClass(MrcInterface.PROGRAMMING);
                                break;
                            case MrcPackets.WRITECVPOMCMD:
                                msg = new MrcMessage(WRITECVPOMLENGTH);
                                msg.setMessageClass(MrcInterface.PROGRAMMING);
                                break;
                            case MrcPackets.SETCLOCKRATIOCMD:
                                msg = new MrcMessage(SETCLOCKRATIOLENGTH);
                                msg.setMessageClass(MrcInterface.CLOCK);
                                break;
                            case MrcPackets.SETCLOCKTIMECMD:
                                msg = new MrcMessage(SETCLOCKTIMELENGTH);
                                msg.setMessageClass(MrcInterface.CLOCK);
                                break;
                            case MrcPackets.SETCLOCKAMPMCMD:
                                msg = new MrcMessage(setClockAMPMLength);
                                msg.setMessageClass(MrcInterface.CLOCK);
                                break;
                            case MrcPackets.READCVHEADERREPLYCODE:
                                msg = new MrcMessage(readCVReplyLength);
                                msg.setMessageClass(MrcInterface.PROGRAMMING);
                                synchronized (transmitLock) {
                                    mCurrentState = IDLESTATE;
                                    transmitLock.notify();
                                }
                                log.debug("CV read reply");
                                break;
                            case MrcPackets.PROGCMDSENTCODE:
                                log.debug("Gd Prog Cmd Sent");
                                synchronized (transmitLock) {
                                    mCurrentState = IDLESTATE;
                                    transmitLock.notify();
                                }
                                msg = new MrcMessage(4);
                                msg.setMessageClass(MrcInterface.PROGRAMMING);
                                break;
                            case MrcPackets.POWERONCMD:
                                mCurrentState = IDLESTATE;
                                msg = new MrcMessage(powerOnLength);
                                msg.setMessageClass(MrcInterface.POWER);
                                break;
                            case MrcPackets.POWEROFFCMD:
                                mCurrentState = IDLESTATE;
                                msg = new MrcMessage(powerOffLength);
                                msg.setMessageClass(MrcInterface.POWER);
                                break;
                            case MrcPackets.ADDTOCONSISTPACKETCMD:
                                mCurrentState = IDLESTATE;
                                msg = new MrcMessage(addToConsistLength);
                                msg.setMessageClass(MrcInterface.THROTTLEINFO);
                                break;
                            case MrcPackets.CLEARCONSISTPACKETCMD:
                                mCurrentState = IDLESTATE;
                                msg = new MrcMessage(clearConsistLength);
                                msg.setMessageClass(MrcInterface.THROTTLEINFO);
                                break;
                            case MrcPackets.ROUTECONTROLPACKETCMD:
                                mCurrentState = IDLESTATE;
                                msg = new MrcMessage(routeControlLength);
                                msg.setMessageClass(MrcInterface.TURNOUTS);
                                break;
                            case MrcPackets.CLEARROUTEPACKETCMD:
                                mCurrentState = IDLESTATE;
                                msg = new MrcMessage(clearRouteLength);
                                msg.setMessageClass(MrcInterface.TURNOUTS);
                                break;
                            case MrcPackets.ADDTOROUTEPACKETCMD:
                                mCurrentState = IDLESTATE;
                                msg = new MrcMessage(addToRouteLength);
                                msg.setMessageClass(MrcInterface.TURNOUTS);
                                break;
                            case MrcPackets.ACCESSORYPACKETCMD:
                                mCurrentState = IDLESTATE;
                                msg = new MrcMessage(accessoryLength);
                                msg.setMessageClass(MrcInterface.TURNOUTS);
                                break;
                            case MrcPackets.LOCODBLCONTROLCODE:
                                synchronized (transmitLock) {
                                    mCurrentState = DOUBLELOCOCONTROL;
                                    transmitLock.notify();
                                }
                                msg = new MrcMessage(4);
                                msg.setMessageClass(MrcInterface.THROTTLEINFO);
                                break;
                            case MrcPackets.LOCOSOLECONTROLCODE:
                                mCurrentState = IDLESTATE;
                                msg = new MrcMessage(4);
                                msg.setMessageClass(MrcInterface.THROTTLEINFO);
                                synchronized (transmitLock) {
                                    mCurrentState = IDLESTATE;
                                    transmitLock.notify();
                                }
                                break;
                            case MrcPackets.GOODCMDRECEIVEDCODE:      //Possibly shouldn't change the state, as we wait for further confirmation.
                                if (mCurrentState == CONFIRMATIONONLY) {
                                    synchronized (transmitLock) {
                                        mCurrentState = IDLESTATE;
                                        transmitLock.notify();
                                    }
                                }
                                msg = new MrcMessage(4);
                                break;
                            case MrcPackets.BADCMDRECEIVEDCODE:
                                mCurrentState = BADCOMMAND;
                                msg = new MrcMessage(4);
                                break;
                            default:
                                msg = new MrcMessage(4); //Unknown
                                log.debug("UNKNOWN " + Integer.toHexString(firstByte)); //IN18N
                        }
                    }

                    msg.setElement(0, firstByte);
                    msg.setElement(1, secondByte);
                    msg.setElement(2, thirdByte);
                    // message exists, now fill it
                    int len = msg.getNumDataElements();
                    log.trace("len: {}", len);
                    for (int i = 3; i < len; i++) {
                        // check for message-blocking error
                        int b = readByteProtected(istream) & 0xFF;
                        msg.setElement(i, b);
                        log.trace("char {} is: {}", i, Integer.toHexString(b));
                    }
                    /*Slight trade off with this we may see any transmitted message go out prior to the
                     poll message being passed to the monitor. */
                    if (pollForUs) {
                        synchronized (xmtHandler) {
                            xmtHandler.notify(); //This will notify the xmt to send a message, even if it is only "no Data" reply
                        }
                    }

                    if ((msg.getMessageClass() & MrcInterface.POLL) != MrcInterface.POLL && msg.getNumDataElements() > 6) {
                        if (!msg.validCheckSum()) {
                            log.warn("Ignore Mrc packet with bad checksum: {}", msg); //IN18N
                            throw new MrcMessageException();
                        } else {
                            for (int i = 1; i < msg.getNumDataElements(); i += 2) {
                                if (msg.getElement(i) != 0x00) {
                                    log.warn("Ignore Mrc packet with bad bit: {}", msg); //IN18N
                                    throw new MrcMessageException();
                                }
                            }
                        }
                    }
                    // message is complete, dispatch it !!
                    {
                        log.trace("queue message for notification: {}", msg);
                        final MrcMessage thisMsg = msg;
                        final MrcPacketizer thisTc = trafficController;
                        // return a notification via the queue to ensure end
                        Runnable r = new Runnable() {
                            MrcMessage msgForLater = thisMsg;
                            MrcPacketizer myTc = thisTc;

                            @Override
                            public void run() {
                                myTc.notifyRcv(time, msgForLater);
                            }
                        };
                        javax.swing.SwingUtilities.invokeLater(r);
                    }
                    // done with this one
                } catch (MrcMessageException e) {
                    // just let it ride for now
                    log.warn("run: unexpected MrcMessageException: {0}", e); //IN18N
                } catch (java.io.EOFException e) {
                    // posted from idle port when enableReceiveTimeout used
                    log.trace("EOFException, is Mrc serial I/O using timeouts?");
                } catch (java.io.IOException e) {
                    // fired when write-end of HexFile reaches end
                    log.debug("IOException, should only happen with HexFile", e);
                    disconnectPort(controller);
                    return;
                } // normally, we don't catch RuntimeException, but in this
                // permanently running loop it seems wise.
                catch (RuntimeException e) {
                    log.warn("Unknown Exception", e);  //IN18N
                }
            } // end of permanent loop
        }
    }

    final static int IDLESTATE = 0x00;
    final static int WAITFORCMDRECEIVED = 0x01;
    final static int DOUBLELOCOCONTROL = 0x02;
    final static int MISSEDPOLL = 0x04;
    final static int BADCOMMAND = 0x08;
    final static int CONFIRMATIONONLY = 0x10;
    int mCurrentState = IDLESTATE;

    int consecutiveMissedPolls = 0;

    final MrcMessage noData = MrcMessage.setNoData();
    final byte noDataMsg[] = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

    /**
     * Captive class to handle transmission
     */
    class XmtHandler implements Runnable {

        @Override
        public void run() {
            byte msg[];
            MrcMessage m;
            int x = 0;
            int state = WAITFORCMDRECEIVED;
            while (true) {   // loop permanently
                m = noData;
                msg = noDataMsg;
                log.trace("check for input");
                synchronized (this) {
                    log.trace("start wait");
                    //log.info("wait until we have been polled");
                    new jmri.util.WaitHandler(this);  // handle synchronization, spurious wake, interruption
                    log.trace("end wait");

                    if (xmtList.size() != 0) {
                        m = xmtList.removeFirst();
                        msg = m.getByteStream();
                        log.debug("xmt list size after get {}", xmtList.size());
                        log.debug("Message to send on {}", m);
                    }
                }
                try {
                    if (m.getMessageClass() != MrcInterface.POLL) {
                        mCurrentState = WAITFORCMDRECEIVED;
                        /* We set the current state before transmitting the message otherwise
                         the reply to the message may be received before the state is set
                         and the message will timeout and be retransmitted */
                        if (!m.isReplyExpected()) {
                            mCurrentState = CONFIRMATIONONLY;
                        }
                        state = mCurrentState;
                    }
                    ostream.write(msg);
                    ostream.flush();
                    messageTransmitted(m);
                    if (m.getMessageClass() != MrcInterface.POLL) {
                        if (log.isTraceEnabled()) { // avoid String building if not needed
                            log.trace("end write to stream: {}", jmri.util.StringUtil.hexStringFromBytes(msg));
                            log.trace("wait : {} : {}", m.getTimeout(), x);
                        }
                        transmitWait(m.getTimeout(), state, "transmitLoop interrupted", x); //IN18N
                        x++;
                    } else {
                        mCurrentState = IDLESTATE;
                    }

                    if (mCurrentState == WAITFORCMDRECEIVED || mCurrentState == CONFIRMATIONONLY) {
                        log.debug("Timed out");
                        if (m.getRetries() >= 0) {
                            m.setRetries(m.getRetries() - 1);
                            synchronized (this) {
                                xmtList.addFirst(m);
                            }
                        } else {
                            messageFailed(m);
                        }
                        mCurrentState = IDLESTATE;
                        consecutiveMissedPolls = 0;
                    } else if (mCurrentState == MISSEDPOLL && m.getRetries() >= 0) {
                        consecutiveMissedPolls++;
                        log.debug("Missed add to front");
                        if (consecutiveMissedPolls < 5) {
                            synchronized (this) {
                                xmtList.addFirst(m);
                                mCurrentState = IDLESTATE;
                                if (log.isDebugEnabled()) { // avoid String building if not needed
                                    log.debug("xmt list size {}", xmtList.size());
                                    Iterator<MrcMessage> iterator = xmtList.iterator();
                                    while (iterator.hasNext()) {
                                        log.debug(iterator.next().toString());
                                    }
                                }
                            }
                        } else {
                            log.warn("Message missed {} polls for message {}", consecutiveMissedPolls, m); //IN18N
                            consecutiveMissedPolls = 0;
                        }
                    } else if (mCurrentState == DOUBLELOCOCONTROL && m.getRetries() >= 0) {
                        if (log.isDebugEnabled()) { // avoid String building if not needed
                            log.debug("Auto Retry send message added back to queue: {}", Arrays.toString(msg));
                        }
                        m.setRetries(m.getRetries() - 1);
                        synchronized (this) {
                            xmtList.addFirst(m);
                            mCurrentState = IDLESTATE;
                        }
                        consecutiveMissedPolls = 0;
                    } else if (mCurrentState == BADCOMMAND) {
                        log.debug("Bad command sent");
                        messageFailed(m);
                        mCurrentState = IDLESTATE;
                        consecutiveMissedPolls = 0;
                    }
                } catch (java.io.IOException e) {
                    log.warn("sendMrcMessage: IOException: {}", e); //IN18N
                }
            }
        }
    }

    static final Object transmitLock = new Object();

    protected void transmitWait(int waitTime, int state, String InterruptMessage, int x) {
        // wait() can have spurious wakeup!
        // so we protect by making sure the entire timeout time is used
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long endTime = currentTime + waitTime;
        while (endTime > (currentTime = Calendar.getInstance().getTimeInMillis())) {
            long wait = endTime - currentTime;
            try {
                synchronized (transmitLock) {
                    // Do not wait if the current state has changed since we
                    // last set it.
                    if (mCurrentState != state) {
                        return;
                    }
                    transmitLock.wait(wait); // rcvr normally ends this w state change
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // retain if needed later
                log.error(InterruptMessage);
            }
        }
        log.debug("Timeout in transmitWait {}, mCurrentState: {} after {}", x, mCurrentState, waitTime);
    }

    protected void messageFailed(MrcMessage m) {
        log.debug("message transmitted");
        if (m.getSource() == null) {
            return;
        }
        // message is queued for transmit, echo it when needed
        // return a notification via the queue to ensure end
        javax.swing.SwingUtilities.invokeLater(new Failed(new Date(), m));

    }

    static class Failed implements Runnable {

        Failed(Date _timestamp, MrcMessage m) {
            msgForLater = m;
            timestamp = _timestamp;
        }
        MrcMessage msgForLater;
        Date timestamp;

        @Override
        public void run() {
            msgForLater.getSource().notifyFailedXmit(timestamp, msgForLater);
        }
    }

    /**
     * When a message is finally transmitted, forward it to listeners if echoing
     * is needed.
     *
     * @param msg message to tag a transmitted message
     */
    protected void messageTransmitted(MrcMessage msg) {
        //if (debug) log.debug("message transmitted");
        if (!echo) {
            return;
        }
        // message is queued for transmit, echo it when needed
        // return a notification via the queue to ensure end
        javax.swing.SwingUtilities.invokeLater(new Echo(this, new Date(), msg));
    }

    static class Echo implements Runnable {

        Echo(MrcPacketizer t, Date _timestamp, MrcMessage m) {
            myTc = t;
            msgForLater = m;
            timestamp = _timestamp;
        }
        MrcMessage msgForLater;
        MrcPacketizer myTc;
        Date timestamp;

        @Override
        public void run() {
            myTc.notifyXmit(timestamp, msgForLater);
        }
    }

    /**
     * Invoked at startup to start the threads needed here.
     */
    public void startThreads() {
        int priority = Thread.currentThread().getPriority();
        log.debug("startThreads current priority = " + priority
                + " max available = " + Thread.MAX_PRIORITY
                + " default = " + Thread.NORM_PRIORITY
                + " min available = " + Thread.MIN_PRIORITY); //IN18N

        // make sure that the xmt priority is no lower than the current priority
        int xmtpriority = (Thread.MAX_PRIORITY - 1 > priority ? Thread.MAX_PRIORITY : Thread.MAX_PRIORITY - 1);
        // start the XmtHandler in a thread of its own
        if (xmtHandler == null) {
            xmtHandler = new XmtHandler();
        }
        Thread xmtThread = new Thread(xmtHandler, "Mrc transmit handler"); //IN18N
        log.debug("Xmt thread starts at priority " + xmtpriority); //IN18N
        xmtThread.setDaemon(true);
        xmtThread.setPriority(Thread.MAX_PRIORITY - 1);
        xmtThread.start();

        // start the RcvHandler in a thread of its own
        if (rcvHandler == null) {
            rcvHandler = new RcvHandler(this);
        }
        Thread rcvThread = new Thread(rcvHandler, "Mrc receive handler " + Thread.MAX_PRIORITY); //IN18N
        rcvThread.setDaemon(true);
        rcvThread.setPriority(Thread.MAX_PRIORITY);
        rcvThread.start();

    }

    private final static Logger log = LoggerFactory.getLogger(MrcPacketizer.class);
}
