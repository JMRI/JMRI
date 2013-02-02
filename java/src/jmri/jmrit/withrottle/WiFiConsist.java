package jmri.jmrit.withrottle;

import org.apache.log4j.Logger;
import jmri.DccConsist;
import jmri.DccLocoAddress;

/**
 *	@author Brett Hoffman   Copyright (C) 2010, 2011
 *	@version $Revision: 18416 $
 */
public class WiFiConsist extends DccConsist{
    
    public WiFiConsist(DccLocoAddress address){
        super(address);
    }
    
    @Override
    public void add(DccLocoAddress loco,boolean dirNorm) {
        restore(loco, dirNorm);
        sendConsistCommand(loco, dirNorm, this);
    }
    
    @Override
    public void remove(DccLocoAddress loco) {
        ConsistDir.remove(loco);
        ConsistList.remove(loco);
        ConsistPosition.remove(loco);
        sendConsistCommand(loco, true, null);
    }
    
    /**
     * Send an NMRA consisting command to add or remove a loco from a consist
     * @param loco      The loco to add or remove
     * @param dirNorm   true for normal, false for reverse
     * @param consist   The short consist address for a loco, null to remove
     */
    public void sendConsistCommand(DccLocoAddress loco, boolean dirNorm, WiFiConsist consist){
        int conAddr = 0;
        if (consist != null){
            conAddr = getConsistAddress().getNumber();
        }
        //  Use NMRA consist command to set consist address
        byte packet[] = jmri.NmraPacket.consistControl(loco.getNumber(),
                                        loco.isLongAddress(),
                                        conAddr,
                                        dirNorm);
        if (packet != null) {
            log.debug(packet);
            jmri.InstanceManager.commandStationInstance().sendPacket(packet, 1);
        }
    }
    
        static Logger log = Logger.getLogger(WiFiConsist.class.getName());
    
}
