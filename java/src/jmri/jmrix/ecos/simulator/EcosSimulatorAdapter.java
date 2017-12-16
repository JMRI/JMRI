package jmri.jmrix.ecos.simulator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import jmri.jmrix.NetworkPortAdapter;
import jmri.jmrix.ecos.EcosConnectionTypeList;
import jmri.jmrix.ecos.EcosMessage;
import jmri.jmrix.ecos.EcosPortController;
import jmri.jmrix.ecos.EcosReply;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import jmri.jmrix.ecos.EcosTrafficController;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to a simulated ECoS system.
 * <p>
 * Currently, the ECoS EcosSimulatorAdapter reacts to commands sent from the
 * user interface with messages an appropriate reply message. Based on
 * jmri.jmrix.lenz.xnetsimulator.XNetSimulatorAdapter / DCCppSimulatorAdapter
 * 2017 support infrastructure.
 *
 * @author Paul Bender, Copyright (C) 2009-2010
 * @author Mark Underwood, Copyright (C) 2015
 * @author Egbert Broerse, Copyright (C) 2017
 * @author Randall Wood Copyright 2017
 */
public class EcosSimulatorAdapter extends EcosPortController implements NetworkPortAdapter, Runnable {

    // private control members
    private boolean opened = true;
    private Thread sourceThread;

    final static int SENSOR_MSG_RATE = 10;

    private boolean outputBufferEmpty = true;
    private boolean checkBuffer = true;
    private boolean trackPowerState = false;

    // Simulator responses
    char EDC_OPS = 0x4F;
    char EDC_PROG = 0x50;

