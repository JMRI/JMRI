package jmri.jmrix.direct;

import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.jmrix.AbstractThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Direct DCC implementation of a ThrottleManager.
 * <P>
 * When the traffic manager doesn't have anything else to do, it comes here to
 * get a command to send.
 * <P>
 * This is a partial implementation, which can only handle one Throttle at a
 * time. It also is missing logic to alternate sending speed and function
 * commands; right now it only sends the first group of function packets.
 *
 * @author	Bob Jacobsen Copyright (C) 2004
 */
public class ThrottleManager extends AbstractThrottleManager {

    /**
     * Constructor.
     */
    public ThrottleManager() {
        super();
        if (mInstance != null) {
            log.warn("Creating too many objects");
        }
        mInstance = this;
    }

    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static private ThrottleManager mInstance = null;

    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public ThrottleManager instance() {
        return mInstance;
    }

    Throttle currentThrottle = null;

    /**
     * Create throttle data structures.
     */
    public void requestThrottleSetup(LocoAddress address, boolean control) {
        if (currentThrottle != null) {
            log.error("DCC direct cannot handle more than one throttle now");
            failedThrottleRequest((DccLocoAddress) address, "DCC direct cannot handle more than one throttle " + address);
            return;
        }
        log.warn("requestThrottleSetup should preserve actual address object, not use ints");
        currentThrottle = new Throttle(((DccLocoAddress) address).getNumber());
        notifyThrottleKnown(currentThrottle, currentThrottle.getLocoAddress());
    }

    public boolean addressTypeUnique() {
        return false;
    }

    public boolean canBeShortAddress(int a) {
        return a < 128;
    }

    public boolean canBeLongAddress(int a) {
        return a > 0;
    }

    /**
     * Invoked when a throttle is released, this updates the local data
     * structures
     */
    public boolean disposeThrottle(jmri.DccThrottle t, jmri.ThrottleListener l) {
        if (super.disposeThrottle(t, l)) {
            currentThrottle = null;
            return true;
        }
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(ThrottleManager.class.getName());

}
