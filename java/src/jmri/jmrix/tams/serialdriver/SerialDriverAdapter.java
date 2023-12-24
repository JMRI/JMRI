package jmri.jmrix.tams.serialdriver;

import java.util.Arrays;
import jmri.jmrix.tams.TamsPortController;
import jmri.jmrix.tams.TamsSystemConnectionMemo;
import jmri.jmrix.tams.TamsTrafficController;

/**
 * Implements SerialPortAdapter for the TAMS system.
 * <p>
 * This connects an TAMS command station via a serial com port.
 * <p>
 * Based on work by Bob Jacobsen
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class SerialDriverAdapter extends TamsPortController {

    public SerialDriverAdapter() {
        super(new TamsSystemConnectionMemo());
        setManufacturer(jmri.jmrix.tams.TamsConnectionTypeList.TAMS);
    }

    @Override
    public String openPort(String portName, String appName) {

        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("failed to connect TAMS to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting TAMS to {} {}", portName, currentSerialPort);
        
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
     * set up all of the other objects to operate with an NCE command station
     * connected to this port
     */
    @Override
    public void configure() {
        TamsTrafficController tc = new TamsTrafficController();
        this.getSystemConnectionMemo().setTamsTrafficController(tc);
        tc.setAdapterMemo(this.getSystemConnectionMemo());

        tc.connectPort(this);

        this.getSystemConnectionMemo().configureManagers();
    }

    // base class methods for the TamsPortController interface

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

    private final String[] validSpeeds = new String[]{Bundle.getMessage("Baud57600"),
            Bundle.getMessage("Baud2400"), Bundle.getMessage("Baud9600"),
            Bundle.getMessage("Baud19200")};
    private final int[] validSpeedValues = new int[]{57600, 2400, 9600, 19200};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialDriverAdapter.class);

}
