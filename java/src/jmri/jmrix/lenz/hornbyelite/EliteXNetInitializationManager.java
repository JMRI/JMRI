package jmri.jmrix.lenz.hornbyelite;

import jmri.jmrix.lenz.*;

/**
 * This class performs Command Station dependant initialization for the Hornby
 * Elite. It adds the appropriate Managers via the Initialization Manager based
 * on the Command Station Type.
 *
 * @author Paul Bender Copyright (C) 2003,2008
 * @deprecated since 4.21.1.  Use {@link XNetInitializationManager} instead.
 */
@Deprecated
public class EliteXNetInitializationManager extends XNetInitializationManager {

    public EliteXNetInitializationManager(XNetSystemConnectionMemo memo) {
                memo(memo);
                powerManager(XNetPowerManager.class);
                throttleManager(EliteXNetThrottleManager.class);
                programmer(EliteXNetProgrammer.class);
                programmerManager(XNetProgrammerManager.class);
                turnoutManager(EliteXNetTurnoutManager.class);
                lightManager(XNetLightManager.class);
                init();
    }
}
