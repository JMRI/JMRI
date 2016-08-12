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
import jmri.jmrix.rfid.protocol.olimex.OlimexRfidProtocol;
import jmri.jmrix.rfid.protocol.parallax.ParallaxRfidProtocol;
import jmri.jmrix.rfid.protocol.seeedstudio.SeeedStudioRfidProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements PortAdapter for a network connection.
 * <P>
 * This connects via a telnet connection. Normally
 * controlled by the NetworkDriverFrame class.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002, 2003, 2015
 */
public class NetworkDriverAdapter extends RfidNetworkPortController {

    public NetworkDriverAdapter() {
        super(new RfidSystemConnectionMemo());
        option1Name = "Adapter";
        option2Name = "Concentrator-Range";
        option3Name = "Protocol";
        options.put(option1Name, new Option("Adapter:", new String[]{"Generic Stand-alone", "MERG Concentrator"}, false));
        options.put(option2Name, new Option("Concentrator range:", new String[]{"A-H", "I-P"}, false));
        options.put(option3Name, new Option("Protocol:", new String[]{"CORE-ID", "Olimex", "Parallax", "SeeedStudio"}, false));
        setManufacturer(jmri.jmrix.rfid.RfidConnectionTypeList.RFID);
    }

    /**
     * set up all of the other objects to operate connected to this port
     */
    @Override
    public void configure() {
        RfidTrafficController control;
        RfidProtocol protocol;

        // set up the system connection first
        String opt1 = getOptionState(option1Name);
        switch (opt1) {
            case "Generic Stand-alone":
                // create a Generic Stand-alone port controller
                log.debug("Create Generic Standalone SpecificTrafficController");
                control = new StandaloneTrafficController(this.getSystemConnectionMemo());
                this.getSystemConnectionMemo().configureManagers(
                        new StandaloneSensorManager(control, this.getSystemPrefix()),
                        new StandaloneReporterManager(control, this.getSystemPrefix()));
                break;
            case "MERG Concentrator":
                // create a MERG Concentrator port controller
                log.debug("Create MERG Concentrator SpecificTrafficController");
                control = new ConcentratorTrafficController(this.getSystemConnectionMemo(), getOptionState(option2Name));
                this.getSystemConnectionMemo().configureManagers(
                        new ConcentratorSensorManager(control, this.getSystemPrefix()),
                        new ConcentratorReporterManager(control, this.getSystemPrefix()));
                break;
            default:
                // no connection at all - warn
                log.warn("adapter option " + opt1 + " defaults to Generic Stand-alone");
                // create a Generic Stand-alone port controller
                control = new StandaloneTrafficController(this.getSystemConnectionMemo());
                this.getSystemConnectionMemo().configureManagers(
                        new StandaloneSensorManager(control, this.getSystemPrefix()),
                        new StandaloneReporterManager(control, this.getSystemPrefix()));
                break;
        }

        // Now do the protocol
        String opt3 = getOptionState(option3Name);
        if (opt1.equals("MERG Concentrator")) {
            // MERG Concentrator only supports CORE-ID
            log.info("set protocol to CORE-ID");
            String opt2 = getOptionState(option2Name);
            switch (opt2) {
                case "A-H" :
                    log.info("set concentrator range to 'A-H' at position 1");
                    protocol = new CoreIdRfidProtocol('A', 'H', 1);
                    break;
                case "I-P" :
                    log.info("set concentrator range to 'I-P' at position 1");
                    protocol = new CoreIdRfidProtocol('I', 'P', 1);
                    break;
                default :
                    // unrecognised concentrator range - warn
                    log.warn("concentrator range '{}' not supported - default to no concentrator", opt2);
                    protocol = new CoreIdRfidProtocol();
                    break;
            }
        } else {
            switch (opt3) {
                case "CORE-ID":
                    log.info("set protocol to CORE-ID");
                    protocol = new CoreIdRfidProtocol();
                    break;
                case "Olimex":
                    log.info("set protocol to Olimex");
                    protocol = new OlimexRfidProtocol();
                    break;
                case "Parallax":
                    log.info("set protocol to Parallax");
                    protocol = new ParallaxRfidProtocol();
                    break;
                case "SeeedStudio":
                    log.info("set protocol to SeeedStudio");
                    protocol = new SeeedStudioRfidProtocol();
                    break;
                default:
                    // no protocol at all - warn
                    log.warn("protocol option " + opt3 + " defaults to CORE-ID");
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


    private final static Logger log = LoggerFactory.getLogger(NetworkDriverAdapter.class.getName());

}
