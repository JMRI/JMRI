package jmri.jmrix.nce.ph5driver;

import java.util.Arrays;

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
public class Ph5DriverAdapter extends NcePortController {

    public Ph5DriverAdapter() {
        super(new NceSystemConnectionMemo());
        option1Name = "Eprom"; // NOI18N
        // the default is 2023 or later
        options.put(option1Name, new Option("Command Station EPROM", new String[]{"2023 or later"}));
        // TODO I18N
        setManufacturer(jmri.jmrix.nce.NceConnectionTypeList.NCE);
    }

    @Override
    public String openPort(String portName, String appName) {

        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("{}: failed to connect PH5 to {}", manufacturerName, portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("{}: Connecting PH5 to {} {}", manufacturerName, portName, currentSerialPort);
        
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

        this.getSystemConnectionMemo().configureCommandStation(NceTrafficController.OPTION_PH5);
        this.getSystemConnectionMemo().setNceCmdGroups(~NceTrafficController.CMDS_USB);

        tc.csm = new Ph5CmdStationMemory();
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Ph5DriverAdapter.class);

}
