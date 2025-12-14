package jmri.time;

import java.time.LocalTime;

/**
 * Set the time of the time provider.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public interface TimeSetter {

    /**
     * Set the time of the time provider.
     * @param time the time
     * @throws UnsupportedOperationException if the time couldn't be set
     */
    void setTime(LocalTime time) throws UnsupportedOperationException;

    /**
     * Can the time be set?
     * @return true if the time can be set, false otherwise
     */
    boolean canSetTime();

}