    public EcosSimulatorAdapter() {
        super(new EcosSystemConnectionMemo("U", "ECoS Simulator")); // pass customized user name
        setManufacturer(EcosConnectionTypeList.ESU);
        this.setHostAddress("127.0.0.1");
        this.setHostName("localhost");
        this.setPort(15471);
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
     * Set up all of the other objects to operate with an EcosSimulator
     * connected to this port.
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        log.debug("set tc for memo {}", getSystemConnectionMemo().getUserName());
        EcosTrafficController control = new EcosSimulatorTrafficController(getSystemConnectionMemo());
        //compare with: XNetTrafficController packets = new XNetPacketizer(new LenzCommandStation());
        control.connectPort(this);
        this.getSystemConnectionMemo().setEcosTrafficController(control);
        // do the common manager config
        this.getSystemConnectionMemo().configureManagers();

        // start the simulator
        sourceThread = new Thread(this);
        sourceThread.setName("ECoS Simulator");
        sourceThread.setPriority(Thread.MIN_PRIORITY);
        sourceThread.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect() throws java.io.IOException {
        log.debug("connect called");
    }

    // Base class methods for the ECoSPortController simulated interface
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

    @Override
    public void run() { // start a new thread
        // This thread has one task. It repeatedly reads from the input pipe
        // and writes an appropriate response to the output pipe. This is the heart
        // of the ECoS command station simulation.
        log.info("ECoS Simulator Started");
        while (true) {
            try {
                synchronized (this) {
                    wait(50);
                }
            } catch (InterruptedException e) {
                log.debug("interrupted, ending");
                return;
            }
            EcosMessage m = readMessage();
            EcosReply r;
            if (log.isDebugEnabled()) {
                StringBuilder buf = new StringBuilder();
                buf.append("ECoS Simulator Thread received message: ");
                if (m != null) {
                    for (int i = 0; i < m.getNumDataElements(); i++) {
                        buf.append(Integer.toHexString(0xFF & m.getElement(i))).append(" ");
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
                    StringBuilder buf = new StringBuilder();
                    buf.append("ECoS Simulator Thread sent reply: ");
                    for (int i = 0; i < r.getNumDataElements(); i++) {
                        buf.append(Integer.toHexString(0xFF & r.getElement(i))).append(" ");
                    }
                    log.debug(buf.toString());
                }
            }
        }
    }

    /**
     * Read one incoming message from the buffer and set outputBufferEmpty to
     * true.
     */
    private EcosMessage readMessage() {
        EcosMessage msg = null;
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
     * This is the heart of the simulation. It translates an incoming
     * EcosMessage into an outgoing EcosReply.
     * <p>
     * As yet, not all messages receive a meaningful reply. TODO: Throttle,
     * Program
     */
    @SuppressWarnings("fallthrough")
    private EcosReply generateReply(EcosMessage msg) {
        log.debug("Generate Reply to message type {} (string = {})", msg.toString().charAt(0), msg);

        String s, r;
        EcosReply reply = new EcosReply();
        int i = 0;
        char command = msg.toString().charAt(0);
        log.debug("Message type = {}", command);
        switch (command) {

            case 'X': // eXit programming
            case 'S': // Send packet
            case 'D': // Deque packet
            case 'Q': // Que packet
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
                trackPowerState = true;
                reply.setElement(i++, EDC_OPS); // capital O for Operation
                break;

            case 'K':
                log.debug("TRACK_POWER_OFF detected");
                trackPowerState = false;
                reply.setElement(i++, EDC_OPS); // capital O for Operation
                break;

            case 'V':
                log.debug("Read_CS_Version detected");
                String replyString = "V999 01 01 1999";
                reply = new EcosReply(replyString); // fake version number reply
                i = replyString.length();
//                reply.setElement(i++, 0x0d); // add CR for second reply line
//                reply.setElement(i++, EDC_OPS); // capital O for Operation
                break;

            case 'G': // Consist
                log.debug("Consist detected");
                if (msg.toString().charAt(0) == 'D') { // Display consist
                    replyString = "G" + msg.getElement(2) + msg.getElement(3) + "0000";
                    reply = new EcosReply(replyString); // fake version number reply
                    i = replyString.length();
//                    reply.setElement(i++, 0x0d); // add CR
                    break;
                }
                reply.setElement(i++, EDC_OPS); // capital O for Operation, anyway
                break;

            case 'L': // Read Loco
                log.debug("Read Loco detected");
                replyString = "L" + msg.getElement(1) + msg.getElement(2) + msg.getElement(3) + msg.getElement(4) + "000000";
                reply = new EcosReply(replyString); // fake reply dir = 00 step = 00 F5-12=00
                i = replyString.length();
//                reply.setElement(i++, 0x0d); // add CR for second reply line
//                reply.setElement(i++, EDC_OPS); // capital O for Operation, anyway
                break;

            case 'R':
                log.debug("Read_CV detected");
                replyString = "--";
                reply = new EcosReply(replyString); // cannot read
                i = replyString.length();
//                reply.setElement(i++, 0x0d); // add CR for second reply line
//                reply.setElement(i++, EDC_PROG); // capital O for Operation
                break;

            default:
                log.debug("non-reply message detected");
                reply.setElement(i++, EDC_OPS); // capital O for Operation
        }
        log.debug("Reply generated = {}", reply);
        reply.setElement(i++, 0x0d); // add final CR for all replies
        return (reply);
    }

    /**
     * Write reply to output.
     * <p>
 Copied from jmri.jmrix.nce.simulator.EcosSimulatorAdapter.
     *
     * @param r reply on message
     */
    private void writeReply(EcosReply r) {
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
    private EcosMessage loadChars() throws java.io.IOException {
        int nchars;
        byte[] rcvBuffer = new byte[32];

        nchars = inpipe.read(rcvBuffer, 0, 32);
        //log.debug("new message received");
        EcosMessage msg = new EcosMessage(nchars);

        for (int i = 0; i < nchars; i++) {
            msg.setElement(i, rcvBuffer[i] & 0xFF);
        }
        return msg;
    }

    // streams to share with user class
    private DataOutputStream pout = new DataOutputStream(new NullOutputStream());
    private DataInputStream pin = new DataInputStream(new NullInputStream(0));
    // internal ends of the pipes
    private DataOutputStream outpipe = null; // feed pin
    private DataInputStream inpipe = null; // feed pout

    private final static Logger log = LoggerFactory.getLogger(EcosSimulatorAdapter.class);

}
