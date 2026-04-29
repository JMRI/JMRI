package jmri.jmrit.throttle.interfaces;

import java.util.EventListener;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.jmrit.roster.RosterEntry;

/**
 * Interface for classes that wish to get notification that a new decoder
 * address has been selected.
 * 
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author glen Copyright (C) 2002
 * @author Lionel Jeanson 2007-2026
 * 
 */
public interface AddressListener extends EventListener {

    /**
     * Receive notification that a new address has been selected and validated.
     *
     * @param address The address that is now selected and validated.
     */
    void notifyAddressChosen(LocoAddress address);

    /**
     * Receive notification that an address has been released/dispatched
     *
     * @param address The address released/dispatched
     */
    void notifyAddressReleased(LocoAddress address);

    /**
     * Receive notification that a throttle has been found
     *
     * @param throttle The throttle
     */
    void notifyAddressThrottleFound(DccThrottle throttle);

    /**
     * Receive notification that a new Consist address has been selected.
     *
     * @param address The address that is now selected.
     */
    void notifyConsistAddressChosen(LocoAddress address);

    /**
     * Receive notification that a consist address has been released/dispatched.
     *
     * @param address The address that is now selected.
     */
    void notifyConsistAddressReleased(LocoAddress address);

    /**
     * Receive notification that a throttle has been found
     *
     * @param throttle The throttle
     */
    void notifyConsistAddressThrottleFound(DccThrottle throttle);

    /**
     * Receive notification that a new roster entry has been selected (and not validated yet).
     * In the RosterEntry combobox for example.
     *
     * @param re The roster entry that is now selected.
     */
    void notifyRosterEntrySelected(RosterEntry re);
}
