package jmri.jmrix.lenz;

import jmri.ThrottleListener;
import jmri.ThrottleManager;
import com.sun.java.util.collections.HashMap;

import jmri.jmrix.AbstractThrottleManager;

/**
 * XNet implementation of a ThrottleManager based on the AbstractThrottleManager.
 * @author     Paul Bender Copyright (C) 2002-2004
 * @version    $Revision: 2.2 $
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
     public void requestThrottleSetup(int address) {
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetThrottleManager.class.getName());

}

