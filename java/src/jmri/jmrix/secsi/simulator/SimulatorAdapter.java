package jmri.jmrix.secsi.simulator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import javax.swing.JOptionPane;
import jmri.jmrix.secsi.SerialMessage;
import jmri.jmrix.secsi.SerialPortController; // no special xSimulatorController
import jmri.jmrix.secsi.SerialReply;
import jmri.jmrix.secsi.SecsiSystemConnectionMemo;
import jmri.jmrix.secsi.SerialTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to a simulated SECSI system.
 * <p>
 * Currently, the Secsi SimulatorAdapter reacts to the following commands sent from the user
 * interface with an appropriate reply {@link #generateReply(SerialMessage)}:
 * <ul>
 *     <li>Poll (length = 1)
 * </ul>
 *
 * Based on jmri.jmrix.grapevine.simulator.SimulatorAdapter 2018
 * <p>
 * NOTE: Some material in this file was modified from other portions of the
 * support infrastructure.
 *
 * @author Paul Bender, Copyright (C) 2009-2010
 * @author Mark Underwood, Copyright (C) 2015
 * @author Egbert Broerse, Copyright (C) 2018
 */
public class SimulatorAdapter extends SerialPortController implements jmri.jmrix.SerialPortAdapter, Runnable {

    // private control members
    private boolean opened = false;
    private Thread sourceThread;

    private boolean outputBufferEmpty = true;
    private boolean checkBuffer = true;
    /**
     * Simulator auto-init setting for number of banks to auto-reply on poll
     */
    private int autoInit = 0;

    /**
     * Create a new SimulatorAdapter.
     */
    public SimulatorAdapter() {
        super(new SecsiSystemConnectionMemo("V", Bundle.getMessage("SecsiSimulatorName"))); // pass customized user name
        setManufacturer(jmri.jmrix.secsi.SerialConnectionTypeList.TRACTRONICS);
    }

    /**
     * {@inheritDoc}
     * Simulated input/output pipes.
     */
    @Override
    public String openPort(String portName, String appName) {
        try {
            PipedOutputStream tempPipeI = new PipedOutputStream();
            log.debug("tempPipeI created");
            pout = new DataOutputStream(tempPipeI);
            inpipe = new DataInputStream(new PipedInputStream(tempPipeI));
            log.debug("inpipe created {}", inpipe != null);
            PipedOutputStream tempPipeO = new PipedOutputStream();
            outpipe = new DataOutputStream(tempPipeO);
            pin = new DataInputStream(new PipedInputStream(tempPipeO));
        } catch (java.io.IOException e) {
            log.error("init (pipe): Exception: {}", e.toString());
        }
        opened = true;
        return null; // indicates OK return
    }

    /**
     * Set if the output buffer is empty or full. This should only be set to
     * false by external processes.
     *
     * @param s true if output buffer is empty; false otherwise
     */
    synchronized public void setOutputBufferEmpty(boolean s) {
        outputBufferEmpty = s;
    }

    /**
     * Can the port accept additional characters? The state of CTS determines
     * this, as there seems to be no way to check the number of queued bytes and
     * buffer length. This might go false for short intervals, but it might also
     * stick off if something goes wrong.
     *
     * @return true if port can accept additional characters; false otherwise
     */
    public boolean okToSend() {
        if (checkBuffer) {
            log.debug("Buffer Empty: {}", outputBufferEmpty);
            return (outputBufferEmpty);
        } else {
            log.debug("No Flow Control or Buffer Check");
            return (true);
        }
    }

    /**
     * Set up all of the other objects to operate with a SECSI
     * connected to this port.
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        log.debug("set tc for memo {}", getSystemConnectionMemo().getUserName());
        ((SecsiSystemConnectionMemo) getSystemConnectionMemo()).getTrafficController().connectPort(this);
        // do the common manager config
        ((SecsiSystemConnectionMemo) getSystemConnectionMemo()).configureManagers();

        // start the simulator
        sourceThread = new Thread(this);
        sourceThread.setName("Secsi Simulator");
        sourceThread.setPriority(Thread.MIN_PRIORITY);
        sourceThread.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect() throws java.io.IOException {
        log.debug("connect called");
        super.connect();
    }

    // Base class methods for the SECSI SerialPortController simulated interface

    /**
     * {@inheritDoc}
     */
    @Override
    public DataInputStream getInputStream() {
        if (!opened || pin == null) {
            log.error("getInputStream called before load(), stream not available");
        }
        log.debug("DataInputStream pin returned");
        return pin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataOutputStream getOutputStream() {
        if (!opened || pout == null) {
            log.error("getOutputStream called before load(), stream not available");
        }
        log.debug("DataOutputStream pout returned");
        return pout;
    }

    /**
     * {@inheritDoc}
     * @return always true, given this SimulatorAdapter is running
     */
    @Override
    public boolean status() {
        return opened;
    }

    /**
     * Get an array of valid baud rates.
     *
     * @return null
     */
    @Override
    public String[] validBaudRates() {
        log.debug("validBaudRates should not have been invoked");
        return null;
    }

    @Override
    public String getCurrentBaudRate() {
        return "";
    }

    @Override
    public void run() { // start a new thread
        // This thread has one task. It repeatedly reads from the input pipe
        // and writes an appropriate response to the output pipe. This is the heart
        // of the Secsi command station simulation.
        log.info("Secsi Simulator Started");
        while (true) {
            try {
                synchronized (this) {
                    wait(50);
                }
            } catch (InterruptedException e) {
                log.debug("interrupted, ending");
                return;
            }
            SerialMessage m = readMessage();
            SerialReply r;
            if (log.isDebugEnabled()) {
                StringBuffer buf = new StringBuffer();
                buf.append("Secsi Simulator Thread received message: ");
                if (m != null) {
                    for (int i = 0; i < m.getNumDataElements(); i++) {
                        buf.append(Integer.toHexString(0xFF & m.getElement(i)) + " ");
                    }
                } else {
                    buf.append("null message buffer");
                }
                log.trace(buf.toString()); // generates a lot of traffic
            }
            if (m != null) {
                r = generateReply(m);
                if (r != null) { // ignore errors and null replies
                    writeReply(r);
                    if (log.isDebugEnabled()) {
                        StringBuffer buf = new StringBuffer();
                        buf.append("Secsi Simulator Thread sent reply: ");
                        for (int i = 0; i < r.getNumDataElements(); i++) {
                            buf.append(Integer.toHexString(0xFF & r.getElement(i)) + " ");
                        }
                        log.debug(buf.toString());
                    }
                }
            }
        }
    }

    /**
     * Read one incoming message from the buffer
     * and set outputBufferEmpty to true.
     */
    private SerialMessage readMessage() {
        SerialMessage msg = null;
        // log.debug("Simulator reading message"); // lots of traffic in loop
        try {
            if (inpipe != null && inpipe.available() > 0) {
                msg = loadChars();
            }
        } catch (java.io.IOException e) {
            // should do something meaningful here.
        }
        setOutputBufferEmpty(true);
        return (msg);
    }

    // operational instance variable (should not be preserved between runs)
    protected boolean[] nodesSet = new boolean[127]; // node init received and replied?

    /**
     * This is the heart of the simulation. It translates an
     * incoming SerialMessage into an outgoing SerialReply.
     * See {@link jmri.jmrix.secsi.SerialNode#markChanges(SerialReply)} and
     * the (draft) secsi <a href="../package-summary.html">Binary Message Format Summary</a>.
     *
     * @param msg the message received in the simulated node
     * @return a single Secsi message to confirm the requested operation, or a series
     * of messages for each (fictitious) node/pin/state. To ignore certain commands, return null.
     */
    @SuppressWarnings("fallthrough")
    private SerialReply generateReply(SerialMessage msg) {
        int nodeaddr = msg.getAddr();
        log.debug("Generate Reply to message to node {} (string = {}; {})", nodeaddr, msg.toString(), msg.getElement(1));
        SerialReply reply = new SerialReply();  // reply length is determined by highest byte added
        if (nodesSet[nodeaddr] == false) { // only Polls expect a reply from the node
            log.debug("Poll message detected");
            reply.setElement(0, nodeaddr); // node addres from msg element(0)
            reply.setElement(1, 47); // poll reply has only 2 elements
            nodesSet[nodeaddr] = true; // mark node as inited
//            for (int j = 1; j < 2; j++) {
//                reply.setElement(j, 47);
//            }
            return reply;
        }
        log.debug(reply == null ? "Message ignored" : "Reply generated " + reply.toString());
        return null; //reply;
        // Poll will give an error:
        // jmrix.AbstractMRTrafficController ERROR - Transmit thread terminated prematurely by:
        // java.lang.ArrayIndexOutOfBoundsException: 1 [secsi.SerialTrafficController Transmit thread]
    }

    /**
     * Write reply to output.
     * <p>
     * Adapted from jmri.jmrix.nce.simulator.SimulatorAdapter.
     *
     * @param r reply on message
     */
    private void writeReply(SerialReply r) {
        if (r == null) {
            return; // there is no reply to be sent
        }
        for (int i = 0; i < r.getNumDataElements(); i++) {
            try {
                outpipe.writeByte((byte) r.getElement(i));
            } catch (java.io.IOException ex) {
            }
        }
        try {
            outpipe.flush();
        } catch (java.io.IOException ex) {
        }
    }

    protected int currentAddr = -1; // at startup, can't match
    /**
     * Get characters from the input source. No opcode, so must read per byte.
     * Length will be either 1, 5 or 9 bytes.
     * <p>
     * Only used in the Receive thread.
     *
     * @return filled message, only when the message is complete.
     * @throws IOException when presented by the input source.
     */
    private SerialMessage loadChars() throws java.io.IOException {
        int[] chars = new int[1];
        int i = 0;
        // get 1st byte, see if ending too soon
        byte char0 = readByteProtected(inpipe);
        if (nodesSet[char0 & 0xFF] == true) { // after 1 byte node poll message, expect longer messages to node
            byte chari;
            chars = new int[5]; // temporary store of bytes
            for (i = 1; i < 5; i++) { // reading next max 9 bytes (but inpipe never gets empty)
                log.debug("reading rest of message in simulator");
                try {
                    chari = readByteProtected(inpipe);
                    chars[i] = (chari & 0xFF);
                } catch (java.io.IOException e) {
                    break;
                }
            }
        } else {
            nodesSet[char0 & 0xFF] = false;
        }
        // copy bytes to Message
        SerialMessage msg = new SerialMessage(i);
        msg.setElement(0, char0 & 0xFF); // address
        for (int k = 1; k < i; k++) { // copy bytes
            msg.setElement(k, chars[k] & 0xFF); // bytes read
        }
        log.debug("message received by simulator, length={}", i);
        return msg;
    }

    /**
     * Read a single byte, protecting against various timeouts, etc.
     * <p>
     * When a port is set to have a receive timeout (via the
     * enableReceiveTimeout() method), some will return zero bytes or an
     * EOFException at the end of the timeout. In that case, the read should be
     * repeated to get the next real character.
     * <p>
     * Copied from DCCppSimulatorAdapter
     */
    protected byte readByteProtected(DataInputStream istream) throws java.io.IOException {
        byte[] rcvBuffer = new byte[1];
        while (true) { // loop will repeat until character found
            int nchars;
            nchars = istream.read(rcvBuffer, 0, 1);
            if (nchars > 0) {
                return rcvBuffer[0];
            }
        }
    }

    // streams to share with user class
    private DataOutputStream pout = null; // this is provided to classes who want to write to us
    private DataInputStream pin = null; // this is provided to classes who want data from us
    // internal ends of the pipes
    private DataOutputStream outpipe = null; // feed pin
    private DataInputStream inpipe = null; // feed pout

    private final static Logger log = LoggerFactory.getLogger(SimulatorAdapter.class);

}
