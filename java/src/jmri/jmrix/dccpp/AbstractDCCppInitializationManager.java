package jmri.jmrix.dccpp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a base implementation for Command Station/interface
 * dependent initialization for DCC++. It adds the appropriate Managers via the
 * Initialization Manager based on the Command Station Type.
 *
 * @author Paul Bender Copyright (C) 2003-2010
 * @author Mark Underwood Copyright (C) 2015
 * @author Harald Barth Copyright (C) 2019
 *
 * Based on AbstractXNetInitializationManager
 */
public abstract class AbstractDCCppInitializationManager {

    protected Thread initThread = null;

    protected DCCppSystemConnectionMemo systemMemo = null;

    public AbstractDCCppInitializationManager(DCCppSystemConnectionMemo memo) {

        log.debug("Starting DCC++ Initialization Process");
        systemMemo = memo;
        // Continue with the non-abstract init
        init();
    }

    protected abstract void init();

    private static final Logger log = LoggerFactory.getLogger(AbstractDCCppInitializationManager.class);

}
