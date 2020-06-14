package jmri.jmrix.lenz.li100;

import jmri.jmrix.lenz.XNetInitializationManager;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;

/**
 * This class performs Command Station dependent initialization for XpressNet.
 * It adds the appropriate Managers via the Initialization Manager based on the
 * Command Station Type.
 *
 * @author Paul Bender Copyright (C) 2003
 * @deprecated since 4.21.1.  Use {@link XNetInitializationManager} instead.
 */
@Deprecated
public class LI100XNetInitializationManager extends XNetInitializationManager {


    public LI100XNetInitializationManager(XNetSystemConnectionMemo memo){
        memo(memo);
        setDefaults();
        setTimeout(getInitTimeout());
        versionCheck();
        programmer(LI100XNetProgrammer.class);
        init();
    }
}
