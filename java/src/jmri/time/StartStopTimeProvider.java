package jmri.time;

/**
 * Start/stop the time provider.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public interface StartStopTimeProvider {

    /**
     * Start the the time provider.
     * @throws UnsupportedOperationException if the clock couldn't be started
     */
    void start() throws UnsupportedOperationException;

    /**
     * Stop the the time provider.
     * @throws UnsupportedOperationException if the clock couldn't be stopped
     */
    void stop() throws UnsupportedOperationException;

    /**
     * Can this time provider be started and stopped?
     * @return true if the clock can be started and stopped, false otherwise
     */
    boolean canStartAndStop();

}
