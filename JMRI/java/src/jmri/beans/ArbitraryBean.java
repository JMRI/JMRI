package jmri.beans;

import java.util.Set;

/**
 * Generic implementation of {@link jmri.beans.BeanInterface} with a complete
 * implementation of {@link java.beans.PropertyChangeSupport} and support for
 * arbitrary properties defined at runtime.
 * <p>
 * See the PropertyChangeSupport documentation for complete documentation of
 * those methods.
 *
 * @author rhwood
 * @see java.beans.PropertyChangeSupport
 */
public abstract class ArbitraryBean extends Bean {

    protected final ArbitraryPropertySupport arbitraryPropertySupport = new ArbitraryPropertySupport(this);

    @Override
    public void setProperty(String key, Object value) {
        if (Beans.hasIntrospectedProperty(this, key)) {
            Beans.setIntrospectedProperty(this, key, value);
        } else {
            Object oldValue = this.arbitraryPropertySupport.getProperty(key);
            this.arbitraryPropertySupport.setProperty(key, value);
            this.firePropertyChange(key, oldValue, value);
        }
    }

    @Override
    public void setIndexedProperty(String key, int index, Object value) {
        if (Beans.hasIntrospectedIndexedProperty(this, key)) {
            Beans.setIntrospectedIndexedProperty(this, key, index, value);
        } else {
            Object oldValue = this.arbitraryPropertySupport.getIndexedProperty(key, index);
            this.arbitraryPropertySupport.setIndexedProperty(key, index, value);
            this.fireIndexedPropertyChange(key, index, oldValue, value);
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
