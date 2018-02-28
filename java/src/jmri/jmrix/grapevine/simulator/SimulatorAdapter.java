package jmri.jmrix.grapevine.simulator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import javax.swing.JOptionPane;
import jmri.jmrix.grapevine.SerialMessage;
import jmri.jmrix.grapevine.SerialPortController; // no special xSimulatorController
import jmri.jmrix.grapevine.SerialReply;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.jmrix.grapevine.SerialTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to a simulated Grapevine system.
 * <p>
 * Currently, the Grapevine SimulatorAdapter reacts to the following commands sent from the user
 * interface with an appropriate reply message:
 * <ul>
 *     <li>Software version (poll)
 *     <li>Renumber (displays dialog: not supported)
 *     <li>Node Init (2 replies)
 *     <li>Set signal/sensor/turnout (echoes message)
 * </ul>
 * {@link #generateReply(SerialMessage)}
 * <p>
 * Based on jmri.jmrix.lenz.xnetsimulator.XNetSimulatorAdapter / EasyDCCSimulatorAdapter 2017
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
    // Simulator auto-init setting
    private int autoInit = 0;

    public SimulatorAdapter() {
        super(new GrapevineSystemConnectionMemo("G", Bundle.getMessage("GrapevineSimulatorName"))); // pass customized user name
        option1Name = "InitPreference"; // NOI18N
        // init pref setting, the default is No init
        options.put(option1Name, new Option(Bundle.getMessage("AutoInitLabel"),
                new String[]{Bundle.getMessage("ButtonNoInit"),
                Bundle.getMessage("ButtonAll"), Bundle.getMessage("Button4Each")}));
        setManufacturer(jmri.jmrix.grapevine.SerialConnectionTypeList.PROTRAK);
    }

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
     * Set up all of the other objects to operate with a GrapevineSimulator
     * connected to this port.
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        log.debug("set tc for memo {}", getSystemConnectionMemo().getUserName());
        SerialTrafficController control = new GrapevineSimulatorTrafficController(getSystemConnectionMemo());
        //compare with: XNetTrafficController packets = new XNetPacketizer(new LenzCommandStation());
        control.connectPort(this);
        getSystemConnectionMemo().setTrafficController(control);
        // do the common manager config
        getSystemConnectionMemo().configureManagers();

        if (getOptionState(option1Name).equals(getOptionChoices(option1Name)[0])) {
            // no auto-init
            autoInit = 0;
        } else if (getOptionState(option1Name).equals(getOptionChoices(option1Name)[1])) {
            // auto-init all
            autoInit = 1;
        } else {
            autoInit = 2;
        }

        // start the simulator
        sourceThread = new Thread(this);
        sourceThread.setName("Grapevine Simulator");
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

    // Base class methods for the Grapevine SerialPortController simulated interface

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
        // of the Grapevine command station simulation.
        log.info("Grapevine Simulator Started");
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
                buf.append("Grapevine Simulator Thread received message: ");
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
                writeReply(r);
                if (log.isDebugEnabled() && r != null) {
                    StringBuffer buf = new StringBuffer();
                    buf.append("Grapevine Simulator Thread sent reply: ");
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
     * {@link jmri.jmrix.grapevine.SerialMessage#generateReply(SerialMessage)}
     * See the Grapevine <a href="../package-summary.html">Binary Message Format Summary</a>
     *
     * @param msg the message received in the simulated node
     */
    @SuppressWarnings("fallthrough")
    private SerialReply generateReply(SerialMessage msg) {
        log.debug("Generate Reply to message from node {} (string = {})", msg.getAddr(), msg.toString());

        SerialReply reply = new SerialReply(); // reply length is determined by highest byte added
        int nodeaddr = msg.getAddr();          // node addres from element(0)
        int b1 = msg.getElement(0);            // raw hex value from element(0)
        int b2 = msg.getElement(1);            // command instruction
        int b3 = msg.getElement(2);            // element(2), must repeat node address
        int b4 = msg.getElement(3);            // instruction
        int bank = (b4 & 0xF0) >> 4;           // bank # on node
        log.debug("Message address={} b1= {} b2={} b3={} b4={}", nodeaddr, b1, b2, b3, b4);
        switch (b2) {

            case 119 :
                log.debug("get software version (poll) message detected");
                // 2 byte software version number reply
                reply.setElement(0, nodeaddr | 0x80);
                reply.setElement(1, 9); // pretend version "9"
                // no parity
                break;

            case 0x71 :
                log.debug("init node message 1 detected - ASD sensors");
                // 4 byte init reply, wanted?
                if (autoInit > 0) { // preference init option
                    log.debug("start init of node {}", nodeaddr);
                    NodeResponse(nodeaddr, 1, 5, autoInit); // all bits
                    log.debug("ready init of node {}", nodeaddr);
                }
                // all replies generated by NodeResponse()
                reply.setElement(0, nodeaddr | 0x80);
                reply.setElement(1, b2); // echo id + state
                reply.setElement(2, nodeaddr | 0x80);
                reply.setElement(3, bank); // echo bank
                reply = setParity(reply,0);
                break;

            case 0x73 :
                log.debug("init node message 2 detected - parallel sensors");
                // 4 byte init reply
                reply.setElement(0, nodeaddr | 0x80);
                reply.setElement(1, b2); // echo id + state
                reply.setElement(2, nodeaddr | 0x80);
                reply.setElement(3, bank); // echo bank (bit 1234): 1-3 = signals, 4-5 = sensors
                reply = setParity(reply,0);
                break;

            default:
                if (bank == 6) { // this is the rename command, with element 2 = new node number
                    JOptionPane.showMessageDialog(null,
                            Bundle.getMessage("RenumberSupport"),
                            Bundle.getMessage("MessageTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    log.debug("rename command not supported, old address: {}, new address: {}, bank: {}",
                            nodeaddr, b2, bank);
                    break;
                } else if ((b1 & 0x7F) == 0) { // 0 = error
                    log.debug("general error: coded as: {}", ((b4 & 0x70) >> 4 - 1));
                    break;
                } else {
                    log.debug("echo normal command, node {} bank {}", nodeaddr, bank);
                    // 4 byte general reply
                    reply.setElement(0, nodeaddr | 0x80);
                    reply.setElement(1, b2);  // normally: command (values 0x6, 0x7 are for signals
                    reply.setElement(2, nodeaddr | 0x80);
                    reply.setElement(3, bank); // 0 = error, bank 1..3 for signals, 4..5 sensors (and parity)
                    reply = setParity(reply, 0);
                }
        }
        log.debug("Reply generated = {}", reply.toString());
        return (reply);
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

    /**
     * Get characters from the input source.
     * <p>
     * Only used in the Receive thread.
     *
     * @returns filled message, only when the message is complete.
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

    /**
     * Set parity on simulated Grapevine Node reply.
     * Code copied from {@link SerialMessage#setParity(int)}
     *
     * @param r the SerialReply to complete
     * @param start bit index to start
     * @return SerialReply with parity set
     */
    public SerialReply setParity(SerialReply r, int start) {
        // nibble sum method
        int sum = r.getElement(0 + start) & 0x0F;
        sum += (r.getElement(0 + start) & 0x70) >> 4;
        sum += (r.getElement(1 + start) * 2) & 0x0F;
        sum += ((r.getElement(1 + start) * 2) & 0xF0) >> 4;
        sum += (r.getElement(3 + start) & 0x70) >> 4;

        int parity = 16 - (sum & 0xF);

        r.setElement(3 + start, (r.getElement(3 + start) & 0xF0) | (parity & 0xF));
        return r;
    }

    int SignalBankSize = 16;  // theoretically: 16
    int SensorBankSize = 999; // theoretically: 0x3F

    /**
     * Pretend a node init reply for a (limited) range of banks and bits. Is this a proper simulation of hardware?
     * <p>
     * Based on information in {@link jmri.jmrix.grapevine.SerialMessage#staticFormat(int, int, int, int)}.
     *
     * @param node      the node address
     * @param startBank first bank id to report
     * @param endBank   last bank id to report
     */
    private void NodeResponse(int node, int startBank, int endBank, int initBits) {
        if (node < 0 || node > 127) {
            log.warn("Invalid Node Address; no response generated");
            return; // node address invalid
        }
        if (initBits > 1) { // leave at max when 1
            SignalBankSize = initBits;
            SensorBankSize = initBits;
        }
        byte countByte; // counter for signal/sensor id calculation
        int elementThree;
        SerialReply nReply = new SerialReply(); // reply length is determined by highest byte added
        nReply.setElement(0, node | 0x80);
        nReply.setElement(2, node | 0x80);
        for (int k = startBank; k <= endBank; k++) {
            if (k <= 3) { // bank 1 to 3, signals
                switch (k) {
                    case 1:
                        elementThree = 0x08; // bank (bit 1234): 1-3 = signals
                        break;
                    case 2:
                        elementThree = 0x11; // repeat for bank 2 (reply.setElement(3, 0x11))
                        break;
                    case 3:
                    default:
                        elementThree = 0x18; // repeat for bank 3 (reply.setElement(3, 0x18))
                }
                nReply.setElement(3, elementThree);
                for (int j = 1; j <= SignalBankSize; j++) { // send state of each signal bit (banks 1, 2 3)
                    log.debug("Sending output state of bank {}, bit {} on node {}", k, j, node);
                    Integer countInt = Integer.valueOf(j);
                    countByte = countInt.byteValue();
                    nReply.setElement(1, (countByte << 3 & 0x06)); // id (bit 2345) + command (bit 678): set to Red
                    nReply = setParity(nReply, 0);
                    writeReply(nReply);
                }
            } else { // bank 4 and 5, sensors
                switch (k) {
                    case 4:
                        elementThree = 0x40; // bank (bit 1234): 4 = sensors
                        break;
                    case 5:
                    default:
                        elementThree = 0x50; // repeat for bank 5 (reply.setElement(3, 0x50))
                }
                nReply.setElement(3, elementThree); // bank (bit 1234): 4-5 = sensors
                for (int j = 1; j <= SensorBankSize; j++) { // send state of each sensor bit (banks 4, 5)
                    log.debug("Sending sensor state of bank {}, bit {} on node {}", k, j, node);
                    Integer countInt = Integer.valueOf(j);
                    countByte = countInt.byteValue();

                    nReply.setElement(1, (countByte << 1 & 0x01)); // id (bit 234567) + state (bit 8): inactive
                    nReply = setParity(nReply,0);
                    writeReply(nReply);
                }
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
