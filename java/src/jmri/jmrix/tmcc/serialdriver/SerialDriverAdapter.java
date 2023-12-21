package jmri.jmrix.tmcc.serialdriver;

import java.util.Arrays;
import jmri.jmrix.tmcc.SerialPortController;
import jmri.jmrix.tmcc.SerialTrafficController;
import jmri.jmrix.tmcc.TmccSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to TMCC via a serial com port. Normally controlled by the
 * tmcc.serialdriver.SerialDriverFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2006
 */
public class SerialDriverAdapter extends SerialPortController {

    public SerialDriverAdapter() {
        super(new TmccSystemConnectionMemo());
        this.manufacturerName = jmri.jmrix.tmcc.SerialConnectionTypeList.LIONEL;
    }

    @Override
    public String openPort(String portName, String appName) {
    
    
        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("failed to connect TMCC to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting TMCC to {} {}", portName, currentSerialPort);
        
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
     * @return true
     */
    public boolean okToSend() {
        return true;
    }

    /**
     * Set up all of the other objects to operate connected to this port.
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        log.debug("set tc for memo {}", getSystemConnectionMemo().getUserName());
        SerialTrafficController control = new SerialTrafficController(getSystemConnectionMemo());
        control.connectPort(this);
        this.getSystemConnectionMemo().setTrafficController(control);
        // do the common manager config
        this.getSystemConnectionMemo().configureManagers();
    }

    // Base class methods for the SerialPortController interface

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

    protected String[] validSpeeds = new String[]{Bundle.getMessage("Baud9600"),
            Bundle.getMessage("Baud19200"), Bundle.getMessage("Baud57600")};
    protected int[] validSpeedValues = new int[]{9600, 19200, 57600};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    /**
     * Get an array of valid values for "option 2"; used to display valid
     * options. May not be null, but may have zero entries.
     *
     * @return a single element array containing an empty string
     */
    public String[] validOption2() {
        return new String[]{""};
    }

    /**
     * Get a String that says what Option 2 represents May be an empty string,
     * but will not be null.
     *
     * @return an empty string
     */
    public String option2Name() {
        return "";
    }

    // private control members

    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class);

}
