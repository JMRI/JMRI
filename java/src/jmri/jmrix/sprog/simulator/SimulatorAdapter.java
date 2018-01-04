package jmri.jmrix.sprog.simulator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import jmri.jmrix.sprog.SprogCommandStation;
import jmri.jmrix.sprog.SprogConstants.SprogMode;
import jmri.jmrix.sprog.SprogMessage;
import jmri.jmrix.sprog.SprogPortController; // no special xSimulatorController
import jmri.jmrix.sprog.SprogReply;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.jmrix.sprog.SprogTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to a simulated SPROG system.
 * <p>
 * Currently, the SPROG SimulatorAdapter reacts to commands sent from the user interface
 * with an appropriate reply message.
 * Based on jmri.jmrix.lenz.xnetsimulator.XNetSimulatorAdapter / DCCppSimulatorAdapter 2017
 * <p>
 * NOTE: Some material in this file was modified from other portions of the
 * support infrastructure.
 *
 * @author Paul Bender, Copyright (C) 2009-2010
 * @author Mark Underwood, Copyright (C) 2015
 * @author Egbert Broerse, Copyright (C) 2018
 */
public class SimulatorAdapter extends SprogPortController implements Runnable {

    // private control members
    private boolean opened = false;
    private Thread sourceThread;

    final static int SENSOR_MSG_RATE = 10;

    private boolean outputBufferEmpty = true;
    private boolean checkBuffer = true;
    private boolean trackPowerState = false;

    // Simulator responses
    String SPR_OK = "OK"; // TODO replace by documented ack reply

    public SimulatorAdapter() {
        super(new SprogSystemConnectionMemo(SprogMode.SERVICE)); // uses default user name, suppose Service mode
        setManufacturer(jmri.jmrix.sprog.SprogConnectionTypeList.SPROG);
        this.getSystemConnectionMemo().setUserName(Bundle.getMessage("SprogSimulatorTitle"));
        // create the traffic controller
        this.getSystemConnectionMemo().setSprogTrafficController(new SprogTrafficController(this.getSystemConnectionMemo()));
    }

    @Override
    public String openPort(String portName, String appName) {
        try {
            PipedOutputStream tempPipeI = new PipedOutputStream();
            log.debug("tempPipeI created {}", tempPipeI != null);
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
     * Set up all of the other objects to operate with an Sprog Simulator.
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        log.debug("set tc for memo {}", getSystemConnectionMemo().getUserName());
        SprogTrafficController control = new SprogTrafficController(getSystemConnectionMemo());
        //compare with: XNetTrafficController packets = new XNetPacketizer(new LenzCommandStation());
        control.connectPort(this);
        this.getSystemConnectionMemo().setSprogTrafficController(control);
        // do the common manager config
        //this.getSystemConnectionMemo().configureCommandStation();
        this.getSystemConnectionMemo().configureManagers();

        // start the simulator
        sourceThread = new Thread(this);
        sourceThread.setName("SPROG Simulator");
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

    // Base class methods for the SprogPortController simulated interface

    @Override
    public DataInputStream getInputStream() {
        if (!opened || pin == null) {
            log.error("getInputStream called before load(), stream not available");
        }
        log.debug("DataInputStream pin returned");
        return pin;
    }

    @Override
    public DataOutputStream getOutputStream() {
        if (!opened || pout == null) {
            log.error("getOutputStream called before load(), stream not available");
        }
        log.debug("DataOutputStream pout returned");
        return pout;
    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * Get an array of valid baud rates. This is currently just a message saying
     * its fixed.
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
        // of the SPROG command station simulation.
        log.info("SPROG Simulator Started");
        while (true) {
            try {
                synchronized (this) {
                    wait(50);
                }
            } catch (InterruptedException e) {
                log.debug("interrupted, ending");
                return;
            }
            SprogMessage m = readMessage();
            SprogReply r;
            if (log.isDebugEnabled()) {
                StringBuffer buf = new StringBuffer();
                buf.append("SPROG Simulator Thread received message: ");
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
                    buf.append("SPROG Simulator Thread sent reply: ");
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
    private SprogMessage readMessage() {
        SprogMessage msg = null;
//        log.debug("Simulator reading message");
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
     * incoming SprogMessage into an outgoing SprogReply.
     *
     * As yet, no messages receive a meaningful reply. TODO: add all supported msg as cases, fill in useful ack reply in SPR_OK
     */
    @SuppressWarnings("fallthrough")
    private SprogReply generateReply(SprogMessage msg) {
        log.debug("Generate Reply to message type {} (string = {})", msg.toString().charAt(0), msg.toString());

        String s, r;
        SprogReply reply = new SprogReply();
        char command = msg.toString().charAt(0);
        log.debug("Message type = " + command);
        switch (command) {
            // TODO add in more SPROG replies

            case 'O': // Send packet
//            case 'X': // eXit programming
//            case 'D': // Deque packet
//            case 'Q': // Que packet
//            case 'F': // display memory
//            case 'C': // program loCo
                reply = new SprogReply(SPR_OK); // confirm
                break;

            case 'A':
                log.debug("Aquire Throttle command detected");
                reply = new SprogReply(SPR_OK); // confirm
                break;

            case '>':
                log.debug("Set Throttle command detected");
                reply = new SprogReply(SPR_OK); // confirm
                break;

            case '+':
                log.debug("TRACK_POWER_ON detected");
                trackPowerState = true;
                reply = new SprogReply(SPR_OK); // confirm
                break;

            case '-':
                log.debug("TRACK_POWER_OFF detected");
                trackPowerState = false;
                reply = new SprogReply(SPR_OK); // confirm
                break;

            case '?':
                log.debug("Read_Sprog_Version detected");
                String replyString = "V0.1 - simulator";
                reply = new SprogReply(replyString); // fake version number reply
                break;
//
//            case 'L': // Read Loco
//                log.debug("Read Loco detected");
//                reply = new SprogReply(SPR_OK); // confirm
//                break;
//
//            case 'R':
//                log.debug("Read_CV detected");
//                reply = new SprogReply(SPR_OK); // confirm
//                break;

            default:
                log.debug("non-reply message detected");
                reply = new SprogReply(""); // is there a default SPROG error reply?
        }
        log.debug("Reply generated = {}", reply.toString());
        return reply;
    }

    /**
     * Write reply to output.
     * <p>
     * Copied from jmri.jmrix.nce.simulator.SimulatorAdapter.
     *
     * @param r reply on message
     */
    private void writeReply(SprogReply r) {
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
     * @returns filled message, only when the message is complete.
     * @throws IOException when presented by the input source.
     */
    private SprogMessage loadChars() throws java.io.IOException {
        int nchars;
        byte[] rcvBuffer = new byte[32];

        nchars = inpipe.read(rcvBuffer, 0, 32);
        //log.debug("new message received");
        SprogMessage msg = new SprogMessage(nchars);

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
