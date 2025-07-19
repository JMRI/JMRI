package jmri.jmrix.cmri.serial.serialdriver;

import java.util.Arrays;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.SerialPortAdapter;
import jmri.jmrix.cmri.serial.SerialTrafficController;

/**
 * Provide access to C/MRI via a serial com port. Normally controlled by the
 * cmri.serial.serialdriver.SerialDriverFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2023
 */
public class SerialDriverAdapter extends SerialPortAdapter {

    public SerialDriverAdapter() {
        super(new CMRISystemConnectionMemo());
        this.manufacturerName = jmri.jmrix.cmri.CMRIConnectionTypeList.CMRI;
    }

    @Override
    public String openPort(String portName, String appName) {

        // get and open the primary port
        currentSerialPort = activatePort(portName, log, 2);
        if (currentSerialPort == null) {
            log.error("failed to connect C/MRI to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting C/MRI to {} {}", portName, currentSerialPort);
        
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
     * Can the port accept additional characters? Yes, always
     * @return always true.
     */
    public boolean okToSend() {
        return true;
    }

    /**
     * set up all of the other objects to operate connected to this port
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        SerialTrafficController tc = new SerialTrafficController();
        tc.connectPort(this);
        ((CMRISystemConnectionMemo)getSystemConnectionMemo()).setTrafficController(tc);
        ((CMRISystemConnectionMemo)getSystemConnectionMemo()).configureManagers();
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
            Bundle.getMessage("Baud19200"), Bundle.getMessage("Baud28800"),
            Bundle.getMessage("Baud57600"), Bundle.getMessage("Baud115200")};
    protected int[] validSpeedValues = new int[]{9600, 19200, 28800, 57600, 115200};

    @Override
    public int defaultBaudIndex() {
        return 1;
    }

    // private control members

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialDriverAdapter.class);

}
