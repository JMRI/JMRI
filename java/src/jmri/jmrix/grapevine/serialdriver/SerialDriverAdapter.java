package jmri.jmrix.grapevine.serialdriver;

import java.util.Arrays;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.jmrix.grapevine.SerialPortController;
import jmri.jmrix.grapevine.SerialTrafficController;

/**
 * Provide access to ProTrak Grapevine via a serial com port. Normally
 * controlled by the serialdriver.SerialDriverFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2006, 2007, 2023
 */
public class SerialDriverAdapter extends SerialPortController {

    /**
     * Create a new SerialDriverAdapter.
     */
    public SerialDriverAdapter() {
        // needs to provide a SystemConnectionMemo
        super(new GrapevineSystemConnectionMemo());
        this.manufacturerName = jmri.jmrix.grapevine.SerialConnectionTypeList.PROTRAK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String openPort(String portName, String appName) {

        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("failed to connect Grapevine to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting Grapevine to {} {}", portName, currentSerialPort);
        
        // try to set it for communication via SerialDriver
        // find the baud rate value, configure comm options
        int baud = currentBaudNumber(mBaudRate);
        setBaudRate(currentSerialPort, baud);
        configureLeads(currentSerialPort, true, true);
        setFlowControl(currentSerialPort, FlowControl.NONE);

        // report status
        reportPortStatus(log, portName);

        opened = true;

        return null; // indicates OK return
    }

    /**
     * Can the port accept additional characters?
     *
     * @return Yes, always
     */
    public boolean okToSend() {
        return true;
    }

    /**
     * Set up all of the other objects to operate connected to this port.
     */
    @Override
    public void configure() {
        log.debug("SerialDriverAdapter configure() with prefix = {}", this.getSystemConnectionMemo().getSystemPrefix());
        // connect to the traffic controller
        SerialTrafficController control = new SerialTrafficController(this.getSystemConnectionMemo());
        control.connectPort(this);
        control.setSystemConnectionMemo(this.getSystemConnectionMemo());
        log.debug("SimulatorAdapter configure() set tc for memo {}", getSystemConnectionMemo().getUserName());
        this.getSystemConnectionMemo().setTrafficController(control);
        // do the common manager config
        getSystemConnectionMemo().configureManagers();
    }

    // base class methods for the SerialPortController interface

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean status() {
        return opened;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] validBaudRates() {
        return Arrays.copyOf(validSpeeds, validSpeeds.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return Arrays.copyOf(validSpeedValues, validSpeedValues.length);
    }

    protected String[] validSpeeds = new String[]{Bundle.getMessage("Baud38400")};
    protected int[] validSpeedValues = new int[]{38400};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialDriverAdapter.class);

}
