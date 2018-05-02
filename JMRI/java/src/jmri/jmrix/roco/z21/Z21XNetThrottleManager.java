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
public class Z21XNetThrottleManager extends jmri.jmrix.lenz.XNetThrottleManager {

    /**
     * Constructor.
     */
    public Z21XNetThrottleManager(XNetSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * What speed modes are supported by this system? value should be xor of
     * possible modes specifed by the DccThrottle interface.
     * Z21 XpressNet supports 14,28 and 128 speed step modes.
     */
    @Override
    public int supportedSpeedModes() {
        return (jmri.DccThrottle.SpeedStepMode128
                | jmri.DccThrottle.SpeedStepMode28
                | jmri.DccThrottle.SpeedStepMode27
                | jmri.DccThrottle.SpeedStepMode14);
    }

    /**
     * Request a new throttle object be created for the address, and let the
     * throttle listeners know about it.
     */
    @Override
    public void requestThrottleSetup(LocoAddress address, boolean control) {
        Z21XNetThrottle throttle;
        if (log.isDebugEnabled()) {
            log.debug("Requesting Throttle: " + address);
        }
        if (throttles.containsKey(address)) {
            notifyThrottleKnown(throttles.get(address), address);
        } else {
            throttle = new Z21XNetThrottle((XNetSystemConnectionMemo) adapterMemo, address, tc);
            throttles.put(address, throttle);
            notifyThrottleKnown(throttle, address);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Z21XNetThrottleManager.class);

}
