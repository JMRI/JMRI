package jmri.time;

/**
 * Can the rate of the time provider be set?
 *
 * This is separated from {@link RateSetter} since the method
 * {@link #canSetRate()} needs to be in the class implementing
 * {@link TimeProvider}, not the class implementing {@link Rate}
 * and {@link RateSetter}.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public interface CanSetRate {

    /**
     * Can the rate be set?
     * @return true if the rate can be set, false otherwise
     */
    boolean canSetRate();

}
