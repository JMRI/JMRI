package jmri.jmrit.withrottle;

import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.ThrottleListener;
import jmri.jmrit.roster.RosterEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Brett Hoffman Copyright (C) 2010, 2011
 */
public class ConsistFunctionController implements ThrottleListener {

    private DccThrottle throttle;
    private RosterEntry rosterLoco = null;
    private ThrottleController throttleController;

    public ConsistFunctionController(ThrottleController tc) {
        throttleController = tc;
    }

    public ConsistFunctionController(ThrottleController tc, RosterEntry re) {
        throttleController = tc;
        rosterLoco = re;
    }

    @Override
    public void notifyThrottleFound(DccThrottle t) {
        if (log.isDebugEnabled()) {
            log.debug("Lead Loco throttle found: " + t
                    + ", for consist: " + throttleController.getCurrentAddressString());
        }
        throttle = t;

        if (rosterLoco == null) {
            rosterLoco = throttleController.findRosterEntry(throttle);
        }

        throttleController.syncThrottleFunctions(throttle, rosterLoco);
        throttleController.setFunctionThrottle(t);
        throttleController.sendFunctionLabels(rosterLoco);
        throttleController.sendAllFunctionStates(throttle);
    }

    @Override
    public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
        log.error("Throttle request failed for " + address + " because " + reason);
    }

    @Override
    public void notifyStealThrottleRequired(LocoAddress address){
        // this is an automatically stealing impelementation.
        InstanceManager.throttleManagerInstance().stealThrottleRequest(address, this, true);
    }

    public void dispose() {
        jmri.InstanceManager.throttleManagerInstance().releaseThrottle(throttle, this);
    }

    public DccThrottle getThrottle() {
        return throttle;
    }

    boolean requestThrottle(DccLocoAddress loco) {
        return jmri.InstanceManager.throttleManagerInstance().requestThrottle(loco.getNumber(), loco.isLongAddress(), this);
    }

    private final static Logger log = LoggerFactory.getLogger(ConsistFunctionController.class);

}
