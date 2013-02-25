package jmri.jmrix.debugthrottle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.LocoAddress;
import jmri.DccLocoAddress;

import jmri.jmrix.AbstractThrottleManager;

import jmri.DccThrottle;


/**
 * Implementation of a ThrottleManager for debugging.
 * <P>
 * @author	    Bob Jacobsen  Copyright (C) 2003, 2005
 * @version         $Revision$
 */
public class DebugThrottleManager extends AbstractThrottleManager {

    public DebugThrottleManager(){
        super();
    }
    /**
     * Constructor.
     */
    public DebugThrottleManager(jmri.jmrix.SystemConnectionMemo memo) {
        super(memo);
    }

    public void requestThrottleSetup(LocoAddress a, boolean control) {
        // Immediately trigger the callback.
        DccLocoAddress address = (DccLocoAddress) a;
        log.debug("new debug throttle for "+address);
        notifyThrottleKnown(new DebugThrottle(address, adapterMemo), a);
    }

    /**
     * Address 1 and above can be a long address
     **/
    public boolean canBeLongAddress(int address) {
        return (address>=1);
    }
    
    /**
     * Address 127 and below can be a short address
     **/
    public boolean canBeShortAddress(int address) {
        return (address<=127);
    }

    /**
     * Are there any ambiguous addresses (short vs long) on this system?
     */
    public boolean addressTypeUnique() { return false; }
    
    public boolean disposeThrottle(DccThrottle t, jmri.ThrottleListener l){
        log.debug("disposeThrottle called for " + t);
        if ( super.disposeThrottle(t, l)){
            DebugThrottle lnt = (DebugThrottle) t;
            lnt.throttleDispose();
            return true;
        }
        return false;
    }

    static Logger log = LoggerFactory.getLogger(DebugThrottleManager.class.getName());


}
