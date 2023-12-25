package jmri.jmrix.direct.serial;

import jmri.jmrix.direct.PortController;
import jmri.jmrix.direct.TrafficController;

/**
 * Implements SerialPortAdapter for direct serial drive.
 * <p>
 * Normally controlled by the SerialDriverFrame class.
 * <p>
 * The current implementation only handles the 19,200 baud rate, and does not
 * use any other options at configuration time. A prior implementation
 * tried 17240, then 16457, then finally 19200.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2004, 2023
 */
public class SerialDriverAdapter extends PortController {

    @Override
    public String openPort(String portName, String appName) {

        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("failed to connect Direct Serial to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting Direct Serial to {} {}", portName, currentSerialPort);
        
        // try to set it for communication via SerialDriver
        setBaudRate(currentSerialPort, 19200);
        configureLeads(currentSerialPort, true, true);
        setFlowControl(currentSerialPort, FlowControl.NONE);

        // report status
        reportPortStatus(log, portName);

        opened = true;

        return null; // indicates OK return
    }

    /**
     * Set up all of the other objects to operate with direct drive on this port.
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        TrafficController tc = new TrafficController((jmri.jmrix.direct.DirectSystemConnectionMemo)getSystemConnectionMemo());
        ((jmri.jmrix.direct.DirectSystemConnectionMemo)getSystemConnectionMemo()).setTrafficController(tc);
        // connect to the traffic controller
        tc.connectPort(this);

        // do the common manager config
        ((jmri.jmrix.direct.DirectSystemConnectionMemo)getSystemConnectionMemo()).configureManagers();
    }

    // base class methods for the PortController interface

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean status() {
        return opened;
    }

    /**
     * {@inheritDoc}
     * Currently only 19,200 bps.
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{Bundle.getMessage("Baud19200")};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{19200};
    }

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialDriverAdapter.class);

}
