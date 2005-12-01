// LocoBufferAdapter.java

package jmri.jmrix.loconet.Intellibox;

import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;

/**
 * Update the code in jmri.jmrix.loconet.locobuffer so that it
 * operates correctly with the Intellibox on-board serial port.
 * <P>
 * Since this is by definition connected to an Intellibox, 
 * the command station prompt is suppressed.
 *
 * @author			Alex Shepherd   Copyright (C) 2004
 * @author          Bob Jacobsen    Copyright (C) 2005
 * @version			$Revision: 1.4 $
 */
public class IntelliboxAdapter extends LocoBufferAdapter {


    public IntelliboxAdapter() {
        super();

        validSpeeds = new String[]{"19200", "38400"};
        validSpeedValues = new int[]{19200, 38400};
    }

    /**
 * Set up all of the other objects to operate with a LocoBuffer
 * connected to this port.
 */
public void configure() {
    // connect to a packetizing traffic controller
    IBLnPacketizer packets = new IBLnPacketizer();
    packets.connectPort(this);

    // do the common manager config
    configureCommandStation(mCanRead, mProgPowersOff);
    configureManagers();

    // start operation
    packets.startThreads();
    jmri.jmrix.loconet.ActiveFlag.setActive();
}

    /**
     * Get an array of valid baud rates. 
     */
    public String[] validBaudRates() {
        return validSpeeds;
    }

    /**
     * Get an array of valid baud rates as integers. 
     */
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


    static public LocoBufferAdapter instance() {
        if (m3Instance == null) m3Instance = new IntelliboxAdapter();
        return m3Instance;
    }
    static private IntelliboxAdapter m3Instance = null;

}
