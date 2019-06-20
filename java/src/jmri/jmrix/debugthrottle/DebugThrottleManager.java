package jmri.jmrix.debugthrottle;

import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.jmrix.AbstractThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a ThrottleManager for debugging.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2005
 */
public class DebugThrottleManager extends AbstractThrottleManager {

    public DebugThrottleManager() {
        super();
    }

    /**
     * Constructor.
     */
    public DebugThrottleManager(jmri.jmrix.SystemConnectionMemo memo) {
        super(memo);
    }

    @Override
    public void requestThrottleSetup(LocoAddress a, boolean control) {
        if (a instanceof DccLocoAddress) {
            // Immediately trigger the callback.
            DccLocoAddress address = (DccLocoAddress) a;
            log.debug("new debug throttle for " + address);
            notifyThrottleKnown(new DebugThrottle(address, adapterMemo), a);
        }
        else {
            log.error("LocoAddress {} is not a DccLocoAddress",a);
        }
    }

    /**
     * Address 1 and above can be a long address
     *
     */
    @Override
    public boolean canBeLongAddress(int address) {
        return (address >= 1);
    }

    /**
     * Address 127 and below can be a short address
     *
     */
    @Override
    public boolean canBeShortAddress(int address) {
        return (address <= 127);
    }

    /**
     * Are there any ambiguous addresses (short vs long) on this system?
     */
    @Override
    public boolean addressTypeUnique() {
        return false;
    }

    @Override
    public boolean disposeThrottle(DccThrottle t, jmri.ThrottleListener l) {
        log.debug("disposeThrottle called for " + t);
        if (super.disposeThrottle(t, l)) {
            if (t instanceof DebugThrottle) {
                DebugThrottle lnt = (DebugThrottle) t;
                lnt.throttleDispose();
                return true;
            }
            else {
                log.error("DccThrottle {} is not a DebugThrottle",t);
            }
        }
        return false;
    }

    /**
     * What speed modes are supported by this system? value should be xor of
     * possible modes specified by the DccThrottle interface
     */
    @Override
    public int supportedSpeedModes() {
        return (DccThrottle.SpeedStepMode128
                | DccThrottle.SpeedStepMode28
                | DccThrottle.SpeedStepMode27
                | DccThrottle.SpeedStepMode14);
    }

    private final static Logger log = LoggerFactory.getLogger(DebugThrottleManager.class);

}
