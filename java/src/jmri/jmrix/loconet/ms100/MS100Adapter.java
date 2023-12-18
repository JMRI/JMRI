package jmri.jmrix.loconet.ms100;

import java.util.Vector;

import jmri.jmrix.loconet.LnPacketizer;
import jmri.jmrix.loconet.LnPortController;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * Provide access to LocoNet via a MS100 attached to a serial com port.
 * Normally controlled by the jmri.jmrix.loconet.ms100.ConnectionConfig class.
 * <p>
 * By default, this attempts to use 16600 baud. If that fails, it falls back to
 * 16457 baud. Neither the baud rate configuration nor the "option 1" option are
 * used.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class MS100Adapter extends LnPortController {

    public MS100Adapter() {
        super(new LocoNetSystemConnectionMemo());
        option2Name = "CommandStation"; // NOI18N
        option3Name = "TurnoutHandle"; // NOI18N
        options.put(option2Name, new Option(Bundle.getMessage("CommandStationTypeLabel"), commandStationNames, false));
        options.put(option3Name, new Option(Bundle.getMessage("TurnoutHandling"),
                new String[]{Bundle.getMessage("HandleNormal"), Bundle.getMessage("HandleSpread"), Bundle.getMessage("HandleOneOnly"), Bundle.getMessage("HandleBoth")})); // I18N

    }

    Vector<String> portNameVector = null;

    @Override
    public String openPort(String portName, String appName) {

        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("failed to connect MS100 to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting MS100 via {} {}", portName, currentSerialPort);
        
        // try to set it for communication via SerialDriver
        // fixed baud rate
        setBaudRate(currentSerialPort, 16600);
        configureLeads(currentSerialPort, true, false);  // for MS100 power
        setFlowControl(currentSerialPort, FlowControl.NONE);

        // report status
        reportPortStatus(log, portName);

        opened = true;

        return null; // indicates OK return
    }

    /**
     * set up all of the other objects to operate with a MS100 connected to this
     * port
     */
    @Override
    public void configure() {

        setCommandStationType(getOptionState(option2Name));
        setTurnoutHandling(getOptionState(option3Name));
        // connect to a packetizing traffic controller
        LnPacketizer packets = new LnPacketizer(this.getSystemConnectionMemo());
        packets.connectPort(this);

        // create memo
        this.getSystemConnectionMemo().setLnTrafficController(packets);
        // do the common manager config
        this.getSystemConnectionMemo().configureCommandStation(commandStationType,
                mTurnoutNoRetry, mTurnoutExtraSpace, mTranspondingAvailable, mInterrogateAtStart, mLoconetProtocolAutoDetect);
        this.getSystemConnectionMemo().configureManagers();

        // start operation
        packets.startThreads();
    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * {@inheritDoc}
     *
     * Just a message saying it's fixed
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"fixed at 16,600 baud"};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{16600};
    }

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    /**
     * Set the second port option. Only to be used after construction, but
     * before the openPort call
     */
    @Override
    public void configureOption2(String value) {
        super.configureOption2(value);
        log.debug("configureOption2: {}", value);
        setCommandStationType(value);
    }

    // private control members
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MS100Adapter.class);

}
