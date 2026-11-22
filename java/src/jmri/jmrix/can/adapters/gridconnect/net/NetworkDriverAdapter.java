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

    /**
     * Option name: how frames are handed to the network. {@link #SEND_AT_ONCE}
     * sends each frame as soon as it is written; {@link #SEND_GROUPED} lets the
     * operating system hold a frame back while the gateway has not yet
     * acknowledged the one before, and send the frames written meanwhile
     * together, as JMRI did up to 5.17.4. For gateways that miss frames which
     * arrive separately in quick succession, as some CANPi software does.
     */
    public static final String FRAME_SENDING_OPTION = "FrameSending"; // NOI18N

    /** {@link #FRAME_SENDING_OPTION} value: each frame sent at once (TCP_NODELAY on). The default. */
    public static final String SEND_AT_ONCE = "At once"; // NOI18N

    /** {@link #FRAME_SENDING_OPTION} value: frames grouped, as JMRI 5.17.4 sent them (TCP_NODELAY off). */
    public static final String SEND_GROUPED = "Grouped (as JMRI 5.17.4)"; // NOI18N

    public NetworkDriverAdapter() {
        super(new CanSystemConnectionMemo());
        option1Name = "Gateway"; // NOI18N
        options.put(option1Name, new Option(Bundle.getMessage("ConnectionGateway"), new String[]{"Pass All", "Filtering"}));
        option2Name = "Protocol"; // NOI18N
        options.put(option2Name, new Option(Bundle.getMessage("ConnectionProtocol"), jmri.jmrix.can.ConfigurationManager.getSystemOptions(), false));
        options.put(FRAME_GAP_OPTION, new Option(Bundle.getMessage("ConnectionFrameGap"), new String[]{"0", "5", "10", "20", "50"}));
        options.put(FRAME_SENDING_OPTION, new Option(Bundle.getMessage("ConnectionFrameSending"), new String[]{SEND_AT_ONCE, SEND_GROUPED}));
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
        if (sendGrouped()) {
            log.info("Frames sent grouped, as JMRI 5.17.4 sent them (TCP_NODELAY off)");
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
     * frames back, by up to the gateway's delayed acknowledgement time, and
     * sends the frames written meanwhile together. It is turned off unless
     * {@link #FRAME_SENDING_OPTION} is {@link #SEND_GROUPED}, on the first
     * connection and on every reconnection.
     */
    @Override
    public void connect() throws java.io.IOException {
        super.connect();
        if (opened && socketConn != null) {
            socketConn.setTcpNoDelay(!sendGrouped());
        }
    }

    /**
     * @return true if the frame sending option asks for frames grouped, as
     *         JMRI 5.17.4 sent them
     */
    boolean sendGrouped() {
        return SEND_GROUPED.equals(getOptionState(FRAME_SENDING_OPTION));
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
