package jmri.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.util.Set;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * JMRI-specific extensions to the Java Beans utility class.
 * <p>
 * Since this class extends {@link java.beans.Beans}, classes using methods from
 * both classes need only import this class.
 *
 * @author Randall Wood
 * @deprecated since 4.19.5; use {@link BeanUtil} and {@link java.beans.Beans}
 *             instead
 */
@Deprecated
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "Intentional pattern")
public class Beans extends java.beans.Beans {

    /**
     * Set element <i>index</i> of property <i>key</i> of <i>bean</i> to
     * <i>value</i>.
     * <p>
     * If <i>bean</i> implements {@link BeanInterface}, this method calls
     * {@link jmri.beans.BeanInterface#setIndexedProperty(java.lang.String, int, java.lang.Object)}
     * otherwise it calls
     * {@link jmri.beans.BeanUtil#setIntrospectedIndexedProperty(java.lang.Object, java.lang.String, int, java.lang.Object)}
     *
     * @param bean  The bean to update.
     * @param key   The indexed property to set.
     * @param index The element to use.
     * @param value The value to set.
     * @see jmri.beans.BeanInterface#setIndexedProperty(java.lang.String, int,
     *      java.lang.Object)
     */
    public static void setIndexedProperty(Object bean, String key, int index, Object value) {
        BeanUtil.setIndexedProperty(bean, key, index, value);
    }

    /**
     * Set element <i>index</i> of property <i>key</i> of <i>bean</i> to
     * <i>value</i>.
     * <p>
     * This method relies on the standard JavaBeans coding patterns to get and
     * invoke the setter for the property. Note that if <i>key</i> is not a
     * {@link String}, this method will not attempt to set the property
     * (JavaBeans introspection rules require that <i>key</i> be a String, while
     * other JMRI coding patterns accept that <i>key</i> can be an Object). Note
     * also that the setter must be public. This should only be called from
     * outside this class in an implementation of
     * {@link jmri.beans.BeanInterface#setIndexedProperty(java.lang.String, int, java.lang.Object)},
     * but is public so it can be accessed by any potential implementation of
     * that method.
     *
     * @param bean  The bean to update.
     * @param key   The indexed property to set.
     * @param index The element to use.
     * @param value The value to set.
     */
    public static void setIntrospectedIndexedProperty(Object bean, String key, int index, Object value) {
        BeanUtil.setIntrospectedIndexedProperty(bean, key, index, value);
    }

    /**
     * Get the item at index <i>index</i> of property <i>key</i> of <i>bean</i>.
     * If the index <i>index</i> of property <i>key</i> does not exist, this
     * method returns null instead of throwing
     * {@link java.lang.ArrayIndexOutOfBoundsException} do to the inability to
     * get the size of the indexed property using introspection.
     *
     * @param bean  The bean to inspect.
     * @param key   The indexed property to get.
     * @param index The element to return.
     * @return the value at <i>index</i> or null
     */
    public static Object getIndexedProperty(Object bean, String key, int index) {
        return BeanUtil.getIndexedProperty(bean, key, index);
    }

    /**
     * Get the item at index <i>index</i> of property <i>key</i> of <i>bean</i>.
     * This should only be called from outside this class in an implementation
     * of
     * {@link jmri.beans.BeanInterface#setProperty(java.lang.String, java.lang.Object)},
     * but is public so it can be accessed by any potential implementation of
     * that method.
     *
     * @param bean  The bean to inspect.
     * @param key   The indexed property to get.
     * @param index The element to return.
     * @return the value at <i>index</i> or null
     */
    public static Object getIntrospectedIndexedProperty(Object bean, String key, int index) {
        return BeanUtil.getIntrospectedIndexedProperty(bean, key, index);
    }

