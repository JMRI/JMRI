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
 * Can be loaded as either a Programmer or Command Station.
 * The SPROG SimulatorAdapter reacts to commands sent from the user interface
 * with an appropriate reply message.
 * <p>
 * Based on jmri.jmrix.lenz.xnetsimulator.XNetSimulatorAdapter / DCCppSimulatorAdapter 2017
 *
 * @author Paul Bender, Copyright (C) 2009-2010
 * @author Mark Underwood, Copyright (C) 2015
 * @author Egbert Broerse, Copyright (C) 2018
 */
public class SimulatorAdapter extends SprogPortController implements Runnable {

    // private control members
    private boolean opened = false;
    private Thread sourceThread;
    private SprogTrafficController control;

    private boolean outputBufferEmpty = true;
    private boolean checkBuffer = true;
    private SprogMode operatingMode = SprogMode.SERVICE;

    // Simulator responses
    String SPR_OK = "OK";
    String SPR_NO = "No Ack";
    String SPR_PR = "\nP> "; // prompt

    public SimulatorAdapter() {
        super(new SprogSystemConnectionMemo(SprogMode.SERVICE)); // use default user name
        // starts as SERVICE mode (Programmer); may be set to OPS (Command Station) from connection option
        setManufacturer(jmri.jmrix.sprog.SprogConnectionTypeList.SPROG);
        this.getSystemConnectionMemo().setUserName(Bundle.getMessage("SprogSimulatorTitle"));
        // create the traffic controller
        control = new SprogTrafficController(this.getSystemConnectionMemo());
        this.getSystemConnectionMemo().setSprogTrafficController(control);

        options.put("OperatingMode", // NOI18N
                new Option(Bundle.getMessage("MakeLabel", Bundle.getMessage("SprogSimOption")), // NOI18N
                        new String[]{Bundle.getMessage("SprogProgrammerTitle"),
                                Bundle.getMessage("SprogCSTitle")}, true));
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
     * Set up all of the other objects to operate with a Sprog Simulator.
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        this.getSystemConnectionMemo().getSprogTrafficController().connectPort(this);

        if (getOptionState("OperatingMode") != null && getOptionState("OperatingMode").equals(Bundle.getMessage("SprogProgrammerTitle"))) {
            operatingMode = SprogMode.SERVICE;
        } else { // default, also used after Locale change
            operatingMode = SprogMode.OPS;
        }
        this.getSystemConnectionMemo().setSprogMode(operatingMode); // first update mode in memo
        this.getSystemConnectionMemo().configureCommandStation();   // CS only if in OPS mode, memo will take care of that
        this.getSystemConnectionMemo().configureManagers();         // wait for mode to be correct

        if (getOptionState("TrackPowerState") != null && getOptionState("TrackPowerState").equals(Bundle.getMessage("PowerStateOn"))) {
            try {
                this.getSystemConnectionMemo().getPowerManager().setPower(jmri.PowerManager.ON);
            } catch (jmri.JmriException e) {
                log.error(e.toString());
            }
        }

        log.debug("SimulatorAdapter configure() with prefix = {}", this.getSystemConnectionMemo().getSystemPrefix());
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
        return (pout != null && pin != null);
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
                    buf.append(m);
                } else {
                    buf.append("null message buffer");
                }
                //log.debug(buf.toString()); // generates a lot of output
            }
            if (m != null) {
                r = generateReply(m);
                writeReply(r);
                if (log.isDebugEnabled() && r != null) {
                    log.debug("Simulator Thread sent Reply: \"{}\"", r);
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
     * incoming SprogMessage into an outgoing SprogReply.
     *
     * Based on SPROG information from A. Crosland.
     * @see jmri.jmrix.sprog.SprogReply#value()
     */
    private SprogReply generateReply(SprogMessage msg) {
        log.debug("Generate Reply to message type {} (string = {})", msg.toString().charAt(0), msg.toString());

        SprogReply reply = new SprogReply();
        int i = 0;
        char command = msg.toString().charAt(0);
        log.debug("Message type = " + command);
        switch (command) {

            case 'I':
                log.debug("CurrentQuery detected");
                reply = new SprogReply("= h3E7\n"); // reply fictionary current (decimal 999mA)
                break;

            case 'C':
            case 'V':
                log.debug("Read/Write CV detected");
                reply = new SprogReply("= " + msg.toString().substring(2) + "\n"); // echo CV value (hex)
                break;

            case 'O':
                log.debug("Send packet command detected");
                reply = new SprogReply("= " + msg.toString().substring(2) + "\n"); // echo command (hex)
                break;

            case 'A':
                log.debug("Address (open Throttle) command detected");
                reply = new SprogReply(msg.toString().substring(2) + "\n"); // echo address (decimal)
                break;

            case '>':
                log.debug("Set speed (Throttle) command detected");
                reply = new SprogReply(msg.toString().substring(1) + "\n"); // echo speed (decimal)
                break;

            case '+':
                log.debug("TRACK_POWER_ON detected");
                //reply = new SprogReply(SPR_PR);
                break;

            case '-':
                log.debug("TRACK_POWER_OFF detected");
                //reply = new SprogReply(SPR_PR);
                break;

            case '?':
                log.debug("Read_Sprog_Version detected");
                String replyString = "\nSPROG II Ver 4.3\n";
                reply = new SprogReply(replyString);
                break;

            case 'M':
                log.debug("Mode Word detected");
                reply = new SprogReply("P>M=h800\n"); // default mode reply
                break;

            case 'S':
                log.debug("getStatus detected");
                reply = new SprogReply("OK\n");
                break;

            default:
                log.debug("non-reply message detected: {}", msg.toString());
                reply = new SprogReply("!E\n"); // SPROG error reply
        }
        i = reply.toString().length();
        reply.setElement(i++, 'P'); // add prompt to all replies
        reply.setElement(i++, '>');
        reply.setElement(i++, ' ');
        log.debug("Reply generated = \"{}\"", reply.toString());
        return reply;
    }

    /**
     * Write reply to output.
     * <p>
     * Copied from jmri.jmrix.nce.simulator.SimulatorAdapter,
     * adapted for {@link jmri.jmrix.sprog.SprogTrafficController#handleOneIncomingReply()}.
     *
     * @param r reply on message
     */
    private void writeReply(SprogReply r) {
        if (r == null) {
            return; // there is no reply to be sent
        }
        int len = r.getNumDataElements();
        for (int i = 0; i < len; i++) {
            try {
                outpipe.writeByte((byte) r.getElement(i));
                log.debug("{} of {} bytes written to outpipe", i + 1, len);
                if (pin.available() > 0) {
                    control.handleOneIncomingReply();
                }
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
    private SprogMessage loadChars() throws java.io.IOException {
        // code copied from EasyDcc/NCE Simulator
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
    private DataOutputStream pout = null;    // this is provided to classes who want to write to us
    private DataInputStream pin = null;      // this is provided to classes who want data from us
    // internal ends of the pipes
    private DataOutputStream outpipe = null; // feed pin
    private DataInputStream inpipe = null;   // feed pout

    private final static Logger log = LoggerFactory.getLogger(SimulatorAdapter.class);

}
