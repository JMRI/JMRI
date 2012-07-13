package jmri.jmrix.nce;

import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.DccLocoAddress;

import jmri.jmrix.AbstractThrottleManager;

/**
 * NCE implementation of a ThrottleManager.
 * <P>
 * @author	    Bob Jacobsen  Copyright (C) 2001
 * @version         $Revision$
 */
public class NceThrottleManager extends AbstractThrottleManager {

    /**
     * Constructor.
     */
    public NceThrottleManager(NceSystemConnectionMemo memo) {
        super(memo);
        this.tc = memo.getNceTrafficController();
        this.prefix = memo.getSystemPrefix();
    }
    
    NceTrafficController tc = null;
    String prefix = "";

    public void requestThrottleSetup(LocoAddress a, boolean control) {
        // the NCE protocol doesn't require an interaction with the command
        // station for this, so immediately trigger the callback.
        DccLocoAddress address = (DccLocoAddress) a;
        log.debug("new NceThrottle for "+address);
        notifyThrottleKnown(new NceThrottle((NceSystemConnectionMemo)adapterMemo, address), address);
    }

    /**
     * Address 1 and above can be long
     **/
    public boolean canBeLongAddress(int address) {
        return (address>=1);
    }
    
    /**
     * The full range of short addresses are available
     **/
    public boolean canBeShortAddress(int address) {
        return (address<=127);
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
            NceThrottle nct = (NceThrottle) t;
            nct.throttleDispose();
            return true;
        }
        return false;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NceThrottleManager.class.getName());

}