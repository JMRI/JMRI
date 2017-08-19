package jmri.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import jmri.util.ThreadingUtil;

/**
 * Generic implementation of {@link jmri.beans.BeanInterface} with a complete
 * implementation of {@link java.beans.PropertyChangeSupport}.
 * <p>
 * See the PropertyChangeSupport documentation for complete documentation of
 * those methods.
 *
 * @author Randall Wood (c) 2011, 2014, 2015, 2016
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
     * Fire an indexed property change on the Event dispatch (Swing) thread. Use
     * {@link java.beans.PropertyChangeSupport#fireIndexedPropertyChange(java.lang.String, int, boolean, boolean)}
     * directly to fire this notification on another thread.
     *
     * @param propertyName the programmatic name of the property that was
     *                     changed
     * @param index        the index of the property element that was changed
     * @param oldValue     the old value of the property
     * @param newValue     the new value of the property
     */
    protected void fireIndexedPropertyChange(String propertyName, int index, boolean oldValue, boolean newValue) {
        ThreadingUtil.runOnGUIEventually(() -> {
            propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
        });
    }

    /**
     * Fire an indexed property change on the Event dispatch (Swing) thread. Use
     * {@link java.beans.PropertyChangeSupport#fireIndexedPropertyChange(java.lang.String, int, int, int)}
     * directly to fire this notification on another thread.
     *
     * @param propertyName the programmatic name of the property that was
     *                     changed
     * @param index        the index of the property element that was changed
     * @param oldValue     the old value of the property
     * @param newValue     the new value of the property
     */
    protected void fireIndexedPropertyChange(String propertyName, int index, int oldValue, int newValue) {
        ThreadingUtil.runOnGUIEventually(() -> {
            propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
        });
    }

    /**
     * Fire an indexed property change on the Event dispatch (Swing) thread. Use
     * {@link java.beans.PropertyChangeSupport#fireIndexedPropertyChange(java.lang.String, int, java.lang.Object, java.lang.Object)}
     * directly to fire this notification on another thread.
     *
     * @param propertyName the programmatic name of the property that was
     *                     changed
     * @param index        the index of the property element that was changed
     * @param oldValue     the old value of the property
     * @param newValue     the new value of the property
     */
    protected void fireIndexedPropertyChange(String propertyName, int index, Object oldValue, Object newValue) {
        ThreadingUtil.runOnGUIEventually(() -> {
            propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
        });
    }

    /**
     * Fire a property change on the Event dispatch (Swing) thread. Use
     * {@link java.beans.PropertyChangeSupport#firePropertyChange(java.lang.String, boolean, boolean)}
     * directly to fire this notification on another thread.
     *
     * @param propertyName the programmatic name of the property that was
     *                     changed
     * @param oldValue     the old value of the property
     * @param newValue     the new value of the property
     */
    protected void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        ThreadingUtil.runOnGUIEventually(() -> {
            propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
        });
    }

    /**
     * Fire a property change on the Event dispatch (Swing) thread. Use
     * {@link java.beans.PropertyChangeSupport#firePropertyChange(java.beans.PropertyChangeEvent)}
     * directly to fire this notification on another thread.
     *
     * @param event the PropertyChangeEvent to be fired
     */
    protected void firePropertyChange(PropertyChangeEvent event) {
        ThreadingUtil.runOnGUIEventually(() -> {
            propertyChangeSupport.firePropertyChange(event);
        });
    }

    /**
     * Fire a property change on the Event dispatch (Swing) thread. Use
     * {@link java.beans.PropertyChangeSupport#firePropertyChange(java.lang.String, int, int)}
     * directly to fire this notification on another thread.
     *
     * @param propertyName the programmatic name of the property that was
     *                     changed
     * @param oldValue     the old value of the property
     * @param newValue     the new value of the property
     */
    protected void firePropertyChange(String propertyName, int oldValue, int newValue) {
        ThreadingUtil.runOnGUIEventually(() -> {
            propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
        });
    }

    /**
     * Fire a property change on the Event dispatch (Swing) thread. Use
     * {@link java.beans.PropertyChangeSupport#firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)}
     * directly to fire this notification on another thread.
     *
     * @param propertyName the programmatic name of the property that was
     *                     changed
     * @param oldValue     the old value of the property
     * @param newValue     the new value of the property
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        ThreadingUtil.runOnGUIEventually(() -> {
            propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
        });
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
