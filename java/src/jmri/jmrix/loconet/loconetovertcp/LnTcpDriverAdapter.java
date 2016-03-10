// LnTcpDriverAdapter.java
package jmri.jmrix.loconet.loconetovertcp;

import jmri.jmrix.loconet.LnNetworkPortController;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements SerialPortAdapter for the LocoNetOverTcp system network
 * connection.
 * <P>
 * This connects a Loconet via a telnet connection. Normally controlled by the
 * LnTcpDriverFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2003
 * @author Alex Shepherd Copyright (C) 2003, 2006
 * @version $Revision$
 */
public class LnTcpDriverAdapter extends LnNetworkPortController {

    public LnTcpDriverAdapter() {
        super(new LocoNetSystemConnectionMemo());
        option2Name = "CommandStation";
        option3Name = "TurnoutHandle";
        options.put(option2Name, new Option("Command station type:", commandStationNames, false));
        options.put(option3Name, new Option("Turnout command handling:", new String[]{"Normal", "Spread", "One Only", "Both"}));
    }

    /**
     * set up all of the other objects to operate with a LocoNet connected via
     * this class.
     */
    public void configure() {

        setCommandStationType(getOptionState(option2Name));
        setTurnoutHandling(getOptionState(option3Name));
        // connect to a packetizing traffic controller
        LnOverTcpPacketizer packets = new LnOverTcpPacketizer();
        packets.connectPort(this);

        // create memo
        this.getSystemConnectionMemo().setLnTrafficController(packets);
        // do the common manager config
        this.getSystemConnectionMemo().configureCommandStation(commandStationType,
                mTurnoutNoRetry, mTurnoutExtraSpace);
        this.getSystemConnectionMemo().configureManagers();

        // start operation
        packets.startThreads();
        jmri.jmrix.loconet.ActiveFlag.setActive();

    }

    public boolean status() {
        return opened;
    }

    // private control members
    private boolean opened = false;

    public void configureOption1(String value) {
        super.configureOption1(value);
        log.debug("configureOption1: " + value);
        setCommandStationType(value);
    }

    private final static Logger log = LoggerFactory.getLogger(LnTcpDriverAdapter.class.getName());

}
