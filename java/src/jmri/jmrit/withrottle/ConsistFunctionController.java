package jmri.jmrit.withrottle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.ThrottleListener;
import jmri.jmrit.roster.RosterEntry;

/**
 *
 *	@author Brett Hoffman   Copyright (C) 2010, 2011
 *	@version $Revision$
 */
public class ConsistFunctionController implements ThrottleListener{

    private DccThrottle throttle;
    private RosterEntry rosterLoco = null;
    private ThrottleController throttleController;

    public ConsistFunctionController(ThrottleController tc){
        throttleController = tc;
    }

    public ConsistFunctionController(ThrottleController tc, RosterEntry re){
        throttleController = tc;
        rosterLoco = re;
    }
    
    public void notifyThrottleFound(DccThrottle t) {
        if (log.isDebugEnabled()) log.debug("Lead Loco throttle found: " + t +
                                            ", for consist: " + throttleController.getCurrentAddressString());
        throttle = t;

        if (rosterLoco == null){
            rosterLoco = throttleController.findRosterEntry(throttle);
        }

        throttleController.syncThrottleFunctions(throttle, rosterLoco);
        throttleController.setFunctionThrottle(t);
        throttleController.sendFunctionLabels(rosterLoco);
        throttleController.sendAllFunctionStates(throttle);
    }

    public void notifyFailedThrottleRequest(DccLocoAddress address, String reason){
    }

    
    public void dispose(){
        jmri.InstanceManager.throttleManagerInstance().releaseThrottle(throttle, this);
    }

    public DccThrottle getThrottle(){
        return throttle;
    }

    boolean requestThrottle(DccLocoAddress loco) {
        return jmri.InstanceManager.throttleManagerInstance().requestThrottle(loco.getNumber(), loco.isLongAddress(), this);
    }

    static Logger log = LoggerFactory.getLogger(ConsistFunctionController.class.getName());

}
