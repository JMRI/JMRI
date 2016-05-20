package jmri.jmrix.lenz.hornbyelite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.ThrottleManager;
import jmri.LocoAddress;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;

/**
 * XNet implementation of a ThrottleManager based on the AbstractThrottleManager.
 * @author     Paul Bender Copyright (C) 2008
 * @version    $Revision$
 */

public class EliteXNetThrottleManager extends jmri.jmrix.lenz.XNetThrottleManager implements ThrottleManager
{
    /**
     * Constructor.
     */
    public EliteXNetThrottleManager(XNetSystemConnectionMemo memo)
    {
       super(memo);
    }

    /**
     * Request a new throttle object be created for the address, and let 
     * the throttle listeners know about it.
     **/
     public void requestThrottleSetup(LocoAddress address, boolean control) {
        EliteXNetThrottle throttle;
        if(log.isDebugEnabled()) log.debug("Requesting Throttle: " +address);
        if(throttles.containsKey(address))
           notifyThrottleKnown(throttles.get(address),address);
        else {
           throttle=new EliteXNetThrottle((XNetSystemConnectionMemo)adapterMemo, address,tc);
           throttles.put(address,throttle);
           notifyThrottleKnown(throttle,address);
        }

     }


    static Logger log = LoggerFactory.getLogger(EliteXNetThrottleManager.class.getName());
}

