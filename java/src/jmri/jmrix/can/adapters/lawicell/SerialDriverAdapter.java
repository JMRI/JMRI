package jmri.jmrix.can.adapters.lawicell;

import java.util.Arrays;

import jmri.jmrix.can.TrafficController;

/**
 * Implements SerialPortAdapter for the LAWICELL protocol.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2008
 * @author Andrew Crosland Copyright (C) 2008
 */
public class SerialDriverAdapter extends PortController {

    public SerialDriverAdapter() {
        super(new jmri.jmrix.can.CanSystemConnectionMemo());
        option1Name = "Protocol"; // NOI18N
        options.put(option1Name, new Option(Bundle.getMessage("ConnectionProtocol"),
                jmri.jmrix.can.ConfigurationManager.getSystemOptions()));
    }

    @Override
    public String openPort(String portName, String appName) {

        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("failed to connect CAN Lawicell to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting CAN Lawicell to {} {}", portName, currentSerialPort);
        
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
     * Set up all of the other objects to operate with a CAN RS adapter
     * connected to this port.
     */
    @Override
    public void configure() {

        // Register the CAN traffic controller being used for this connection
        TrafficController tc = new LawicellTrafficController();
        this.getSystemConnectionMemo().setTrafficController(tc);

        // Now connect to the traffic controller
        log.debug("Connecting port");
        tc.connectPort(this);

        // send a request for version information, set 125kbps, open channel
        log.debug("send version request");
        jmri.jmrix.can.CanMessage m
                = new jmri.jmrix.can.CanMessage(new int[]{'V', 13, 'S', '4', 13, 'O', 13}, tc.getCanid());
        m.setTranslated(true);
        tc.sendCanMessage(m, null);

        // do central protocol-specific configuration
        this.getSystemConnectionMemo().setProtocol(getOptionState(option1Name));

        this.getSystemConnectionMemo().configureManagers();
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

    @Override
    public int[] validBaudNumbers() {
        return Arrays.copyOf(validSpeedValues, validSpeedValues.length);
    }

    protected String[] validSpeeds = new String[]{Bundle.getMessage("Baud57600"),
            Bundle.getMessage("Baud115200"), Bundle.getMessage("Baud230400"),
            Bundle.getMessage("Baud250000"), Bundle.getMessage("Baud333333"),
            Bundle.getMessage("Baud460800"), Bundle.getMessage("Baud500000")};
    protected int[] validSpeedValues = new int[]{57600, 115200, 230400, 250000, 333333, 460800, 500000};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    // private control members

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialDriverAdapter.class);

}
