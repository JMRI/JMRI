package jmri.jmrix.nce;

import jmri.jmrix.AbstractThrottleManager;


/**
 * NCE implementation of a ThrottleManager
 * <P>
 * @author	    Bob Jacobsen  Copyright (C) 2001
 * @version         $Revision: 1.1 $
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
        notifyThrottleKnown(new NceThrottle(), address);
    }

}