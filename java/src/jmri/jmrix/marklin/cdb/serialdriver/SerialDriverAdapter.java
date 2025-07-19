package jmri.jmrix.marklin.cdb.serialdriver;

import java.util.Arrays;
import jmri.jmrix.marklin.cdb.CdBPortController;
import jmri.jmrix.marklin.cdb.CdBSystemConnectionMemo;
import jmri.jmrix.marklin.MarklinTrafficController;

/**
 * Implements SerialPortAdapter for the Marklin CDB system.
 * <p>
 * This connects a CC-Schnitte command station via a serial usb port.
 * <p>
 * Based on work by Bob Jacobsen
 *
 * @author Ralf Lang Copyright (C) 2022
 */
public class SerialDriverAdapter extends CdBPortController {

    public SerialDriverAdapter() {
        super(new CdBSystemConnectionMemo());
        setManufacturer(jmri.jmrix.marklin.cdb.CdBConnectionTypeList.CDB);
    }

    @Override
    public String openPort(String portName, String appName) {

        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("failed to connect Marklin CDB to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting Marklin CDB to {} {}", portName, currentSerialPort);
        
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
        MarklinTrafficController tc = new MarklinTrafficController();
        this.getSystemConnectionMemo().setMarklinTrafficController(tc);
        tc.setAdapterMemo(this.getSystemConnectionMemo());

        tc.connectPort(this);

        this.getSystemConnectionMemo().configureManagers();
    }

    // base class methods for the MarklinPortController interface

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

    private final String[] validSpeeds = new String[]{Bundle.getMessage("Baud500000")};
    private final int[] validSpeedValues = new int[]{500000};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialDriverAdapter.class);

}
