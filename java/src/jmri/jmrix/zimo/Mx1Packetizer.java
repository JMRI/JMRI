package jmri.jmrix.zimo;

import static jmri.jmrix.zimo.Mx1Message.PROGCMD;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access to Zimo Mx1 messages via stream-based I/O. The "Mx1Interface" * side
 * sends/receives Mx1Message objects. The connection to a Mx1PortController is
 * via a pair of *Streams, which then carry sequences of characters for
 * transmission.
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
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 *
 * Adapted by Sip Bosch for use with zimo Mx-1
 *
 */
public class Mx1Packetizer extends Mx1TrafficController {

    public Mx1Packetizer(Mx1CommandStation pCommandStation, boolean prot) {
        super(pCommandStation, prot);
        protocol = prot;
    }

    // The methods to implement the Mx1Interface
    @Override
    public boolean status() {
        return (ostream != null && istream != null);
    }

    public final static boolean ASCII = false;
    public final static boolean BINARY = true;

    boolean protocol = ASCII;

    /**
     * Synchronized list used as a transmit queue
     */
    LinkedList<byte[]> xmtList = new LinkedList<>();

    ConcurrentHashMap<Integer, MessageQueued> xmtPackets = new ConcurrentHashMap<>(16, 0.9f, 1);

    /**
     * XmtHandler (a local class) object to implement the transmit thread
     */
    XmtHandler xmtHandler = new XmtHandler();

    /**
     * XmtHandler (a local class) object to implement the transmit thread
     */
    RetryHandler retryHandler = new RetryHandler(this);

    /**
     * RcvHandler (a local class) object to implement the receive thread
     */
    RcvHandler rcvHandler = new RcvHandler(this);

    /**
     * Forward a preformatted Mx1Message to the actual interface.
     *
     * End of Message is added here, then the message is converted to a byte
     * array and queued for transmission
     *
     * @param m Message to send; will be updated with CRC
     */
    @Override
    public void sendMx1Message(Mx1Message m, Mx1Listener reply) {
        byte msg[];
        if (protocol) {
            processPacketForSending(m);
            msg = m.getRawPacket();
            if (m.replyL1Expected()) {
                xmtPackets.put(m.getSequenceNo(), new MessageQueued(m, reply));
            }
            //notify(new Mx1Message(msg), reply);  //Sends a fully formated packet to the command monitor
        } else {
            // set the CR code byte
            int len = m.getNumDataElements();
            m.setElement(len - 1, 0x0D);  // CR is last element of message
            // notify all _other_ listeners
            // stream to port in single write, as that's needed by serial
            msg = new byte[len];
            for (int i = 0; i < len; i++) {
                msg[i] = (byte) m.getElement(i);
            }
            if (log.isDebugEnabled()) {
                log.debug("queue outgoing packet: " + m.toString());
            }
            // in an atomic operation, queue the request and wake the xmit thread
        }
        notifyLater(m, reply);
        //notify(m, reply);
        synchronized (xmtHandler) {
            xmtList.addLast(msg);
            xmtHandler.notify();
        }
    }

    byte getNextSequenceNo() {
        lastSequence++;
        if ((lastSequence & 0xff) == 0xff) {
            lastSequence = 0x00;
        }
        //lastSequenceSent = (byte)(lastSequence&0xff);
        return lastSequence;
    }

    void processPacketForSending(Mx1Message m) {
        ArrayList<Byte> msgFormat = new ArrayList<>();
        //Add <SOH>
        msgFormat.add((byte) SOH);
        msgFormat.add((byte) SOH);

        //m.setSequenceNo((byte)(lastSequenceSent&0xff));
        m.setSequenceNo(getNextSequenceNo());

        for (int i = 0; i < m.getNumDataElements(); i++) {
            formatByteToPacket((byte) m.getElement(i), msgFormat);
        }
        //Add CRC
        if (m.getLongMessage()) {
            int crc = get16BitCRC(m);
            formatByteToPacket((byte) ((crc >>> 8) & 0xff), msgFormat);
            formatByteToPacket((byte) (crc & 0xff), msgFormat);
        } else {
            //add 8bit crc
            int checksum = get8BitCRC(m);
            formatByteToPacket((byte) checksum, msgFormat);
        }
        //Add EOT
        msgFormat.add((byte) EOT);
        byte msg[] = new byte[msgFormat.size()];
        for (int i = 0; i < msgFormat.size(); i++) {
            msg[i] = msgFormat.get(i);
        }
        m.setRawPacket(msg);
        m.setTimeStamp(System.currentTimeMillis());
    }

