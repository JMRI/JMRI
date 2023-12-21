package jmri.jmrix.rfid.serialdriver;

import java.util.Arrays;
import jmri.jmrix.rfid.RfidPortController;
import jmri.jmrix.rfid.RfidProtocol;
import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import jmri.jmrix.rfid.RfidTrafficController;
import jmri.jmrix.rfid.generic.standalone.StandaloneReporterManager;
import jmri.jmrix.rfid.generic.standalone.StandaloneSensorManager;
import jmri.jmrix.rfid.generic.standalone.StandaloneTrafficController;
import jmri.jmrix.rfid.merg.concentrator.ConcentratorReporterManager;
import jmri.jmrix.rfid.merg.concentrator.ConcentratorSensorManager;
import jmri.jmrix.rfid.merg.concentrator.ConcentratorTrafficController;
import jmri.jmrix.rfid.protocol.coreid.CoreIdRfidProtocol;
import jmri.jmrix.rfid.protocol.em18.Em18RfidProtocol;
import jmri.jmrix.rfid.protocol.olimex.OlimexRfid1356mifareProtocol;
import jmri.jmrix.rfid.protocol.olimex.OlimexRfidProtocol;
import jmri.jmrix.rfid.protocol.parallax.ParallaxRfidProtocol;
import jmri.jmrix.rfid.protocol.seeedstudio.SeeedStudioRfidProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to RFID devices via a serial com port.
 * Derived from the Oaktree code.
 *
 * @author Bob Jacobsen Copyright (C) 2006, 2007, 2008
 * @author Matthew Harris Copyright (C) 2011
 * @author Oscar A. Pruitt Copyright (C) 2015
 * @author B. Milhaupt Copyright (C) 2017
 * @since 2.11.4
 */
public class SerialDriverAdapter extends RfidPortController {

    public SerialDriverAdapter() {
        super(new RfidSystemConnectionMemo());
        option1Name = "Adapter"; // NOI18N
        option2Name = "Concentrator-Range"; // NOI18N
        option3Name = "Protocol"; // NOI18N
        option4Name = "Device"; // NOI18N
        options.put(option1Name, new Option(Bundle.getMessage("ConnectionAdapter"), new String[]{"Generic Stand-alone", "MERG Concentrator"}, false)); // NOI18N
        options.put(option2Name, new Option(Bundle.getMessage("ConnectionConcentratorRange"), new String[]{"A-H", "I-P"}, false)); // NOI18N
        options.put(option3Name, new Option(Bundle.getMessage("ConnectionProtocol"), new String[]{"CORE-ID", "Olimex", "Parallax", "SeeedStudio", "EM-18"}, false)); // NOI18N
        options.put(option4Name, new Option(Bundle.getMessage("ConnectionDeviceType"), new String[]{"MOD-RFID125", "MOD-RFID1356MIFARE"}, false)); // NOI18N
        this.manufacturerName = jmri.jmrix.rfid.RfidConnectionTypeList.RFID;
    }

    @Override
    public String openPort(String portName, String appName) {
        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("failed to connect RFID to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting RFID to {} {}", portName, currentSerialPort);
        
        // try to set it for communication via SerialDriver
        // find the baud rate value, configure comm options
        int baud = currentBaudNumber(mBaudRate);

        // the Parallax reader uses 2400 baud, so set that here
        if (getOptionState(option3Name).equals("Parallax")) {
            log.debug("Set baud rate to 2400 for Parallax reader");
            baud = 2400;
        }
        setBaudRate(currentSerialPort, baud);
        configureLeads(currentSerialPort, true, true);
        // find and configure flow control
        FlowControl flow = FlowControl.NONE; // default
        if (getOptionState(option1Name).equals("MERG Concentrator")) {
            // Set Hardware Flow Control for Concentrator
            log.debug("Set hardware flow control for Concentrator");
            flow = FlowControl.RTSCTS;
        }
        setFlowControl(currentSerialPort, flow);

        // report status
        reportPortStatus(log, portName);

        opened = true;

        return null; // indicates OK return
    }

    /**
     * Can the port accept additional characters?
     *
     * @return always true
     */
    public boolean okToSend() {
        return true;
    }

