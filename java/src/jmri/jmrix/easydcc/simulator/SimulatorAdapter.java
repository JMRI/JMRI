package jmri.jmrix.easydcc.simulator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import jmri.jmrix.easydcc.EasyDccMessage;
import jmri.jmrix.easydcc.EasyDccPortController; // no special xSimulatorController
import jmri.jmrix.easydcc.EasyDccReply;
import jmri.jmrix.easydcc.EasyDccSystemConnectionMemo;
import jmri.jmrix.easydcc.EasyDccTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to a simulated EasyDCC system.
 * <p>
 * Currently, the EasyDCC SimulatorAdapter reacts to commands sent from the user interface
 * with an appropriate reply message.
 * Based on jmri.jmrix.lenz.xnetsimulator.XNetSimulatorAdapter / DCCppSimulatorAdapter 2017
 * <p>
 * NOTE: Some material in this file was modified from other portions of the
 * support infrastructure.
 *
 * @author Paul Bender, Copyright (C) 2009-2010
 * @author Mark Underwood, Copyright (C) 2015
 * @author Egbert Broerse, Copyright (C) 2017
 */
public class SimulatorAdapter extends EasyDccPortController implements Runnable {

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
        super(new EasyDccSystemConnectionMemo("E", "EasyDCC Simulator")); // pass customized user name
        setManufacturer(jmri.jmrix.easydcc.EasyDccConnectionTypeList.EASYDCC);
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
            log.debug("Buffer Empty: " + outputBufferEmpty);
            return (outputBufferEmpty);
        } else {
            log.debug("No Flow Control or Buffer Check");
            return (true);
        }
    }

    /**
     * Set up all of the other objects to operate with an EasyDccSimulator
     * connected to this port.
     */
    @Override
    public void configure() {
        // connect to the traffic controller, which is provided via the memo
        log.debug("set tc for memo {}", getSystemConnectionMemo().getUserName());

        getSystemConnectionMemo().getTrafficController().connectPort(this);

        // do the common manager config
        this.getSystemConnectionMemo().configureManagers();

        // start the simulator
        sourceThread = new Thread(this);
        sourceThread.setName("EasyDCC Simulator");
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

    // Base class methods for the EasyDccPortController simulated interface

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
        // of the EasyDCC command station simulation.
        log.info("EasyDCC Simulator Started");
        while (true) {
            try {
                synchronized (this) {
                    wait(50);
                }
            } catch (InterruptedException e) {
                log.debug("interrupted, ending");
                return;
            }
            EasyDccMessage m = readMessage();
            EasyDccReply r;
            if (log.isDebugEnabled()) {
                StringBuffer buf = new StringBuffer();
                buf.append("EasyDCC Simulator Thread received message: ");
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
                    buf.append("EasyDCC Simulator Thread sent reply: ");
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
    private EasyDccMessage readMessage() {
        EasyDccMessage msg = null;
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
     * incoming EasyDccMessage into an outgoing EasyDccReply.
     *
     * As yet, not all messages receive a meaningful reply. TODO: Throttle, Program
     */
    private EasyDccReply generateReply(EasyDccMessage msg) {
        log.debug("Generate Reply to message type {} (string = {})", msg.toString().charAt(0), msg.toString());

        EasyDccReply reply = new EasyDccReply();
        int i = 0;
        char command = msg.toString().charAt(0);
        log.debug("Message type = " + command);
        switch (command) {

            case 'X': // eXit programming
            case 'S': // Send packet
            case 'D': // Dequeue packet
            case 'Q': // Queue packet
            case 'F': // display memory
            case 'C': // program loCo
                reply.setElement(i++, EDC_OPS); // capital O for Operation
                break;

            case 'P':
            case 'M':
                reply.setElement(i++, EDC_PROG); // capital P for Programming
                break;

            case 'E':
                log.debug("TRACK_POWER_ON detected");
                reply.setElement(i++, EDC_OPS); // capital O for Operation
                break;

            case 'K':
                log.debug("TRACK_POWER_OFF detected");
                reply.setElement(i++, EDC_OPS); // capital O for Operation
                break;

            case 'V':
                log.debug("Read_CS_Version detected");
                String replyString = "V999 01 01 1999";
                reply = new EasyDccReply(replyString); // fake version number reply
                i = replyString.length();
//                reply.setElement(i++, 0x0d); // add CR for second reply line
//                reply.setElement(i++, EDC_OPS); // capital O for Operation
                break;

            case 'G': // Consist
                log.debug("Consist detected");
                if (msg.toString().charAt(0) == 'D') { // Display consist
                    replyString = "G" + msg.getElement(2) + msg.getElement(3) + "0000";
                    reply = new EasyDccReply(replyString); // fake version number reply
                    i = replyString.length();
//                    reply.setElement(i++, 0x0d); // add CR
                    break;
                }
                reply.setElement(i++, EDC_OPS); // capital O for Operation, anyway
                break;

            case 'L': // Read Loco
                log.debug("Read Loco detected");
                replyString = "L" + msg.getElement(1) + msg.getElement(2) + msg.getElement(3) + msg.getElement(4) + "000000";
                reply = new EasyDccReply(replyString); // fake reply dir = 00 step = 00 F5-12=00
                i = replyString.length();
//                reply.setElement(i++, 0x0d); // add CR for second reply line
//                reply.setElement(i++, EDC_OPS); // capital O for Operation, anyway
                break;

            case 'R':
                log.debug("Read_CV detected");
                replyString = "--";
                reply = new EasyDccReply(replyString); // cannot read
                i = replyString.length();
//                reply.setElement(i++, 0x0d); // add CR for second reply line
//                reply.setElement(i++, EDC_PROG); // capital O for Operation
                break;

            default:
                log.debug("non-reply message detected");
                reply.setElement(i++, '?'); // per page 2 of the EasyDCC computer
                                          // operations manual, an invalid 
                                          // command returns ?<CR>
        }
        log.debug("Reply generated = {}", reply.toString());
        reply.setElement(i++, 0x0d); // add final CR for all replies
        return (reply);
    }

    /**
     * Write reply to output.
     * <p>
     * Copied from jmri.jmrix.nce.simulator.SimulatorAdapter.
     *
     * @param r reply on message
     */
    private void writeReply(EasyDccReply r) {
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
    private EasyDccMessage loadChars() throws java.io.IOException {
        int nchars;
        byte[] rcvBuffer = new byte[32];

        nchars = inpipe.read(rcvBuffer, 0, 32);
        //log.debug("new message received");
        EasyDccMessage msg = new EasyDccMessage(nchars);

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
