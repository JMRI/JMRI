package jmri;

import java.util.EventListener;

/**
 * A listener interface for a class requesting a DccThrottle from
 * the ThrottleManager.
 */
public interface ThrottleListener extends EventListener
{
    /**
     * Get notification that a throttle has been found as you requested.
     * @param t An instantiation of the DccThrottle with the address requested.
     */
    public void notifyThrottleFound(DccThrottle t);
}