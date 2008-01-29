package jmri.jmrix.lenz.hornbyelite;

import jmri.ThrottleManager;
import jmri.LocoAddress;

import jmri.jmrix.AbstractThrottleManager;
import jmri.jmrix.lenz.XNetThrottleManager;

/**
 * XNet implementation of a ThrottleManager based on the AbstractThrottleManager.
 * @author     Paul Bender Copyright (C) 2008
 * @version    $Revision: 1.1 $
 */

public class EliteXNetThrottleManager extends jmri.jmrix.lenz.XNetThrottleManager implements ThrottleManager
{
    /**
     * Constructor.
     */
    public EliteXNetThrottleManager()
    {
       super();
    }

    /**
     * Request a new throttle object be creaetd for the address, and let 
     * the throttle listeners know about it.
     **/
     public void requestThrottleSetup(LocoAddress address) {
	if(log.isDebugEnabled()) log.debug("Requesting Throttle: " +address);
	EliteXNetThrottle throttle=new EliteXNetThrottle(address);
	notifyThrottleKnown(throttle,address);	
     }


    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EliteXNetThrottleManager.class.getName());
}

