package jmri.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.SwingPropertyChangeSupport;

/**
 * Generic implementation of {@link jmri.beans.BeanInterface} with a complete
 * implementation of {@link java.beans.PropertyChangeSupport}.
 * <p>
 * See the PropertyChangeSupport documentation for complete documentation of
 * those methods.
 * <p>
 * This class is thread safe.
 *
 * @author Randall Wood (c) 2011, 2014, 2015, 2016, 2020
 * @see java.beans.PropertyChangeSupport
 */
public abstract class Bean extends UnboundBean implements PropertyChangeFirer, PropertyChangeProvider {

    /**
     * Provide a {@link java.beans.PropertyChangeSupport} helper.
     */
    protected final SwingPropertyChangeSupport propertyChangeSupport;

    /**
     * Create a bean that notifies property change listeners on the thread the
     * event was generated on.
     */
    protected Bean() {
        this(false);
    }

    /**
     * Create a bean.
     *
     * @param notifyOnEDT true to notify property change listeners on the EDT;
     *                    false to notify listeners on the thread the event was
     *                    generated on (which may or may not be the EDT)
     */
    protected Bean(boolean notifyOnEDT) {
        propertyChangeSupport = new SwingPropertyChangeSupport(this, notifyOnEDT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fireIndexedPropertyChange(String propertyName, int index, boolean oldValue, boolean newValue) {
        propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fireIndexedPropertyChange(String propertyName, int index, int oldValue, int newValue) {
        propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fireIndexedPropertyChange(String propertyName, int index, Object oldValue, Object newValue) {
        propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void firePropertyChange(PropertyChangeEvent event) {
        propertyChangeSupport.firePropertyChange(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return propertyChangeSupport.getPropertyChangeListeners();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return propertyChangeSupport.getPropertyChangeListeners(propertyName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Is this Bean assuring that all property change listeners will be notified
     * on the EDT?
     *
     * @return true if notifying listeners of events on the EDT; false if
     *         notifying listeners on the thread that the event was generated on
     *         (which may or may not be the EDT)
     */
    public boolean isNotifyOnEDT() {
        return propertyChangeSupport.isNotifyOnEDT();
    }
}
