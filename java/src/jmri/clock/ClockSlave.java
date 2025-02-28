package jmri.clock;

import javax.annotation.Nonnull;

/**
 * A clock that is a slave clock.
 *
 * @author Bob Jacobsen      Copyright (C) 2004, 2007, 2008
 * @author Daniel Bergqvist  Copyright (C) 2025
 */
public interface ClockSlave {

    /**
     * Set the Master Clock Name.
     * @param name master clock name.
     */
    void setMasterName(@Nonnull String name);

    /**
     * Get the Master Clock Name.
     * @return master clock name.
     */
    String getMasterName();

}
