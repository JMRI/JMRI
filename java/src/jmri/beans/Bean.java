// Bean.java
package jmri.beans;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Generic implementation of {@link jmri.beans.BeanInterface}
 * 
 * @author rhwood
 */
public abstract class Bean implements BeanInterface {

    /**
     * Store properties in a hashMap for easy access.
     * <p>
     * Note that unless you use the methods in this class to manipulate this
     * variable, you will need to instantiate it, or test that it is not null
     * prior to use.
     */
    protected HashMap<String, Object> properties = null;
    
    /**
     * Set property <i>key</i> to <i>value</i>.
     * <p>
     * This implementation checks that a write method is not available for the
     * property using JavaBeans introspection, and stores the property in 
     * {@link Bean#properties} only if a write method does not exist.
     * 
     * @param key
     * @param value 
     * @see BeanInterface#setProperty(java.lang.String, java.lang.Object) 
     */
    public void setProperty(String key, Object value) {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        // use write method for property if it exists
        if (Beans.hasIntrospectedProperty(this, null)) {
            Beans.setIntrospectedProperty(this, null, value);
        } else {
            properties.put(key, value);
        }
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
    public Object getProperty(String key) {
        if (properties != null && properties.containsKey(key)) {
            return properties.get(key);
        }
        return Beans.getIntrospectedProperty(this, null);
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
    public boolean hasProperty(String key) {
        if (properties != null && properties.containsKey(key)) {
            return true;
        }
        return Beans.hasIntrospectedProperty(this, null);
    }

    /**
     * Return a list of property names.
     * <p>
     * This implementation combines the keys in {@link Bean#properties} with
     * the results of {@link Beans#getIntrospectedPropertyNames(java.lang.Object)}.
     * 
     * @return a Set of names
     * @see BeanInterface#getPropertyNames() 
     */
    public Set<String> getPropertyNames() {
        HashSet<String> names = new HashSet<String>();
        if (properties != null) {
            names.addAll(properties.keySet());
        }
        names.addAll(Beans.getIntrospectedPropertyNames(this));
        return names;
    }
    
}
