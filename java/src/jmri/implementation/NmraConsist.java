package jmri.implementation;

import jmri.ConsistListener;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.CommandStation;

/**
 * This is the Default DCC consist manager installed on systems which support
 * the command station interface. It uses the NMRA consist creation packet
 * instead of Operations Mode programming to build a consist, but otherwise is
 * derived from the DccConsist code.
 *
 * @author Paul Bender Copyright (C) 2011
 */
public class NmraConsist extends DccConsist {
        
    private CommandStation commandStation = null;

// Initialize a consist for the specific address.
    public NmraConsist(int address) {
        super(address);
        log.debug("Nmra Consist created for address: {}", address);
    }

    public NmraConsist(DccLocoAddress address) {
        this(address,InstanceManager.getDefault(CommandStation.class));
    }
 
    public NmraConsist(DccLocoAddress address,CommandStation cs){
        super(address);
        commandStation = cs;
        log.debug("Nmra Consist created for address: {}", address);
    }

    /*
     *  Add a Locomotive to an Advanced Consist
     *  @param address is the Locomotive address to add to the locomotive
     *  @param directionNormal is True if the locomotive is traveling
     *        the same direction as the consist, or false otherwise.
     */
    @Override
    protected void addToAdvancedConsist(DccLocoAddress locoAddress, boolean directionNormal) {
        log.debug("Add Locomotive {} to advanced consist {} With Direction Normal {}.",
                locoAddress, consistAddress, directionNormal);
        // create the message and fill it,
        byte[] contents = jmri.NmraPacket.consistControl(locoAddress.getNumber(),
                locoAddress.isLongAddress(),
                consistAddress.getNumber(),
                directionNormal);
        commandStation.sendPacket(contents, 4);
        notifyConsistListeners(locoAddress, ConsistListener.OPERATION_SUCCESS);

    }

    /**
     *  Remove a Locomotive from an Advanced Consist
     *  @param locoAddress is the Locomotive address to add to the locomotive
     */
    @Override
    protected void removeFromAdvancedConsist(DccLocoAddress locoAddress) {
        log.debug("Remove Locomotive {} from advanced consist {}.",
                locoAddress, consistAddress);
        // create the message and fill it,
        byte[] contents = jmri.NmraPacket.consistControl(locoAddress.getNumber(),
                locoAddress.isLongAddress(),
                0, //set to 0 to remove
                true);//always normal direction
        commandStation.sendPacket(contents, 4);
        notifyConsistListeners(locoAddress, ConsistListener.OPERATION_SUCCESS);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NmraConsist.class);

}
