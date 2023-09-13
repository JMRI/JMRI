package jmri.jmrit.throttle;

import java.util.EventListener;
import jmri.DccThrottle;
import jmri.LocoAddress;

/**
 * Interface for classes that wish to get notification that a new decoder
 * address has been selected.
 *
 * @author glen Copyright (C) 2002
 */
public interface AddressListener extends EventListener {

    /**
     * Receive notification that a new address has been selected.
     *
     * @param address The address that is now selected.
     */
    void notifyAddressChosen(jmri.LocoAddress address);

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
    void notifyConsistAddressChosen(jmri.LocoAddress address);

    /**
     * Receive notification that a consist address has been released/dispatched.
     *
     * @param address The address that is now selected.
     */
    void notifyConsistAddressReleased(jmri.LocoAddress address);

    /**
     * Receive notification that a throttle has been found
     *
     * @param throttle The throttle
     */
    void notifyConsistAddressThrottleFound(DccThrottle throttle);
}
