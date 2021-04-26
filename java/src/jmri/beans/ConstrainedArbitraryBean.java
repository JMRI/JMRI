package jmri.beans;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.Set;

/**
 * A Bean with support for {@link java.beans.VetoableChangeListener}s.
 *
 * @author Randall Wood Copyright 2015, 2016, 2020
 */
public class ConstrainedArbitraryBean extends ConstrainedBean {

    protected final ArbitraryPropertySupport arbitraryPropertySupport = new ArbitraryPropertySupport(this);

    @Override
    public void setProperty(String key, Object value) {
        try {
            this.fireVetoableChange(key, getProperty(key), value);
            this.arbitraryPropertySupport.setProperty(key, value);
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
            this.arbitraryPropertySupport.setIndexedProperty(key, index, value);
        } catch (PropertyVetoException ex) {
            // fire a property change that does not have the new value to indicate
            // to any other listeners that the property was "reset" back to its
            // original value as a result of the veto
            this.fireIndexedPropertyChange(key, index, getProperty(key), getProperty(key));
        }
    }

    @Override
    public Object getIndexedProperty(String key, int index) {
        return this.arbitraryPropertySupport.getIndexedProperty(key, index);
    }

    @Override
    public Object getProperty(String key) {
        return this.arbitraryPropertySupport.getProperty(key);
    }

    @Override
    public boolean hasProperty(String key) {
        return this.arbitraryPropertySupport.hasProperty(key);
    }

    @Override
    public boolean hasIndexedProperty(String key) {
        return this.arbitraryPropertySupport.hasIndexedProperty(key);
    }

    @Override
    public Set<String> getPropertyNames() {
        return this.arbitraryPropertySupport.getPropertyNames();
    }

}
