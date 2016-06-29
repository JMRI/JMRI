package jmri.jmrix.powerline.simulator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import jmri.jmrix.powerline.SerialPortController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement simulator for powerline serial systems
 * <P>
 * System names are "PLnnn", where nnn is the bit number without padding.
 * <P>
 * This is based on the NCE simulator.
 *
 * @author	Dave Duchamp Copyright (C) 2004
 * @author	Bob Jacobsen Copyright (C) 2006, 2007, 2008 Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 */
public class SimulatorAdapter extends SerialPortController implements
        jmri.jmrix.SerialPortAdapter, Runnable {

    // private control members
    private boolean opened = false;
    private Thread sourceThread;

    // streams to share with user class
    private DataOutputStream pout = null; // this is provided to classes who want to write to us
    private DataInputStream pin = null; // this is provided to class who want data from us

    // internal ends of the pipes
    @SuppressWarnings("unused")
    private DataOutputStream outpipe = null; // feed pin
    @SuppressWarnings("unused")
    private DataInputStream inpipe = null; // feed pout

    public SimulatorAdapter() {
        super(new SpecificSystemConnectionMemo());
    }

    public String openPort(String portName, String appName) {
        try {
            PipedOutputStream tempPipeI = new PipedOutputStream();
            pout = new DataOutputStream(tempPipeI);
            inpipe = new DataInputStream(new PipedInputStream(tempPipeI));
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
     * set up all of the other objects to simulate operation with an command
     * station.
     */
    public void configure() {
        SpecificTrafficController tc = new SpecificTrafficController(this.getSystemConnectionMemo());

        // connect to the traffic controller
        this.getSystemConnectionMemo().setTrafficController(tc);
        tc.setAdapterMemo(this.getSystemConnectionMemo());
        this.getSystemConnectionMemo().configureManagers();
        tc.connectPort(this);

        // Configure the form of serial address validation for this connection
        this.getSystemConnectionMemo().setSerialAddress(new jmri.jmrix.powerline.SerialAddress(this.getSystemConnectionMemo()));

        // start the simulator
        sourceThread = new Thread(this);
        sourceThread.setName("Powerline Simulator");
        sourceThread.setPriority(Thread.MIN_PRIORITY);
        sourceThread.start();
    }

    // base class methods for the PortController interface
    public DataInputStream getInputStream() {
        if (!opened || pin == null) {
            log.error("getInputStream called before load(), stream not available");
        }
        return pin;
    }

    public DataOutputStream getOutputStream() {
        if (!opened || pout == null) {
            log.error("getOutputStream called before load(), stream not available");
        }
        return pout;
    }

    public boolean status() {
        return opened;
    }

    /**
     * Get an array of valid baud rates.
     */
    public String[] validBaudRates() {
        log.debug("validBaudRates should not have been invoked");
        return null;
    }

    public String getCurrentBaudRate() {
        return "";
    }

    public void run() { // start a new thread
        // Simulator thread just reports start and ends
        if (log.isInfoEnabled()) {
            log.info("Powerline Simulator Started");
        }
    }

    private final static Logger log = LoggerFactory
            .getLogger(SimulatorAdapter.class.getName());

}
