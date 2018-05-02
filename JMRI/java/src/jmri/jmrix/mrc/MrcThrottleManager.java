package jmri.jmrix.mrc;

import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.jmrix.AbstractThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MRC implementation of a ThrottleManager.
 * <P>
 * @author Bob Jacobsen Copyright (C) 2001
 * 
 */
public class MrcThrottleManager extends AbstractThrottleManager {

    /**
     * Throttle Manager Constructor.
     * @param memo system connection memo
     */
    public MrcThrottleManager(MrcSystemConnectionMemo memo) {
        super(memo);
        this.tc = memo.getMrcTrafficController();
        this.prefix = memo.getSystemPrefix();
    }

    MrcTrafficController tc = null;
    String prefix = "";

    @Override
    public void requestThrottleSetup(LocoAddress a, boolean control) {
        //We do interact
        DccLocoAddress address = (DccLocoAddress) a;
        log.debug("new MrcThrottle for " + address); //IN18N
        notifyThrottleKnown(new MrcThrottle((MrcSystemConnectionMemo) adapterMemo, address), address);
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
    public int supportedSpeedModes() {
        return (DccThrottle.SpeedStepMode128 | DccThrottle.SpeedStepMode28);
    }

    @Override
    public boolean disposeThrottle(jmri.DccThrottle t, jmri.ThrottleListener l) {
        if (super.disposeThrottle(t, l)) {
            MrcThrottle nct = (MrcThrottle) t;
            nct.throttleDispose();
            return true;
        }
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(MrcThrottleManager.class);

}
