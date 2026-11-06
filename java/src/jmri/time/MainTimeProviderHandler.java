package jmri.time;

import java.beans.PropertyChangeListener;

import javax.annotation.Nonnull;

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
     * Set whenether the primary or secondary time provider should be used.
     * @param select true to select the primary time provider, false to select the
     *               secondary time provider.
     */
    void setUsePrimaryTimeProvider(boolean select);

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

    /**
     * Request a call-back when a bound property changes. Bound properties are
     * the known state, commanded state, user and system names.
     *
     * @param listener    The listener. This may change in the future to be a
     *                        subclass of NamedProprtyChangeListener that
     *                        carries the name and listenerRef values internally
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Request a call-back when a bound property changes. Bound properties are
     * the known state, commanded state, user and system names.
     *
     * @param propertyName The name of the property to listen to
     * @param listener     The listener. This may change in the future to be a
     *                         subclass of NamedProprtyChangeListener that
     *                         carries the name and listenerRef values
     *                         internally
     */
    void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * Remove the specified listener from this object.
     *
     * @param listener The {@link java.beans.PropertyChangeListener} to remove.
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove the specified listener of the specified property from this object.
     *
     * @param propertyName The name of the property to stop listening to.
     * @param listener     The {@link java.beans.PropertyChangeListener} to
     *                     remove.
     */
    void removePropertyChangeListener(String propertyName,
            PropertyChangeListener listener);

    /**
     * Get all {@link java.beans.PropertyChangeListener}s currently listening.
     *
     * @return an array of PropertyChangeListeners
     */
    @Nonnull
    PropertyChangeListener[] getPropertyChangeListeners();

    /**
     * Get all {@link java.beans.PropertyChangeListener}s currently listening to
     * changes to the specified property.
     *
     * @param propertyName the name of the property of interest
     * @return an array of PropertyChangeListeners
     */
    @Nonnull
    PropertyChangeListener[] getPropertyChangeListeners(String propertyName);

}
