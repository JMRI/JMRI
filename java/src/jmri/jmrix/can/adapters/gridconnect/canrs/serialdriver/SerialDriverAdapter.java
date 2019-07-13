package jmri.jmrix.can.adapters.gridconnect.canrs.serialdriver;

import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.adapters.gridconnect.GcSerialDriverAdapter;
import jmri.jmrix.can.adapters.gridconnect.canrs.MergTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        options.put(option2Name, new Option("CAN ID for CAN-USB", new String[]{"127", "126", "125", "124", "123", "122", "121", "120"}));
    }

    /**
     * Set up all of the other objects to operate with a CAN RS adapter
     * connected to this port.
     */
    @Override
    public void configure() {

        // Register the CAN traffic controller being used for this connection
        TrafficController tc = new MergTrafficController();
        try {
            tc.setCanId(Integer.parseInt(getOptionState(option2Name)));
        } catch (Exception e) {
            log.error("Cannot parse CAN ID - check your preference settings " + e);
            log.error("Now using default CAN ID");
        }

        this.getSystemConnectionMemo().setTrafficController(tc);

        // Now connect to the traffic controller
        log.debug("Connecting port");
        tc.connectPort(this);

        this.getSystemConnectionMemo().setProtocol(getOptionState(option1Name));

        // do central protocol-specific configuration    
        //jmri.jmrix.can.ConfigurationManager.configure(getOptionState(option1Name));
        this.getSystemConnectionMemo().configureManagers();
    }

    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class);

}
