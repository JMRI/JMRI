package jmri.jmrix.powerline.simulator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import jmri.jmrix.powerline.SerialPortController;
import jmri.util.ImmediatePipedOutputStream;

/**
 * Implement simulator for Powerline serial systems.
 * <p>
 * System names are "PLnnn", where P is the user-configurable system prefix,
 * nnn is the bit number without padding.
 * <p>
 * Based on the NCE simulator.
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2006, 2007, 2008 Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 */
public class SimulatorAdapter extends SerialPortController implements Runnable {

    // private control members
    private Thread sourceThread;

    // streams to share with user class
    private DataOutputStream pout = null; // this is provided to classes who want to write to us
    private DataInputStream pin = null; // this is provided to classes who want data from us

    // internal ends of the pipes

    //private DataOutputStream outpipe = null; // feed pin

    //private DataInputStream inpipe = null; // feed pout

    public SimulatorAdapter() {
        super(new SpecificSystemConnectionMemo());
    }

    @Override
    public String openPort(String portName, String appName) {
        try (PipedOutputStream tempPipeI = new ImmediatePipedOutputStream();
            PipedOutputStream tempPipeO = new ImmediatePipedOutputStream()) {

            pout = new DataOutputStream(tempPipeI);
            pin = new DataInputStream(new PipedInputStream(tempPipeO));
            opened = true;
            return null; // indicates OK return
        } catch (java.io.IOException e) {
            log.error("init (pipe): Exception: ", e);
            opened = false;
            return e.toString();
        }
    }

    /**
     * Set up all of the other objects to simulate operation with an command
     * station.
     */
    @Override
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
    @Override
    public DataInputStream getInputStream() {
        if (!opened || pin == null) {
            log.error("getInputStream called before load(), stream not available");
        }
        return pin;
    }

    @Override
    public DataOutputStream getOutputStream() {
        if (!opened || pout == null) {
            log.error("getOutputStream called before load(), stream not available");
        }
        return pout;
    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * {@inheritDoc}
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
        // Simulator thread just reports start and ends
        log.info("Powerline Simulator Started");
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SimulatorAdapter.class);

}
