package jmri.jmrix.lenz.hornbyelite;

import jmri.LocoAddress;
import jmri.ThrottleManager;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XNet implementation of a ThrottleManager based on the
 * AbstractThrottleManager.
 *
 * @author Paul Bender Copyright (C) 2008
 */
public class EliteXNetThrottleManager extends jmri.jmrix.lenz.XNetThrottleManager {

    /**
     * Constructor.
     */
    public EliteXNetThrottleManager(XNetSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * Request a new throttle object be created for the address, and let the
     * throttle listeners know about it.
     */
    @Override
    public void requestThrottleSetup(LocoAddress address, boolean control) {
        EliteXNetThrottle throttle;
        log.debug("Requesting Throttle: {}", address);
        if (throttles.containsKey(address)) {
            notifyThrottleKnown(throttles.get(address), address);
        } else {
            throttle = new EliteXNetThrottle((XNetSystemConnectionMemo) adapterMemo, address, tc);
            throttles.put(address, throttle);
            notifyThrottleKnown(throttle, address);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(EliteXNetThrottleManager.class);

}