    /**
     * Set up all of the other objects to operate connected to this port
     */
    @Override
    public void configure() {
        RfidTrafficController control;
        RfidProtocol protocol;

        // set up the system connection first
        String opt1 = getOptionState(option1Name);
        switch (opt1) {
            case "Generic Stand-alone": // NOI18N
                // create a Generic Stand-alone port controller
                log.debug("Create Generic Standalone SpecificTrafficController"); // NOI18N
                control = new StandaloneTrafficController(this.getSystemConnectionMemo());
                this.getSystemConnectionMemo().setRfidTrafficController(control);
                this.getSystemConnectionMemo().configureManagers(
                        new StandaloneSensorManager(this.getSystemConnectionMemo()),
                        new StandaloneReporterManager(this.getSystemConnectionMemo()));
                break;
            case "MERG Concentrator": // NOI18N
                // create a MERG Concentrator port controller
                log.debug("Create MERG Concentrator SpecificTrafficController"); // NOI18N
                control = new ConcentratorTrafficController(this.getSystemConnectionMemo(), getOptionState(option2Name));
                this.getSystemConnectionMemo().setRfidTrafficController(control);
                this.getSystemConnectionMemo().configureManagers(
                        new ConcentratorSensorManager(this.getSystemConnectionMemo()),
                        new ConcentratorReporterManager(this.getSystemConnectionMemo()));
                break;
            default:
                // no connection at all - warn
                log.warn("adapter option {} defaults to Generic Stand-alone", opt1); // NOI18N
                // create a Generic Stand-alone port controller
                control = new StandaloneTrafficController(this.getSystemConnectionMemo());
                this.getSystemConnectionMemo().setRfidTrafficController(control);
                this.getSystemConnectionMemo().configureManagers(
                        new StandaloneSensorManager(this.getSystemConnectionMemo()),
                        new StandaloneReporterManager(this.getSystemConnectionMemo()));
                break;
        }

        // Now do the protocol
        String opt3 = getOptionState(option3Name);
        String opt4 = getOptionState(option4Name);
        if (opt1.equals("MERG Concentrator")) { // NOI18N
            // MERG Concentrator only supports CORE-ID
            log.info("set protocol to CORE-ID"); // NOI18N
            String opt2 = getOptionState(option2Name);
            switch (opt2) {
                case "A-H": // NOI18N
                    log.info("set concentrator range to 'A-H' at position 1"); // NOI18N
                    protocol = new CoreIdRfidProtocol('A', 'H', 1);
                    break;
                case "I-P": // NOI18N
                    log.info("set concentrator range to 'I-P' at position 1"); // NOI18N
                    protocol = new CoreIdRfidProtocol('I', 'P', 1);
                    break;
                default:
                    // unrecognised concentrator range - warn
                    log.warn("concentrator range '{}' not supported - default to no concentrator", opt2); // NOI18N
                    protocol = new CoreIdRfidProtocol();
                    break;
            }
        } else {
            switch (opt3) {
                case "CORE-ID": // NOI18N
                    log.info("set protocol to CORE-ID"); // NOI18N
                    protocol = new CoreIdRfidProtocol();
                    break;
                case "Olimex": // NOI18N
                    if (opt4.equals("MOD-RFID1356MIFARE")) { // NOI18N
                        log.info("set protocol for Olimex MOD-RFID1356MIFARE"); // NOI18N
                        protocol = new OlimexRfid1356mifareProtocol();
                    } else {
                        log.info("set protocol for Olimex MOD-RFID125"); // NOI18N
                        protocol = new OlimexRfidProtocol();
                    }
                    break;
                case "Parallax": // NOI18N
                    log.info("set protocol to Parallax"); // NOI18N
                    protocol = new ParallaxRfidProtocol();
                    break;
                case "SeeedStudio": // NOI18N
                    log.info("set protocol to SeeedStudio"); // NOI18N
                    protocol = new SeeedStudioRfidProtocol();
                    break;
                case "EM-18": // NOI18N
                    log.info("set protocol to EM-18"); // NOI18N
                    protocol = new Em18RfidProtocol();
                    break;
                default:
                    // no protocol at all - warn
                    log.warn("protocol option {} defaults to CORE-ID", opt3);
                    // create a coreid protocol
                    protocol = new CoreIdRfidProtocol();
                    break;
            }
        }
        this.getSystemConnectionMemo().setProtocol(protocol);

        // connect to the traffic controller
        this.getSystemConnectionMemo().setRfidTrafficController(control);
        control.setAdapterMemo(this.getSystemConnectionMemo());
        control.connectPort(this);
        control.sendInitString();
    }

    // base class methods for the RfidPortController interface
    
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

    protected String[] validSpeeds = new String[]{Bundle.getMessage("BaudAutomatic")};
    protected int[] validSpeedValues = new int[]{9600};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    // private control members

    private static final Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class);

}