    //Format any bytes that need to be masked with DLE
    void formatByteToPacket(byte b, ArrayList<Byte> message) {
        b = (byte) (b & 0xff);

        if (b == SOH || b == EOT || b == DLE) {
            message.add((byte) DLE);
            message.add((byte) (b ^ 0x20));
        } else {
            message.add((byte) (b & 0xff));
        }
    }

    // methods to connect/disconnect to a source of data in a Mx1PortController
    private Mx1PortController controller = null;

    /**
     * Make connection to existing Mx1PortController object.
     *
     * @param p Port controller for connected. Save this for a later disconnect
     *          call
     */
    public void connectPort(Mx1PortController p) {
        istream = p.getInputStream();
        ostream = p.getOutputStream();
        if (controller != null) {
            log.warn("connectPort: connect called while connected");
        }
        controller = p;
    }

    /**
     * Break connection to existing LnPortController object. Once broken,
     * attempts to send via "message" member will fail.
     *
     * @param p previously connected port
     */
    public void disconnectPort(Mx1PortController p) {
        istream = null;
        ostream = null;
        if (controller != p) {
            log.warn("disconnectPort: disconnect called from non-connected Mx1PortController");
        }
        controller = null;
    }

// data members to hold the streams
    DataInputStream istream = null;
    OutputStream ostream = null;

    /**
     * Read a single byte, protecting against various timeouts, etc.
     * <p>
     * When a port is set to have a receive timeout (via the
     * enableReceiveTimeout() method), some will return zero bytes or an
     * EOFException at the end of the timeout. In that case, the read should be
     * repeated to get the next real character.
     *
     * @param istream the input stream
     * @return the first byte in the stream
     * @throws java.io.IOException if unable to read istream
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

    private byte[] rcvBuffer = new byte[1];

    byte lastSequence = 0x00;
    //byte lastSequenceSent = 0x00;

    final static int SOH = 0x01;
    final static int EOT = 0x17;
    final static int DLE = 0x10;

    /**
     * Handle incoming characters. This is a permanent loop, looking for input
     * messages in character form on the stream connected to the
     * Mx1PortController via <code>connectPort</code>. Terminates with the input
     * stream breaking out of the try block.
     */
    class RcvHandler implements Runnable {

        /**
         * Remember the Packetizer object
         */
        Mx1Packetizer trafficController;

        public RcvHandler(Mx1Packetizer lt) {
            trafficController = lt;
        }

