package jmri.beans;

import java.beans.VetoableChangeListener;
import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;

/**
 * A set of methods that would need to be implemented to ensure the implementing
 * class provides a complete external interface for vetoable property changes.
 * This interface is merely a convenience for developers to ensure support for
 * vetoable property change listening is thorough, if not complete. Developers
 * of classes implementing this interface still need to ensure that
 * {@link java.beans.VetoableChangeListener}s are queried and that
 * {@link java.beans.PropertyChangeEvent}s are fired when properties are set.
 *
 * {@link jmri.beans.ConstrainedArbitraryBean} and
 * {@link jmri.beans.ConstrainedBean} provide complete implementations of this
 * interface.
 *
 * This interface defines all public methods of
 * {@link java.beans.VetoableChangeSupport} except the methods to fire
 * PropertyChangeEvents.
 *
 * @author Randall Wood
 */
public interface VetoableChangeProvider {

    /**
     * Add a {@link java.beans.VetoableChangeListener} to the listener list.
     *
     * @param listener The VetoableChangeListener to be added
     */
    public void addVetoableChangeListener(@CheckForNull VetoableChangeListener listener);

    /**
     * Add a {@link java.beans.VetoableChangeListener} for a specific property.
     *
     * @param propertyName The name of the property to listen on.
     * @param listener     The VetoableChangeListener to be added
     */
    public void addVetoableChangeListener(@CheckForNull String propertyName, @CheckForNull VetoableChangeListener listener);

    /**
     * Get all {@link java.beans.VetoableChangeListener}s currently attached to
     * this object.
     *
     * @return An array of VetoableChangeListeners.
     */
    @Nonnull
    public VetoableChangeListener[] getVetoableChangeListeners();

    /**
     * Get all {@link java.beans.VetoableChangeListener}s currently listening to
     * changes to the specified property.
     *
     * @param propertyName The name of the property of interest
     * @return An array of VetoableChangeListeners.
     */
    @Nonnull
    public VetoableChangeListener[] getVetoableChangeListeners(@CheckForNull String propertyName);

    /**
     * Remove the specified listener from this object.
     *
     * @param listener The {@link java.beans.VetoableChangeListener} to remove.
     */
    public void removeVetoableChangeListener(@CheckForNull VetoableChangeListener listener);

    /**
     * Remove the specified listener of the specified property from this object.
     *
     * @param propertyName The name of the property to stop listening to.
     * @param listener     The {@link java.beans.VetoableChangeListener} to
     *                     remove.
     */
    public void removeVetoableChangeListener(@CheckForNull String propertyName, @CheckForNull VetoableChangeListener listener);

}
