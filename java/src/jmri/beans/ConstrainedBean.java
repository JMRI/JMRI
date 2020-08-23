package jmri.beans;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

/**
 * A Bean with support for {@link java.beans.VetoableChangeListener}s.
 *
 * @author Randall Wood
 */
public abstract class ConstrainedBean extends Bean implements VetoableChangeProvider {

    protected final VetoableChangeSupport vetoableChangeSupport = new VetoableChangeSupport(this);

    @Override
    public void setProperty(String key, Object value) {
        try {
            this.fireVetoableChange(key, getProperty(key), value);
            super.setProperty(key, value);
        } catch (PropertyVetoException ex) {
            // fire a property change that does not have the new value to indicate
            // to any other listeners that the property was "reset" back to its
            // original value as a result of the veto
            this.firePropertyChange(key, getProperty(key), getProperty(key));
        }
    }

    @Override
    public void setIndexedProperty(String key, int index, Object value) {
        try {
            Object old = this.getIndexedPropertyOrNull(key, index);
            this.fireVetoableChange(new IndexedPropertyChangeEvent(this, key, old, value, index));
            super.setIndexedProperty(key, index, value);
        } catch (PropertyVetoException ex) {
            // fire a property change that does not have the new value to indicate
            // to any other listeners that the property was "reset" back to its
            // original value as a result of the veto
            this.fireIndexedPropertyChange(key, index, getProperty(key), getProperty(key));
        }
    }

    @Override
    public void addVetoableChangeListener(VetoableChangeListener listener) {
        this.vetoableChangeSupport.addVetoableChangeListener(listener);
    }

    @Override
    public void addVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        this.vetoableChangeSupport.addVetoableChangeListener(propertyName, listener);
    }

    @Override
    public VetoableChangeListener[] getVetoableChangeListeners() {
        return this.vetoableChangeSupport.getVetoableChangeListeners();
    }

    @Override
    public VetoableChangeListener[] getVetoableChangeListeners(String propertyName) {
        return this.vetoableChangeSupport.getVetoableChangeListeners(propertyName);
    }

    @Override
    public void removeVetoableChangeListener(VetoableChangeListener listener) {
        this.vetoableChangeSupport.removeVetoableChangeListener(listener);
    }

    @Override
    public void removeVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        this.vetoableChangeSupport.removeVetoableChangeListener(propertyName, listener);
    }

    /**
     * Fire a vetoable property change on the current thread. Use
     * {@link java.beans.VetoableChangeSupport#fireVetoableChange(java.beans.PropertyChangeEvent)}
     * directly to fire this notification on another thread. If a
     * PropertyVetoException is thrown, ensure the property change does not
     * complete.
     *
     * @param event {@link PropertyChangeEvent} to be fired
     * @throws PropertyVetoException if property update vetoed
     */
    public void fireVetoableChange(PropertyChangeEvent event) throws PropertyVetoException {
        this.vetoableChangeSupport.fireVetoableChange(event);
    }

    /**
     * Fire a vetoable property change on the current thread. Use
     * {@link java.beans.VetoableChangeSupport#fireVetoableChange(java.lang.String, java.lang.Object, java.lang.Object)}
     * directly to fire this notification on another thread. If a
     * PropertyVetoException is thrown, ensure the property change does not
     * complete.
     *
     * @param propertyName property that is about to change
     * @param oldValue     old value of the property
     * @param newValue     new value of the property
     * @throws PropertyVetoException if property update vetoed
     */
    public void fireVetoableChange(String propertyName, Object oldValue, Object newValue) throws PropertyVetoException {
        this.vetoableChangeSupport.fireVetoableChange(propertyName, oldValue, newValue);
    }

    /**
     * Fire a vetoable property change on the current thread. Use
     * {@link java.beans.VetoableChangeSupport#fireVetoableChange(java.lang.String, int, int)}
     * directly to fire this notification on another thread. If a
     * PropertyVetoException is thrown, ensure the property change does not
     * complete.
     *
     * @param propertyName property that is about to change
     * @param oldValue     old value of the property
     * @param newValue     new value of the property
     * @throws PropertyVetoException if property update vetoed
     */
    public void fireVetoableChange(String propertyName, int oldValue, int newValue) throws PropertyVetoException {
        this.vetoableChangeSupport.fireVetoableChange(propertyName, oldValue, newValue);
    }

    /**
     * Fire a vetoable property change on the current thread. Use
     * {@link java.beans.VetoableChangeSupport#fireVetoableChange(java.lang.String, boolean, boolean)}
     * directly to fire this notification on another thread. If a
     * PropertyVetoException is thrown, ensure the property change does not
     * complete.
     *
     * @param propertyName property that is about to change
     * @param oldValue     old value of the property
     * @param newValue     new value of the property
     * @throws PropertyVetoException if property update vetoed
     */
    public void fireVetoableChange(String propertyName, boolean oldValue, boolean newValue)
            throws PropertyVetoException {
        this.vetoableChangeSupport.fireVetoableChange(propertyName, oldValue, newValue);
    }

    /**
     * Get the indexed property or return null if the index is invalid. Used to
     * get the old value when setting an indexed property where the index may
     * not previously have been set.
     * 
     * @param key   the property name
     * @param index the index
     * @return the value at index or null
     */
    protected Object getIndexedPropertyOrNull(String key, int index) {
        try {
            return this.getIndexedProperty(key, index);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }

    }
}
