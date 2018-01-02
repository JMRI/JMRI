package jmri.jmrix.ecos.simulator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import jmri.jmrix.ecos.EcosConnectionTypeList;
import jmri.jmrix.ecos.EcosMessage;
import jmri.jmrix.ecos.EcosPortController;
import jmri.jmrix.ecos.EcosReply;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import jmri.jmrix.ecos.EcosTrafficController;
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
public class EcosSimulatorAdapter extends EcosPortController implements Runnable {

    // streams to share with user class
    private DataOutputStream pout = null; // provided to classes who want to write to us
    private DataInputStream pin = null; // provided to classes who want data from us
    // internal ends of the pipes
    private DataOutputStream outpipe = null; // feed pin
    private DataInputStream inpipe = null; // feed pout

    // private control members
    private Thread sourceThread;

    final static int SENSOR_MSG_RATE = 10;

    private boolean outputBufferEmpty = true;
    private final boolean checkBuffer = true;
    private boolean trackPowerState = false;

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
        EcosTrafficController control = new EcosTrafficController(getSystemConnectionMemo());
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
        try {
            PipedOutputStream tempPipeI = new PipedOutputStream();
            log.debug("tempPipeI created");
            pout = new DataOutputStream(tempPipeI);
            inpipe = new DataInputStream(new PipedInputStream(tempPipeI));
            log.debug("inpipe created {}", inpipe != null);
            PipedOutputStream tempPipeO = new PipedOutputStream();
            outpipe = new DataOutputStream(tempPipeO);
            pin = new DataInputStream(new PipedInputStream(tempPipeO));
        } catch (IOException e) {
            log.error("IOException creating pipes", e);
        }
        opened = true;
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
            if (log.isTraceEnabled()) {
                StringBuilder buf = new StringBuilder();
                buf.append("ECoS Simulator Thread received message: ");
                if (m != null) {
                    for (int i = 0; i < m.getNumDataElements(); i++) {
                        buf.append(Integer.toHexString(0xFF & m.getElement(i))).append(" ");
                    }
                } else {
                    buf.append("null message buffer");
                }
                log.trace(buf.toString());
            }
            if (m != null) {
                r = generateReply(m);
                writeReply(r);
                if (log.isTraceEnabled() && r != null) {
                    StringBuilder buf = new StringBuilder();
                    buf.append("ECoS Simulator Thread sent reply: ");
                    for (int i = 0; i < r.getNumDataElements(); i++) {
                        buf.append(Integer.toHexString(0xFF & r.getElement(i))).append(" ");
                    }
                    log.trace(buf.toString());
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
     */
    private EcosReply generateReply(EcosMessage msg) {
        log.debug("Generate Reply to message: {}", msg);

        EcosReply reply;
        log.error("Replying to \"{}\"", msg.toString().substring(0, msg.toString().indexOf(',')));
        switch (msg.toString().substring(0, msg.toString().indexOf(','))) {
            case "request(1":
                reply = new EcosReply(String.format("<EVENT 1>status[%s]", trackPowerState ? "GO" : "STOP"));
                break;
            case "get(1": // power request
                reply = new EcosReply(String.format("<REPLY get(1,%s", trackPowerState ? " go)" : " stop)"));
                break;
            case "set(1": // power set
                trackPowerState = msg.toString().contains("go");
                reply = new EcosReply(String.format("<REPLY set(1,%s", trackPowerState ? " go)" : " stop)"));
                break;
            default:
                reply = null; // all others
        }
        log.debug("Reply is: {}", reply);
        return reply;
    }

    /**
     * Write reply to output.
     * <p>
     * Copied from jmri.jmrix.nce.simulator.EcosSimulatorAdapter.
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

    private final static Logger log = LoggerFactory.getLogger(EcosSimulatorAdapter.class);

}