        @Override
        public void run() {
            int opCode;
            if (protocol) {
                ArrayList<Integer> message;
                while (true) {
                    try {
                        int firstByte = readByteProtected(istream) & 0xFF;
                        int secondByte = readByteProtected(istream) & 0xFF;
                        // start by looking for command 0x01, 0x01
                        while (firstByte != SOH && secondByte != SOH) {
                            log.debug("Skipping: {} {}", Integer.toHexString(firstByte), Integer.toHexString(secondByte));
                            firstByte = secondByte;
                            secondByte = readByteProtected(istream) & 0xFF;
                        }
                        message = new ArrayList<>();
                        while (true) {
                            int b = readByteProtected(istream) & 0xFF;
                            if (b == EOT) //End of Frame
                            {
                                break;
                            }
                            if (b == DLE) { //0x10 is a ident to inform that the next byte will need to be xor'd with 0x20 to get correct value.
                                b = readByteProtected(istream) & 0xFF;
                                b = b ^ 0x20;
                            }
                            message.add(b);
                            log.debug("char is: {}", Integer.toHexString(b));
                        }
                        //lastSequence = ((byte)(message.get(0)&0xff));
                        //Remove the message from the list
                        synchronized (rcvHandler) {
                            xmtPackets.remove(message.get(3));
                        }
                        Mx1Message msg;
                        //boolean error = false;
                        if ((message.get(1) & 0x80) == 0x80) { //Long Packet
                            //Remove crc element
                            msg = new Mx1Message(message.size() - 2, Mx1Packetizer.BINARY);
                            for (int i = 0; i < message.size() - 2; i++) {
                                msg.setElement(i, message.get(i));
                            }
                        } else { //Short packet
                            msg = new Mx1Message(message.size() - 1, Mx1Packetizer.BINARY);
                            for (int i = 0; i < message.size() - 1; i++) {
                                msg.setElement(i, message.get(i));
                            }
                            if (message.get(message.size() - 1) != (get8BitCRC(msg) & 0xff)) {
                                log.error("Message with invalid CRC received Expecting:" + (get8BitCRC(msg) & 0xff) + " found:" + message.get(message.size() - 1));
                                msg.setCRCError();
                            } else {
                                xmtPackets.remove(message.get(3));
                            }
                        }
                        isAckReplyRequired(msg);
                        final Mx1Message thisMsg = msg;
                        final Mx1Packetizer thisTc = trafficController;
                        // return a notification via the queue to ensure end
                        Runnable r = new Runnable() {
                            Mx1Message msgForLater = thisMsg;
                            Mx1Packetizer myTc = thisTc;

                            @Override
                            public void run() {
                                myTc.notify(msgForLater, null);
                            }
                        };
                        log.debug("schedule notify of incoming packet");
                        javax.swing.SwingUtilities.invokeLater(r);

                    } catch (java.io.IOException e) {
                        // fired when write-end of HexFile reaches end
                        log.debug("IOException, should only happen with HexFIle", e);
                        disconnectPort(controller);
                        return;
                    } catch (RuntimeException e) {
                        log.warn("run: unexpected exception:", e);
                    }
                }
            } else {
                //Original version
                while (true) {   // loop permanently, program close will exit
                    try {
                        // start by looking for command
                        opCode = istream.readByte() & 0xFF;
                        // Create output message
                        log.debug("RcvHandler: Start message with opcode: " + Integer.toHexString(opCode));
                        int len = 1;
                        Mx1Message msgn = new Mx1Message(15);
                        msgn.setElement(0, opCode);
                        // message exists, now fill it
                        for (int i = 1; i < 15; i++) {
                            int b = istream.readByte() & 0xFF;
                            len = len + 1;
                            //if end of message
                            if (b == 0x0D || b == 0x0A) {
                                msgn.setElement(i, b);
                                break;
                            }
                            msgn.setElement(i, b);
                        }
                        //transfer to array with now known size
                        Mx1Message msg = new Mx1Message(len);
                        for (int i = 0; i < len; i++) {
                            msg.setElement(i, msgn.getElement(i) & 0xFF);
                        }
                        // message is complete, dispatch it !!
                        {
                            final Mx1Message thisMsg = msg;
                            final Mx1Packetizer thisTc = trafficController;
                            // return a notification via the queue to ensure end
                            Runnable r = new Runnable() {
                                Mx1Message msgForLater = thisMsg;
                                Mx1Packetizer myTc = thisTc;

                                @Override
                                public void run() {
                                    myTc.notify(msgForLater, null);
                                }
                            };
                            log.debug("schedule notify of incoming packet");
                            javax.swing.SwingUtilities.invokeLater(r);
                        }
                    } catch (java.io.EOFException e) {
                        // posted from idle port when enableReceiveTimeout used
                        log.debug("EOFException, is serial I/O using timeouts?");
                    } catch (java.io.IOException e) {
                        // fired when write-end of HexFile reaches end
                        log.debug("IOException, should only happen with HexFIle: " + e);
                        disconnectPort(controller);
                        return;
                    } catch (RuntimeException e) {
                        log.warn("run: unexpected exception: " + e);
                    }
                } // end of permanent loop
            }
        }
    }

    void notifyLater(Mx1Message m, Mx1Listener reply) {
        final Mx1Message thisMsg = m;
        final Mx1Packetizer thisTc = this;
        final Mx1Listener thisLst = reply;
        Runnable r = new Runnable() {
            Mx1Message msgForLater = thisMsg;
            Mx1Packetizer myTc = thisTc;
            Mx1Listener myListener = thisLst;

            @Override
            public void run() {
                myTc.notify(msgForLater, myListener);
            }
        };
        log.debug("schedule notify of incoming packet");
        javax.swing.SwingUtilities.invokeLater(r);
    }

