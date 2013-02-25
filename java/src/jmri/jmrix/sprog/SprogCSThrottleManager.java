package jmri.jmrix.sprog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.DccThrottle;


import jmri.jmrix.AbstractThrottleManager;

/**
 * SPROG Command Station implementation of a ThrottleManager.
 * <P> Updated by Andrew Crosland February 2012 to enable 28 step
 * speed packets</P>
 * @author	    Andrew Crosland  Copyright (C) 2006, 2012
 * @version         $Revision$
 */
public class SprogCSThrottleManager extends AbstractThrottleManager {

    /**
     * Constructor.
     */
    public SprogCSThrottleManager(SprogSystemConnectionMemo memo) {
        super(memo);
    }

    public void requestThrottleSetup(LocoAddress a, boolean control) {
        // The SPROG protocol doesn't require an interaction with the command
        // station for this, so immediately trigger the callback
        DccLocoAddress address = (DccLocoAddress) a;
        log.debug("new SprogThrottle for "+address);
        notifyThrottleKnown(new SprogCSThrottle((SprogSystemConnectionMemo)adapterMemo, address), address);
    }

    /**
     * What speed modes are supported by this system?
     * value should be or of possible modes specified by the
     * DccThrottle interface
     */
    public int supportedSpeedModes() {
        return(DccThrottle.SpeedStepMode128 | DccThrottle.SpeedStepMode28);
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
    }

    static Logger log = LoggerFactory.getLogger(SprogCSThrottleManager.class.getName());

}
