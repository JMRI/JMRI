package jmri;

import javax.annotation.Nonnull;

/**
 * A limit on the speed that throttles may command for a locomotive.
 * <p>
 * When one is registered as the default in the {@link InstanceManager}, every
 * throttle built on {@link jmri.jmrix.AbstractThrottle} asks it for the
 * locomotive's limit before it sends a speed command to the layout, and sends
 * no more than that limit. A stop or emergency stop is always sent unchanged.
 * When none is registered, which is the case unless something registers one,
 * speed commands are sent exactly as they are set.
 * <p>
 * Throttles call {@link #getSpeedLimit(LocoAddress)} on the thread that sets
 * the speed, which may be any thread, often while holding the throttle's own
 * lock. An implementation must therefore be thread safe, must return quickly,
 * and must not call back into a throttle.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public interface ThrottleSpeedLimiter {

    /**
     * The highest speed a throttle may command for a locomotive.
     *
     * @param address the locomotive's address
     * @return the limit, from 0 (stopped) to 1 (full speed); 1 or more, or
     *         {@link Float#NaN}, for no limit
     */
    float getSpeedLimit(@Nonnull LocoAddress address);

}
