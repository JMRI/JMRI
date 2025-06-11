package jmri.time;

import jmri.Manager;

/**
 * Manager of time providers.
 *
 * The type letter for TimeProvider is 'U', Uhr in German.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public interface TimeProviderManager extends Manager<TimeProvider> {

    /**
     * Get the current time provider.
     * @return the time provider
     */
    TimeProvider getCurrentTimeProvider();

    /**
     * Get the main time provider handler.
     * @return the main time provider handler.
     */
    MainTimeProviderHandler getMainTimeProviderHandler();

}
