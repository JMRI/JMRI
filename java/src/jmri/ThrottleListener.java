package jmri;

import java.util.EventListener;

/**
 * A listener interface for a class requesting a DccThrottle from the
 * ThrottleManager.
 * <p>
 * Implementing classes used the methods here as part of the throttle request and initilization process as described shown in
 * <img src="doc-files/ThrottleListener-Sequence.png" alt="Throttle initialization sequence UML diagram">
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
public interface ThrottleListener extends EventListener {
    
    /**
     * A decision type requested from ThrottleManager to ThrottleListener,
     * or decision made from ThrottleListener to ThrottleManager
     *
     * @since 4.15.7
     */
    enum DecisionType {
        /**
         * Notification for decision needed, Steal OR Cancel, or wish to Steal
         */
        STEAL,
        /**
         * Notification for decision needed, Share OR Cancel, or wish to Share
         */
        SHARE,
        /**
         * Notification for decision needed, Steal OR Share OR Cancel
         */
        STEAL_OR_SHARE
    }
    
    /**
     * Get notification that a throttle request requires is in use by another
     * device, and a "steal" may be required.
     * @deprecated since 4.15.7; use
     * #notifyDecisionRequired(LocoAddress, DecisionType) instead
     */
    @Deprecated
    public void notifyStealThrottleRequired(LocoAddress address);

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
     * Get notification that a throttle request is in use by another
     * device, and a "steal", "share", or "steal/share" decision may be required.
     *
     * @param address The LocoAddress that needs the decision.
     * @param question The question being asked, steal / cancel, share / cancel, steal / share / cancel
     */
    public void notifyDecisionRequired(LocoAddress address, DecisionType question);

}

/*
 * @startuml jmri/doc-files/ThrottleListener-Sequence.png
 *  participant ThrottleListener
 *  participant ThrottleManager
 *  participant Throttle
 * 
 *  == Scenario 1: Quick Failure ==
 *  ThrottleListener --> ThrottleManager : requestThrottle(address,ThrottleListener,*)
 *  group If Throttle Request Cannot Continue
 *      ThrottleListener <-- ThrottleManager : return false
 *      note over ThrottleListener : Request ends here
 *  end
 *  == Scenario 2: Steal/Share not supported by the manager ==
 *  ThrottleListener --> ThrottleManager : requestThrottle(address,ThrottleListener,*)
 *  ThrottleListener <-- ThrottleManager : return true 
 *  note over ThrottleListener: Wait for callback, see Common Process Completion
 *
 *  == Scenario 3: ThrottleListener asks ThrottleManager to handle steal/share decision. == 
 *  
 *     ThrottleListener --> ThrottleManager : requestThrottle(address,ThrottleListener,<b>false</b>)
 *     ThrottleListener <-- ThrottleManager : return true 
 *     note over ThrottleListener: Wait for callback, see Common Process Completion
 *  == Scenario 4: ThrottleListener wants to make steal/share decision ==
 *     ThrottleListener --> ThrottleManager : requestThrottle(address,ThrottleListener,<b>true</b>)
 *     ThrottleListener <-- ThrottleManager : return true 
 *     group Manager determines steal/share is required
 *        group Case 1: Only steal is an option
 *     
 *           ThrottleListener <-- ThrottleManager : notifyDecisionRequired(LocoAddress, ThrottleListener.DecisionType.STEAL)
 *        
 *           group If the Listener chooses to steal
 *               ThrottleListener --> ThrottleManager : responseThrottleDecision(LocoAddress, ThrottleListener, ThrottleListener.DecisionType.STEAL)
 *           end
 *        
 *           group If the Listener chooses to cancel
 *               ThrottleListener --> ThrottleManager : cancelThrottleRequest(LocoAddress, ThrottleListener)
 *               note over ThrottleListener : Request ends here
 *           end
 *       end
 *        
 *       group Case 2: Only share is an option
 *            ThrottleListener <-- ThrottleManager : notifyDecisionRequired(LocoAddress, ThrottleListener.DecisionType.SHARE)
 *        
 *            group If the Listener chooses to share
 *               ThrottleListener --> ThrottleManager : responseThrottleDecision(LocoAddress, ThrottleListener, ThrottleListener.DecisionType.SHARE)
 *            end
 *        
 *            group If the Listener chooses to cancel
 *               ThrottleListener --> ThrottleManager : cancelThrottleRequest(LocoAddress, ThrottleListener)
 *               note over ThrottleListener : Request ends here
 *            end        
         end

 *       group Case 3: steal and share are options
 *            ThrottleListener <-- ThrottleManager : notifyDecisionRequired(LocoAddress, ThrottleListener.DecisionType.STEAL_OR_SHARE)
 *        
 *            group If the Listener chooses to steal
 *               ThrottleListener --> ThrottleManager : responseThrottleDecision(LocoAddress, ThrottleListener, ThrottleListener.DecisionType.STEAL)
 *            end
 *        
 *            group If the Listener chooses to share
 *               ThrottleListener --> ThrottleManager : responseThrottleDecision(LocoAddress, ThrottleListener, ThrottleListener.DecisionType.SHARE)
 *            end
 *        
 *            group If the Listener chooses to cancel
 *               ThrottleListener --> ThrottleManager : cancelThrottleRequest(LocoAddress, ThrottleListener)
 *               note over ThrottleListener : Request ends here
 *            end
        end
 *  end
 *  note over ThrottleListener: Wait for callback, see Common Process Completion
 *    == Common Process Completion ==
 *    group If Throttle Does Not Exist 
 *    == Throttle Creation  ==
 *       ThrottleManager --> Throttle: new Throttle(address)
 *       group If the Throttle creation fails
 *          ThrottleListener <-- ThrottleManager: notifyFailedThrottleReqest(address)
 *          note over ThrottleListener : Request ends here
 *       end                    
 *       group If the Throttle creation succeeds
 *         ThrottleManager <-- Throttle: return Throttle        
 *       end
 *    end
 *    ThrottleListener <-- ThrottleManager: notifyThrottleFound(Throttle)
 *    note over ThrottleListener : Throttle can now be controlled by ThrottleListener or a delegate.
 *  
 * @enduml
 */ 
