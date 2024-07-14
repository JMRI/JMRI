package jmri.jmrix.loconet.alm;

import jmri.NmraPacket;
import jmri.jmrix.loconet.ds64.SimpleTurnoutStateEntry;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 *
 * @author B. Milhaupt (C) 2024
 */
public class LnSimpleRouteEntry {
    // data members
    private int turnoutNumber;   // LocoNet Turnout turnoutNumber
    private RouteSwitchPositionEnum position;
    public static final int UNUSEDTURNOUTNUMBER = -1;
    public SimpleTurnoutStateEntry guiEntry;

    /**
     * C'tor.
     */
    public LnSimpleRouteEntry() {
        this(-1, true, true);
    }

    /**
     * C'tor.
     * @param addr address
     * @param closed true if closed, else thrown
     * @param unused determines whether the entry is unused or used
     */
    public LnSimpleRouteEntry(int addr, boolean closed, boolean unused) {
        turnoutNumber = addr;
        if (unused) {
            position = RouteSwitchPositionEnum.UNUSED;
        } else {
            position = (closed)
                    ? RouteSwitchPositionEnum.CLOSED
                    : RouteSwitchPositionEnum.THROWN;
        }
        guiEntry = new SimpleTurnoutStateEntry(addr, closed, unused);
    }

    /**
     * Getter.
     *
     * @return turnout number
     */
    public int getNumber() {
        return turnoutNumber;
    }

    /**
     * Setter.
     * @param turnoutNumber Turnout number
     * @return the turnout number which was set
     */
    public int setNumber(int turnoutNumber) {
        if (((turnoutNumber < NmraPacket.accIdLowLimit) || (turnoutNumber > NmraPacket.accIdAltHighLimit)) &&
                ((turnoutNumber != 0) && (turnoutNumber != -1))) {
            throw new IllegalArgumentException("Turnout value: " + turnoutNumber// NOI18N
                    + " not in the range " + NmraPacket.accIdLowLimit + " to " // NOI18N
                    + NmraPacket.accIdAltHighLimit + " and not 'Unused'.");
        }
        this.turnoutNumber = turnoutNumber;
        return this.turnoutNumber;
    }

    /**
     * Getter.
     * @return RouteSwitchPositionEnum
     */
    public RouteSwitchPositionEnum getPosition() {
        return position;
    }

    /**
     * Setter.
     * @param position Turnout position
     */
    public void setPosition(RouteSwitchPositionEnum position) {
        this.position = position;
    }

//    private final static Logger log = LoggerFactory.getLogger(LnSimpleRouteEntry.class);
}
