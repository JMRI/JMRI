package jmri.jmrit.withrottle;

import jmri.DccConsist;
import jmri.DccLocoAddress;
import jmri.util.StringUtil;

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
        //  Use NMRA consist command to set consist address
        String packet = null;
        int loAdd = StringUtil.lowIntForLongAddr(loco.getNumber());
        int ctl = 16 + 2 + (dirNorm ? 0 : 1);
        int conAdd = 0;
        if (consist != null){
            conAdd = getConsistAddress().getNumber();
        }
        if (loco.isLongAddress()) {
            int hiAdd = StringUtil.hiIntForLongAddr(loco.getNumber());
            packet = StringUtil.formatPacketFromIntegers(hiAdd, loAdd, ctl, conAdd);
        } else {
            packet = StringUtil.formatPacketFromIntegers(loAdd, ctl, conAdd);
        }
        if (packet != null) {
            log.debug(packet);
            jmri.InstanceManager.commandStationInstance().sendPacket(StringUtil.bytesFromHexString(packet), 1);
        }
    }
    
        static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WiFiConsist.class.getName());
    
}
