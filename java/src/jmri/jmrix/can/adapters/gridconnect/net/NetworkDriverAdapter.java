package jmri.jmrix.can.adapters.gridconnect.net;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.ConfigurationManager;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.adapters.gridconnect.GcTrafficController;
import jmri.jmrix.can.adapters.gridconnect.canrs.MergTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements SerialPortAdapter for the OpenLCB system network connection.
 * <p>
 * This connects via a telnet connection. Normally controlled by the
 * NetworkDriverFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class NetworkDriverAdapter extends jmri.jmrix.AbstractNetworkPortController {

    public NetworkDriverAdapter() {
        super(new CanSystemConnectionMemo());
        option1Name = "Gateway"; // NOI18N
        options.put(option1Name, new Option(Bundle.getMessage("ConnectionGateway"), new String[]{"Pass All", "Filtering"}));
        option2Name = "Protocol"; // NOI18N
        options.put(option2Name, new Option(Bundle.getMessage("ConnectionProtocol"), jmri.jmrix.can.ConfigurationManager.getSystemOptions(), false));
        super.setManufacturer(jmri.jmrix.openlcb.OlcbConnectionTypeList.OPENLCB); // overriden by MERG Connections.
        allowConnectionRecovery = true;
    }

    /**
     * Set up all of the other objects to operate with the CAN bus connected via
     * this TCP/IP link.
     */
    @Override
    public void configure() {
        TrafficController tc;
        switch (getOptionState(option2Name)) {
            case ConfigurationManager.MERGCBUS:
            case ConfigurationManager.SPROGCBUS:
                // Register the CAN traffic controller being used for this connection
                tc = new MergTrafficController();
                 try {
                    tc.setCanId(Integer.parseInt(getOptionState("CANID")));
                } catch (NumberFormatException e) {
                    log.error("Cannot parse CAN ID \"{}\" - check your preference settings", getOptionState("CANID"), e);
                    log.error("Now using default CAN ID {}",tc.getCanid());
                }
                break;
            default:
                tc = new GcTrafficController();
                break;
        }
        this.getSystemConnectionMemo().setTrafficController(tc);

        // Now connect to the traffic controller
        log.debug("Connecting port");
        tc.connectPort(this);
        this.getSystemConnectionMemo().setProtocol(getOptionState(option2Name));

        // do central protocol-specific configuration    
        this.getSystemConnectionMemo().configureManagers();
        if (socketConn != null) {
            log.info("{} Connection via {} complete with {}",
                this.getSystemConnectionMemo().getUserName(),
                getOptionState(option2Name), socketConn.getInetAddress());
        }
    }

    @Override
    public boolean status() {
        return opened;
    }

    @Override
    public CanSystemConnectionMemo getSystemConnectionMemo() {
        return (CanSystemConnectionMemo) super.getSystemConnectionMemo();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void resetupConnection() {
        log.info("reconnected to Network after lost connection");
        if (opened) {
            this.getSystemConnectionMemo().getTrafficController().connectPort(this);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(NetworkDriverAdapter.class);

}
