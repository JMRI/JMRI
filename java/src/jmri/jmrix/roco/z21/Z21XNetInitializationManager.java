package jmri.jmrix.roco.z21;

import jmri.jmrix.lenz.*;

/**
 * This class performs Command Station dependant initialization for the Roco
 * z21/Z21 XpressNet implementation. It adds the appropriate Managers via the
 * Initialization Manager based on the Command Station Type.
 *
 * @author Paul Bender Copyright (C) 2015
 * @deprecated since 4.21.1.  Use {@link XNetInitializationManager} instead.
 */
@Deprecated
public class Z21XNetInitializationManager extends XNetInitializationManager {

    public Z21XNetInitializationManager(XNetSystemConnectionMemo memo) {
        memo(memo);
        setDefaults();
        throttleManager(Z21XNetThrottleManager.class);
        programmer(Z21XNetProgrammer.class);
        programmerManager(Z21XNetProgrammerManager.class);
        turnoutManager(Z21XNetTurnoutManager.class);
        consistManager(null);
        noCommandStation();
        init();
    }
}
