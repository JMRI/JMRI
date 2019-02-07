package jmri.jmrix.loconet.loconetovertcp;

import jmri.jmrix.loconet.LnNetworkPortController;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements SerialPortAdapter for the LocoNetOverTcp system network
 * connection.
 * <p>
 * This connects a LocoNet via a telnet connection. Normally controlled by the
 * LnTcpDriverFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2003
 * @author Alex Shepherd Copyright (C) 2003, 2006
 */
public class LnTcpDriverAdapter extends LnNetworkPortController {

    public LnTcpDriverAdapter(LocoNetSystemConnectionMemo m) {
        super(m);
        option2Name = "CommandStation";
        option3Name = "TurnoutHandle";
        options.put(option2Name, new Option(Bundle.getMessage("CommandStationTypeLabel"), commandStationNames, false));
        options.put(option3Name, new Option(Bundle.getMessage("TurnoutHandling"),
                new String[]{Bundle.getMessage("HandleNormal"), Bundle.getMessage("HandleSpread"), Bundle.getMessage("HandleOneOnly"), Bundle.getMessage("HandleBoth")})); // I18N
        options.put("TranspondingPresent", new Option(Bundle.getMessage("TranspondingPresent"),
                new String[]{Bundle.getMessage("ButtonNo"), Bundle.getMessage("ButtonYes")} )); // NOI18N
    }

    public LnTcpDriverAdapter() {
        this(new LocoNetSystemConnectionMemo());
    }

    /**
     * Set up all of the other objects to operate with a LocoNet connected via
     * this class.
     */
    @Override
    public void configure() {

        setCommandStationType(getOptionState(option2Name));
        setTurnoutHandling(getOptionState(option3Name));
        setTranspondingAvailable(getOptionState("TranspondingPresent"));

        // connect to a packetizing traffic controller
        LnOverTcpPacketizer packets = new LnOverTcpPacketizer(this.getSystemConnectionMemo());
        packets.connectPort(this);

        // create memo
        this.getSystemConnectionMemo().setLnTrafficController(packets);
        // do the common manager config
        this.getSystemConnectionMemo().configureCommandStation(commandStationType,
                mTurnoutNoRetry, mTurnoutExtraSpace, mTranspondingAvailable);
        this.getSystemConnectionMemo().configureManagers();

        // start operation
        packets.startThreads();
    }

    @Override
    public boolean status() {
        return opened;
    }

    // private control members
    private boolean opened = false;

    @Override
    public void configureOption1(String value) {
        super.configureOption1(value);
        log.debug("configureOption1: {}", value);
        setCommandStationType(value);
    }

    private final static Logger log = LoggerFactory.getLogger(LnTcpDriverAdapter.class);

}
