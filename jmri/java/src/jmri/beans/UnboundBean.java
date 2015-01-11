// UnboundBean.java
package jmri.beans;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Generic implementation of {@link jmri.beans.BeanInterface}.
 * <p>
 * <b>NOTE</b> This class does not implement
 * {@link java.beans.PropertyChangeSupport}. Subclass {@link jmri.beans.Bean} if
 * you need to support property change listeners.
 *
 * @author rhwood
 */
public abstract class UnboundBean implements BeanInterface {

    /**
     * Store properties in a hashMap for easy access.
     * <p>
     * Note that unless you use the methods in this class to manipulate this
     * variable, you will need to instantiate it, or test that it is not null
     * prior to use.
     */
    protected final HashMap<String, Object> properties = new HashMap<String, Object>();

    /**
     * Get value of element at <i>index</i> of property array <i>key</i>.
     * <p>
     * This implementation calls a read method for the indexed property using
     * JavaBeans introspection, and assumes, based on JavaBeans coding patterns,
     * that the read method has the following parameter:
     * <code>index</code>.
     *
     * @param key
     * @param index
     * @return value of element or null
     */
    @Override
    public Object getIndexedProperty(String key, int index) {
        if (properties.containsKey(key) && properties.get(key).getClass().isArray()) {
            return ((Object[]) properties.get(key))[index];
        }
        return Beans.getIntrospectedIndexedProperty(this, key, index);
    }

    /**
     * Get the value of property key.
     * <p>
     * If <i>null</i> is a valid (or expected) value for <i>key</i>, you might
     * want to use {@link Bean#hasProperty(java.lang.String)} to test that the
     * property exists.
     * <p>
     * This implementation searches {@link Bean#properties} and uses
     * introspection to get the property.
     *
     * @param key
     * @return value of key or null.
     * @see BeanInterface#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(String key) {
        if (properties.containsKey(key)) {
            return properties.get(key);
        }
        return Beans.getIntrospectedProperty(this, key);
    }

    /**
     * Return a list of property names.
     * <p>
     * This implementation combines the keys in {@link Bean#properties} with the
     * results of {@link Beans#getIntrospectedPropertyNames(java.lang.Object)}.
     *
     * @return a Set of names
     * @see BeanInterface#getPropertyNames()
     */
    @Override
    public Set<String> getPropertyNames() {
        HashSet<String> names = new HashSet<String>();
        names.addAll(properties.keySet());
        names.addAll(Beans.getIntrospectedPropertyNames(this));
        return names;
    }

    /**
     * Test if a property exists.
     * <p>
     * This implementation searches {@link Bean#properties} and uses
     * introspection to get the property.
     *
     * @param key
     * @return true if property exists
     * @see BeanInterface#hasProperty(java.lang.String)
     */
    @Override
    public boolean hasProperty(String key) {
        if (properties.containsKey(key)) {
            return true;
        }
        return Beans.hasIntrospectedProperty(this, key);
    }

    /**
     * Set element at <i>index</i> of property array <i>key</i> to <i>value</i>.
     * <p>
     * This implementation calls a write method for the indexed property using
     * JavaBeans introspection, and assumes, based on JavaBeans coding patterns,
     * that the write method has the following two parameters in order:
     * <code>index</code>,
     * <code>value</code>.
     *
     * @param key
     * @param index
     * @param value
     * @see BeanInterface#setIndexedProperty(java.lang.String, int,
     * java.lang.Object)
     */
    @Override
    public void setIndexedProperty(String key, int index, Object value) {
        if (Beans.hasIntrospectedProperty(this, key)) {
            Beans.setIntrospectedIndexedProperty(this, key, index, value);
        } else {
            if (!properties.containsKey(key)) {
                properties.put(key, new Object[0]);
            }
            ((Object[]) properties.get(key))[index] = value;
        }
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
            properties.put(key, value);
        }
    }
}
