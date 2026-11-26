package jmri.time;

/**
 * Set the rate of the time provider.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public interface RateSetter {

    /**
     * Set the rate of the time provider.
     * @param rate the rate
     * @throws UnsupportedOperationException if the rate couldn't be set
     */
    void setRate(double rate) throws UnsupportedOperationException;

}
