// LocoBufferAdapter.java

package jmri.jmrix.loconet.Intellibox;

import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;

/**
 * Update the code in jmri.jmrix.loconet.locobuffer so that it
 * operates correctly with the Intellibox on-board serial port

 * @author			Alex Shepherd   Copyright (C) 2004
 * @version			$Revision: 1.1 $
 */
public class IntelliboxAdapter extends LocoBufferAdapter {


    public IntelliboxAdapter() {
        super();

        validSpeeds = new String[]{"2400", "4800", "9600", "19200", "57600"};
        validSpeedValues = new int[]{2400, 4800,9600, 19200, 57600};

        m2Instance = this;
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
     * Get an array of valid baud rates. This is currently just a message
     * saying its fixed
     */
    public String[] validBaudRates() {
        return validSpeeds;
    }

    static public LocoBufferAdapter instance() {
        if (m2Instance == null) m2Instance = new IntelliboxAdapter();
        return m2Instance;
    }
    static IntelliboxAdapter m2Instance = null;

}
