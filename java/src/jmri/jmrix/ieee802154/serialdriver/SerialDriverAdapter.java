package jmri.jmrix.ieee802154.serialdriver;

import java.util.Arrays;
import jmri.jmrix.ieee802154.IEEE802154PortController;
import jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to IEEE802.15.4 devices via a serial com port.
 * Derived from the Oaktree code.
 *
 * @author Bob Jacobsen Copyright (C) 2006, 2007, 2008
 * @author Ken Cameron, (C) 2009, sensors from poll replies Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011
 * @author Paul Bender Copyright (C) 2013,2023
 */
public class SerialDriverAdapter extends IEEE802154PortController {

    public SerialDriverAdapter() {
        this(new SerialSystemConnectionMemo());
    }

    protected SerialDriverAdapter(IEEE802154SystemConnectionMemo connectionMemo) {
        super(connectionMemo);
        this.manufacturerName = jmri.jmrix.ieee802154.SerialConnectionTypeList.IEEE802154;
    }

    @Override
    public String openPort(String portName, String appName) {
        currentSerialPort = activatePort(portName,log);
        // try to set it for serial
        setSerialPort();

        // report status?
        reportPortStatus(log,portName);
        opened = true;

        return null; // normal operation
    }

    /**
     * Can the port accept additional characters? Yes, always
     * @return always true
     */
    public boolean okToSend() {
        return true;
    }

    /**
     * Set up all of the other objects to operate connected to this port.
     */
    @Override
    public void configure() {
        log.debug("configure() called.");
        SerialTrafficController tc = new SerialTrafficController();

        // connect to the traffic controller
        this.getSystemConnectionMemo().setTrafficController(tc);
        tc.setAdapterMemo(this.getSystemConnectionMemo());
        this.getSystemConnectionMemo().configureManagers();
        tc.connectPort(this);
    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * Local method to do specific port configuration.
     */
    protected void setSerialPort() {
        // find the baud rate value, configure comm options
        int baud = currentBaudNumber(mBaudRate);
        setBaudRate(currentSerialPort,baud);

        // find and configure flow control
        configureLeads(currentSerialPort,true,true);
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

    String[] stdOption1Values = new String[]{"CM11", "CP290", "Insteon 2412S"}; // NOI18N

    public String[] validOption1() {
        return Arrays.copyOf(stdOption1Values, stdOption1Values.length);
    }

    /**
     * Get a String that says what Option 1 represents.
     *
     * @return fixed string 'Adapter'
     */
    public String option1Name() {
        return "Adapter"; // NOI18N
    }

    private String[] validSpeeds = new String[]{Bundle.getMessage("BaudAutomatic")};
    private int[] validSpeedValues = new int[]{9600};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    /**
     * Get an array of valid values for "option 2"; used to display valid
     * options. May not be null, but may have zero entries.
     *
     * @return empty string array
     */
    public String[] validOption2() {
        return new String[]{""};
    }

    /**
     * Get a String that says what Option 2 represents. May be an empty string,
     * but will not be null.
     *
     * @return empty string
     */
    public String option2Name() {
        return "";
    }

    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class);

}
