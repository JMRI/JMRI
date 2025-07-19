package jmri.jmrix.lenz.hornbyelite;

import java.util.Arrays;

import jmri.jmrix.lenz.*;

/**
 * Provide access to XpressNet via the Hornby Elite's built in USB port.
 * Normally controlled by the lenz.hornbyelite.EliteFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender, Copyright (C) 2003,2008-2010
 */
public class EliteAdapter extends XNetSerialPortController {

    public EliteAdapter() {
        super(new EliteXNetSystemConnectionMemo());
        option1Name = "FlowControl"; // NOI18N
        options.put(option1Name, new Option(Bundle.getMessage("HornbyEliteConnectionLabel"), validOption1));
        this.manufacturerName = EliteConnectionTypeList.HORNBY;
    }

    @Override
    public String openPort(String portName, String appName) {
        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("failed to connect Elite Adapter to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting Elite Adapter to {} {}", portName, currentSerialPort);
        
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
     * Set up all of the other objects to operate with the Hornby Elite
     * connected to this port.
     */
    @Override
    public void configure() {
        // connect to a packetizing traffic controller
        XNetTrafficController packets = new XNetPacketizer(new HornbyEliteCommandStation());
        packets.connectPort(this);

        // start operation
        this.getSystemConnectionMemo().setXNetTrafficController(packets);
        new XNetInitializationManager()
                .memo(this.getSystemConnectionMemo())
                .powerManager(XNetPowerManager.class)
                .throttleManager(EliteXNetThrottleManager.class)
                .programmer(EliteXNetProgrammer.class)
                .programmerManager(XNetProgrammerManager.class)
                .turnoutManager(EliteXNetTurnoutManager.class)
                .lightManager(XNetLightManager.class)
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

    /**
     * validOption1 controls flow control option.
     */
    protected final String[] validSpeeds = new String[]{Bundle.getMessage("Baud9600"),
            Bundle.getMessage("Baud19200"), Bundle.getMessage("Baud38400"),
            Bundle.getMessage("Baud57600"), Bundle.getMessage("Baud115200")};
    protected final int[] validSpeedValues = new int[]{9600, 19200, 38400, 57600, 115200};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    // meanings are assigned to these above, so make sure the order is consistent
    protected final String[] validOption1 = new String[]{Bundle.getMessage("FlowOptionNo"), Bundle.getMessage("FlowOptionHw")};

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EliteAdapter.class);

}
