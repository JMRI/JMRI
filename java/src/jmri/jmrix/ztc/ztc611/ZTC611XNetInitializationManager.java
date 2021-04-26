package jmri.jmrix.ztc.ztc611;

import jmri.jmrix.lenz.*;

/**
 * This class performs Command Station dependant initialization for the ZTC
 * ZTC611. It adds the appropriate Managers via the Instance Manager based
 * on the Command Station Type.
 *
 * @author Paul Bender Copyright (C) 2003, 2008
 * @deprecated since 4.21.1.  Use {@link XNetInitializationManager} instead.
 */
@Deprecated
public class ZTC611XNetInitializationManager extends XNetInitializationManager {

    public ZTC611XNetInitializationManager(XNetSystemConnectionMemo memo) {
        memo(memo);
        setDefaults();
        turnoutManager(ZTC611XNetTurnoutManager.class);
        init();
    }
}
