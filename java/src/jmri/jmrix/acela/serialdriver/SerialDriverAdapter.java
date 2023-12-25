package jmri.jmrix.acela.serialdriver;

import jmri.jmrix.acela.AcelaPortController;
import jmri.jmrix.acela.AcelaSystemConnectionMemo;
import jmri.jmrix.acela.AcelaTrafficController;

/**
 * Implements SerialPortAdapter for the Acela system. This connects an Acela
 * interface to the CTI network via a serial com port. Normally controlled by
 * the SerialDriverFrame class.
 * <p>
 * The current implementation only handles the 9,600 baud rate, and does not use
 * any other options at configuration time.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2023
 *
 * @author Bob Coleman, Copyright (C) 2007, 2008 Based on MRC example, modified
 * to establish Acela support.
 */
public class SerialDriverAdapter extends AcelaPortController {

    public SerialDriverAdapter() {
        super(new AcelaSystemConnectionMemo());
        log.debug("opening Acela serial connection from memo");
        setManufacturer(jmri.jmrix.acela.AcelaConnectionTypeList.CTI);
    }

    @Override
    public String openPort(String portName, String appName) {
 
        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("failed to connect Acela to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting Acela to {} {}", portName, currentSerialPort);
        
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
     * Set up all of the other objects to operate with a serial command station
     * connected to this port.
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        AcelaTrafficController control = new AcelaTrafficController();
        control.connectPort(this);

        getSystemConnectionMemo().setAcelaTrafficController(control);
        getSystemConnectionMemo().configureManagers();
    }

    // base class methods for the AcelaPortController interface

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] validBaudRates() {
        // Really just want 9600 Baud for Acela
        return new String[]{Bundle.getMessage("Baud9600")};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
    // Only 9600 Baud for Acela
        return new int[]{9600};
    }

    @Override
    public int defaultBaudIndex() {
        return 0;
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialDriverAdapter.class);

}
