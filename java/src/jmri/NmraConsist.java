// NmraConsist.java

package jmri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.ConsistListener;


/**
 * This is the Default DCC consist manager installed on systems which
 * support the command station interface.  It uses the NMRA consist 
 * creation packet instead of Operations Mode programming to build
 * a consist, but otherwise is derived from the DccConsist code.
 *
 * @author                      Paul Bender Copyright (C) 2011
 * @version                     $Revision 1.0 $
 */
public class NmraConsist extends DccConsist implements Consist{

// Initialize a consist for the specific address.

        public NmraConsist(int address) {
	     super(address);
             if (log.isDebugEnabled())
	         log.debug("Nmra Consist created for address: " +address);
        }

        public NmraConsist(DccLocoAddress address) {
	     super(address);
             if (log.isDebugEnabled())
	         log.debug("Nmra Consist created for address: " +address.toString());
        }

        /*
	 *  Add a Locomotive to an Advanced Consist
	 *  @param address is the Locomotive address to add to the locomotive
	 *  @param directionNormal is True if the locomotive is traveling 
         *        the same direction as the consist, or false otherwise.
         */
	protected void addToAdvancedConsist(DccLocoAddress LocoAddress, boolean directionNormal) {
                if (log.isDebugEnabled()) log.debug("Add Locomotive " +
                                                    LocoAddress.toString() +
                                                    " to advanced consist " +
                                                    ConsistAddress.toString() +
                                                    " With Direction Normal " +
                                                     directionNormal + ".");
                 // create the message and fill it,
                 byte[] contents = jmri.NmraPacket.consistControl(LocoAddress.getNumber(),
                                                LocoAddress.isLongAddress(),
                                                ConsistAddress.getNumber(),
                                                directionNormal);
                 InstanceManager.commandStationInstance().sendPacket(contents, 4);
                 notifyConsistListeners(LocoAddress,ConsistListener.OPERATION_SUCCESS);

	}

        /*
	 *  Remove a Locomotive from an Advanced Consist
	 *  @param address is the Locomotive address to add to the locomotive
         */
	protected void removeFromAdvancedConsist(DccLocoAddress LocoAddress) {
                if (log.isDebugEnabled()) log.debug("Remove Locomotive " +
                                                    LocoAddress.toString() +
                                                    " from advanced consist " +
                                                    ConsistAddress.toString() +
                                                    ".");
                 // create the message and fill it,
                 byte[] contents = jmri.NmraPacket.consistControl(LocoAddress.getNumber(),
                                                LocoAddress.isLongAddress(),
                                                0,  //set to 0 to remove
                                                true );//always normal direction
                 InstanceManager.commandStationInstance().sendPacket(contents, 4);
                 notifyConsistListeners(LocoAddress,ConsistListener.OPERATION_SUCCESS);
	}


	static Logger log = LoggerFactory.getLogger(NmraConsist.class.getName());

}
