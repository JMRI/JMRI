package jmri.clock;

import jmri.NamedBean;

/**
 * A clock.
 *
 * @author Bob Jacobsen      Copyright (C) 2004, 2007, 2008
 * @author Daniel Bergqvist  Copyright (C) 2025
 */
public interface Clock extends NamedBean {

    /**
     * Initialise the clock.
     * Should only be invoked at start up.
     */
    void initializeClock();

    /**
     * @return true if call to initialize Hardware Clock has occurred
     */
    boolean getIsInitialized();

    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     */
    @Override
    void dispose();

}
