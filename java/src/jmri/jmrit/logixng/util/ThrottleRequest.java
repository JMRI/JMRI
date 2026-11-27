package jmri.jmrit.logixng.util;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.LocoAddress;
import jmri.ThrottleListener;
import jmri.ThrottleManager;

/**
 * Requests a throttle for a loco address and an optional protocol, for the
 * LogixNG throttle actions.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public final class ThrottleRequest {

    private ThrottleRequest() {
        // static methods only
    }

    /**
     * Request a throttle.
     * <p>
     * With no protocol, the throttle manager chooses the protocol for the
     * address number, as {@link ThrottleManager#requestThrottle(int, ThrottleListener)}
     * does. With a protocol, the throttle manager makes the address for that
     * protocol with {@link ThrottleManager#getAddress(String, LocoAddress.Protocol)},
     * so that, for example, a Marklin connection drives a Motorola or MFX loco.
     *
     * @param manager  the throttle manager
     * @param address  the loco address number
     * @param protocol the protocol, or null to let the throttle manager choose
     * @param listener the listener to tell when the throttle is found
     * @return true if the request was made, false if not
     */
    public static boolean requestThrottle(@Nonnull ThrottleManager manager, int address,
            @CheckForNull LocoAddress.Protocol protocol, @Nonnull ThrottleListener listener) {
        if (protocol == null) {
            return manager.requestThrottle(address, listener);
        }
        LocoAddress locoAddress = manager.getAddress(Integer.toString(address), protocol);
        if (locoAddress == null) {
            log.warn("No {} address {} on this throttle connection", protocol.getPeopleName(), address);
            return false;
        }
        return manager.requestThrottle(locoAddress, listener, false);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ThrottleRequest.class);

}
