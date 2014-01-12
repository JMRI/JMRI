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
public abstract class Bean extends UnboundBean {

    /**
     * Provide a {@link java.beans.PropertyChangeSupport} helper.
     */
    protected final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Add a PropertyChangeListener to the listener list.
     *
     * @param listener The PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Add a PropertyChangeListener for a specific property.
     *
     * @param propertyName The name of the property to listen on.
     * @param listener The PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    protected void fireIndexedPropertyChange(String propertyName, int index, boolean oldValue, boolean newValue) {
        propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, oldValue);
    }

    protected void fireIndexedPropertyChange(String propertyName, int index, int oldValue, int newValue) {
        propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, oldValue);
    }

    protected void fireIndexedPropertyChange(String propertyName, int index, Object oldValue, Object newValue) {
        propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, oldValue);
    }

    protected void firePropertyChange(String key, boolean oldValue, boolean value) {
        propertyChangeSupport.firePropertyChange(key, oldValue, value);
    }

    protected void firePropertyChange(PropertyChangeEvent evt) {
        propertyChangeSupport.firePropertyChange(evt);
    }

    protected void firePropertyChange(String key, int oldValue, int value) {
        propertyChangeSupport.firePropertyChange(key, oldValue, value);
    }

    protected void firePropertyChange(String key, Object oldValue, Object value) {
        propertyChangeSupport.firePropertyChange(key, oldValue, value);
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        return propertyChangeSupport.getPropertyChangeListeners();
    }

    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return propertyChangeSupport.getPropertyChangeListeners(propertyName);
    }

    public boolean hasListeners(String propertyName) {
        return propertyChangeSupport.hasListeners(propertyName);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Set property <i>key</i> to <i>value</i>.
     * <p>
     * This implementation checks that a write method is not available for the
     * property using JavaBeans introspection, and stores the property in
     * {@link Bean#properties} only if a write method does not exist. This
     * implementation also fires a PropertyChangeEvent for the property.
     *
     * @param key
     * @param value
     * @see BeanInterface#setProperty(java.lang.String, java.lang.Object)
     */
    @Override
    public void setProperty(String key, Object value) {
        // use write method for property if it exists
        if (Beans.hasIntrospectedProperty(this, key)) {
            Beans.setIntrospectedProperty(this, key, value);
        } else {
            // HashMap.put returns the old value, so this works
            firePropertyChange(key, properties.put(key, value), value);
        }
    }
}
