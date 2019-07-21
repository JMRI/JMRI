package jmri.jmrix.nce;

import java.util.EnumSet;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NCE implementation of a ThrottleManager.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class NceThrottleManager extends AbstractThrottleManager {

    /**
     * Constructor.
     * @param memo system connection memo
     */
    public NceThrottleManager(NceSystemConnectionMemo memo) {
        super(memo);
        this.tc = memo.getNceTrafficController();
        this.prefix = memo.getSystemPrefix();
    }

    NceTrafficController tc = null;
    String prefix = "";

    @Override
    public void requestThrottleSetup(LocoAddress a, boolean control) {
        if (a instanceof DccLocoAddress ) {
            // the NCE protocol doesn't require an interaction with the command
            // station for this, so immediately trigger the callback.
            DccLocoAddress address = (DccLocoAddress) a;
            log.debug("new NceThrottle for " + address);
            notifyThrottleKnown(new NceThrottle((NceSystemConnectionMemo) adapterMemo, address), address);
        }
        else {
            log.error("{} is not a DccLocoAddress",a);
            failedThrottleRequest(a, "LocoAddress " +a+ " is not a DccLocoAddress");
        }
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
    public EnumSet<SpeedStepMode> supportedSpeedModes() {
        return EnumSet.of(SpeedStepMode.NMRA_DCC_128, SpeedStepMode.NMRA_DCC_28);
    }

    @Override
    public boolean disposeThrottle(jmri.DccThrottle t, jmri.ThrottleListener l) {
        if (super.disposeThrottle(t, l)) {
            if (t instanceof NceThrottle) {
                NceThrottle nct = (NceThrottle) t;
                nct.throttleDispose();
                return true;
            }
        }
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(NceThrottleManager.class);

}
