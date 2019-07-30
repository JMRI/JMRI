package jmri.jmrix.tmcc;

import java.util.EnumSet;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a TMCC ThrottleManager.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006
 */
public class SerialThrottleManager extends AbstractThrottleManager {

    private TmccSystemConnectionMemo _memo = null;

    /**
     * Create a throttle manager.
     *
     * @param memo the memo for the connection this will use
     */
    public SerialThrottleManager(TmccSystemConnectionMemo memo) {
        super(memo);
        _memo = memo;
        userName = "Lionel TMCC";
    }

    @Override
    public void requestThrottleSetup(LocoAddress a, boolean control) {
        if (a instanceof DccLocoAddress ) {
            // the protocol doesn't require an interaction with the command
            // station for this, so immediately trigger the callback.
            DccLocoAddress address = (DccLocoAddress) a;
            log.debug("new throttle for {}", address);
            notifyThrottleKnown(new SerialThrottle(_memo, address), address);
        }
        else {
            log.error("{} is not a DccLocoAddress",a);
            failedThrottleRequest(a, "LocoAddress " +a+ " is not a DccLocoAddress");
        }
    }

    /**
     * Address 1 and above can be long.
     */
    @Override
    public boolean canBeLongAddress(int address) {
        return (address >= 1);
    }

    /**
     * The full range of short addresses are available.
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

        /**
     * What speed modes are supported by this system? value should be xor of
     * possible modes specifed by the DccThrottle interface
     */
    @Override
    public EnumSet<SpeedStepMode> supportedSpeedModes() {
        return EnumSet.of(SpeedStepMode.TMCC_32);
    }

    private final static Logger log = LoggerFactory.getLogger(SerialThrottleManager.class);

}
