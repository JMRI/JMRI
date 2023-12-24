package jmri.jmrix.dccpp.serial;

import java.util.Arrays;
import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppInitializationManager;
import jmri.jmrix.dccpp.DCCppSerialPortController;
import jmri.jmrix.dccpp.DCCppTrafficController;

/**
 * Provide access to DCC++ via a FTDI Virtual Com Port.
 *
 * @author Mark Underwood Copyright (C) 2015
 *
 * Based on jmri.jmirx.lenz.liusb.LIUSBAdapter by Paul Bender
 */
public class DCCppAdapter extends DCCppSerialPortController {

    public DCCppAdapter() {
        super();
        //option1Name = "FlowControl";
        //options.put(option1Name, new Option("DCC++ connection uses : ", validOption1));
        this.manufacturerName = jmri.jmrix.dccpp.DCCppConnectionTypeList.DCCPP;
    }

    @Override
    public String openPort(String portName, String appName) {
        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("failed to connect DCC++ to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting DCC++ to {} {}", portName, currentSerialPort);
        
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
     * Set up all of the other objects to operate with a DCC++ device connected
     * to this port.
     */
    @Override
    public void configure() {
        // connect to a packetizing traffic controller
        DCCppTrafficController packets = new SerialDCCppPacketizer(new DCCppCommandStation());
        packets.connectPort(this);

        // start operation
        // packets.startThreads();
        this.getSystemConnectionMemo().setDCCppTrafficController(packets);

        new DCCppInitializationManager(this.getSystemConnectionMemo());
    }

    // base class methods for the XNetSerialPortController interface

//     public BufferedReader getInputStreamBR() {
//         if (!opened) {
//             log.error("getInputStream called before load(), stream not available");
//             return null;
//         }
//         return new BufferedReader(new InputStreamReader(getInputStream()));
//     }

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

    protected String[] validSpeeds = new String[]{Bundle.getMessage("Baud115200")};
    protected int[] validSpeedValues = new int[]{115200};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    // meanings are assigned to these above, so make sure the order is consistent
    // protected String[] validOption1 = new String[]{Bundle.getMessage("FlowOptionHw"), Bundle.getMessage("FlowOptionNo")};
    protected String[] validOption1 = new String[]{Bundle.getMessage("FlowOptionNo")};

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DCCppAdapter.class);

}
