// LnTcpDriverAdapter.java

package jmri.jmrix.loconet.loconetovertcp;

import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.loconet.*;

/**
 * Implements SerialPortAdapter for the LocoNetOverTcp system network connection.
 * <P>This connects
 * a Loconet via a telnet connection.
 * Normally controlled by the LnTcpDriverFrame class.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2002, 2003
 * @author      Alex Shepherd Copyright (C) 2003, 2006
 * @version     $Revision$
 */

public class LnTcpDriverAdapter extends LnNetworkPortController {

    public LnTcpDriverAdapter() {
        super();
        adaptermemo = new LocoNetSystemConnectionMemo();
    }
    /**
     * set up all of the other objects to operate with a LocoNet
     * connected via this class.
     */
    public void configure() {
        // connect to a packetizing traffic controller
        LnOverTcpPacketizer packets = new LnOverTcpPacketizer();
        packets.connectPort(this);

        // create memo
        adaptermemo.setSlotManager(new SlotManager(packets));
        adaptermemo.setLnTrafficController(packets);
        // do the common manager config
        adaptermemo.configureCommandStation(mCanRead, mProgPowersOff, commandStationName, 
                                            mTurnoutNoRetry, mTurnoutExtraSpace);
        adaptermemo.configureManagers();

        // start operation
        packets.startThreads();
        jmri.jmrix.loconet.ActiveFlag.setActive();

    }


    public boolean status() {return opened;}

    // private control members
    private boolean opened = false;
    
    /**
     * Get an array of valid values for "option 2"; used to display valid options.
     * May not be null, but may have zero entries
     */
    /*@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="EI_EXPOSE_REP") // OK to expose array instead of copy until Java 1.6
    public String[] validOption1() { return commandStationNames; }*/

    /**
     * Get a String that says what Option 2 represents
     * May be an empty string, but will not be null
     */
    //public String option1Name() { return "Command station type: "; }
    
    public void configureOption1(String value) {
        super.configureOption1(value);
    	log.debug("configureOption1: "+value);
        setCommandStationType(value);
    }
    
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="EI_EXPOSE_REP") // OK to expose array instead of copy until Java 1.6
    public String[] getCommandStationNames() { return commandStationNames; }
    public String   getCurrentCommandStation() { return commandStationName; }
    
    public SystemConnectionMemo getSystemConnectionMemo() { return adaptermemo; }
    
    public void dispose(){
        if (adaptermemo!=null)
            adaptermemo.dispose();
        adaptermemo = null;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LnTcpDriverAdapter.class.getName());

}
