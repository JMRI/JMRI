package jmri.time;

/**
 * The rate.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public interface Rate {

    /**
     * Get the rate.
     * @return the rate
     */
    double getRate();

    /**
     * Return the rate as a formatted string.
     *
     * @return the rate as a string
     */
    String getRateString();

}
