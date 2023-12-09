package jmri.jmrix.loconet.ms100;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
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
        activeSerialPort = activatePort(portName, log);
        if (activeSerialPort == null) {
            log.error("failed to connect SPROG to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting MS100 via {} {}", portName, activeSerialPort);
        
        // try to set it for communication via SerialDriver
        // fixed baud rate
        setBaudRate(activeSerialPort, 16600);
        configureLeads(activeSerialPort, true, false);  // for MS100 power
        setFlowControl(activeSerialPort, FlowControl.NONE);

        // get and save stream
        serialStream = activeSerialPort.getInputStream();

        // purge contents, if any
        purgeStream(serialStream);

        // report status?
        if (log.isInfoEnabled()) {
            log.info("{} port opened at {} baud, sees  DTR: {} RTS: {} DSR: {} CTS: {}  name: {}", 
                    portName, activeSerialPort.getBaudRate(), activeSerialPort.getDTR(), 
                    activeSerialPort.getRTS(), activeSerialPort.getDSR(), activeSerialPort.getCTS(), 
                    activeSerialPort);
        }

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

    // base class methods for the LnPortController interface
    @Override
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("called before load(), stream not available");
            return null;
        }
        return new DataInputStream(serialStream);
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
    private boolean opened = false;
    InputStream serialStream = null;
 
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MS100Adapter.class);

}
