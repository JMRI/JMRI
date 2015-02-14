package jmri.jmrix.mrc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.DccLocoAddress;

import jmri.jmrix.AbstractThrottleManager;

/**
 * MRC implementation of a ThrottleManager.
 * <P>
 * @author	    Bob Jacobsen  Copyright (C) 2001
 * @version         $Revision: 24649 $
 */
public class MrcThrottleManager extends AbstractThrottleManager {

    /**
     * Constructor.
     */
    public MrcThrottleManager(MrcSystemConnectionMemo memo) {
        super(memo);
        this.tc = memo.getMrcTrafficController();
        this.prefix = memo.getSystemPrefix();
    }
    
    MrcTrafficController tc = null;
    String prefix = "";

    public void requestThrottleSetup(LocoAddress a, boolean control) {
        //We do interact
        DccLocoAddress address = (DccLocoAddress) a;
        log.debug("new MrcThrottle for "+address); //IN18N
        notifyThrottleKnown(new MrcThrottle((MrcSystemConnectionMemo)adapterMemo, address), address);
    }

    /**
     * Addresses 0-10239 can be long
     **/
    public boolean canBeLongAddress(int address) {
        return ( (address>=0) && (address<=10239) );
    }
    
    /**
     * The short addresses 1-127 are available
     **/
    public boolean canBeShortAddress(int address) {
        return ( (address>=1) && (address<=127) );
    }

    /**
     * Are there any ambiguous addresses (short vs long) on this system?
     */
    public boolean addressTypeUnique() { return false; }
    
    public int supportedSpeedModes() {
    	return(DccThrottle.SpeedStepMode128 | DccThrottle.SpeedStepMode28);
    }
    
    public boolean disposeThrottle(jmri.DccThrottle t, jmri.ThrottleListener l){
        if (super.disposeThrottle(t, l)){
            MrcThrottle nct = (MrcThrottle) t;
            nct.throttleDispose();
            return true;
        }
        return false;
    }

    static Logger log = LoggerFactory.getLogger(MrcThrottleManager.class.getName());

}
