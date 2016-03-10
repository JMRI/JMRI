// LocoBufferAdapter.java
package jmri.jmrix.loconet.Intellibox;

import jmri.jmrix.loconet.LnCommandStationType;
import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;

/**
 * Update the code in jmri.jmrix.loconet.locobuffer so that it operates
 * correctly with the Intellibox on-board serial port.
 * <P>
 * Since this is by definition connected to an Intellibox, the command station
 * prompt has limited choices
 *
 * @author	Alex Shepherd Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2005, 2010
 * @version	$Revision$
 */
public class IntelliboxAdapter extends LocoBufferAdapter {

    public IntelliboxAdapter() {
        super();

        // define command station options
        options.remove(option2Name);
        options.put(option2Name, new Option("Command station type:", commandStationOptions(), false));

        validSpeeds = new String[]{"19200", "38400", "115200"};
        validSpeedValues = new int[]{19200, 38400, 115200};
    }

    /**
     * Set up all of the other objects to operate with a LocoBuffer connected to
     * this port.
     */
    public void configure() {

        setCommandStationType(getOptionState(option2Name));
        setTurnoutHandling(getOptionState(option3Name));
        // connect to a packetizing traffic controller
        IBLnPacketizer packets = new IBLnPacketizer();
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

    /**
     * Get an array of valid baud rates.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP") // OK to expose array instead of copy until Java 1.6
    public String[] validBaudRates() {
        return validSpeeds;
    }

    /**
     * Get an array of valid baud rates as integers.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP") // OK to expose array instead of copy until Java 1.6
    public int[] validBaudNumber() {
        return validSpeedValues;
    }

    /**
     * Rephrase option 1, so that it doesn't talk about LocoBuffer
     */
    public String option1Name() {
        return "Serial connection uses ";
    }

    /**
     * Provide just one valid command station value
     */
    public String[] commandStationOptions() {
        String[] retval = {
            LnCommandStationType.COMMAND_STATION_IBX_TYPE_1.getName()
        };
        return retval;
    }

}
