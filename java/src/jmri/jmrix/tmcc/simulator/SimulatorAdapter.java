package jmri.jmrix.tmcc.simulator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;

import jmri.jmrix.tmcc.SerialMessage;
import jmri.jmrix.tmcc.SerialPortController; // no special xSimulatorController
import jmri.jmrix.tmcc.SerialReply;
import jmri.jmrix.tmcc.SerialTrafficController;
import jmri.jmrix.tmcc.TmccSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to a simulated TMCC system.
 * <p>
 * Currently, the TMCC SimulatorAdapter reacts to commands sent from the user interface
 * with messages an appropriate reply message.
 * Based on jmri.jmrix.lenz.xnetsimulator.XNetSimulatorAdapter / DCCppSimulatorAdapter 2017
 * <p>
 * NOTE: Some material in this file was modified from other portions of the
 * support infrastructure.
 *
 * @author Paul Bender, Copyright (C) 2009-2010
 * @author Mark Underwood, Copyright (C) 2015
 * @author Egbert Broerse, Copyright (C) 2017
 */
public class SimulatorAdapter extends SerialPortController implements Runnable {

    // private control members
    private boolean opened = false;
    private Thread sourceThread;

    final static int SENSOR_MSG_RATE = 10;

    private boolean outputBufferEmpty = true;
    private boolean checkBuffer = true;
    // Simulator responses
    char EDC_OPS = 0x4F;
    char EDC_PROG = 0x50;

    public SimulatorAdapter() {
        super(new TmccSystemConnectionMemo("T", "TMCC Simulator")); // pass customized user name
        setManufacturer(jmri.jmrix.tmcc.SerialConnectionTypeList.LIONEL);
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
            log.error("init (pipe): Exception: " + e.toString());
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
            log.debug("Buffer Empty: " + outputBufferEmpty);
            return (outputBufferEmpty);
        } else {
            log.debug("No Flow Control or Buffer Check");
            return (true);
        }
    }

    /**
     * Set up all of the other objects to operate with a TMCCSimulator
     * connected to this port.
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        log.debug("set tc for memo {}", getSystemConnectionMemo().getUserName());
        SerialTrafficController control = new SerialTrafficController(getSystemConnectionMemo());
        //compare with: XNetTrafficController packets = new XNetPacketizer(new LenzCommandStation());
        control.connectPort(this);
        this.getSystemConnectionMemo().setTrafficController(control);
        // do the common manager config
        this.getSystemConnectionMemo().configureManagers();

        // start the simulator
        sourceThread = new Thread(this);
        sourceThread.setName("TMCC Simulator");
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

    // Base class methods for the SerialPortController simulated interface

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
        // of the TMCC command station simulation.
        log.info("TMCC Simulator Started");
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
                buf.append("TMCC Simulator Thread received message: ");
                if (m != null) {
                    for (int i = 0; i < m.getNumDataElements(); i++) {
                        buf.append(Integer.toHexString(0xFF & m.getElement(i)) + " ");
                    }
                } else {
                    buf.append("null message buffer");
                }
//                log.debug(buf.toString());
            }
            if (m != null) {
                r = generateReply(m);
                writeReply(r);
                if (log.isDebugEnabled() && r != null) {
                    StringBuffer buf = new StringBuffer();
                    buf.append("TMCC Simulator Thread sent reply: ");
                    for (int i = 0; i < r.getNumDataElements(); i++) {
                        buf.append(Integer.toHexString(0xFF & r.getElement(i)) + " ");
                    }
                    log.debug(buf.toString());
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
     * incoming SerialMessage into an outgoing SerialReply.
     *
     * As yet, no a meaningful reply. TODO: Throttle
     */
    private SerialReply generateReply(SerialMessage msg) {
        log.debug("Generate Reply to message type {} (string = {})", msg.toString().charAt(0), msg.toString());

        SerialReply reply = new SerialReply();
        char command = msg.toString().charAt(0);
        log.debug("Message type = " + command);
        switch (command) {

            default:
                log.debug("non-reply message detected");
        }
        log.debug("Reply generated = {}", reply.toString());
        // no confirm sequence for TMCC
        return (reply);
    }

    /**
     * Write reply to output.
     * <p>
     * Copied from jmri.jmrix.nce.simulator.SimulatorAdapter.
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

    /**
     * Get characters from the input source.
     * <p>
     * Only used in the Receive thread.
     *
     * @return filled message, only when the message is complete.
     * @throws IOException when presented by the input source.
     */
    private SerialMessage loadChars() throws java.io.IOException {
        int nchars;
        byte[] rcvBuffer = new byte[32];

        nchars = inpipe.read(rcvBuffer, 0, 32);
        //log.debug("new message received");
        SerialMessage msg = new SerialMessage(nchars);

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
