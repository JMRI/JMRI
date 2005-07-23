package jmri.jmrix.easydcc;

import jmri.jmrix.AbstractThrottleManager;

/**
 * NCE implementation of a ThrottleManager.
 * <P>
 * @author	    Bob Jacobsen  Copyright (C) 2001, Modified by Kelly Loyd
 * @version         $Revision: 1.2 $
 */
public class EasyDccThrottleManager extends AbstractThrottleManager {

    /**
     * Constructor.
     */
    public EasyDccThrottleManager() {
        super();
    }

    public void requestThrottleSetup(int address) {
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
        notifyThrottleKnown(new EasyDccThrottle(address), address);
    }
    
    // KSL 20040409 - EasyDcc does not have a 'dispatch' function.
    public boolean hasDispatchFunction() { return false; }
    
    /**
     * Address 100 and above is a long address
     **/
    public boolean canBeLongAddress(int address) {
        return (address>=100);
    }
    
    /**
     * Address 99 and below is a short address
     **/
    public boolean canBeShortAddress(int address) {
        return (address<=99);
    }

    /**
     * Are there any ambiguous addresses (short vs long) on this system?
     */
    public boolean addressTypeUnique() { return true; }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EasyDccThrottleManager.class.getName());

}
