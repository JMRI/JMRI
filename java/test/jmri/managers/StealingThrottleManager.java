package jmri.managers;

import jmri.jmrix.debugthrottle.DebugThrottleManager;
import jmri.jmrix.debugthrottle.DebugThrottle;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.ThrottleListener;
import jmri.LocoAddress;

/**
 * This is an extension of the DebugThrottleManager that always requires
 * the calling throttle object to steal to get a valid throttle.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2005
 * @author Bob Jacobsen Copyright (C) 2018
 */
public class StealingThrottleManager extends DebugThrottleManager {

    public StealingThrottleManager() {
        super();
    }

    /**
     * Constructor.
     */
    public StealingThrottleManager(jmri.jmrix.SystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestThrottleSetup(LocoAddress a, boolean control) {
        // Immediately trigger the steal callback.
        notifyDecisionRequest(a,ThrottleListener.DecisionType.STEAL);
    }
    
    /**
     * @deprecated since 4.15.7; use #responseThrottleDecision
     */
    @Deprecated
    @Override
    public void stealThrottleRequest(LocoAddress a, ThrottleListener l,boolean steal){
        if(steal) {
            responseThrottleDecision(a, l, ThrottleListener.DecisionType.STEAL);
        } else {
            cancelThrottleRequest(a,l);
            failedThrottleRequest(a,"user declined to steal");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void responseThrottleDecision(LocoAddress address, ThrottleListener l, ThrottleListener.DecisionType decision){
        if ( decision == ThrottleListener.DecisionType.STEAL ) {
            DccLocoAddress a = (DccLocoAddress) address;
            notifyThrottleKnown(new DebugThrottle(a, adapterMemo), address);
        }
        else {
            cancelThrottleRequest(address,l);
            failedThrottleRequest(address,"user declined to steal");
        }
    }


}
