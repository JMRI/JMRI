package jmri.jmrix.roco.z21;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import jmri.jmrix.loconet.LnCommandStationType;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * Override default LocoNet classes to use z21 specific versions.
 *
 * @author Paul Bender Copyright (C) 2018
 */
public class Z21LnStreamPortController extends jmri.jmrix.loconet.streamport.LnStreamPortController {

    public Z21LnStreamPortController(LocoNetSystemConnectionMemo connectionMemo, DataInputStream in, DataOutputStream out, String pname) {
        super(connectionMemo, in, out, pname);
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
        setInterrogateOnStart("No");  // same default as turnout handling
        // connect to a packetizing traffic controller
        Z21LnStreamPortPacketizer packets = new Z21LnStreamPortPacketizer(this.getSystemConnectionMemo());
        packets.connectPort(this);

        // setup memo
        this.getSystemConnectionMemo().setLnTrafficController(packets);
        // do the common manager config
        this.getSystemConnectionMemo().configureCommandStation(commandStationType,
                mTurnoutNoRetry, mTurnoutExtraSpace, false, mInterrogateAtStart, false); // never transponding or Xp slots
        this.getSystemConnectionMemo().configureManagers();

        // start operation
        packets.startThreads();
    }

    @Override
    public void dispose(){
        this.getSystemConnectionMemo().dispose();
        super.dispose();
    }

}
