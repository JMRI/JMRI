package jmri.jmrix.nce.usbdriver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Vector;
import jmri.jmrix.nce.NcePortController;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Implements UsbPortAdapter for the NCE system.
 * <P>
 * This connects an NCE PowerCab or PowerHouse via a USB port. Normally
 * controlled by the UsbDriverFrame class.
 * <P>
 *
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @author Daniel Boudreau Copyright (C) 2007
 * @author ken cameron Copyright (C) 2013
 */
public class UsbDriverAdapter extends NcePortController {

    Vector<String> portNameVector = null;
    SerialPort activeSerialPort = null;

    public UsbDriverAdapter() {
        super(new NceSystemConnectionMemo());
        option1Name = "System"; // NOI18N
        options.put(option1Name, new Option("System:", option1Values, false));
        option2Name = "USB Version";
        options.put(option2Name, new Option("USB Version", option2Values, false));
        // Set default USB version to V7.x.x
        setOptionState(option2Name, getOptionChoices(option2Name)[1]);
    }

    @Override
    public String openPort(String portName, String appName) {
        // open the port, check ability to set moderators
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 2000);  // name of program, msec to wait
            } catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }

            // try to set it for communication via SerialDriver
            try {
                // find the baud rate value, configure comm options
                int baud = validSpeedValues[0];  // default, but also defaulted in the initial value of selectedSpeed
                for (int i = 0; i < validSpeeds.length; i++) {
                    if (validSpeeds[i].equals(mBaudRate)) {
                        baud = validSpeedValues[i];
                    }
                }
                activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port " + portName + ": " + e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
            }

            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            configureLeadsAndFlowControl(activeSerialPort, 0);
            activeSerialPort.enableReceiveTimeout(50);  // 50 mSec timeout before sending chars

            // set timeout
            // activeSerialPort.enableReceiveTimeout(1000);
            log.debug("Serial timeout was observed as: " + activeSerialPort.getReceiveTimeout()
                    + " " + activeSerialPort.isReceiveTimeoutEnabled());

            // get and save stream
            serialStream = activeSerialPort.getInputStream();

            // purge contents, if any
            purgeStream(serialStream);

            // report status
            if (log.isInfoEnabled()) {
                log.info("NCE USB " + portName + " port opened at "
                        + activeSerialPort.getBaudRate() + " baud");
            }
            opened = true;

        } catch (NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (UnsupportedCommOperationException | IOException ex) {
            log.error("Unexpected exception while opening port {}", portName, ex);
            return "Unexpected error while opening port " + portName + ": " + ex;
        }

        return null; // indicates OK return

    }

    String[] option1Values = new String[]{"PowerCab", "SB3/SB3a", "Power Pro", "Twin", "SB5"};
    String[] option2Values = new String[]{"V6.x.x", "V7.x.x"};

    /**
     * set up all of the other objects to operate with an NCE command station
     * connected to this port
     */
    @Override
    public void configure() {
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
                tc.setUsbSystem(NceTrafficController.USB_SYSTEM_POWERHOUSE);
                tc.setCmdGroups(NceTrafficController.CMDS_OPS_PGM
                        | NceTrafficController.CMDS_AUI_READ
                        | NceTrafficController.CMDS_USB
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
                tc.setUsbSystem(NceTrafficController.USB_SYSTEM_POWERHOUSE);
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

        tc.connectPort(this);

        this.getSystemConnectionMemo().configureManagers();
    }

    // base class methods for the NcePortController interface
    @Override
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
            return null;
        }
        return new DataInputStream(serialStream);
    }

    @Override
    public DataOutputStream getOutputStream() {
        if (!opened) {
            log.error("getOutputStream called before load(), stream not available");
        }
        try {
            return new DataOutputStream(activeSerialPort.getOutputStream());
        } catch (java.io.IOException e) {
            log.error("getOutputStream exception: " + e);
        }
        return null;
    }

    @Override
    public boolean status() {
        return opened;
    }

    @Override
    public String[] validBaudRates() {
        return Arrays.copyOf(validSpeeds, validSpeeds.length);
    }

    private String[] validSpeeds = new String[]{"9,600 baud", "19,200 baud"};
    private int[] validSpeedValues = new int[]{9600, 19200};

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;

    private final static Logger log = LoggerFactory.getLogger(UsbDriverAdapter.class);

}
