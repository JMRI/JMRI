package jmri.jmrix.internal;

import jmri.time.implementation.DefaultMainTimeProviderHandler;
import jmri.managers.AbstractTimeProviderManager;
import jmri.time.MainTimeProviderHandler;

/**
 * Implement a time provider manager for "Internal" time providers.
 *
 * @author Bob Jacobsen Copyright (C) 2006
 */
public class InternalTimeProviderManager extends AbstractTimeProviderManager {

    private final MainTimeProviderHandler _mainClockHandler = new DefaultMainTimeProviderHandler();

    public InternalTimeProviderManager(InternalSystemConnectionMemo memo) {
        super(memo);
    }

    @Override
    public MainTimeProviderHandler getMainTimeProviderHandler() {
        return _mainClockHandler;
    }


//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InternalClockManager.class);

}
