package jmri.jmrix.oaktree.serialdriver;

import java.util.Arrays;
import jmri.jmrix.oaktree.OakTreeSystemConnectionMemo;
import jmri.jmrix.oaktree.SerialPortController;

/**
 * Provide access to Oak Tree via a serial com port. Normally controlled by the
 * oaktree.serialdriver.SerialDriverFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2006
 */
public class SerialDriverAdapter extends SerialPortController {

    public SerialDriverAdapter() {
        super(new OakTreeSystemConnectionMemo());
        this.manufacturerName = jmri.jmrix.oaktree.SerialConnectionTypeList.OAK;
    }

    @Override
    public String openPort(String portName, String appName) {

        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("failed to connect Oak Tree to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting Oak Tree to {} {}", portName, currentSerialPort);
        
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
     * @return true, always
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
        ((OakTreeSystemConnectionMemo) getSystemConnectionMemo()).getTrafficController().connectPort(this);
        // do the common manager config
        ((OakTreeSystemConnectionMemo) getSystemConnectionMemo()).configureManagers();

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

    protected String[] validSpeeds = new String[]{Bundle.getMessage("Baud19200"), Bundle.getMessage("Baud38400")};
    protected int[] validSpeedValues = new int[]{19200, 38400};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialDriverAdapter.class);

}
