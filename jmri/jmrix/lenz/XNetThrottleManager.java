package jmri.jmrix.lenz;

import jmri.ThrottleListener;
import jmri.ThrottleManager;
import jmri.LocoAddress;

import com.sun.java.util.collections.HashMap;

import jmri.jmrix.AbstractThrottleManager;

/**
 * XNet implementation of a ThrottleManager based on the AbstractThrottleManager.
 * @author     Paul Bender Copyright (C) 2002-2004
 * @version    $Revision: 2.5 $
 */

public class XNetThrottleManager extends AbstractThrottleManager implements ThrottleManager
{
    /**
     * Constructor.
     */
    public XNetThrottleManager()
    {
       super();
    }

    /**
     * Request a new throttle object be creaetd for the address, and let 
     * the throttle listeners know about it.
     **/
     public void requestThrottleSetup(LocoAddress address) {
	if(log.isDebugEnabled()) log.debug("Requesting Throttle: " +address);
	XNetThrottle throttle=new XNetThrottle(address);
	notifyThrottleKnown(throttle,address);	
     }

    /*
     * XPressNet based systems DO NOT use the Dispatch Function
     */
    public boolean hasDispatchFunction() { return false; }

    /*
     * XPressNet based systems can have multiple throttles for the same 
     * device
     */
     protected boolean singleUse() { return false; }

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

   /**
     * What speed modes are supported by this system?
     * value should be xor of possible modes specifed by the
     * DccThrottle interface
     * XPressNet supports 14,27,28 and 128 speed step modes
     */
    public int supportedSpeedModes() {
        return( jmri.DccThrottle.SpeedStepMode128 | 
		jmri.DccThrottle.SpeedStepMode28 |
		jmri.DccThrottle.SpeedStepMode27 | 
		jmri.DccThrottle.SpeedStepMode14 );
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetThrottleManager.class.getName());

}

