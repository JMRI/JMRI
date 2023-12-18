package jmri.jmrix.ztc.ztc611;

import java.util.Arrays;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInitializationManager;
import jmri.jmrix.lenz.XNetSerialPortController;
import jmri.jmrix.lenz.XNetTrafficController;

/**
 * Provide access to XpressNet via a ZTC611 connected via an FTDI virtual comm
 * port.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender, Copyright (C) 2003-2017
 */
public class ZTC611Adapter extends XNetSerialPortController {

    public ZTC611Adapter() {
        super();
        option1Name = "FlowControl"; // NOI18N
        options.put(option1Name, new Option(Bundle.getMessage("XconnectionUsesLabel", Bundle.getMessage("CSTypeZtc640")), validOption1));
    }

    @Override
    public String openPort(String portName, String appName) {
        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("failed to connect ZTC611 to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting ZTC611 to {} {}", portName, currentSerialPort);
        
        // try to set it for communication via SerialDriver
        // find the baud rate value, configure comm options
        int baud = currentBaudNumber(mBaudRate);
        setBaudRate(currentSerialPort, baud);
        configureLeads(currentSerialPort, true, true);
        
        // find and configure flow control
        FlowControl flow = FlowControl.NONE;  // no flow control is first in the elite setup,
        // since it doesn't seem to work with flow
        // control enabled.
        if (!getOptionState(option1Name).equals(validOption1[0])) {
            flow = FlowControl.RTSCTS;
        }
        setFlowControl(currentSerialPort, flow);

        // report status
        reportPortStatus(log, portName);

        opened = true;

        return null; // indicates OK return
    }

    /**
     * set up all of the other objects to operate with a ZTC611 connected to
     * this port
     */
    @Override
    public void configure() {
        // connect to a packetizing traffic controller
        XNetTrafficController packets = new ZTC611XNetPacketizer(new LenzCommandStation());
        packets.connectPort(this);

        // start operation
        // packets.startThreads();
        this.getSystemConnectionMemo().setXNetTrafficController(packets);
        new XNetInitializationManager()
                .memo(this.getSystemConnectionMemo())
                .setDefaults()
                .turnoutManager(ZTC611XNetTurnoutManager.class)
                .init();
    }

    // base class methods for the XNetSerialPortController interface
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

    protected final String[] validSpeeds = new String[]{Bundle.getMessage("Baud9600")};
    protected final int[] validSpeedValues = new int[]{19200};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    // meanings are assigned to these above, so make sure the order is consistent
    protected final String[] validOption1 = new String[]{Bundle.getMessage("FlowOptionNoRecomm"), Bundle.getMessage("FlowOptionHw")};

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ZTC611Adapter.class);

}
