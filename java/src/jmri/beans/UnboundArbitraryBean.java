package jmri.beans;

import java.util.Set;

/**
 * Generic implementation of {@link jmri.beans.BeanInterface} that supports
 * arbitrary properties defined at runtime.
 * <p>
 * <b>NOTE</b> This class does not implement
 * {@link java.beans.PropertyChangeSupport}. Subclass {@link jmri.beans.Bean} if
 * you need to support property change listeners.
 *
 * @author Randall Wood
 */
public abstract class UnboundArbitraryBean extends UnboundBean {

    protected final ArbitraryPropertySupport arbitraryPropertySupport = new ArbitraryPropertySupport(this);

    /**
     * Get value of element at <i>index</i> of property array <i>key</i>.
     * <p>
     * This implementation calls a read method for the indexed property using
     * JavaBeans introspection, and assumes, based on JavaBeans coding patterns,
     * that the read method has the following parameter: <code>index</code>.
     *
     * Note that this method returns null instead of throwing
     * {@link java.lang.ArrayIndexOutOfBoundsException} if the index is invalid
     * since the Java introspection methods provide no reliable way to get the
     * size of the indexed property.
     *
     * @param key Property array to parse.
     * @param index Element to retrieve.
     * @return value of element or null
     */
    @Override
    public Object getIndexedProperty(String key, int index) {
        return this.arbitraryPropertySupport.getIndexedProperty(key, index);
    }

    /**
     * Get the value of property key.
     * <p>
     * If <i>null</i> is a valid (or expected) value for <i>key</i>, you might
     * want to use {@link Bean#hasProperty(java.lang.String)} to test that the
     * property exists.
     * <p>
     * This implementation searches the internal property collection
     * and uses introspection to get the property.
     *
     * @param key Property to retrieve.
     * @return value of key or null.
     * @see BeanInterface#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(String key) {
        return this.arbitraryPropertySupport.getProperty(key);
    }

    /**
     * Return a list of property names.
     * <p>
     * This implementation combines the keys in
     * {@link ArbitraryPropertySupport#getPropertyNames()} with the results of
     * {@link Beans#getIntrospectedPropertyNames(java.lang.Object)}.
     *
     * @return a Set of names
     * @see BeanInterface#getPropertyNames()
     */
    @Override
    public Set<String> getPropertyNames() {
        return this.arbitraryPropertySupport.getPropertyNames();
    }

    /**
     * Test if a property exists.
     * <p>
     * This implementation searches the internal property collection
     * and uses introspection to get the property.
     *
     * @param key Property to inspect.
     * @return true if property exists
     * @see BeanInterface#hasProperty(java.lang.String)
     */
    @Override
    public boolean hasProperty(String key) {
        return this.arbitraryPropertySupport.hasProperty(key);
    }

    @Override
    public boolean hasIndexedProperty(String key) {
        return this.arbitraryPropertySupport.hasIndexedProperty(key);
    }

    /**
     * Set element at <i>index</i> of property array <i>key</i> to <i>value</i>.
     * <p>
     * This implementation calls a write method for the indexed property using
     * JavaBeans introspection, and assumes, based on JavaBeans coding patterns,
     * that the write method has the following two parameters in order:
     * <code>index</code>, <code>value</code>.
     *
     * @param key Property array to use.
     * @param index Element to write.
     * @param value Value to set.
     * @see BeanInterface#setIndexedProperty(java.lang.String, int,
     *      java.lang.Object)
     */
    @Override
    public void setIndexedProperty(String key, int index, Object value) {
        this.arbitraryPropertySupport.setIndexedProperty(key, index, value);
    }

    /**
     * Set property <i>key</i> to <i>value</i>.
     * <p>
     * This implementation checks that a write method is not available for the
     * property using JavaBeans introspection, and stores the property using
     * {@link ArbitraryPropertySupport#setProperty(String, Object)} only if a
     * write method does not exist. This implementation also fires a
     * PropertyChangeEvent for the property.
     *
     * @param key Property to use.
     * @param value Value to store.
     * @see BeanInterface#setProperty(java.lang.String, java.lang.Object)
     */
    @Override
    public void setProperty(String key, Object value) {
        this.arbitraryPropertySupport.setProperty(key, value);
    }
}
