package jmri.jmrix.debugthrottle;

import jmri.jmrix.AbstractThrottleManager;


/**
 * Implementation of a ThrottleManager for debugging.
 * <P>
 * @author	    Bob Jacobsen  Copyright (C) 2003
 * @version         $Revision: 1.1 $
 */
public class DebugThrottleManager extends AbstractThrottleManager {

    /**
     * Constructor.
     */
    public DebugThrottleManager() {
        super();
    }

    public void requestThrottleSetup(int address) {
        // Immediately trigger the callback.
        log.debug("new NceThrottle for "+address);
        notifyThrottleKnown(new DebugThrottle(address), address);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DebugThrottleManager.class.getName());


}