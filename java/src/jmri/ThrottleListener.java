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
     * A decision type requested from ThrottleManager to ThrottleListener,
     * or decision made from ThrottleListener to ThrottleManager
     *
     * @since 4.15.6
     */
    enum DecisionType {
        /**
         * Notification for decision needed Steal OR Cancel, or wish to Steal
         */
        STEAL,
        /**
         * Notification for decision needed Share OR Cancel, or wish to Share
         */
        SHARE,
        /**
         * Notification for decision needed Steal OR Share OR Cancel
         */
        STEAL_OR_SHARE,
        /**
         * Notification of wish to Cancel
         */
        CANCEL
    }

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
 * 
 *  group ThrottleListener wants ThrottleManager to make steal / share decisions
 * 
 *      note over ThrottleListener : AutoDispatcher\nAbstractAutomation
 *      ThrottleListener-> ThrottleManager : requestThrottle(LocoAddress,ThrottleListener,<b>false</b>)
 *      ThrottleListener-> ThrottleManager : requestThrottle(BasicRosterEntry,ThrottleListener,<b>false</b>)
 *  end
 *  
 *  group ThrottleListener wants to make a custom steal / share decision
 *      note over ThrottleListener : AddressPanel\nWiThrottle
 *      ThrottleListener-> ThrottleManager : requestThrottle(LocoAddress,ThrottleListener,<b>true</b>)
 *      ThrottleListener-> ThrottleManager : requestThrottle(BasicRosterEntry,ThrottleListener,<b>true</b>)
 *  end
 *  
 *  group If Throttle Request Cannot Continue
 *     ThrottleListener <-- ThrottleManager : return false 
 *     deactivate ThrottleManager
 *     note over ThrottleListener : Request ends here
 *  end
 *  deactivate ThrottleListener
 * 
 *  group If Throttle Request Can Continue
 *     ThrottleListener <-- ThrottleManager : return true
 *     note over ThrottleListener : Waits for callback to proceed.
 *  end
 *     
 *  group If Throttle Object exists
 *     
 *      note over ThrottleManager : Sets BasicRosterEntry ( if present ) to Throttle
 *      ThrottleListener <-- ThrottleManager: notifyThrottleFound(DccThrottle)
 *      note over ThrottleListener : Throttle can now be controlled by ThrottleListener or a delegate.
 *     
 *  end
 *     
 *  group If Throttle Object does not exist
 *     
 *      note over ThrottleManager : Throttle Manager starts system specific actions to create a throttle.
 *      note over ThrottleManager : If the throttle is available by normal means, no stealing or sharing,\nThrottle creation continues normally.
 *      
 *      == Optional: A steal or share decision is needed requestThrottle( .. , ThrottleListener, false) ==
 *
 *      note over ThrottleManager : Abstract defaults to request steal, maintaining current behaviour.\nUsers prefs. may be queried\nThrottle creation continues normally.
 *    
 *      == Optional: A steal or share decision is needed requestThrottle( .. , ThrottleListener, true) ==
 *       
 *      note over ThrottleManager : User prefs. are not queried.
 *
 *      ThrottleListener <-- ThrottleManager : notifyDecisionRequired(LocoAddress, ThrottleListener.DecisionType.STEAL)
 *        
 *      note over ThrottleListener : The question is to Steal or Cancel?
 *        
 *      ThrottleListener <-- ThrottleManager : notifyDecisionRequired(LocoAddress, ThrottleListener.DecisionType.SHARE)
 *        
 *      note over ThrottleListener : The question is to Share or Cancel?
 *        
 *      ThrottleListener <-- ThrottleManager : notifyDecisionRequired(LocoAddress, ThrottleListener.DecisionType.STEAL_OR_SHARE)
 *        
 *      note over ThrottleListener : The question is to Steal, Share or Cancel?
 *        
 *      note over ThrottleListener : The listener might query user prefs. to reach decision
 *
 *      group If the Listener wishes to steal
 *            ThrottleListener --> ThrottleManager : responseThrottleDecision(LocoAddress, ThrottleListener, ThrottleListener.DecisionType.STEAL)
 *            note over ThrottleManager : Throttle creation continues normally.
 *      end
 *        
 *      group If the Listener wishes to share
 *            ThrottleListener --> ThrottleManager : responseThrottleDecision(LocoAddress, ThrottleListener, ThrottleListener.DecisionType.SHARE )
 *            note over ThrottleManager : Throttle creation continues normally.
 *      end
 *        
 *      group If the Listener does not wish to steal or share
 *            ThrottleListener --> ThrottleManager : responseThrottleDecision(LocoAddress, ThrottleListener, ThrottleListener.DecisionType.CANCEL )
 *            note over ThrottleListener : Request ends here
 *      end
 * 
 * 
 *      == Throttle Creation  ==
 *        
 *        group If the Throttle creation fails
 *           ThrottleListener <-- ThrottleManager: notifyFailedThrottleReqest(LocoAddress)
 *           note over ThrottleListener : Request ends here
 *        end                    
 *        group If the Throttle creation succeeds
 *        
 *          note over ThrottleManager : Sets BasicRosterEntry ( if present ) to Throttle
 *          ThrottleListener <-- ThrottleManager: notifyThrottleFound(DccThrottle)
 *          note over ThrottleListener : Throttle can now be controlled by ThrottleListener or a delegate.
 *        end
 *  end
 *     
 *  
 * @enduml
 */ 
