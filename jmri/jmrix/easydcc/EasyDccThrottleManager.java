package jmri.jmrix.easydcc;

import jmri.LocoAddress;
import jmri.DccLocoAddress;

import jmri.jmrix.AbstractThrottleManager;

/**
 * EasyDCC implementation of a ThrottleManager.
 * <P>
 * Based on early NCE code.
 *
 * @author	    Bob Jacobsen  Copyright (C) 2001, 2005
 * @author Modified by Kelly Loyd
 * @version         $Revision: 1.3 $
 */
public class EasyDccThrottleManager extends AbstractThrottleManager {

    /**
     * Constructor.
     */
    public EasyDccThrottleManager() {
        super();
    }

    public void requestThrottleSetup(LocoAddress address) {
        // KSL 20040409 - EasyDcc does not require feedback afaik
        // don't quite know if the EasyDcc requires feedback.
        // may need to extend this.
        /* KSL - appears that the first command sent to the Queue in EasyDcc
           is 'lost' - so it may be beneficial to send a 'Send' command 
           just to wake up the command station.
           This was tested on v418 - also appears as an issue with the
           radio throttles. 
        */
        log.debug("new EasyDccThrottle for "+address);
        notifyThrottleKnown(new EasyDccThrottle((DccLocoAddress)address), address);
    }
    
    // KSL 20040409 - EasyDcc does not have a 'dispatch' function.
    public boolean hasDispatchFunction() { return false; }
    
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EasyDccThrottleManager.class.getName());

}
