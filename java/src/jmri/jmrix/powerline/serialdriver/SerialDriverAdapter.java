package jmri.jmrix.powerline.serialdriver;

import java.util.Arrays;
import jmri.jmrix.powerline.SerialPortController;
import jmri.jmrix.powerline.SerialSystemConnectionMemo;
import jmri.jmrix.powerline.SerialTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to Powerline devices via a serial com port.
 * Derived from the Oaktree code.
 *
 * @author Bob Jacobsen Copyright (C) 2006, 2007, 2008
 * @author Ken Cameron, (C) 2009, sensors from poll replies Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 */
public class SerialDriverAdapter extends SerialPortController {

    public SerialDriverAdapter() {
        super(new SerialSystemConnectionMemo());
        option1Name = "Adapter"; // NOI18N
        options.put(option1Name, new Option("Adapter", stdOption1Values));
        this.manufacturerName = jmri.jmrix.powerline.SerialConnectionTypeList.POWERLINE;
    }

    @Override
    public String openPort(String portName, String appName) {

        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("failed to connect Powerline to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting Powerline to {} {}", portName, currentSerialPort);
        
        // try to set it for communication via SerialDriver
        // find the baud rate value, configure comm options
        // find the baud rate value, configure comm options
        int baud = currentBaudNumber(mBaudRate);

        // check for specific port type
        String opt1 = getOptionState(option1Name);
        if (opt1.equals("CM11")) {
            // leave as 4800 baud
        } else if (opt1.equals("CP290")) {
            // set to 600 baud
            baud = 600;
        } else if (opt1.equals("Insteon 2412S")) {
            // set to 19200 baud
            baud = 19200;
        }
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
     * @return boolean of xmit port status
     */
    public boolean okToSend() {
        return true;
    }

    /**
     * Set up all of the other objects to operate connected to this port.
     */
    @Override
    public void configure() {
        SerialTrafficController tc = null;
        // set up the system connection first
        String opt1 = getOptionState(option1Name);
        if (opt1.equals("CM11")) {
            // create a CM11 port controller
            this.setSystemConnectionMemo(new jmri.jmrix.powerline.cm11.SpecificSystemConnectionMemo());
            tc = new jmri.jmrix.powerline.cm11.SpecificTrafficController(this.getSystemConnectionMemo());
        } else if (opt1.equals("CP290")) {
            // create a CP290 port controller
            this.setSystemConnectionMemo(new jmri.jmrix.powerline.cp290.SpecificSystemConnectionMemo());
            tc = new jmri.jmrix.powerline.cp290.SpecificTrafficController(this.getSystemConnectionMemo());
        } else if (opt1.equals("Insteon 2412S")) {
            // create an Insteon 2412s port controller
            this.setSystemConnectionMemo(new jmri.jmrix.powerline.insteon2412s.SpecificSystemConnectionMemo());
            tc = new jmri.jmrix.powerline.insteon2412s.SpecificTrafficController(this.getSystemConnectionMemo());
        } else {
            // no connection at all - warn
            log.warn("protocol option {} defaults to CM11", opt1);
            // create a CM11 port controller
            this.setSystemConnectionMemo(new jmri.jmrix.powerline.cm11.SpecificSystemConnectionMemo());
            tc = new jmri.jmrix.powerline.cm11.SpecificTrafficController(this.getSystemConnectionMemo());
        }

        // connect to the traffic controller
        this.getSystemConnectionMemo().setTrafficController(tc);
        tc.setAdapterMemo(this.getSystemConnectionMemo());
        this.getSystemConnectionMemo().configureManagers();
        tc.connectPort(this);
        // Configure the form of serial address validation for this connection
        this.getSystemConnectionMemo().setSerialAddress(new jmri.jmrix.powerline.SerialAddress(this.getSystemConnectionMemo()));
    }

    @Override
    public boolean status() {
        return opened;
    }

    String[] stdOption1Values = new String[]{"CM11", "CP290", "Insteon 2412S"}; // NOI18N

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

    protected String[] validSpeeds = new String[]{Bundle.getMessage("BaudAutomatic")};
    protected int[] validSpeedValues = new int[]{4800};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    // private control members

    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class);

}
