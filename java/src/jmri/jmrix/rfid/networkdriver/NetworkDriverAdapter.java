package jmri.jmrix.rfid.networkdriver;

import jmri.jmrix.rfid.RfidNetworkPortController;
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
import jmri.jmrix.rfid.protocol.olimex.OlimexRfid1356mifareProtocol;
import jmri.jmrix.rfid.protocol.olimex.OlimexRfidProtocol;
import jmri.jmrix.rfid.protocol.parallax.ParallaxRfidProtocol;
import jmri.jmrix.rfid.protocol.seeedstudio.SeeedStudioRfidProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements PortAdapter for a network connection.
 * <p>
 * This connects via a telnet connection. Normally
 * controlled by the NetworkDriverFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2003, 2015
 * @author B. Milhaupt  Copyright (C) 2017
 */
public class NetworkDriverAdapter extends RfidNetworkPortController {

    public NetworkDriverAdapter() {
        super(new RfidSystemConnectionMemo());
        option1Name = "Adapter"; // NOI18N
        option2Name = "Concentrator-Range"; // NOI18N
        option3Name = "Protocol"; // NOI18N
        option4Name = "Device"; // NOI18N
        options.put(option1Name, new Option(Bundle.getMessage("ConnectionAdapter"), new String[]{"Generic Stand-alone", "MERG Concentrator"}, false)); // NOI18N
        options.put(option2Name, new Option(Bundle.getMessage("ConnectionConcentratorRange"), new String[]{"A-H", "I-P"}, false)); // NOI18N
        options.put(option3Name, new Option(Bundle.getMessage("ConnectionProtocol"), new String[]{"CORE-ID", "Olimex", "Parallax", "SeeedStudio"}, false)); // NOI18N
        options.put(option4Name, new Option(Bundle.getMessage("ConnectionDeviceType"), new String[] {"MOD-RFID125", "MOD-RFID1356MIFARE"}, false)); // NOI18N
        setManufacturer(jmri.jmrix.rfid.RfidConnectionTypeList.RFID);
    }

    /**
     * Set up all of the other objects to operate connected to this port.
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
                log.warn("adapter option " + opt1 + " defaults to Generic Stand-alone"); // NOI18N
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
                case "A-H" :
                    log.info("set concentrator range to 'A-H' at position 1"); // NOI18N
                    protocol = new CoreIdRfidProtocol('A', 'H', 1);
                    break;
                case "I-P" :
                    log.info("set concentrator range to 'I-P' at position 1"); // NOI18N
                    protocol = new CoreIdRfidProtocol('I', 'P', 1);
                    break;
                default :
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
                default:
                    // no protocol at all - warn
                    log.warn("protocol option " + opt3 + " defaults to CORE-ID"); // NOI18N
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


    private final static Logger log = LoggerFactory.getLogger(NetworkDriverAdapter.class);

}
