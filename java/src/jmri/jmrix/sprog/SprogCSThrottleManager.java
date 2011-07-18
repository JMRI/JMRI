package jmri.jmrix.sprog;

import jmri.LocoAddress;
import jmri.DccLocoAddress;


import jmri.jmrix.AbstractThrottleManager;

/**
 * SPROG Command Station implementation of a ThrottleManager.
 * <P>
 * @author	    Andrew Crosland  Copyright (C) 2006
 * @version         $Revision$
 */
public class SprogCSThrottleManager extends AbstractThrottleManager {

    /**
     * Constructor.
     */
    public SprogCSThrottleManager() {
        super();
    }

    public void requestThrottleSetup(LocoAddress a, boolean control) {
        // The SPROG protocol doesn't require an interaction with the command
        // station for this, so immediately trigger the callback
        DccLocoAddress address = (DccLocoAddress) a;
        log.debug("new SprogThrottle for "+address);
        notifyThrottleKnown(new SprogCSThrottle(address), address);
    }

    /**
     * Address 100 and above is a long address
     **/
    public boolean canBeLongAddress(int address) {
        return isLongAddress(address);
    }

    /**
     * Address 99 and below is a short address
     **/
    public boolean canBeShortAddress(int address) {
        return !isLongAddress(address);
    }

    /**
     * Are there any ambiguous addresses (short vs long) on this system?
     */
    public boolean addressTypeUnique() { return true; }

    /*
     * Local method for deciding short/long address
     */
    static boolean isLongAddress(int num) {
        return (num>=100);
    }

    public boolean disposeThrottle(jmri.DccThrottle t, jmri.ThrottleListener l){
        if (super.disposeThrottle(t, l)){
            SprogCSThrottle lnt = (SprogCSThrottle) t;
            lnt.throttleDispose();
            return true;
        }
        return false;
        //LocoNetSlot tSlot = lnt.getLocoNetSlot();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SprogCSThrottleManager.class.getName());

}
