package jmri.jmrix.nce;

import jmri.jmrix.AbstractThrottleManager;


/**
 * NCE implementation of a ThrottleManager
 * <P>
 * @author	    Bob Jacobsen  Copyright (C) 2001
 * @version         $Revision: 1.2 $
 */
public class NceThrottleManager extends AbstractThrottleManager {

    /**
     * Constructor.
     */
    public NceThrottleManager() {
        super();
    }

    public void requestThrottleSetup(int address) {
        // the NCE protocol doesn't require an interaction with the command
        // station for this, so immediately trigger the callback.
        log.debug("new NceThrottle for "+address);
        notifyThrottleKnown(new NceThrottle(address), address);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceThrottleManager.class.getName());


}