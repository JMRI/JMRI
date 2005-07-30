package jmri.jmrix.nce;

import jmri.LocoAddress;
import jmri.DccLocoAddress;

import jmri.jmrix.AbstractThrottleManager;

/**
 * NCE implementation of a ThrottleManager.
 * <P>
 * @author	    Bob Jacobsen  Copyright (C) 2001
 * @version         $Revision: 1.5 $
 */
public class NceThrottleManager extends AbstractThrottleManager {

    /**
     * Constructor.
     */
    public NceThrottleManager() {
        super();
    }

    public void requestThrottleSetup(LocoAddress a) {
        // the NCE protocol doesn't require an interaction with the command
        // station for this, so immediately trigger the callback.
        DccLocoAddress address = (DccLocoAddress) a;
        log.debug("new NceThrottle for "+address);
        notifyThrottleKnown(new NceThrottle(address), address);
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceThrottleManager.class.getName());

}