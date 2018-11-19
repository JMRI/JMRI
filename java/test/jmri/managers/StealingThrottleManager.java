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
 * <P>
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
        DccLocoAddress address = (DccLocoAddress) a;
        notifyStealRequest(address);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stealThrottleRequest(LocoAddress a, ThrottleListener l,boolean steal){
        DccLocoAddress address = (DccLocoAddress) a;
        if(steal) {
           notifyThrottleKnown(new DebugThrottle(address, adapterMemo), a);
        } else {
           cancelThrottleRequest(address,l);
           failedThrottleRequest(a,"user declined to steal");
        }
    }


}
