package jmri.jmrix.secsi.serialdriver;

import java.util.Arrays;
import jmri.jmrix.secsi.SecsiSystemConnectionMemo;
import jmri.jmrix.secsi.SerialPortController;

/**
 * Provide access to SECSI via a serial com port. Normally controlled by the
 * secsi.serialdriver.SerialDriverFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2006, 2007
 */
public class SerialDriverAdapter extends SerialPortController {

    public SerialDriverAdapter() {
        super(new SecsiSystemConnectionMemo());
        this.manufacturerName = jmri.jmrix.secsi.SerialConnectionTypeList.TRACTRONICS;
    }

    @Override
    public String openPort(String portName, String appName) {

        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("failed to connect SECSI to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting SECSI to {} {}", portName, currentSerialPort);
        
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
     * Can the port accept additional characters
     *
     * @return true always
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
        ((SecsiSystemConnectionMemo) getSystemConnectionMemo()).getTrafficController().connectPort(this);
        ((SecsiSystemConnectionMemo) getSystemConnectionMemo()).configureManagers();
    }

    // base class methods for the SerialPortController interface

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

    /**
     * Get an array of valid values for "option 2"; used to display valid
     * options.May not be null, but may have zero entries
     * @return zero entries.
     */
    public String[] validOption2() {
        return new String[]{""};
    }

    /**
     * Get a String that says what Option 2 represents.
     *
     * @return may be an empty string, but will not be null
     */
    public String option2Name() {
        return "";
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialDriverAdapter.class);

}
