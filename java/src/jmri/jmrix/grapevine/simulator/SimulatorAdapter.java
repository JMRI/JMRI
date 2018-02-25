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
 *     <li>Software version (poll)</li>
 *     <li>Node Init (2 replies)</li>
 * </ul>
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

    final static int SENSOR_MSG_RATE = 10;

    private boolean outputBufferEmpty = true;
    private boolean checkBuffer = true;
    // Simulator responses
    char EDC_OPS = 0x4F;
    char EDC_PROG = 0x50;

    public SimulatorAdapter() {
        super(new GrapevineSystemConnectionMemo("G", Bundle.getMessage("GrapevineSimulatorName"))); // pass customized user name
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
     * incoming SerialMessage into an outgoing SerialReply.
     */
    @SuppressWarnings("fallthrough")
    private SerialReply generateReply(SerialMessage msg) {
        log.debug("Generate Reply to message from node {} (string = {})", msg.getElement(0), msg.toString());

        SerialReply reply = new SerialReply(); // reply length is determined by highest byte added
        int nodeaddr = msg.getAddr();          // node addres from element(0)
        int command = msg.getElement(1);       // command instruction from element(1)
        int b3 = msg.getElement(2);            // command instruction from element(2), not used
        int bank = msg.getElement(3) ;         // bank or instruction from element(3)
        log.debug("Message address={} b2={} b3={} b4={}", nodeaddr, command, b3, bank);
        switch (command) {

            case 119 :
                log.debug("get software version (poll) message detected");
                // 2 byte software version number reply
                reply.setElement(0, nodeaddr | 0x80);
                reply.setElement(1, 9); // pretend version "9"
                // no parity
                break;

            case 113 :
                log.debug("init node message 1 detected - ASD sensors");
                // 4 byte init reply
                NodeResponse(nodeaddr, 1, 4);
                // all replies generated by NodeResponse()
                reply.setElement(0, nodeaddr | 0x80);
                reply.setElement(3, bank | 0x80); // echo bank (bit 1234): 1-3 = signals, 4-5 = sensors
                reply = setParity(reply,0);
                break;

            case 6 :
                log.debug("init node message 2 detected - parallel sensors");
                // 4 byte init reply
                reply.setElement(0, nodeaddr | 0x80);
                reply.setElement(1, command | 0x80); // echo id + state
                reply.setElement(2, b3 | 0x80);
                log.debug ("Confirm signal mast {} set", bank);
                reply.setElement(3, bank | 0x80); // echo bank (bit 1234): 1-3 = signals, 4-5 = sensors
                reply = setParity(reply,0);
                break;

            case 8 :
            case 9 :
            case 10 :
            case 11 :
            case 12 :
            case 13 :
            case 14 :
            case 15 :
                log.debug("set signal message detected");
                // 4 byte reply
                reply.setElement(0, nodeaddr | 0x80);
                reply.setElement(1, command); // echo id + state
                reply.setElement(2, b3);
                log.debug ("Confirm signal mast {} set", bank);
                reply.setElement(3, bank); // echo bank (bit 1234): 1-3 = signals
                reply = setParity(reply,0);
                break;

            case 115 :
                log.debug("set signal head message detected");
                // 4 byte reply
                NodeResponse(nodeaddr, 5, 5);
                // all replies generated by NodeResponse()
                reply.setElement(0, nodeaddr | 0x80);
                reply.setElement(3, bank | 0x80); // echo bank (bit 1234): 1-3 = signals, 4-5 = sensors
                reply = setParity(reply,0);
                break;

            case 17 :
                log.debug("normal message detected");
                // 4 byte reply
                reply.setElement(0, nodeaddr | 0x80);
                reply.setElement(1, 0x01); // id (bit 2345) + command (bit 678)/bit (bit 234567) + state (bit 8)
                reply.setElement(2, nodeaddr | 0x80);
                reply.setElement(3, 0x30); // bank (bit 1234): 1-3 = signals, 4-5 = sensors
                reply = setParity(reply,0);
                break;

            default:
                if (msg.getElement(3) == 101) { // this is the rename command, with element 2 = new node number
                    JOptionPane.showMessageDialog(null,
                            Bundle.getMessage("RenumberSupport"),
                            Bundle.getMessage("MessageTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    log.debug("rename command not supported, old address: {}, new address: {}, bank: {}", nodeaddr, command, bank);
                    return null;
                }
                log.debug("message unrecognized command detected, code: {}", command);
                // 4 byte general reply
                reply.setElement(0, nodeaddr | 0x80);
                reply.setElement(1, 0x0);  // normally: command (values 0x6, 0x7 are for signals
                reply.setElement(2, nodeaddr | 0x80);
                reply.setElement(3, 0x00); // 0 = error, bank 1..3 for signals, 4..5 sensors (and parity)
                reply = setParity(reply,0);
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

    int SignalBankSize = 16;
    int SensorBankSize = 0x3F;

    /**
     * Pretend a node init reply for a (limited) range of banks and bits.
     * <p>
     * Based on information in {@link jmri.jmrix.grapevine.SerialMessage#staticFormat(int, int, int, int)}.
     *
     * @param i the node address
     */
    private void NodeResponse(int node, int startBank, int endBank) {
        byte countByte = 0x00; // counter for signal/sensor id calculation
        int elementThree;
        SerialReply reply = new SerialReply(); // reply length is determined by highest byte added
        if (node < 1 || node > 127) {
            log.warn("Invalid Node Address, no response generated");
            return; // node address invalid
        }
        for (int k = startBank; k <= endBank; k++) {
            if (k <= 3) {
                switch (k) { // signals
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
                for (int j = 0; j < SignalBankSize; j++) { // send state of each signal bit (banks 1, 2 3)
                    log.debug("Sending state of bank {}, bit {} on node {}", k, j, node);
                    Integer countInt = Integer.valueOf(j);
                    countByte = countInt.byteValue();

                    reply.setElement(0, node | 0x80);
                    reply.setElement(1, (countByte << 3 & 0x06)); // id (bit 2345) + command (bit 678): set to Red
                    reply.setElement(2, node | 0x80);
                    reply.setElement(3, elementThree);
                    reply = setParity(reply, 0);
                }
            } else { // bank 4 and 5
                switch (k) { // sensors
                    case 4:
                        elementThree = 0x40; // bank (bit 1234): 4 = sensors
                        break;
                    case 5:
                    default:
                        elementThree = 0x50; // repeat for bank 5 (reply.setElement(3, 0x50))
                }
                for (int j = 0; j < SignalBankSize; j++) { // send state of each sensor bit (banks 4, 5)
                    log.debug("Sending state of bank {}, bit {} on node {}", k, j, node);
                    Integer countInt = Integer.valueOf(j);
                    countByte = countInt.byteValue();

                    reply.setElement(0, node | 0x80);
                    reply.setElement(1, (countByte << 1 & 0x01)); // id (bit 234567) + state (bit 8): inactive
                    reply.setElement(2, node | 0x80);
                    reply.setElement(3, elementThree); // bank (bit 1234): 4-5 = sensors
                    reply = setParity(reply,0);
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
