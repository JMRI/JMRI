package jmri.jmrix.roco;

import jmri.LocoAddress;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Roco XNet implementation of a ThrottleManager based on the
 * AbstractThrottleManager.
 *
 * @author Paul Bender Copyright (C) 2002-2004
 */
public class RocoXNetThrottleManager extends jmri.jmrix.lenz.XNetThrottleManager {

    /**
     * Constructor.
     */
    public RocoXNetThrottleManager(XNetSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * Request a new throttle object be created for the address, and let the
     * throttle listeners know about it.
     */
    @Override
    public void requestThrottleSetup(LocoAddress address, boolean control) {
        // range check for LocoMaus II
        if (tc.getCommandStation().getCommandStationType() == 0x04 ) {
            if(address.getNumber()>=100) {
               String typeString = Bundle.getMessage("CSTypeLokMaus");
               failedThrottleRequest(address,Bundle.getMessage("ThrottleErrorCSTwoDigit",typeString));
               return;
            }
        }
        RocoXNetThrottle throttle;
        if (log.isDebugEnabled()) {
            log.debug("Requesting Throttle: " + address);
        }
        if (throttles.containsKey(address)) {
            notifyThrottleKnown(throttles.get(address), address);
        } else {
            throttle = new RocoXNetThrottle((XNetSystemConnectionMemo) adapterMemo, address, tc);
            throttles.put(address, throttle);
            notifyThrottleKnown(throttle, address);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(RocoXNetThrottleManager.class);

}
