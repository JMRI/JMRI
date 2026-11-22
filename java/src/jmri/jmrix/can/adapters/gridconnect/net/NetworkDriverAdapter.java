package jmri.jmrix.can.adapters.gridconnect.net;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.ConfigurationManager;
import jmri.jmrix.can.adapters.gridconnect.GcTrafficController;
import jmri.jmrix.can.adapters.gridconnect.canrs.MergTrafficController;

/**
 * Implements SerialPortAdapter for the OpenLCB system network connection.
 * <p>
 * This connects via a telnet connection. Normally controlled by the
 * NetworkDriverFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class NetworkDriverAdapter extends jmri.jmrix.AbstractNetworkPortController {

    /**
     * Option name: the least time between frames sent, in milliseconds. For
     * gateways that hold back a frame that arrives while they are still
     * handling the one before, as some CANPi software does.
     */
    public static final String FRAME_GAP_OPTION = "FrameGap"; // NOI18N

    public NetworkDriverAdapter() {
        super(new CanSystemConnectionMemo());
        option1Name = "Gateway"; // NOI18N
        options.put(option1Name, new Option(Bundle.getMessage("ConnectionGateway"), new String[]{"Pass All", "Filtering"}));
        option2Name = "Protocol"; // NOI18N
        options.put(option2Name, new Option(Bundle.getMessage("ConnectionProtocol"), jmri.jmrix.can.ConfigurationManager.getSystemOptions(), false));
        options.put(FRAME_GAP_OPTION, new Option(Bundle.getMessage("ConnectionFrameGap"), new String[]{"0", "5", "10", "20", "50"}));
        super.setManufacturer(jmri.jmrix.openlcb.OlcbConnectionTypeList.OPENLCB); // overriden by MERG Connections.
        allowConnectionRecovery = true;
    }

    /**
     * Set up all of the other objects to operate with the CAN bus connected via
     * this TCP/IP link.
     */
    @Override
    public void configure() {
        GcTrafficController tc;
        switch (getOptionState(option2Name)) {
            case ConfigurationManager.MERGCBUS:
            case ConfigurationManager.SPROGCBUS:
                // Register the CAN traffic controller being used for this connection
                tc = new MergTrafficController();
                tc.setCanId(getOptionState("CANID"));
                break;
            default:
                tc = new GcTrafficController();
                break;
        }
        this.getSystemConnectionMemo().setTrafficController(tc);

        int gap = frameGapMillis();
        tc.setFrameGapMillis(gap);
        if (gap > 0) {
            log.info("Frames sent at least {} ms apart", gap);
        }

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

    /**
     * {@inheritDoc}
     * <p>
     * Each GridConnect frame is written to the socket in a single write, so
     * TCP's delay for small writes while an earlier one is unacknowledged
     * (Nagle's algorithm) cannot combine the parts of a frame; it only holds
     * frames back, by up to the gateway's delayed acknowledgement time. It is
     * turned off, on the first connection and on every reconnection.
     */
    @Override
    public void connect() throws java.io.IOException {
        super.connect();
        if (opened && socketConn != null) {
            socketConn.setTcpNoDelay(true);
        }
    }

    /**
     * @return the frame gap option as milliseconds, 0 if not set or not a number
     */
    int frameGapMillis() {
        try {
            return Math.max(0, Integer.parseInt(getOptionState(FRAME_GAP_OPTION)));
        } catch (NumberFormatException e) {
            return 0;
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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NetworkDriverAdapter.class);

}
