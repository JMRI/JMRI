package jmri.jmrix.zimo;

import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.jmrix.AbstractThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MRC implementation of a ThrottleManager.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 *
 */
public class Mx1ThrottleManager extends AbstractThrottleManager {

    /**
     * Create a new manager.
     *
     * @param memo the system connection this manager is associated with
     */
    public Mx1ThrottleManager(Mx1SystemConnectionMemo memo) {
        super(memo);
        this.tc = memo.getMx1TrafficController();
        this.prefix = memo.getSystemPrefix();
    }

    Mx1TrafficController tc = null;
    String prefix = "";

    @Override
    public void requestThrottleSetup(LocoAddress a, boolean control) {
        //We do interact
        DccLocoAddress address = (DccLocoAddress) a;
        log.debug("new Mx1Throttle for " + address); //IN18N
        notifyThrottleKnown(new Mx1Throttle((Mx1SystemConnectionMemo) adapterMemo, address), address);
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
            Mx1Throttle nct = (Mx1Throttle) t;
            nct.throttleDispose();
            return true;
        }
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(Mx1ThrottleManager.class);

}
