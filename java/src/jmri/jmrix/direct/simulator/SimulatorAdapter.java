package jmri.jmrix.direct.simulator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;

import jmri.jmrix.direct.DirectSystemConnectionMemo;
import jmri.jmrix.direct.Message;
import jmri.jmrix.direct.PortController; // no special xSimulatorController
import jmri.jmrix.direct.Reply;
import jmri.jmrix.direct.TrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to a simulated DirectDrive system.
 * <p>
 * Currently, the Direct SimulatorAdapter reacts to the following commands sent from the user
 * interface with an appropriate reply {@link #generateReply(Message)}:
 * <ul>
 *     <li>N/A
 * </ul>
 *
 * Based on jmri.jmrix.lenz.xnetsimulator.XNetSimulatorAdapter / EasyDCCSimulatorAdapter 2017
 * <p>
 * NOTE: Some material in this file was modified from other portions of the
 * support infrastructure.
 *
 * @author Paul Bender, Copyright (C) 2009-2010
 * @author Mark Underwood, Copyright (C) 2015
 * @author Egbert Broerse, Copyright (C) 2018
 */
public class SimulatorAdapter extends PortController implements Runnable {

    // private control members
    private boolean opened = false;
    private Thread sourceThread;

    private boolean outputBufferEmpty = true;
    private boolean checkBuffer = true;

    /**
     * Create a new SimulatorAdapter.
     */
    public SimulatorAdapter() {
        super(new DirectSystemConnectionMemo("N", Bundle.getMessage("DirectSimulatorName"))); // pass customized user name

        setManufacturer(jmri.jmrix.direct.DirectConnectionTypeList.DIRECT);
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
     * Set up all of the other objects to operate with a DirectSimulator
     * connected to this port.
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        TrafficController tc = new TrafficController((jmri.jmrix.direct.DirectSystemConnectionMemo)getSystemConnectionMemo());
        ((jmri.jmrix.direct.DirectSystemConnectionMemo)getSystemConnectionMemo()).setTrafficController(tc);
        // connect to the traffic controller
        tc.connectPort(this);
        log.debug("set tc for memo {}", getSystemConnectionMemo().getUserName());

        // do the common manager config
        ((jmri.jmrix.direct.DirectSystemConnectionMemo)getSystemConnectionMemo()).configureManagers();

        // start the simulator: Notice that normally, transmission is not a threaded operation!
        sourceThread = new Thread(this);
        sourceThread.setName("Direct Simulator"); // NOI18N
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

    // Base class methods for the Direct SerialPortController simulated interface

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
     * {@inheritDoc}
     *
     * @return null
     */
    @Override
    public String[] validBaudRates() {
        log.debug("validBaudRates should not have been invoked");
        return new String[]{};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{};
    }

    @Override
    public String getCurrentBaudRate() {
        return "";
    }

    @Override
    public String getCurrentPortName(){
        return "";
    }

    @Override
    public void run() { // start a new thread
        // This thread has one task. It repeatedly reads from the input pipe
        // and writes an appropriate response to the output pipe. This is the heart
        // of the Direct command station simulation.
        log.info("Direct Simulator Started");
        while (true) {
            try {
                synchronized (this) {
                    wait(50);
                }
            } catch (InterruptedException e) {
                log.debug("interrupted, ending");
                return;
            }
            Message m = readMessage();
            Reply r;
            if (log.isDebugEnabled()) {
                StringBuffer buf = new StringBuffer();
                buf.append("Direct Simulator Thread received message: ");
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
                if (r != null) { // ignore errors
                    writeReply(r);
                    if (log.isDebugEnabled()) {
                        StringBuffer buf = new StringBuffer();
                        buf.append("Direct Simulator Thread sent reply: ");
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
     * Read one incoming message from the buffer and set
     * outputBufferEmpty to true.
     */
    private Message readMessage() {
        Message msg = null;
        // log.debug("Simulator reading message");
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

    /**
     * This is the heart of the simulation. It translates an
     * incoming Message into an outgoing Reply.
     *
     * @param msg the message received
     * @return a single Direct message to confirm the requested operation.
     * To ignore certain commands, return null.
     */
    private Reply generateReply(Message msg) {
        log.debug("Generate Reply to message (string = {})", msg.toString());

        Reply reply = new Reply(); // reply length is determined by highest byte added
        int addr = msg.getAddr();          // address from element(0)
        log.debug("Message address={}", addr);

        switch (addr) { // use a more meaningful key

            case 3:
                reply.setElement(0, addr | 0x80);
                reply.setElement(1, 0); // pretend speed 0
                // no parity
                break;

            default:
                reply = null;
        }
        log.debug(reply == null ? "Message ignored" : "Reply generated " + reply.toString());
        return (reply);
    }

    /**
     * Write reply to output.
     * <p>
     * Adapted from jmri.jmrix.nce.simulator.SimulatorAdapter.
     *
     * @param r reply on message
     */
    private void writeReply(Reply r) {
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

    /**
     * Get characters from the input source.
     * <p>
     * Only used in the Receive thread.
     *
     * @return filled message, only when the message is complete.
     * @throws IOException when presented by the input source.
     */
    private Message loadChars() throws java.io.IOException {
        int nchars;
        byte[] rcvBuffer = new byte[32];

        nchars = inpipe.read(rcvBuffer, 0, 32);
        log.debug("new message received");
        Message msg = new Message(nchars);

        for (int i = 0; i < nchars; i++) {
            msg.setElement(i, rcvBuffer[i] & 0xFF);
        }
        return msg;
    }

    // streams to share with user class
    private DataOutputStream pout = null; // this is provided to classes who want to write to us
    private DataInputStream pin = null; // this is provided to classes who want data from us
    // internal ends of the pipes
    private DataOutputStream outpipe = null; // feed pin
    private DataInputStream inpipe = null; // feed pout

    private final static Logger log = LoggerFactory.getLogger(SimulatorAdapter.class);

}
