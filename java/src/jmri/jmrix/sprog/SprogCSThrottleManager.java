package jmri.jmrix.sprog;

import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.jmrix.AbstractThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SPROG Command Station implementation of a ThrottleManager.
 * <P>
 * Updated by Andrew Crosland February 2012 to enable 28 step speed packets</P>
 *
 * @author	Andrew Crosland Copyright (C) 2006, 2012
 * @version $Revision$
 */
public class SprogCSThrottleManager extends AbstractThrottleManager {

    /**
     * Constructor.
     */
    public SprogCSThrottleManager(SprogSystemConnectionMemo memo) {
        super(memo);
    }

    public void requestThrottleSetup(LocoAddress a, boolean control) {
        // The SPROG protocol doesn't require an interaction with the command
        // station for this, so immediately trigger the callback
        DccLocoAddress address = (DccLocoAddress) a;
        log.debug("new SprogThrottle for " + address);
        notifyThrottleKnown(new SprogCSThrottle((SprogSystemConnectionMemo) adapterMemo, address), address);
    }

    /**
     * What speed modes are supported by this system? value should be or of
     * possible modes specified by the DccThrottle interface
     */
    public int supportedSpeedModes() {
        return (DccThrottle.SpeedStepMode128 | DccThrottle.SpeedStepMode28);
    }

    /**
     * Addresses 0-10239 can be long
     *
     */
    public boolean canBeLongAddress(int address) {
        return ((address >= 0) && (address <= 10239));
    }

    /**
     * The short addresses 1-127 are available
     *
     */
    public boolean canBeShortAddress(int address) {
        return ((address >= 1) && (address <= 127));
    }

    /**
     * Are there any ambiguous addresses (short vs long) on this system?
     */
    public boolean addressTypeUnique() {
        return false;
    }

    public boolean disposeThrottle(jmri.DccThrottle t, jmri.ThrottleListener l) {
        if (super.disposeThrottle(t, l)) {
            SprogCSThrottle lnt = (SprogCSThrottle) t;
            lnt.throttleDispose();
            return true;
        }
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(SprogCSThrottleManager.class.getName());

}
