package jmri.jmrix.maple.simulator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import javax.swing.JOptionPane;
import jmri.jmrix.maple.SerialMessage;
import jmri.jmrix.maple.SerialPortController; // no special xSimulatorController
import jmri.jmrix.maple.SerialReply;
import jmri.jmrix.maple.MapleSystemConnectionMemo;
import jmri.jmrix.maple.SerialTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to a simulated Maple system.
 * <p>
 * Currently, the Maple SimulatorAdapter reacts to the following commands sent from the user
 * interface with an appropriate reply {@link #generateReply(SerialMessage)}:
 * <ul>
 *     <li>RC Read Coils (poll), all coil bits 0
 *     <li>WC Write Coils (ACK)
 * </ul>
 *
 * Based on jmri.jmrix.lenz.xnetsimulator.XNetSimulatorAdapter / GrapevineSimulatorAdapter 2017
 * <p>
 * NOTE: Some material in this file was modified from other portions of the
 * support infrastructure.
 *
 * @author Paul Bender, Copyright (C) 2009-2010
 * @author Mark Underwood, Copyright (C) 2015
 * @author Egbert Broerse, Copyright (C) 2018
 */
public class SimulatorAdapter extends SerialPortController implements Runnable {

    // private control members
    private boolean opened = false;
    private Thread sourceThread;

    private boolean outputBufferEmpty = true;
    private boolean checkBuffer = true;

    /**
     * Create a new SimulatorAdapter.
     */
    public SimulatorAdapter() {
        super(new MapleSystemConnectionMemo("K", Bundle.getMessage("MapleSimulatorName"))); // pass customized user name
        setManufacturer(jmri.jmrix.maple.SerialConnectionTypeList.MAPLE);
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
            log.debug("Buffer Empty: {}", outputBufferEmpty);
            return (outputBufferEmpty);
        } else {
            log.debug("No Flow Control or Buffer Check");
            return (true);
        }
    }

    /**
     * Set up all of the other objects to operate with a MapleSimulator
     * connected to this port.
     */
    @Override
    public void configure() {
        log.debug("set tc for memo {}", getSystemConnectionMemo().getUserName());
        // connect to the traffic controller
        ((MapleSystemConnectionMemo) getSystemConnectionMemo()).getTrafficController().connectPort(this);
        // do the common manager config
        ((MapleSystemConnectionMemo) getSystemConnectionMemo()).configureManagers();

        // start the simulator
        sourceThread = new Thread(this);
        sourceThread.setName("Maple Simulator");
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

    // Base class methods for the Maple SerialPortController simulated interface

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
        // of the Maple command station simulation.
        log.info("Maple Simulator Started");
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
                buf.append("Maple Simulator Thread received message: ");
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
                        buf.append("Maple Simulator Thread sent reply: ");
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
     * See {@link jmri.jmrix.maple.SerialMessage}.
     *
     * @param msg the message received in the simulated node
     * @return a single Maple message to confirm the requested operation, or a series
     * of messages for each (fictitious) node/pin/state. To ignore certain commands, return null.
     */
    private SerialReply generateReply(SerialMessage msg) {
        log.debug("Generate Reply to message from node {} (string = {})", msg.getAddress(), msg.toString());

        SerialReply reply = new SerialReply(); // reply length is determined by highest byte added
        int nodeAddress = msg.getUA();         // node addres from element 1 + 2
        //convert hex to character
        char cmd1 = (char) msg.getElement(3);  // command char 1
        char cmd2 = (char) msg.getElement(4);  // command char 2

        log.debug("Message nodeaddress={} cmd={}{}, Start={}, Num={}",
                nodeAddress, cmd1, cmd2,
                getStartAddress(msg), getNumberOfCoils(msg));

        switch ("" + cmd1 + cmd2) {
            case "RC": // Read Coils message
                log.debug("Read Coils (poll) message detected");
                int i = 1;
                // init reply
                log.debug("RC Reply from node {}", nodeAddress);
                reply.setElement(0, 0x02); // <STX>
                reply.setElement(1, msg.getElement(1));
                reply.setElement(2, msg.getElement(2));
                reply.setElement(3, 'R');
                reply.setElement(4, 'C');
                for (i = 1; i < getNumberOfCoils(msg); i++) {
                    reply.setElement(i + 4, 0x00); // report state of each requested coil as Inactive = 0
                    // TODO: echo commanded state from JMRI node-bit using: getCommandedState(nodeAddress * 1000 + getStartAddress(msg) + 1)
                }
                reply.setElement(i + 5, 0x03);
                reply = setChecksum(reply, i + 6);
                break;
            case "WC": // Write Coils message
                log.debug("Write Coils message detected");
                // init reply
                log.debug("WC Reply from node {}", nodeAddress);
                reply.setElement(0, 0x06); // <ACK>
                reply.setElement(1, msg.getElement(1));
                reply.setElement(2, msg.getElement(2));
                reply.setElement(3, 'W');
                reply.setElement(4, 'C');
                break;
            default:
                // TODO "WC" message replies
                log.debug("command ignored");
                reply = null; // ignore all other messages
        }
        log.debug(reply == null ? "Message ignored" : "Reply generated " + reply.toString());
        return (reply);
    }

    /**
     * Extract start coils from RC/WC message.
     *
     * @param msg te SerialMessage received from Simulator inpipe
     * @return decimal coil ID
     */
    private int getStartAddress(SerialMessage msg) {
        int a1 = msg.getElement(5) - '0';  // StartAt char 1
        int a2 = msg.getElement(6) - '0';  // StartAt char 2
        int a3 = msg.getElement(7) - '0';  // StartAt char 3
        int a4 = msg.getElement(8) - '0';  // StartAt char 4
        return 1000 * a1 + 100 * a2 + 10 * a3 + a4; // combine a1..a4
    }

    /**
     * Extract the number of coils to process from RC/WC message.
     *
     * @param msg te SerialMessage received from Simulator inpipe
     * @return the number of consecutive coils to read/write (decimal)
     * after starting Coil
     */
    private int getNumberOfCoils(SerialMessage msg) {
        int n1 = msg.getElement(9) - '0';  // N char 1
        int n2 = msg.getElement(10) - '0'; // N char 2
        return 10 * n1 + n2; // combine n1, n2
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
     * @return filled message, only when the message is complete
     * @throws IOException when presented by the input source
     */
    private SerialMessage loadChars() throws java.io.IOException {
        SerialReply reply = new SerialReply();
        ((MapleSystemConnectionMemo) getSystemConnectionMemo()).getTrafficController().loadChars(reply, inpipe);

        // copy received "reply" to a Maple message of known length
        SerialMessage msg = new SerialMessage(reply.getNumDataElements());
            for (int i = 0; i < msg.getNumDataElements(); i++) {
                //log.debug("" + reply.getElement(i));
                msg.setElement(i, reply.getElement(i));
            }
        log.debug("new message received");
        return msg;
    }

    /**
     * Set checksum on simulated Maple Node reply.
     * Code copied from {@link SerialMessage}#setChecksum(int)
     *
     * @param r the SerialReply to complete
     * @param index element index to place 2 checksum bytes
     * @return SerialReply with parity set
     */
    public SerialReply setChecksum(SerialReply r, int index) {
        int sum = 0;
        for (int i = 1; i < index; i++) {
            sum += r.getElement(i);
        }
        sum = sum & 0xFF;

        char firstChar;
        int firstVal = (sum / 16) & 0xF;
        if (firstVal > 9) {
            firstChar = (char) ('A' - 10 + firstVal);
        } else {
            firstChar = (char) ('0' + firstVal);
        }
        r.setElement(index, firstChar);

        char secondChar;
        int secondVal = sum & 0xf;
        if (secondVal > 9) {
            secondChar = (char) ('A' - 10 + secondVal);
        } else {
            secondChar = (char) ('0' + secondVal);
        }
        r.setElement(index + 1, secondChar);
        return r;
    }

    int signalBankSize = 16; // theoretically: 16
    int sensorBankSize = 64; // theoretically: 0x3F
    javax.swing.Timer timer;

    // streams to share with user class
    private DataOutputStream pout = null; // this is provided to classes who want to write to us
    private DataInputStream pin = null; // this is provided to classes who want data from us
    // internal ends of the pipes
    private DataOutputStream outpipe = null; // feed pin
    private DataInputStream inpipe = null; // feed pout

    private final static Logger log = LoggerFactory.getLogger(SimulatorAdapter.class);

}