    void isAckReplyRequired(Mx1Message m) {
        if (m.isCRCError()) {
            //Send a NAK message
            Mx1Message nack = new Mx1Message(3, BINARY);
            //ack.setElement(0, getNextSequenceNo()&0xff);
            nack.setElement(1, 0x10);
            nack.setElement(2, 0x00);
            //ack.setElement(2, 3);
            processPacketForSending(nack);
            byte msg[] = nack.getRawPacket();
            notify(nack, null);
            //notifyLater(ack, null);
            synchronized (xmtHandler) {
                xmtList.addFirst(msg);
                xmtHandler.notify();
            }
            return;
        }
        if ((m.getElement(1) & 0x80) == 0x80) { //Long Packet

        } else { //Short Packet
            if (((m.getElement(1) & 0x40) == 0x40) || ((m.getElement(1) & 0x60) == 0x60)) {
                //Message is a reply/Ack so no need to acknowledge
                return;
            }
            if ((m.getElement(2) & PROGCMD) == PROGCMD) {
                //log.info("Send L1 reply");
                l1AckPacket(m);
            } else if ((m.getElement(1) & 0x20) == 0x20) {//Level 2 Reply, will need to send back ack L2
                //log.info("Send L2 reply");
                l2AckPacket(m);
            }

        }
    }

    void l1AckPacket(Mx1Message m) {
        Mx1Message ack = new Mx1Message(5, BINARY);
        //ack.setElement(0, getNextSequenceNo()&0xff);
        ack.setElement(1, 0x50);
        ack.setElement(2, m.getElement(2) & 0xff);
        //ack.setElement(2, 3);
        ack.setElement(3, m.getElement(0) & 0xff);
        processPacketForSending(ack);
        byte msg[] = ack.getRawPacket();
        notify(ack, null);
        //notifyLater(ack, null);
        synchronized (xmtHandler) {
            xmtList.addFirst(msg);
            xmtHandler.notify();
        }
    }

    void l2AckPacket(Mx1Message m) {
        Mx1Message ack = new Mx1Message(5, BINARY);
        //ack.setElement(0, getNextSequenceNo()&0xff);
        ack.setElement(1, 0x70);//was b0
        ack.setElement(2, m.getElement(2) & 0xff);
        //ack.setElement(2, 3);
        ack.setElement(3, m.getElement(0) & 0xff);
        processPacketForSending(ack);
        byte msg[] = ack.getRawPacket();
        notify(ack, null);
        //notifyLater(ack, null);
        synchronized (xmtHandler) {
            xmtList.addFirst(msg);
            xmtHandler.notify();
        }
    }

    /**
     * Captive class to handle transmission
     */
    @SuppressFBWarnings(value = "UW_UNCOND_WAIT",
            justification = "while loop controls access")
    class XmtHandler implements Runnable {

        @Override
        public void run() {
            while (true) {   // loop permanently
                // any input?
                try {
                    // get content; failure is a NoSuchElementException
                    log.debug("check for input");
                    byte msg[] = null;
                    synchronized (this) {
                        msg = xmtList.removeFirst();
                    }
                    // input - now send
                    try {
                        if (ostream != null) {
                            //if (!controller.okToSend()) log.warn("Mx1 port not ready to receive");
                            log.debug("start write to stream");
                            ostream.write(msg);
                            ostream.flush();
                            if (protocol) {
                                //Tell the retry handler that a packet has been transmitted and to handle any retry.
                                synchronized (retryHandler) {
                                    retryHandler.notify();
                                }
                            }

                            log.debug("end write to stream");
                        } else {
                            // no stream connected
                            log.warn("send message: no connection established");
                        }
                    } catch (java.io.IOException e) {
                        log.warn("send message: IOException: " + e.toString());
                    }
                } catch (NoSuchElementException e) {
                    //Check retry queue?
                    // message queue was empty, wait for input
                    log.debug("start wait");
                    try {
                        synchronized (this) {
                            wait();
                        }
                    } catch (java.lang.InterruptedException ei) {
                        Thread.currentThread().interrupt(); // retain if needed later
                    }
                    log.debug("end wait");
                }
            }
        }
    }

