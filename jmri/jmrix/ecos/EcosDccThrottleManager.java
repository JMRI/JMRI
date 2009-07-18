package jmri.jmrix.ecos;

import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import java.util.*;

import jmri.jmrix.AbstractThrottleManager;

/**
 * EcosDCC implementation of a ThrottleManager.
 * <P>
 * Based on early NCE code.
 *
 * @author	    Bob Jacobsen  Copyright (C) 2001, 2005
 * @author Modified by Kevin Dickerson
 * @version         $Revision: 1.1 $
 */
public class EcosDccThrottleManager extends AbstractThrottleManager implements EcosListener{

    /**
     * Constructor.
     */
    public EcosDccThrottleManager() {
        super();

    }

    static private EcosDccThrottleManager mInstance = null;
    static public EcosDccThrottleManager instance() {
        return mInstance;
    }

    public void reply(EcosReply m) {
        //We are not sending commands from here yet!

   }

   public void message(EcosMessage m) {
        // messages are ignored
    }

    public void requestThrottleSetup(LocoAddress address) {
        // KSL 20040409 - EcosDcc does not require feedback afaik
        // don't quite know if the EcosDcc requires feedback.
        // may need to extend this.
        /* KSL - appears that the first command sent to the Queue in EcosDcc
           is 'lost' - so it may be beneficial to send a 'Send' command 
           just to wake up the command station.
           This was tested on v418 - also appears as an issue with the
           radio throttles. 
        */
        log.debug("new EcosDccThrottle for "+address);
        notifyThrottleKnown(new EcosDccThrottle((DccLocoAddress)address), address);
    }
    
    // KSL 20040409 - EcosDcc does not have a 'dispatch' function.
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
    
    public int supportedSpeedModes() {
    	return(DccThrottle.SpeedStepMode128 | DccThrottle.SpeedStepMode28);
        }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosDccThrottleManager.class.getName());

}
