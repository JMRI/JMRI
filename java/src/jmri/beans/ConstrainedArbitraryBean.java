package jmri.beans;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.Set;
import org.slf4j.LoggerFactory;

/**
 * A Bean with support for {@link java.beans.VetoableChangeListener}s.
 *
 * @author Randall Wood
 */
public class ConstrainedArbitraryBean extends ConstrainedBean {

    protected final ArbitraryPropertySupport arbitraryPropertySupport = new ArbitraryPropertySupport(this);

    @Override
    public void setProperty(String key, Object value) {
        try {
            this.fireVetoableChange(key, getProperty(key), value);
            if (Beans.hasIntrospectedProperty(this, key)) {
                Beans.setIntrospectedProperty(this, key, value);
            } else {
                Object oldValue = this.arbitraryPropertySupport.getProperty(key);
                this.arbitraryPropertySupport.setProperty(key, value);
                this.firePropertyChange(key, oldValue, value);
            }
        } catch (PropertyVetoException ex) {
            // use the logger for the implementing class instead of a logger for ConstrainedBean
            LoggerFactory.getLogger(this.getClass()).warn("Property {} change vetoed.", key, ex);
            // fire a property change that does not have the new value to indicate
            // to any other listeners that the property was "reset" back to its
            // orginal value as a result of the veto
            this.firePropertyChange(key, getProperty(key), getProperty(key));
        }
    }

    @Override
    public void setIndexedProperty(String key, int index, Object value) {
        try {
            this.fireVetoableChange(new IndexedPropertyChangeEvent(this, key, this.getIndexedProperty(key, index), value, index));
            if (Beans.hasIntrospectedIndexedProperty(this, key)) {
                Beans.setIntrospectedIndexedProperty(this, key, index, value);
            } else {
                Object oldValue = this.arbitraryPropertySupport.getIndexedProperty(key, index);
                this.arbitraryPropertySupport.setIndexedProperty(key, index, value);
                this.fireIndexedPropertyChange(key, index, oldValue, value);
            }
        } catch (PropertyVetoException ex) {
            // use the logger for the implementing class instead of a logger for ConstrainedBean
            LoggerFactory.getLogger(this.getClass()).warn("Property {} change vetoed.", key, ex);
            // fire a property change that does not have the new value to indicate
            // to any other listeners that the property was "reset" back to its
            // orginal value as a result of the veto
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
