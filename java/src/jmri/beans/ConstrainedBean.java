// ConstrainedBean.java
package jmri.beans;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import org.slf4j.LoggerFactory;

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
            this.vetoableChangeSupport.fireVetoableChange(key, getProperty(key), value);
            super.setProperty(key, value);
        } catch (PropertyVetoException ex) {
            // use the logger for the implementing class instead of a logger for ConstrainedBean
            LoggerFactory.getLogger(this.getClass().getName()).warn("Property {} change vetoed.", key, ex);
            // fire a property change that does not have the new value to indicate
            // to any other listeners that the property was "reset" back to its
            // orginal value as a result of the veto
            super.propertyChangeSupport.firePropertyChange(key, getProperty(key), getProperty(key));
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
}
