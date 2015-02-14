// Beans.java
package jmri.beans;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMRI-specific extensions to the Java Beans utility class.
 * <p>
 * Since this class extends {@link java.beans.Beans}, classes using methods from
 * both classes need only import this class.
 *
 * @author rhwood
 */
@SuppressWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class Beans extends java.beans.Beans {

    public static void setIndexedProperty(Object object, String key, int index, Object value) {
        if (implementsBeanInterface(object)) {
            ((BeanInterface) object).setIndexedProperty(key, index, value);
        } else {
            setIntrospectedIndexedProperty(object, key, index, value);
        }
    }

    public static void setIntrospectedIndexedProperty(Object object, String key, int index, Object value) {
        if (object != null && key != null) {
            try {
                PropertyDescriptor[] pds = Introspector.getBeanInfo(object.getClass()).getPropertyDescriptors();
                for (PropertyDescriptor pd : pds) {
                    if (pd instanceof IndexedPropertyDescriptor && pd.getName().equalsIgnoreCase(key)) {
                        ((IndexedPropertyDescriptor) pd).getIndexedWriteMethod().invoke(object, new Object[]{index, value});
                        return; // short circut, since there is nothing left to do at this point.
                    }
                }
                // catch only introspection-related exceptions, and allow all other to pass through
            } catch (IllegalAccessException ex) {
                log.warn(ex.toString(), ex);
            } catch (IllegalArgumentException ex) {
                log.warn(ex.toString(), ex);
            } catch (InvocationTargetException ex) {
                log.warn(ex.toString(), ex);
            } catch (IntrospectionException ex) {
                log.warn(ex.toString(), ex);
            }
        }
    }

    public static Object getIndexedProperty(Object object, String key, int index) {
        if (implementsBeanInterface(object)) {
            return ((BeanInterface) object).getIndexedProperty(key, index);
        } else {
            return getIntrospectedIndexedProperty(object, key, index);
        }
    }

    public static Object getIntrospectedIndexedProperty(Object object, String key, int index) {
        if (object != null && key != null) {
            try {
                PropertyDescriptor[] pds = Introspector.getBeanInfo(object.getClass()).getPropertyDescriptors();
                for (PropertyDescriptor pd : pds) {
                    if (pd instanceof IndexedPropertyDescriptor && pd.getName().equalsIgnoreCase(key)) {
                        return ((IndexedPropertyDescriptor) pd).getIndexedReadMethod().invoke(object, new Object[]{index});
                    }
                }
                // catch only introspection-related exceptions, and allow all other to pass through
            } catch (IllegalAccessException ex) {
                log.warn(ex.toString(), ex);
            } catch (IllegalArgumentException ex) {
                log.warn(ex.toString(), ex);
            } catch (InvocationTargetException ex) {
                log.warn(ex.toString(), ex);
            } catch (IntrospectionException ex) {
                log.warn(ex.toString(), ex);
            }
        }
        return null;
    }

    /**
     * Set property <i>key</i> of <i>object</i> to <i>value</i>.
     * <p>
     * If <i>object</i> implements {@link BeanInterface}, this method calls
     * {@link jmri.beans.BeanInterface#setProperty(java.lang.String, java.lang.Object)},
     * otherwise it calls
     * {@link jmri.beans.Beans#setIntrospectedProperty(java.lang.Object, java.lang.String, java.lang.Object)}.
     *
     * @param object
     * @param value
     * @see jmri.beans.BeanInterface#setProperty(java.lang.String,
     * java.lang.Object)
     */
    public static void setProperty(Object object, String key, Object value) {
        if (implementsBeanInterface(object)) {
            ((BeanInterface) object).setProperty(key, value);
        } else {
            setIntrospectedProperty(object, key, value);
        }
    }

    /**
     * Set property <i>key</i> of <i>object</i> to <i>value</i>.
     * <p>
     * This method relies on the standard JavaBeans coding patterns to get and
     * invoke the property's write method. Note that if <i>key</i> is not a
     * {@link String}, this method will not attempt to set the property
     * (JavaBeans introspection rules require that <i>key</i> be a String, while
     * other JMRI coding patterns accept that <i>key</i> can be an Object).
     *
     * @param object
     * @param value
     */
    public static void setIntrospectedProperty(Object object, String key, Object value) {
        if (object != null && key != null) {
            try {
                PropertyDescriptor[] pds = Introspector.getBeanInfo(object.getClass()).getPropertyDescriptors();
                for (PropertyDescriptor pd : pds) {
                    if (pd.getName().equalsIgnoreCase(key)) {
                        pd.getWriteMethod().invoke(object, new Object[]{value});
                        return; // short circut, since there is nothing left to do at this point.
                    }
                }
                // catch only introspection-related exceptions, and allow all other to pass through
            } catch (IllegalAccessException ex) {
                log.warn(ex.toString(), ex);
            } catch (IllegalArgumentException ex) {
                log.warn(ex.toString(), ex);
            } catch (InvocationTargetException ex) {
                log.warn(ex.toString(), ex);
            } catch (IntrospectionException ex) {
                log.warn(ex.toString(), ex);
            }
        }
    }

    /**
     * Get the property <i>key</i> of <i>object</i>.
     * <p>
     * If the property <i>key</i> cannot be found, this method returns null.
     * <p>
     * If <i>object</i> implements {@link BeanInterface}, this method calls
     * {@link jmri.beans.BeanInterface#getProperty(java.lang.String)}, otherwise
     * it calls
     * {@link jmri.beans.Beans#getIntrospectedProperty(java.lang.Object, java.lang.String)}.
     *
     * @param object
     * @return value of property <i>key</i>
     * @see jmri.beans.BeanInterface#getProperty(java.lang.String)
     */
    public static Object getProperty(Object object, String key) {
        if (implementsBeanInterface(object)) {
            return ((BeanInterface) object).getProperty(key);
        } else {
            return getIntrospectedProperty(object, key);
        }
    }

    /**
     * Get the property <i>key</i> of <i>object</i>.
     * <p>
     * If the property <i>key</i> cannot be found, this method returns null.
     * <p>
     * This method relies on the standard JavaBeans coding patterns to get and
     * invoke the property's read method. Note that if <i>key</i> is not a
     * {@link String}, this method will not attempt to get the property
     * (JavaBeans introspection rules require that <i>key</i> be a String, while
     * other JMRI coding patterns accept that <i>key</i> can be an Object).
     *
     * @param object
     * @return value of property <i>key</i> or null
     */
    public static Object getIntrospectedProperty(Object object, String key) {
        if (object != null && key != null) {
            try {
                PropertyDescriptor[] pds = Introspector.getBeanInfo(object.getClass()).getPropertyDescriptors();
                for (PropertyDescriptor pd : pds) {
                    if (pd.getName().equalsIgnoreCase(key)) {
                        return pd.getReadMethod().invoke(object, (Object[]) null);
                    }
                }
                // catch only introspection-related exceptions, and allow all other to pass through
            } catch (IllegalAccessException ex) {
                log.warn(ex.toString(), ex);
            } catch (IllegalArgumentException ex) {
                log.warn(ex.toString(), ex);
            } catch (InvocationTargetException ex) {
                log.warn(ex.toString(), ex);
            } catch (IntrospectionException ex) {
                log.warn(ex.toString(), ex);
            }
        }
        return null;
    }

    /**
     * Test if <i>object</i> has the property <i>key</i>.
     * <p>
     * If <i>object</i> implements {@link BeanInterface}, this method calls
     * {@link jmri.beans.BeanInterface#hasProperty(java.lang.String)}, otherwise
     * it calls
     * {@link jmri.beans.Beans#hasIntrospectedProperty(java.lang.Object, java.lang.String)}.
     *
     * @param object
     * @return true if <i>object</i> has property <i>key</i>
     */
    public static boolean hasProperty(Object object, String key) {
        if (implementsBeanInterface(object)) {
            return ((BeanInterface) object).hasProperty(key);
        } else {
            return hasIntrospectedProperty(object, key);
        }
    }

    /**
     * Test that <i>object</i> has the property <i>key</i>.
     * <p>
     * This method relies on the standard JavaBeans coding patterns to find the
     * property. Note that if <i>key</i> is not a {@link String}, this method
     * will not attempt to find the property (JavaBeans introspection rules
     * require that <i>key</i> be a String, while other JMRI coding patterns
     * accept that <i>key</i> can be an Object).
     *
     * @param object
     * @return true if <i>object</i> has property <i>key</i>
     */
    public static boolean hasIntrospectedProperty(Object object, String key) {
        if (object != null && key != null) {
            try {
                PropertyDescriptor[] pds = Introspector.getBeanInfo(object.getClass()).getPropertyDescriptors();
                for (PropertyDescriptor pd : pds) {
                    if (pd.getName().equalsIgnoreCase(key)) {
                        return true;
                    }
                }
                // catch only introspection-related exceptions, and allow all other to pass through
            } catch (IntrospectionException ex) {
                log.warn(ex.toString(), ex);
            }
        }
        return false;
    }

    public static Set<String> getPropertyNames(Object object) {
        if (object != null) {
            if (implementsBeanInterface(object)) {
                return ((BeanInterface) object).getPropertyNames();
            } else {
                return getIntrospectedPropertyNames(object);
            }
        }
        return new HashSet<String>(); // return an empty set instead of null
    }

    public static Set<String> getIntrospectedPropertyNames(Object object) {
        HashSet<String> names = new HashSet<String>();
        if (object != null) {
            try {
                PropertyDescriptor[] pds = Introspector.getBeanInfo(object.getClass()).getPropertyDescriptors();
                for (PropertyDescriptor pd : pds) {
                    names.add(pd.getName());
                }
                // catch only introspection-related exceptions, and allow all other to pass through
            } catch (IntrospectionException ex) {
                log.warn(ex.toString(), ex);
            }
        }
        return names;
    }

    /**
     * Test that <i>object</i> implements {@link jmri.beans.BeanInterface}.
     *
     * @param object
     * @return true if <i>object</i> implements BeanInterface.
     */
    public static boolean implementsBeanInterface(Object object) {
        return (null != object && BeanInterface.class.isAssignableFrom(object.getClass()));
    }
    static Logger log = LoggerFactory.getLogger(Beans.class.getName());
}
