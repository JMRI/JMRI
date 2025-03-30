package jmri.jmrix.can.adapters.gridconnect.canrs.serialdriver;

import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.adapters.gridconnect.GcSerialDriverAdapter;
import jmri.jmrix.can.adapters.gridconnect.canrs.MergTrafficController;

/**
 * Implements SerialPortAdapter for the MERG CAN-RS or CAN-USB.
 * <p>
 * This connects to the MERG adapter via a serial com port (real or virtual).
 * Normally controlled by the SerialDriverFrame class.
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Bob Jacobsen Copyright (C) 2009
 */
public class SerialDriverAdapter extends GcSerialDriverAdapter {

    public SerialDriverAdapter() {
        super();
        option2Name = "CANID";
        options.put(option2Name, new Option(Bundle.getMessage("JMRICANID"),
            jmri.jmrix.can.cbus.CbusConstants.getValidFixedCanIds(),
            jmri.jmrix.can.cbus.CbusConstants.DEFAULT_JMRI_CAN_ID_STRING ));
    }

    /**
     * Set up all of the other objects to operate with a CAN RS adapter
     * connected to this port.
     */
    @Override
    public void configure() {

        // Register the CAN traffic controller being used for this connection
        TrafficController tc = new MergTrafficController();
        tc.setCanId(getOptionState(option2Name));

        this.getSystemConnectionMemo().setTrafficController(tc);

        // Now connect to the traffic controller
        log.debug("Connecting port");
        tc.connectPort(this);

        this.getSystemConnectionMemo().setProtocol(getOptionState(option1Name));

        // do central protocol-specific configuration
        //jmri.jmrix.can.ConfigurationManager.configure(getOptionState(option1Name));
        this.getSystemConnectionMemo().configureManagers();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialDriverAdapter.class);

}
