package jmri.jmrix.nce.serialdriver;

import java.util.Arrays;

import jmri.jmrix.nce.NceCmdStationMemory;
import jmri.jmrix.nce.NcePortController;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;

/**
 * Implements SerialPortAdapter for the NCE system.
 * <p>
 * This connects an NCE command station via a serial com port. Normally
 * controlled by the SerialDriverFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @author Ken Cameron Copyright (C) 2013, 2023
 */
public class SerialDriverAdapter extends NcePortController {

    public SerialDriverAdapter() {
        super(new NceSystemConnectionMemo());
        option1Name = "Eprom"; // NOI18N
        // the default is 2006 or later
        options.put(option1Name, new Option("Command Station EPROM", new String[]{"2006 or later", "2004 or earlier"}));
        // TODO I18N
        setManufacturer(jmri.jmrix.nce.NceConnectionTypeList.NCE);
    }

    @Override
    public String openPort(String portName, String appName) {

        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("{}: failed to connect NCE to {}", this.getUserName(), portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("{}: Connecting serial to {} {}", this.getUserName(), portName, currentSerialPort);
        
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
     * Set up all of the other objects to operate with an NCE command station
     * connected to this port.
     */
    @Override
    public void configure() {
        NceTrafficController tc = new NceTrafficController();
        this.getSystemConnectionMemo().setNceTrafficController(tc);
        tc.setAdapterMemo(this.getSystemConnectionMemo());

        if (getOptionState(option1Name).equals(getOptionChoices(option1Name)[0])) {
            // setting binary mode
            this.getSystemConnectionMemo().configureCommandStation(NceTrafficController.OPTION_2006);
            this.getSystemConnectionMemo().setNceCmdGroups(~NceTrafficController.CMDS_USB);
        } else {
            this.getSystemConnectionMemo().configureCommandStation(NceTrafficController.OPTION_2004);
            this.getSystemConnectionMemo().setNceCmdGroups(~NceTrafficController.CMDS_USB);
        }

        tc.csm = new NceCmdStationMemory();
        tc.connectPort(this);
        
        this.getSystemConnectionMemo().configureManagers();
    }

    // base class methods for the NcePortController interface

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

    private String[] validSpeeds = new String[]{Bundle.getMessage("Baud9600")};
    private int[] validSpeedValues = new int[]{9600};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialDriverAdapter.class);

}
