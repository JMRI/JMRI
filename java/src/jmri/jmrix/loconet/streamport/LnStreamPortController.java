package jmri.jmrix.loconet.streamport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.LnCommandStationType;
import jmri.jmrix.loconet.LnConnectionTypeList;

/**
 * Base for classes representing a LocoNet communications port connected via
 * streams.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @author Paul Bender Copyright (C) 2018
 */
public class LnStreamPortController extends jmri.jmrix.AbstractStreamPortController {

    public LnStreamPortController(LocoNetSystemConnectionMemo connectionMemo, DataInputStream in, DataOutputStream out, String pname) {
        super(connectionMemo, in, out, pname);
        setManufacturer(LnConnectionTypeList.DIGITRAX);
    }

    public LnStreamPortController(DataInputStream in, DataOutputStream out, String pname) {
        super(new LocoNetSystemConnectionMemo(), in, out, pname);
        setManufacturer(LnConnectionTypeList.DIGITRAX);
    }

    public LnStreamPortController() {
        super(new LocoNetSystemConnectionMemo());
        setManufacturer(LnConnectionTypeList.DIGITRAX);
    }

    /**
     * Check that this object is ready to operate. This is a question of
     * configuration, not transient hardware status.
     */
    @Override
    public boolean status(){
       return true;
    }

    /**
     * Set up all of the other objects to operate with a LocoNet connected via
     * this class.
     */
    @Override
    public void configure() {

        // hardcode options for now
        setCommandStationType(LnCommandStationType.COMMAND_STATION_STANDALONE);
        setTurnoutHandling("");
        // connect to a packetizing traffic controller
        LnStreamPortPacketizer packets = new LnStreamPortPacketizer(this.getSystemConnectionMemo());
        packets.connectPort(this);

        // setup memo
        this.getSystemConnectionMemo().setLnTrafficController(packets);
        // do the common manager config
        this.getSystemConnectionMemo().configureCommandStation(commandStationType,
                mTurnoutNoRetry, mTurnoutExtraSpace, false); // never transponding
        this.getSystemConnectionMemo().configureManagers();

        // start operation
        packets.startThreads();
    }

    /**
     * Can the port accept additional characters? This might go false for short
     * intervals, but it might also stick off if something goes wrong.
     * <p>
     * Provide a default implementation for the MS100, etc, in which this is
     * _always_ true, as we rely on the queueing in the port itself.
     */
    public boolean okToSend() {
        return true;
    }

    protected LnCommandStationType commandStationType = null;

    protected boolean mTurnoutNoRetry = false;
    protected boolean mTurnoutExtraSpace = false;

    /**
     * Set config info from the Command Station type enum.
     */
    public void setCommandStationType(LnCommandStationType value) {
        if (value == null) {
            return;  // can happen while switching protocols
        }
        log.debug("setCommandStationType: {}", value); // NOI18N
        commandStationType = value;
    }

    public void setTurnoutHandling(String value) {
        if (value.equals("One Only") || value.equals(Bundle.getMessage("HandleOneOnly"))
                || value.equals("Both") || value.equals(Bundle.getMessage("HandleBoth"))) {
            mTurnoutNoRetry = true;
        }
        log.debug("turnout no retry: {}", mTurnoutNoRetry); // NOI18N
        if (value.equals("Spread") || value.equals(Bundle.getMessage("HandleSpread"))
                || value.equals("Both") || value.equals(Bundle.getMessage("HandleBoth"))) {
            mTurnoutExtraSpace = true;
        }
        log.debug("turnout extra space: {}", mTurnoutExtraSpace); // NOI18N
    }

    @Override
    public LocoNetSystemConnectionMemo getSystemConnectionMemo() {
        return (LocoNetSystemConnectionMemo) super.getSystemConnectionMemo();
    }

    private final static Logger log = LoggerFactory.getLogger(LnStreamPortController.class);

}
