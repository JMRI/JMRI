package jmri.jmrix.debugthrottle;

import jmri.jmrix.AbstractThrottleManager;


/**
 * Implementation of a ThrottleManager for debugging.
 * <P>
 * @author	    Bob Jacobsen  Copyright (C) 2003, 2005
 * @version         $Revision: 1.2 $
 */
public class DebugThrottleManager extends AbstractThrottleManager {

    /**
     * Constructor.
     */
    public DebugThrottleManager() {
        super();
    }

    public void requestThrottleSetup(int address) {
        // Immediately trigger the callback.
        log.debug("new debug throttle for "+address);
        notifyThrottleKnown(new DebugThrottle(address), address);
    }

    /**
     * Address 1 and above is a long address
     **/
    public boolean canBeLongAddress(int address) {
        return (address>=1);
    }
    
    /**
     * Address 127 and below is a short address
     **/
    public boolean canBeShortAddress(int address) {
        return (address<=127);
    }

    /**
     * Are there any ambiguous addresses (short vs long) on this system?
     */
    public boolean addressTypeUnique() { return false; }
            

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DebugThrottleManager.class.getName());


}