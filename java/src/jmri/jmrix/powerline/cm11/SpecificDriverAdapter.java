package jmri.jmrix.powerline.cm11;

import java.util.Arrays;
import jmri.jmrix.powerline.SerialPortController;
import jmri.jmrix.powerline.SerialTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to Powerline devices via a serial com port.
 * Derived from the Oaktree code.
 *
 * @author Bob Jacobsen Copyright (C) 2006, 2007, 2008
 * @author Ken Cameron, (C) 2009, sensors from poll replies Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011
 */
public class SpecificDriverAdapter extends SerialPortController {

    public SpecificDriverAdapter() {
        super(new SpecificSystemConnectionMemo());
    }

    @Override
    public String openPort(String portName, String appName) {

        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("failed to connect CM11 to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting CM11 to {} {}", portName, currentSerialPort);
        
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
     * set up all of the other objects to operate connected to this port
     */
    @Override
    public void configure() {
        SerialTrafficController tc = null;
        // create a CM11 port controller
        //adaptermemo = new jmri.jmrix.powerline.cm11.SpecificSystemConnectionMemo();
        tc = new SpecificTrafficController(this.getSystemConnectionMemo());

        // connect to the traffic controller
        this.getSystemConnectionMemo().setTrafficController(tc);
        tc.setAdapterMemo(this.getSystemConnectionMemo());
        this.getSystemConnectionMemo().configureManagers();
        tc.connectPort(this);
        // Configure the form of serial address validation for this connection
        this.getSystemConnectionMemo().setSerialAddress(new jmri.jmrix.powerline.SerialAddress(this.getSystemConnectionMemo()));
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

    @Override
    public boolean status() {
        return opened;
    }

    // private control members

    protected String[] validSpeeds = new String[]{Bundle.getMessage("BaudAutomatic")};
    protected int[] validSpeedValues = new int[]{4800};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    private final static Logger log = LoggerFactory.getLogger(SpecificDriverAdapter.class);

}
