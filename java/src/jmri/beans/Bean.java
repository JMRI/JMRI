// Bean.java
package jmri.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Generic implementation of {@link jmri.beans.BeanInterface} with a complete
 * implementation of {@link java.beans.PropertyChangeSupport}.
 * <p>
 * See the PropertyChangeSupport documentation for complete documentation of
 * those methods.
 *
 * @author rhwood
 * @see java.beans.PropertyChangeSupport
 */
public abstract class Bean extends UnboundBean implements PropertyChangeProvider {

    /**
     * Provide a {@link java.beans.PropertyChangeSupport} helper.
     */
    protected final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Add a PropertyChangeListener to the listener list.
     *
     * @param listener The PropertyChangeListener to be added
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Add a PropertyChangeListener for a specific property.
     *
     * @param propertyName The name of the property to listen on.
     * @param listener     The PropertyChangeListener to be added
     */
    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     *
     * @param propertyName
     * @param index
     * @param oldValue
     * @param newValue
     * @deprecated Use the
     * {@link #propertyChangeSupport} {@link java.beans.PropertyChangeSupport#fireIndexedPropertyChange(java.lang.String, int, boolean, boolean)}
     * directly
     */
    @Deprecated
    protected void fireIndexedPropertyChange(String propertyName, int index, boolean oldValue, boolean newValue) {
        propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    /**
     *
     * @param propertyName
     * @param index
     * @param oldValue
     * @param newValue
     * @deprecated Use the
     * {@link #propertyChangeSupport} {@link java.beans.PropertyChangeSupport#fireIndexedPropertyChange(java.lang.String, int, int, int)}
     * directly
     */
    @Deprecated
    protected void fireIndexedPropertyChange(String propertyName, int index, int oldValue, int newValue) {
        propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    /**
     *
     * @param propertyName
     * @param index
     * @param oldValue
     * @param newValue
     * @deprecated Use the
     * {@link #propertyChangeSupport} {@link java.beans.PropertyChangeSupport#fireIndexedPropertyChange(java.lang.String, int, java.lang.Object, java.lang.Object)}
     * directly
     */
    @Deprecated
    protected void fireIndexedPropertyChange(String propertyName, int index, Object oldValue, Object newValue) {
        propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    /**
     *
     * @param key
     * @param oldValue
     * @param value
     * @deprecated Use the
     * {@link #propertyChangeSupport} {@link java.beans.PropertyChangeSupport#firePropertyChange(java.lang.String, boolean, boolean)}
     * directly
     */
    @Deprecated
    protected void firePropertyChange(String key, boolean oldValue, boolean value) {
        propertyChangeSupport.firePropertyChange(key, oldValue, value);
    }

    /**
     *
     * @param evt
     * @deprecated Use the
     * {@link #propertyChangeSupport} {@link java.beans.PropertyChangeSupport#firePropertyChange(java.beans.PropertyChangeEvent)}
     * directly
     */
    @Deprecated
    protected void firePropertyChange(PropertyChangeEvent evt) {
        propertyChangeSupport.firePropertyChange(evt);
    }

    /**
     *
     * @param key
     * @param value
     * @param oldValue
     * @deprecated Use the
     * {@link #propertyChangeSupport} {@link java.beans.PropertyChangeSupport#firePropertyChange(java.lang.String, int, int)}
     * directly
     */
    @Deprecated
    protected void firePropertyChange(String key, int oldValue, int value) {
        propertyChangeSupport.firePropertyChange(key, oldValue, value);
    }

    /**
     *
     * @param key
     * @param oldValue
     * @param value
     * @deprecated Use the
     * {@link #propertyChangeSupport} {@link java.beans.PropertyChangeSupport#firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)}
     * directly
     */
    @Deprecated
    protected void firePropertyChange(String key, Object oldValue, Object value) {
        propertyChangeSupport.firePropertyChange(key, oldValue, value);
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return propertyChangeSupport.getPropertyChangeListeners();
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return propertyChangeSupport.getPropertyChangeListeners(propertyName);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }
}
