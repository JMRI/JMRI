// LocoBufferAdapter.java

package jmri.jmrix.loconet.Intellibox;

import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;
import jmri.jmrix.loconet.*;

/**
 * Update the code in jmri.jmrix.loconet.locobuffer so that it
 * operates correctly with the Intellibox on-board serial port.
 * <P>
 * Since this is by definition connected to an Intellibox, 
 * the command station prompt is suppressed.
 *
 * @author			Alex Shepherd   Copyright (C) 2004
 * @author          Bob Jacobsen    Copyright (C) 2005, 2010
 * @version			$Revision$
 */
public class IntelliboxAdapter extends LocoBufferAdapter {


    public IntelliboxAdapter() {
        super();

        validSpeeds = new String[]{"19200", "38400", "115200"};
        validSpeedValues = new int[]{19200, 38400, 115200};
    }

    /**
 * Set up all of the other objects to operate with a LocoBuffer
 * connected to this port.
 */
public void configure() {
    // connect to a packetizing traffic controller
    IBLnPacketizer packets = new IBLnPacketizer();
    packets.connectPort(this);

    // create memo
    /*LocoNetSystemConnectionMemo memo 
        = new LocoNetSystemConnectionMemo(packets, new SlotManager(packets));*/
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

    /**
     * Get an array of valid baud rates. 
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="EI_EXPOSE_REP") // OK to expose array instead of copy until Java 1.6
    public String[] validBaudRates() {
        return validSpeeds;
    }

    /**
     * Get an array of valid baud rates as integers. 
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="EI_EXPOSE_REP") // OK to expose array instead of copy until Java 1.6
    public int[] validBaudNumber() {
        return validSpeedValues;
    }

    /**
     * Rephrase option 1, so that it doesn't talk about
     * LocoBuffer
     */
    public String option1Name() { return "Serial connection uses "; }

    /**
     * Option 2, usually used for command station type, is suppressed by
     * providing just one option.
     */
    public String[] validOption2() { 
        String[] retval = {"Intellibox"}; 
        return retval;
    }

    /*String manufacturerName = jmri.jmrix.DCCManufacturerList.UHLEN;
    
    public String getManufacturer() { return manufacturerName; }
    public void setManufacturer(String manu) { manufacturerName=manu; }*/
    
    //public SystemConnectionMemo getSystemConnectionMemo() { return adaptermemo; }
    
    /*public void dispose(){
        adaptermemo.dispose();
        adaptermemo = null;
    }*/
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IntelliboxAdapter.class.getName());

}
