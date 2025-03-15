package jmri.time;

import java.time.LocalDateTime;

import jmri.NamedBean;

/**
 * A time provider.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public interface TimeProvider extends NamedBean {

    /**
     * Get the time object of this time provider. If the time provider implements the
     * {@code DateSupport} interface, this method will also return some kind
     * of date.
     * @return the time and possibly date.
     */
    LocalDateTime getTime();

    /**
     * Get the rate object of this time provider.
     * @return the Rate object.
     */
    Rate getRate();

    /**
     * Is the time provider running?
     * @return true if running, false if stopped.
     */
    boolean isRunning();

}