    /**
     * Class to handle the re-transmission of messages that have not had a Level
     * 1 response from the command station.
     */
    class RetryHandler implements Runnable {

        Mx1Packetizer trafficController;

        public RetryHandler(Mx1Packetizer lt) {
            trafficController = lt;
        }

        @SuppressFBWarnings(value = "UW_UNCOND_WAIT", justification="false postive, guarded by if statement")
        @Override
        public void run() {
            while (true) {   // loop permanently
                if (xmtPackets.isEmpty()) {
                    try {
                        synchronized (this) {
                            wait();
                        }
                    } catch (java.lang.InterruptedException ei) {
                        Thread.currentThread().interrupt(); // retain if needed later
                    }
                } else {
                    for (int key : xmtPackets.keySet()) {
                        MessageQueued mq = xmtPackets.get(key);
                        Mx1Message m = mq.getMessage();
                        if (m.getRetry() <= 0) {
                            xmtPackets.remove(key);
                        } else if (m.getTimeStamp() + 200 < System.currentTimeMillis()) {
                            m.setRetries(m.getRetry() - 1);
                            m.setTimeStamp(System.currentTimeMillis());
                            trafficController.notify(m, mq.getListener());
                            synchronized (xmtHandler) {
                                log.warn("Packet not replied to so will retry");
                                xmtList.addFirst(m.getRawPacket());
                                xmtHandler.notify();
                            }
                        } else {
                            //Using a linked list, so if the first packet we come too isn't 
                            break;
                        }
                    }
                    //As the retry packet list is not empty, we will wait for 200ms before rechecking it.
                    if (!xmtPackets.isEmpty()) {
                        try {
                            synchronized (this) {
                                wait(200);
                            }
                        } catch (java.lang.InterruptedException ei) {
                            Thread.currentThread().interrupt(); // retain if needed later
                        }
                    }

                }
            }
        }
    }

    static class MessageQueued {

        Mx1Message msg;
        Mx1Listener reply;

        MessageQueued(Mx1Message m, Mx1Listener r) {
            msg = m;
            reply = r;
        }

        Mx1Message getMessage() {
            return msg;
        }

        Mx1Listener getListener() {
            return reply;
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
                + " min available = " + Thread.MIN_PRIORITY);

        // start the RetryHandler in a thread of its own simply use standard priority
        Thread retryThread = new Thread(retryHandler, "MX1 retry handler");
        //rcvThread.setPriority(Thread.MAX_PRIORITY-);
        retryThread.start();

        // make sure that the xmt priority is no lower than the current priority
        int xmtpriority = (Thread.MAX_PRIORITY - 1 > priority ? Thread.MAX_PRIORITY - 1 : Thread.MAX_PRIORITY);
        // start the XmtHandler in a thread of its own
        Thread xmtThread = new Thread(xmtHandler, "MX1 transmit handler");
        log.debug("Xmt thread starts at priority " + xmtpriority);
        xmtThread.setPriority(Thread.MAX_PRIORITY - 1);
        xmtThread.start();

        // start the RcvHandler in a thread of its own
        Thread rcvThread = new Thread(rcvHandler, "MX1 receive handler");
        rcvThread.setPriority(Thread.MAX_PRIORITY);
        rcvThread.start();
    }

    public int get16BitCRC(Mx1Message m) {
        int POLYNOMIAL = 0x8408;
        int PRESET_VALUE = 0;
        byte[] array = new byte[m.getNumDataElements()];
        for (int i = 0; i < m.getNumDataElements(); i++) {
            array[i] = (byte) m.getElement(i);
        }
        int current_crc_value = PRESET_VALUE;
        for (int i = 0; i < array.length; i++) {
            current_crc_value ^= array[i] & 0xFF;
            for (int j = 0; j < 8; j++) {
                if ((current_crc_value & 1) != 0) {
                    current_crc_value = (current_crc_value >>> 1) ^ POLYNOMIAL;
                } else {
                    current_crc_value = current_crc_value >>> 1;
                }
            }
        }
        return current_crc_value & 0xFFFF;
    }

