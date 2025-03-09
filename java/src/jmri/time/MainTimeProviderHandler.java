package jmri.time;

/**
 * Handle the main time provider in JMRI.
 *
 * The main time provider is the time provider that is currently shown by JMRI
 * unless a specific time provider is configured to use another time provider. The
 * benefit with this interface is that it allows the user to quickly switch
 * between different time provider, for example a fast time provider and the
 * system time provider.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public interface MainTimeProviderHandler {

    /**
     * Get the current time provider.
     * @return the time provider
     */
    TimeProvider getCurrentTimeProvider();

    /**
     * Select the primary or secondary time provider.
     * @param select true to select the primary time provider, false to select the
     *               secondary time provider.
     */
    void selectPrimaryTimeProvider(boolean select);

    /**
     * Get whenether the primary time provider is selected.
     * @return true to select the primary time provider, false to select the secondary
     *         time provider.
     */
    boolean isPrimaryTimeProviderSelected();

    /**
     * Set the primary time provider.
     * @param timeProvider the time provider
     */
    void setPrimaryTimeProvider(TimeProvider timeProvider);

    /**
     * Get the primary time provider.
     * @return time provider the time provider
     */
    TimeProvider getPrimaryTimeProvider();

    /**
     * Set the secondary time provider.
     * @param timeProvider the time provider
     */
    void setSecondaryTimeProvider(TimeProvider timeProvider);

    /**
     * Get the secondary time provider.
     * @return time provider the time provider
     */
    TimeProvider getSecondaryTimeProvider();

}