    /**
     * Set property <i>key</i> of <i>bean</i> to <i>value</i>.
     * <p>
     * If <i>bean</i> implements {@link BeanInterface}, this method calls
     * {@link jmri.beans.BeanInterface#setProperty(java.lang.String, java.lang.Object)},
     * otherwise it calls
     * {@link jmri.beans.BeanUtil#setIntrospectedProperty(java.lang.Object, java.lang.String, java.lang.Object)}.
     *
     * @param bean  The bean to update.
     * @param key   The property to set.
     * @param value The value to set.
     * @see jmri.beans.BeanInterface#setProperty(java.lang.String,
     *      java.lang.Object)
     */
    public static void setProperty(Object bean, String key, Object value) {
        BeanUtil.setProperty(bean, key, value);
    }

    /**
     * Set property <i>key</i> of <i>bean</i> to <i>value</i>.
     * <p>
     * This method relies on the standard JavaBeans coding patterns to get and
     * invoke the property's write method. Note that if <i>key</i> is not a
     * {@link String}, this method will not attempt to set the property
     * (JavaBeans introspection rules require that <i>key</i> be a String, while
     * other JMRI coding patterns accept that <i>key</i> can be an Object). This
     * should only be called from outside this class in an implementation of
     * {@link jmri.beans.BeanInterface#setProperty(java.lang.String, java.lang.Object)},
     * but is public so it can be accessed by any potential implementation of
     * that method.
     *
     * @param bean  The bean to update.
     * @param key   The property to set.
     * @param value The value to set.
     */
    public static void setIntrospectedProperty(Object bean, String key, Object value) {
        BeanUtil.setIntrospectedProperty(bean, key, value);
    }

    /**
     * Get the property <i>key</i> of <i>bean</i>.
     * <p>
     * If the property <i>key</i> cannot be found, this method returns null.
     * <p>
     * If <i>bean</i> implements {@link BeanInterface}, this method calls
     * {@link jmri.beans.BeanInterface#getProperty(java.lang.String)}, otherwise
     * it calls
     * {@link jmri.beans.BeanUtil#getIntrospectedProperty(java.lang.Object, java.lang.String)}.
     *
     * @param bean The bean to inspect.
     * @param key  The property to get.
     * @return value of property <i>key</i>
     * @see jmri.beans.BeanInterface#getProperty(java.lang.String)
     */
    public static Object getProperty(Object bean, String key) {
        return BeanUtil.getProperty(bean, key);
    }

    /**
     * Get the property <i>key</i> of <i>bean</i>.
     * <p>
     * If the property <i>key</i> cannot be found, this method returns null.
     * <p>
     * This method relies on the standard JavaBeans coding patterns to get and
     * invoke the property's read method. Note that if <i>key</i> is not a
     * {@link String}, this method will not attempt to get the property
     * (JavaBeans introspection rules require that <i>key</i> be a String, while
     * other JMRI coding patterns accept that <i>key</i> can be an Object). This
     * should only be called from outside this class in an implementation of
     * {@link jmri.beans.BeanInterface#getProperty(java.lang.String)}, but is
     * public so it can be accessed by any potential implementation of that
     * method.
     *
     * @param bean The bean to inspect.
     * @param key  The property to get.
     * @return value of property <i>key</i> or null
     */
    public static Object getIntrospectedProperty(Object bean, String key) {
        return BeanUtil.getIntrospectedProperty(bean, key);
    }

    /**
     * Test if <i>bean</i> has the property <i>key</i>.
     * <p>
     * If <i>bean</i> implements {@link BeanInterface}, this method calls
     * {@link jmri.beans.BeanInterface#hasProperty(java.lang.String)}, otherwise
     * it calls
     * {@link jmri.beans.BeanUtil#hasIntrospectedProperty(java.lang.Object, java.lang.String)}.
     *
     * @param bean The bean to inspect.
     * @param key  The property key to check for.
     * @return true if <i>bean</i> has property <i>key</i>
     */
    public static boolean hasProperty(Object bean, String key) {
        return BeanUtil.hasProperty(bean, key);
    }

    /**
     * Test if <i>bean</i> has the indexed property <i>key</i>.
     * <p>
     * If <i>bean</i> implements {@link BeanInterface}, this method calls
     * {@link jmri.beans.BeanInterface#hasIndexedProperty(java.lang.String)},
     * otherwise it calls
     * {@link jmri.beans.BeanUtil#hasIntrospectedIndexedProperty(java.lang.Object, java.lang.String)}.
     *
     * @param bean The bean to inspect.
     * @param key  The indexed property to check for.
     * @return true if <i>bean</i> has indexed property <i>key</i>
     */
    public static boolean hasIndexedProperty(Object bean, String key) {
        return BeanUtil.hasIndexedProperty(bean, key);
    }

