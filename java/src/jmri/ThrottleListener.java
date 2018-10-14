package jmri;

import java.util.EventListener;

/**
 * A listener interface for a class requesting a DccThrottle from the
 * ThrottleManager.
 * <P>
 * Implementing classes used the methods here as part of the throttle request and initilization process as described shown in
 * <img src="doc-files/ThrottleListener-Sequence.png" alt="Throttle initialization sequence UML diagram">
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 * @author Bob Jacobsen Copyright (C) 2007
 */
public interface ThrottleListener extends EventListener {

    /**
     * Get notification that a throttle has been found as you requested.
     *
     * @param t An instantiation of the DccThrottle with the address requested.
     */
    public void notifyThrottleFound(DccThrottle t);

    /**
     * Get notification that an attempt to request a throttle has failed
     *
     * @param address LocoAddress of the failed loco request.
     * @param reason  The reason why the throttle request failed.
     */
    public void notifyFailedThrottleRequest(LocoAddress address, String reason);

    /**
     * Get notification that a throttle request requires is in use by another
     * device, and a "steal" may be required.
     *
     * @param address LocoAddress of the throttle that needs to be stolen.
     */
    public void notifyStealThrottleRequired(LocoAddress address);

}

/*
 * @startuml jmri/doc-files/ThrottleListener-Sequence.png
 * participant ThrottleListener
 * participant ThrottleManager
 * participant Throttle
 *
 * == Quick Failure Scenario ==
 *
 * ThrottleListener-> ThrottleManager : requestThrottle(address,ThrottleListener)
 * group If Throttle Request Cannot Continue
 *    ThrottleListener <-- ThrottleManager : return false 
 *    deactivate ThrottleManager
 *    note over ThrottleListener : Request ends here
 * end
 * deactivate ThrottleListener
 *
 * == Normal Request Scenario ==
 * 
 * ThrottleListener-> ThrottleManager : requestThrottle(address,ThrottleListener)
 *
 * group If Throttle Request Can Continue
 *    ThrottleListener <-- ThrottleManager : return true
 *    note over ThrottleListener : Waits for callback to proceed.
 *    group If Throttle Object Does not exist
 *       note over ThrottleManager : Throttle Manager start system specific actions requred to create a throttle.
 *    == Optional Steal ==
 *       ThrottleListener <-- ThrottleManager : notifyStealThrottleRequired(address)
 *       note over ThrottleListener : To steal or not is determined by the throttle Listener. 
 *       group If the Throttle does not wish to steal
 *           ThrottleListener --> ThrottleManager : stealThrottleRequest(address info, false )
 *           note over ThrottleListener : Request ends here
 *       end
 *       group If the Throttle wishes to steal
 *           ThrottleListener --> ThrottleManager : stealThrottleRequest(address info, true)
 *               
 *               note over ThrottleManager : Throttle creation continues normally.
 *           end
 *
 *    == Creating the throttle  ==
 *       ThrottleManager --> Throttle : new Throttle(address)
 *       group If the Throttle creation fails
            ThrottleListener <-- ThrottleManager: notifyFailedThrottleReqest(address)
 *          note over ThrottleListener : Request ends here
 *       end                    
 *       group If the Throttle creation request succeeds
            ThrottleManager <-- Throttle: return new Throttle
 *       end
 *    end
 * ThrottleListener <-- ThrottleManager: notifyThrottleFound(Throttle)
 * note over ThrottleListener : Throttle can now be controlled by ThrottleListener or a delegate.
 * 
 * @enduml
 */ 
