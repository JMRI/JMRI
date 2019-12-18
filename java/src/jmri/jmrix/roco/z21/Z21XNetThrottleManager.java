package jmri.jmrix.roco.z21;

import jmri.LocoAddress;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * z21XNet implementation of a ThrottleManager based on the
 * AbstractThrottleManager.
 *
 * @author Paul Bender Copyright (C) 2002-2004
 */
public class Z21XNetThrottleManager extends jmri.jmrix.roco.RocoXNetThrottleManager {

    /**
     * Constructor.
     */
    public Z21XNetThrottleManager(XNetSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * Request a new throttle object be created for the address, and let the
     * throttle listeners know about it.
     */
    @Override
    public void requestThrottleSetup(LocoAddress address, boolean control) {
        Z21XNetThrottle throttle;
        log.debug("Requesting Throttle: {}",address);
        if (throttles.containsKey(address)) {
            notifyThrottleKnown(throttles.get(address), address);
        } else {
            throttle = new Z21XNetThrottle((XNetSystemConnectionMemo) adapterMemo, address, tc);
            throttles.put(address, throttle);
            notifyThrottleKnown(throttle, address);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(Z21XNetThrottleManager.class);

}
