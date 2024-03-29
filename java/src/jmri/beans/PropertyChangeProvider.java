package jmri.beans;

import java.beans.PropertyChangeListener;
import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;

/**
 * A set of methods that would need to be implemented to ensure the implementing
 * class provides a complete external interface for property changes. This
 * interface is merely a convenience for developers to ensure support for
 * property change listening is thorough, if not complete. Developers of classes
 * implementing this interface still need to ensure that
 * {@link java.beans.PropertyChangeEvent}s are fired when properties are set.
 * <p>
 * {@link ArbitraryBean}, {@link Bean}, {@link ConstrainedBean},
 * {@link PropertyChangeSupport}, and {@link VetoableChangeSupport} all provide
 * complete implementations of this interface.
 * <p>
 * This interface defines all public methods of
 * {@link java.beans.PropertyChangeSupport} except the methods to fire
 * PropertyChangeEvents so that a consumer of an implementing class can be sure
 * that it can listen for a property change.
 *
 * @author Randall Wood
 */
public interface PropertyChangeProvider {

    /**
     * Add a {@link java.beans.PropertyChangeListener} to the listener list.
     *
     * @param listener The PropertyChangeListener to be added
     */
    void addPropertyChangeListener(@CheckForNull PropertyChangeListener listener);

    /**
     * Add a {@link java.beans.PropertyChangeListener} for a specific property.
     *
     * @param propertyName The name of the property to listen on.
     * @param listener     The PropertyChangeListener to be added
     */
    void addPropertyChangeListener(@CheckForNull String propertyName,
            @CheckForNull PropertyChangeListener listener);

    /**
     * Get all {@link java.beans.PropertyChangeListener}s currently attached to
     * this object.
     *
     * @return An array of PropertyChangeListeners.
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
    PropertyChangeListener[] getPropertyChangeListeners(@CheckForNull String propertyName);

    /**
     * Remove the specified listener from this object.
     *
     * @param listener The {@link java.beans.PropertyChangeListener} to remove.
     */
    void removePropertyChangeListener(@CheckForNull PropertyChangeListener listener);

    /**
     * Remove the specified listener of the specified property from this object.
     *
     * @param propertyName The name of the property to stop listening to.
     * @param listener     The {@link java.beans.PropertyChangeListener} to
     *                     remove.
     */
    void removePropertyChangeListener(@CheckForNull String propertyName,
            @CheckForNull PropertyChangeListener listener);

}