    /**
     * Test that <i>bean</i> has the property <i>key</i>.
     * <p>
     * This method relies on the standard JavaBeans coding patterns to find the
     * property. Note that if <i>key</i> is not a {@link String}, this method
     * will not attempt to find the property (JavaBeans introspection rules
     * require that <i>key</i> be a String, while other JMRI coding patterns
     * accept that <i>key</i> can be an Object). This should only be called from
     * outside this class in an implementation of
     * {@link jmri.beans.BeanInterface#hasProperty(java.lang.String)}, but is
     * public so it can be accessed by any potential implementation of that
     * method.
     *
     * @param bean The bean to inspect.
     * @param key  The property to check for.
     * @return true if <i>bean</i> has property <i>key</i>
     */
    public static boolean hasIntrospectedProperty(Object bean, String key) {
        return BeanUtil.hasIntrospectedProperty(bean, key);
    }

    /**
     * Test that <i>bean</i> has the indexed property <i>key</i>.
     * <p>
     * This method relies on the standard JavaBeans coding patterns to find the
     * property. Note that if <i>key</i> is not a {@link String}, this method
     * will not attempt to find the property (JavaBeans introspection rules
     * require that <i>key</i> be a String, while other JMRI coding patterns
     * accept that <i>key</i> can be an Object). This should only be called from
     * outside this class in an implementation of
     * {@link jmri.beans.BeanInterface#hasIndexedProperty(java.lang.String)},
     * but is public so it can be accessed by any potential implementation of
     * that method.
     *
     * @param bean The bean to inspect.
     * @param key  The indexed property to check for.
     * @return true if <i>bean</i> has indexed property <i>key</i>
     */
    public static boolean hasIntrospectedIndexedProperty(Object bean, String key) {
        return BeanUtil.hasIntrospectedIndexedProperty(bean, key);
    }

    public static Set<String> getPropertyNames(Object bean) {
        return BeanUtil.getPropertyNames(bean);
    }

    /**
     * Use an {@link java.beans.Introspector} to get a set of the named
     * properties of the bean. Note that properties discovered through this
     * mechanism must have public accessors per the JavaBeans specification.
     * This should only be called from outside this class in an implementation
     * of {@link jmri.beans.BeanInterface#getPropertyNames()}, but is public so
     * it can be accessed by any potential implementation of that method.
     *
     * @param bean The bean to inspect.
     * @return {@link Set} of property names
     */
    public static Set<String> getIntrospectedPropertyNames(Object bean) {
        return BeanUtil.getIntrospectedPropertyNames(bean);
    }

    /**
     * Test that <i>bean</i> implements {@link jmri.beans.BeanInterface}.
     *
     * @param bean The bean to inspect.
     * @return true if <i>bean</i> implements BeanInterface.
     */
    public static boolean implementsBeanInterface(Object bean) {
        return BeanUtil.implementsBeanInterface(bean);
    }

    /**
     * Test that <i>listeners</i> contains <i>needle</i> even if listener is
     * contained within a {@link PropertyChangeListenerProxy}.
     * <p>
     * This is intended to be used where action needs to be taken (or not taken)
     * if <i>needle</i> is (or is not) listening for property changes. Note that
     * if a listener was registered to listen for changes in a single property,
     * it is wrapped by a PropertyChangeListenerProxy such that using
     * {@code Arrays.toList(getPropertyChangeListeners()).contains(needle) } may
     * return false when <i>needle</i> is listening to a specific property.
     * 
     * @param listeners the array of listeners to search through
     * @param needle    the listener to search for
     * @return true if <i>needle</i> is in <i>listeners</i>; false otherwise
     */
    public static boolean contains(PropertyChangeListener[] listeners, @Nonnull PropertyChangeListener needle) {
        return BeanUtil.contains(listeners, needle);
    }
}
