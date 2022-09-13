package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver;

import jmri.jmrix.can.ConfigurationManager;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.adapters.gridconnect.GcSerialDriverAdapter;
import jmri.jmrix.can.adapters.gridconnect.canrs.MergTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements SerialPortAdapter for SPROG Generation 5.
 * <p>
 * This connects a SPROG Generation 5 via a serial com port (real or virtual).
 * Normally controlled by the SerialDriverFrame class.
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Bob Jacobsen Copyright (C) 2009
 * @author Andrew Crosland 2019
 */
public class PiSprog3SerialDriverAdapter extends GcSerialDriverAdapter {

    public PiSprog3SerialDriverAdapter() {
        super("S");
    }

    /**
     * Set up all of the other objects to operate with a SPROG Gen 5
     * connected to this port.
     */
    @Override
    public void configure() {

        // Register the CAN traffic controller being used for this connection
        // This connection has no actual CAN interface so set a fixed CAN ID
        TrafficController tc = new MergTrafficController();
        try {
            tc.setCanId(127);
        } catch (Exception e) {
            log.error("Cannot parse CAN ID - check your preference settings", e);
            log.error("Now using default CAN ID");
        }

        this.getSystemConnectionMemo().setTrafficController(tc);

        // Now connect to the traffic controller
        log.debug("Connecting port");
        tc.connectPort(this);

        this.getSystemConnectionMemo().setProtocol(getOptionState(option1Name));
        this.getSystemConnectionMemo().setSubProtocol(ConfigurationManager.SubProtocol.NONE);
        this.getSystemConnectionMemo().setProgModeSwitch(ConfigurationManager.ProgModeSwitch.EITHER);

        // do central protocol-specific configuration
        //jmri.jmrix.can.ConfigurationManager.configure(getOptionState(option1Name));
        this.getSystemConnectionMemo().configureManagers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{Bundle.getMessage("Baud115200")};
    }

    /**
     * And the corresponding values.
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{115200};
    }

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    private final static Logger log = LoggerFactory.getLogger(PiSprog3SerialDriverAdapter.class);

}
