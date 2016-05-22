// ConstrainedBean.java
package jmri.beans;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import org.slf4j.LoggerFactory;

/**
 * A Bean with support for vetoable property change listeners
 * 
 * @author rhwood
 */
public class ConstrainedBean extends Bean {
    
    protected VetoableChangeSupport vetoableChangeSupport = null;
    
    @Override
    public void setProperty(String key, Object value) {
        try {
            getVetoableChangeSupport().fireVetoableChange(key, getProperty(key), value);
            super.setProperty(key, value);
        } catch (PropertyVetoException ex) {
            LoggerFactory.getLogger(this.getClass().getName()).warn("Property " + key + " change vetoed.", ex);
            super.firePropertyChange(key, getProperty(key), getProperty(key));
        }
    }

    public boolean hasPropertyChangeListeners(String propertyName) {
        return super.hasListeners(propertyName);
    }
    
    public boolean hasVetoableChangeListeners(String propertyName) {
        return getVetoableChangeSupport().hasListeners(propertyName);
    }
    
    @Override
    public boolean hasListeners(String propertyName) {
        return (hasPropertyChangeListeners(propertyName) || hasVetoableChangeListeners(propertyName));
    }
    
    protected VetoableChangeSupport getVetoableChangeSupport() {
        if (vetoableChangeSupport == null) {
            vetoableChangeSupport = new VetoableChangeSupport(this);
        }
        return vetoableChangeSupport;
    }
}
