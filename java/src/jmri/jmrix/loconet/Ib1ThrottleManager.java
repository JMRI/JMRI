package jmri.jmrix.loconet;

import jmri.DccThrottle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specialization of a ThrottleManager for the Intellibox.
 *
 * @author Bob Jacobsen Copyright (C) 2014
 */
public class Ib1ThrottleManager extends jmri.jmrix.loconet.LnThrottleManager {

    /**
     * Constructor.
     */
    public Ib1ThrottleManager(LocoNetSystemConnectionMemo memo) {
        super(memo);
        log.debug("Ib2ThrottleManager created");
    }

    @Override
    DccThrottle createThrottle(LocoNetSystemConnectionMemo memo, LocoNetSlot s) {
        return new Ib1Throttle(memo, s);
    }

    private final static Logger log = LoggerFactory.getLogger(Ib1ThrottleManager.class);
}
