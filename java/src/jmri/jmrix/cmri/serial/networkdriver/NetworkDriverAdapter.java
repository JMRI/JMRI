package jmri.jmrix.cmri.serial.networkdriver;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.SerialNetworkPortAdapter;
import jmri.jmrix.cmri.serial.SerialTrafficController;

/**
 * Implements SerialNetworkPortAdapter for a network connection.
 * <p>
 * This connects via a telnet connection. Normally
 * controlled by the NetworkDriverFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2003, 2015
 */
public class NetworkDriverAdapter extends SerialNetworkPortAdapter {

    public NetworkDriverAdapter() {
        super(new CMRISystemConnectionMemo());
        setManufacturer(jmri.jmrix.cmri.CMRIConnectionTypeList.CMRI);
        // Auto-reconnect: AbstractPortController already implements a full
        // reconnect framework (exponential backoff, reconnectMaxAttempts/
        // reconnectMaxInterval loaded from the saved connection XML) that
        // AbstractMRTrafficController.receiveLoop() already calls into
        // automatically whenever the read loop exits abnormally (e.g. this
        // node's ESP32 rebooting and dropping the TCP connection) -- but that
        // framework is a no-op unless allowConnectionRecovery is explicitly
        // turned on, which the CMRI network driver never did. LocoNet-over-TCP
        // and DCC++ network already enable this the same way; CMRI just never
        // had this line. See resetupConnection() below for the other half of
        // the fix -- restarting the traffic controller's threads once
        // reconnected.
        allowConnectionRecovery = true;
    }

    /**
     * Set up all of the other objects to operate connected to this port.
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        SerialTrafficController tc = new SerialTrafficController();
        getSystemConnectionMemo().setTrafficController(tc);

        tc.connectPort(this);

        getSystemConnectionMemo().configureManagers();
    }

    /**
     * Called once a reconnect attempt succeeds (this.connect() returned
     * without throwing). The traffic controller's transmit/receive threads
     * were already torn down when the connection was lost (see
     * AbstractMRTrafficController.recovery()/disconnectPort()), so they need
     * to be started again against the fresh socket streams -- connectPort()
     * does both (re-reads getInputStream()/getOutputStream() from this
     * adapter and spawns new transmit/receive threads), the same call
     * configure() made for the original connection.
     */
    @Override
    protected void resetupConnection() {
        getSystemConnectionMemo().getTrafficController().connectPort(this);
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NetworkDriverAdapter.class);

}
