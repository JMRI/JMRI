package jmri.jmrix.nce.usbdriver;

import java.util.Arrays;
import jmri.jmrix.nce.NcePortController;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;

/**
 * Implements UsbPortAdapter for the NCE system.
 * <p>
 * This connects an NCE PowerCab or PowerPro via a USB port. Normally
 * controlled by the UsbDriverFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @author Daniel Boudreau Copyright (C) 2007
 * @author Ken Cameron Copyright (C) 2013, 2023
 */
public class UsbDriverAdapter extends NcePortController {

    public UsbDriverAdapter() {
        super(new NceSystemConnectionMemo());
        option1Name = "System"; // NOI18N
        options.put(option1Name, new Option(Bundle.getMessage("SystemLabel"), option1Values, false));
        option2Name = "USB Version"; // NOI18N
        options.put(option2Name, new Option(Bundle.getMessage("UsbVersionLabel"), option2Values, false));
        // Set default USB version to V7.x.x
        setOptionState(option2Name, getOptionChoices(option2Name)[1]);
    }

    @Override
    public String openPort(String portName, String appName) {

        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("{}: failed to connect NCE USB to {}", manufacturerName, portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("{}: Connecting NCE USB to {} {}", manufacturerName, portName, currentSerialPort);
        
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

    String[] option1Values = new String[]{"PowerCab", "SB3/SB3a", "Power Pro", "Twin", "SB5"}; // NOI18N
    String[] option2Values = new String[]{"V6.x.x", "V7.x.x"}; // NOI18N

    /**
     * Set up all of the other objects to operate with an NCE command station
     * connected to this port.
     */
    @Override
    public void configure() {
        log.trace("configure with {}", getSystemConnectionMemo());
        NceTrafficController tc = new NceTrafficController();
        this.getSystemConnectionMemo().setNceTrafficController(tc);
        tc.setAdapterMemo(this.getSystemConnectionMemo());

        //set the system the USB is connected to
        if (getOptionState(option2Name).equals(getOptionChoices(option2Name)[1])) { //if V7 (Nov 2012)
            // is new firmware, determine functions available
            if (getOptionState(option1Name).equals(getOptionChoices(option1Name)[4])) { //SB5
                tc.setUsbSystem(NceTrafficController.USB_SYSTEM_SB5);
                tc.setCmdGroups(NceTrafficController.CMDS_MEM
                        | NceTrafficController.CMDS_AUI_READ
                        | NceTrafficController.CMDS_OPS_PGM
                        | NceTrafficController.CMDS_USB
                        | NceTrafficController.CMDS_ALL_SYS);
                this.getSystemConnectionMemo().configureCommandStation(NceTrafficController.OPTION_1_65);

            } else if (getOptionState(option1Name).equals(getOptionChoices(option1Name)[3])) { //TWIN
                tc.setUsbSystem(NceTrafficController.USB_SYSTEM_TWIN);
                tc.setCmdGroups(NceTrafficController.CMDS_MEM
                        | NceTrafficController.CMDS_AUI_READ
                        | NceTrafficController.CMDS_PROGTRACK
                        | NceTrafficController.CMDS_OPS_PGM
                        | NceTrafficController.CMDS_USB
                        | NceTrafficController.CMDS_ALL_SYS);
                this.getSystemConnectionMemo().configureCommandStation(NceTrafficController.OPTION_1_65);
            } else if (getOptionState(option1Name).equals(getOptionChoices(option1Name)[2])) { //PowerPro
                tc.setUsbSystem(NceTrafficController.USB_SYSTEM_POWERPRO);
                tc.setCmdGroups(NceTrafficController.CMDS_USB
                        | NceTrafficController.CMDS_ALL_SYS);
                this.getSystemConnectionMemo().configureCommandStation(NceTrafficController.OPTION_2006);
            } else if (getOptionState(option1Name).equals(getOptionChoices(option1Name)[1])) { //SB3
                tc.setUsbSystem(NceTrafficController.USB_SYSTEM_SB3);
                tc.setCmdGroups(NceTrafficController.CMDS_OPS_PGM
                        | NceTrafficController.CMDS_USB
                        | NceTrafficController.CMDS_ALL_SYS);
                this.getSystemConnectionMemo().configureCommandStation(NceTrafficController.OPTION_1_28);
            } else { //PowerCab
                tc.setUsbSystem(NceTrafficController.USB_SYSTEM_POWERCAB);
                tc.setCmdGroups(NceTrafficController.CMDS_MEM
                        | NceTrafficController.CMDS_AUI_READ
                        | NceTrafficController.CMDS_PROGTRACK
                        | NceTrafficController.CMDS_OPS_PGM
                        | NceTrafficController.CMDS_USB
                        | NceTrafficController.CMDS_ALL_SYS);
                this.getSystemConnectionMemo().configureCommandStation(NceTrafficController.OPTION_1_65);
            }
        } else {
            // old firmware, original functions
            if (getOptionState(option1Name).equals(getOptionChoices(option1Name)[4])) {
                tc.setUsbSystem(NceTrafficController.USB_SYSTEM_SB5);
                tc.setCmdGroups(NceTrafficController.CMDS_OPS_PGM
                        | NceTrafficController.CMDS_USB
                        | NceTrafficController.CMDS_ALL_SYS);
                this.getSystemConnectionMemo().configureCommandStation(NceTrafficController.OPTION_1_28);
            } else if (getOptionState(option1Name).equals(getOptionChoices(option1Name)[3])) {
                tc.setUsbSystem(NceTrafficController.USB_SYSTEM_TWIN);
                tc.setCmdGroups(NceTrafficController.CMDS_OPS_PGM
                        | NceTrafficController.CMDS_USB
                        | NceTrafficController.CMDS_ALL_SYS);
                this.getSystemConnectionMemo().configureCommandStation(NceTrafficController.OPTION_1_28);
            } else if (getOptionState(option1Name).equals(getOptionChoices(option1Name)[2])) {
                tc.setUsbSystem(NceTrafficController.USB_SYSTEM_POWERPRO);
                tc.setCmdGroups(NceTrafficController.CMDS_NONE
                        | NceTrafficController.CMDS_USB
                        | NceTrafficController.CMDS_ALL_SYS);
                this.getSystemConnectionMemo().configureCommandStation(NceTrafficController.OPTION_2006);
            } else if (getOptionState(option1Name).equals(getOptionChoices(option1Name)[1])) {
                tc.setUsbSystem(NceTrafficController.USB_SYSTEM_SB3);
                tc.setCmdGroups(NceTrafficController.CMDS_ACCYADDR250
                        | NceTrafficController.CMDS_OPS_PGM
                        | NceTrafficController.CMDS_USB
                        | NceTrafficController.CMDS_ALL_SYS);
                this.getSystemConnectionMemo().configureCommandStation(NceTrafficController.OPTION_1_28);
            } else {
                tc.setUsbSystem(NceTrafficController.USB_SYSTEM_POWERCAB);
                tc.setCmdGroups(NceTrafficController.CMDS_PROGTRACK
                        | NceTrafficController.CMDS_OPS_PGM
                        | NceTrafficController.CMDS_USB
                        | NceTrafficController.CMDS_ALL_SYS);
                this.getSystemConnectionMemo().configureCommandStation(NceTrafficController.OPTION_1_28);
            }
        }

        tc.csm = new UsbCmdStationMemory();
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

    private String[] validSpeeds = new String[]{Bundle.getMessage("Baud9600"), Bundle.getMessage("Baud19200")};
    private int[] validSpeedValues = new int[]{9600, 19200};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UsbDriverAdapter.class);

}