    byte get8BitCRC(Mx1Message m) {
        int checksum = 0xff;

        for (int i = 0; i < m.getNumDataElements(); i++) {
            checksum = crc8bit_table[(checksum ^ ((m.getElement(i)) & 0xff))];
        }
        return (byte) (checksum & 0xff);
    }

    final int crc8bit_table[] = new int[]{
        0x00, 0x5e, 0xbc, 0xe2, 0x61, 0x3f, 0xdd, 0x83, 0xc2, 0x9c, 0x7e, 0x20, 0xa3, 0xfd, 0x1f,
        0x41, 0x9d, 0xc3, 0x21, 0x7f, 0xfc, 0xa2, 0x40, 0x1e, 0x5f, 0x01, 0xe3, 0xbd, 0x3e, 0x60,
        0x82, 0xdc, 0x23, 0x7d, 0x9f, 0xc1, 0x42, 0x1c, 0xfe, 0xa0, 0xe1, 0xbf, 0x5d, 0x03, 0x80,
        0xde, 0x3c, 0x62, 0xbe, 0xe0, 0x02, 0x5c, 0xdf, 0x81, 0x63, 0x3d, 0x7c, 0x22, 0xc0, 0x9e,
        0x1d, 0x43, 0xa1, 0xff, 0x46, 0x18, 0xfa, 0xa4, 0x27, 0x79, 0x9b, 0xc5, 0x84, 0xda, 0x38,
        0x66, 0xe5, 0xbb, 0x59, 0x07, 0xdb, 0x85, 0x67, 0x39, 0xba, 0xe4, 0x06, 0x58, 0x19, 0x47,
        0xa5, 0xfb, 0x78, 0x26, 0xc4, 0x9a, 0x65, 0x3b, 0xd9, 0x87, 0x04, 0x5a, 0xb8, 0xe6, 0xa7,
        0xf9, 0x1b, 0x45, 0xc6, 0x98, 0x7a, 0x24, 0xf8, 0xa6, 0x44, 0x1a, 0x99, 0xc7, 0x25, 0x7b,
        0x3a, 0x64, 0x86, 0xd8, 0x5b, 0x05, 0xe7, 0xb9, 0x8c, 0xd2, 0x30, 0x6e, 0xed, 0xb3, 0x51,
        0x0f, 0x4e, 0x10, 0xf2, 0xac, 0x2f, 0x71, 0x93, 0xcd, 0x11, 0x4f, 0xad, 0xf3, 0x70, 0x2e,
        0xcc, 0x92, 0xd3, 0x8d, 0x6f, 0x31, 0xb2, 0xec, 0x0e, 0x50, 0xaf, 0xf1, 0x13, 0x4d, 0xce,
        0x90, 0x72, 0x2c, 0x6d, 0x33, 0xd1, 0x8f, 0x0c, 0x52, 0xb0, 0xee, 0x32, 0x6c, 0x8e, 0xd0,
        0x53, 0x0d, 0xef, 0xb1, 0xf0, 0xae, 0x4c, 0x12, 0x91, 0xcf, 0x2d, 0x73, 0xca, 0x94, 0x76,
        0x28, 0xab, 0xf5, 0x17, 0x49, 0x08, 0x56, 0xb4, 0xea, 0x69, 0x37, 0xd5, 0x8b, 0x57, 0x09,
        0xeb, 0xb5, 0x36, 0x68, 0x8a, 0xd4, 0x95, 0xcb, 0x29, 0x77, 0xf4, 0xaa, 0x48, 0x16, 0xe9,
        0xb7, 0x55, 0x0b, 0x88, 0xd6, 0x34, 0x6a, 0x2b, 0x75, 0x97, 0xc9, 0x4a, 0x14, 0xf6, 0xa8,
        0x74, 0x2a, 0xc8, 0x96, 0x15, 0x4b, 0xa9, 0xf7, 0xb6, 0xe8, 0x0a, 0x54, 0xd7, 0x89, 0x6b, 0x35
    };

    private final static Logger log = LoggerFactory.getLogger(Mx1Packetizer.class);
}
