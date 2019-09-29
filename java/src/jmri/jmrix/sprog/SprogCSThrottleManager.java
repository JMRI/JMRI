package jmri.jmrix.sprog;

import java.util.EnumSet;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SPROG Command Station implementation of a ThrottleManager.
 * <p>
 * Updated by Andrew Crosland February 2012 to enable 28 step speed packets
 *
 * @author	Andrew Crosland Copyright (C) 2006, 2012
 */
public class SprogCSThrottleManager extends AbstractThrottleManager {

    /**
     * Constructor.
     */
    public SprogCSThrottleManager(SprogSystemConnectionMemo memo) {
        super(memo);
    }

    @Override
    public void requestThrottleSetup(LocoAddress a, boolean control) {
        if (a instanceof DccLocoAddress ) {
            // Although DCCLocoAddress not enforced in SprogCSThrottle constructor,
            // is required in the construction.
            
            // The SPROG protocol doesn't require an interaction with the command
            // station for this, so immediately trigger the callback
            log.debug("new SprogThrottle for " + a);
            notifyThrottleKnown(new SprogCSThrottle((SprogSystemConnectionMemo) adapterMemo, a), a);
        }
        else {
            log.error("{} is not a DccLocoAddress",a);
            failedThrottleRequest(a, "LocoAddress " +a+ " is not a DccLocoAddress");
        }
    }

    /**
     * What speed modes are supported by this system? value should be or of
     * possible modes specified by the DccThrottle interface
     */
    @Override
    public EnumSet<SpeedStepMode> supportedSpeedModes() {
        return EnumSet.of(SpeedStepMode.NMRA_DCC_128, SpeedStepMode.NMRA_DCC_28);
    }

    /**
     * Addresses 0-10239 can be long
     *
     */
    @Override
    public boolean canBeLongAddress(int address) {
        return ((address >= 0) && (address <= 10239));
    }

    /**
     * The short addresses 1-127 are available
     *
     */
    @Override
    public boolean canBeShortAddress(int address) {
        return ((address >= 1) && (address <= 127));
    }

    /**
     * Are there any ambiguous addresses (short vs long) on this system?
     */
    @Override
    public boolean addressTypeUnique() {
        return false;
    }

    @Override
    public boolean disposeThrottle(jmri.DccThrottle t, jmri.ThrottleListener l) {
        if (super.disposeThrottle(t, l)) {
            if (t instanceof SprogCSThrottle) {
                SprogCSThrottle lnt = (SprogCSThrottle) t;
                lnt.throttleDispose();
                return true;
            }
        }
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(SprogCSThrottleManager.class);

}
