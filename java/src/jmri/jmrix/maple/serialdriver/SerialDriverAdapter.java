package jmri.jmrix.maple.serialdriver;

import java.util.Arrays;
import jmri.jmrix.maple.MapleSystemConnectionMemo;
import jmri.jmrix.maple.SerialPortController;

/**
 * Provide access to Maple via a serial com port. Normally controlled by the
 * maple.serialdriver.SerialDriverFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 */
public class SerialDriverAdapter extends SerialPortController {

    public SerialDriverAdapter() {
        super(new MapleSystemConnectionMemo());
        this.manufacturerName = jmri.jmrix.maple.SerialConnectionTypeList.MAPLE;
    }

    @Override
    public String openPort(String portName, String appName) {

        // get and open the primary port
        currentSerialPort = activatePort(portName, log, 2); // 2 stop bits
        if (currentSerialPort == null) {
            log.error("failed to connect Maple to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting Maple to {} {}", portName, currentSerialPort);
        
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
     * Set up all of the other objects to operate connected to this port.
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        ((MapleSystemConnectionMemo) getSystemConnectionMemo()).getTrafficController().connectPort(this);
        ((MapleSystemConnectionMemo) getSystemConnectionMemo()).configureManagers();
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

    protected String[] validSpeeds = new String[]{Bundle.getMessage("Baud9600"),
            Bundle.getMessage("Baud19200"), Bundle.getMessage("Baud57600")};
    protected int[] validSpeedValues = new int[]{9600, 19200, 57600};

    @Override
    public int defaultBaudIndex() {
        return 1;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialDriverAdapter.class);

}
