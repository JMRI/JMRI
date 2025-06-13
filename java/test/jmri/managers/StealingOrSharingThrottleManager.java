package jmri.managers;

import jmri.jmrix.debugthrottle.DebugThrottleManager;
import jmri.jmrix.debugthrottle.DebugThrottle;
import jmri.DccLocoAddress;
import jmri.ThrottleListener;
import jmri.LocoAddress;

import org.junit.jupiter.api.Assertions;

/**
 * This is an extension of the DebugThrottleManager that always requires
 * the calling throttle object to share to get a valid throttle.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2005
 * @author Bob Jacobsen Copyright (C) 2018
 */
public class StealingOrSharingThrottleManager extends DebugThrottleManager {

    public ThrottleListener.DecisionType lastResponse = null;

    public StealingOrSharingThrottleManager() {
        super();
    }

    /**
     * Constructor.
     */
    public StealingOrSharingThrottleManager(jmri.SystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestThrottleSetup(LocoAddress a, boolean control) {
        // Immediately trigger the steal or share callback.
        notifyDecisionRequest(a,ThrottleListener.DecisionType.STEAL_OR_SHARE);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void responseThrottleDecision(LocoAddress address, ThrottleListener l, ThrottleListener.DecisionType decision){
        if (!(address instanceof DccLocoAddress)){
            Assertions.fail("DebugThrottle needs a dcclocoaddress : " + address );
            return;
        }
        lastResponse = decision;
        if ( decision == ThrottleListener.DecisionType.STEAL ) {
            notifyThrottleKnown(new DebugThrottle((DccLocoAddress) address, adapterMemo), address);
        }
        else if ( decision == ThrottleListener.DecisionType.SHARE ) {
            notifyThrottleKnown(new DebugThrottle((DccLocoAddress) address, adapterMemo), address);
        }
        else {
            cancelThrottleRequest(address,l);
            failedThrottleRequest(address,"user declined to steal or share");
        }
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StealingOrSharingThrottleManager.class);

}
