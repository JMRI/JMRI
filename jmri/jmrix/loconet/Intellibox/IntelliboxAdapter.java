// LocoBufferAdapter.java

package jmri.jmrix.loconet.Intellibox;

import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;

/**
 * Update the code in jmri.jmrix.loconet.locobuffer so that it
 * operates correctly with the Intellibox on-board serial port

 * @author			Alex Shepherd   Copyright (C) 2004
 * @version			$Revision: 1.3 $
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

    static public LocoBufferAdapter instance() {
        if (m3Instance == null) m3Instance = new IntelliboxAdapter();
        return m3Instance;
    }
    static private IntelliboxAdapter m3Instance = null;

}
