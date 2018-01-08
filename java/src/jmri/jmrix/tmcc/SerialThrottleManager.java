package jmri.jmrix.tmcc;

import jmri.DccLocoAddress;
import jmri.LocoAddress;
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
     * Constructor.
     */
    public SerialThrottleManager(TmccSystemConnectionMemo memo) {
        super(memo);
        _memo = memo;
        userName = "Lionel TMCC";
    }

    @Override
    public void requestThrottleSetup(LocoAddress a, boolean control) {
        // the protocol doesn't require an interaction with the command
        // station for this, so immediately trigger the callback.
        DccLocoAddress address = (DccLocoAddress) a;
        log.debug("new throttle for {}", address);
        notifyThrottleKnown(new SerialThrottle(_memo, address), address);
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
     * @return current connection instance
     * @deprecated JMRI Since 4.4 instance() shouldn't be used
     */
    @Deprecated
    static public SerialThrottleManager instance() {
        log.warn("deprecated instance() call for TMCC SerialThrottleManager");
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialThrottleManager.class);

}
