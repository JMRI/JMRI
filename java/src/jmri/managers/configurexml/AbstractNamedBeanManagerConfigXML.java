package jmri.managers.configurexml;

import java.lang.reflect.Constructor;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.configurexml.XmlAdapter;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Provides services for configuring NamedBean manager storage.
 * <p>
 * Not a full abstract implementation by any means, rather this class provides
 * various common service routines to eventual type-specific subclasses.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * @since 2.3.1
 */
public abstract class AbstractNamedBeanManagerConfigXML extends jmri.configurexml.AbstractXmlAdapter {

    static final String STR_SYSTEM_NAME = "systemName";
    static final String STR_USER_NAME = "userName";
    static final String STR_COMMENT = "comment";

    static final String STR_VALUE = "value";
    static final String STR_CLASS = "class";

    static final String STR_KEY = "key";
    static final String STR_PROPERTY = "property";
    static final String STR_PROPERTIES = "properties";

    public AbstractNamedBeanManagerConfigXML() {
    }

    /**
     * Store common items:
     * <ul>
     * <li>user name
     * <li>comment
     * </ul>
     *
     * @param t    The NamedBean being stored
     * @param elem The JDOM element for storing the NamedBean
     */
    protected void storeCommon(@Nonnull NamedBean t, Element elem) {
        storeUserName(t, elem);
        storeComment(t, elem);
        storeProperties(t, elem);
    }

    /**
     * Load common items:
     * <ul>
     * <li>comment
     * </ul>
     * The username is not loaded, because it had to be provided in the ctor
     * earlier.
     *
     * @param t    The NamedBean being loaded
     * @param elem The JDOM element containing the NamedBean
     */
    protected void loadCommon(NamedBean t, Element elem) {
        loadComment(t, elem);
        loadProperties(t, elem);
    }

    /**
     * Store the comment parameter from a NamedBean
     *
     * @param t    The NamedBean being stored
     * @param elem The JDOM element for storing the NamedBean
     */
    void storeComment( @Nonnull NamedBean t, @Nonnull Element elem) {
        // add comment, if present
        if (t.getComment() != null) {
            Element c = new Element(STR_COMMENT);
            c.addContent(t.getComment());
            elem.addContent(c);
        }
    }

    /**
     * Store the username parameter from a NamedBean.
     * <ul>
     * <li>Before 2.9.6, this was an attribute
     * <li>Starting in 2.9.6, this was stored as both attribute and element
     * <li>Starting in 3.1/2.11.1, this will be just an element
     * </ul>
     *
     * @param t    The NamedBean being stored
     * @param elem The JDOM element for storing the NamedBean
     */
    void storeUserName( @Nonnull NamedBean t, @Nonnull Element elem) {
        String uname = t.getUserName();
        if ( uname != null && !uname.isEmpty()) {
            elem.addContent(new Element(STR_USER_NAME).addContent(uname));
        }
    }

    /**
     * Get the username attribute from one element of a list of Elements
     * defining NamedBeans.
     *
     * @param beanList list of Elements
     * @param i        index of Element in list to examine
     * @return the user name of bean in beanList at i or null
     */
    protected String getUserName(List<Element> beanList, int i) {
        return getUserName(beanList.get(i));
    }

    /**
     * Service method to load a user name, check it for validity, and if need be
     * notify about errors.
     * <p>
     * The name can be empty, but if present, has to be valid.
     * <p>
     * There's no check to make sure the name corresponds to an existing bean,
     * as sometimes this is used to check validity before creating the bean.
     * <ul>
     * <li>Before 2.9.6, this was stored as an attribute
     * <li>Starting in 2.9.6, this was stored as both attribute and element
     * <li>Starting in 3.1/2.11.1, this is stored as an element
     * </ul>
     *
     * @param elem The existing Element
     * @return the user name of bean or null
     */
    protected String getUserName(@Nonnull Element elem) {
        if (elem.getChild(STR_USER_NAME) != null) {
            return elem.getChild(STR_USER_NAME).getText();
        }
        if (elem.getAttribute(STR_USER_NAME) != null) {
            return elem.getAttribute(STR_USER_NAME).getValue();
        }
        return null;
    }

    /**
     * Service method to load a system name.
     * <p>
     * There's no check to make sure the name corresponds to an existing bean,
     * as sometimes this is used to check validity before creating the bean.
     * Validity (format) checks are deferred to later, see
     * {@link #checkNameNormalization}.
     * <ul>
     * <li>Before 2.9.6, this was stored as an attribute
     * <li>Starting in 2.9.6, this was stored as both attribute and element
     * <li>Starting in 3.1/2.10.1, this is stored as an element
     * </ul>
     *
     * @param elem The existing Element
     * @return the system name or null if not defined
     */
    protected String getSystemName(@Nonnull Element elem) {
        if (elem.getChild(STR_SYSTEM_NAME) != null) {
            return elem.getChild(STR_SYSTEM_NAME).getText();
        }
        if (elem.getAttribute(STR_SYSTEM_NAME) != null) {
            return elem.getAttribute(STR_SYSTEM_NAME).getValue();
        }
        return null;
    }

    /**
     * Common service routine to check for and report on normalization (errors)
     * in the incoming NamedBean's name(s)
     * <p>
     * If NamedBeam.normalizeUserName changes, this may want to be updated.
     * <p>
     * Right now, this just logs. Someday, perhaps it should notify upward of
     * found issues by throwing an exception.
     * <p>
     * Package-level access to allow testing
     *
     * @param <T>           The type of NamedBean being checked, i.e. Turnout, Sensor, etc
     * @param rawSystemName The proposed system name string, before
     *                      normalization
     * @param rawUserName   The proposed user name string, before normalization
     * @param manager       The NamedBeanManager that will be storing this
     */
    <T extends NamedBean> void checkNameNormalization(@Nonnull String rawSystemName,
        @CheckForNull String rawUserName, @Nonnull Manager<T> manager) {
        // just check and log
        if (rawUserName != null) {
            String normalizedUserName = NamedBean.normalizeUserName(rawUserName);
            if (!rawUserName.equals(normalizedUserName)) {
                log.warn("Requested user name \"{}\" for system name \"{}\" was normalized to \"{}\"",
                        rawUserName, rawSystemName, normalizedUserName);
            }
            if (normalizedUserName != null) {
                NamedBean bean = manager.getByUserName(normalizedUserName);
                if (bean != null && !bean.getSystemName().equals(rawSystemName)) {
                    log.warn("User name \"{}\" already exists as system name \"{}\"", normalizedUserName, bean.getSystemName());
                }
            } else {
                log.warn("User name \"{}\" was normalized into null", rawUserName);
            }
        }
    }

    /**
     * Service method to load a reference to a NamedBean by name, check it for
     * validity, and if need be notify about errors.
     * <p>
     * The name can be empty (method returns null), but if present, has to
     * resolve to an existing bean.
     *
     * @param <T>  The type of NamedBean to return
     * @param name System name, User name, empty string or null
     * @param type A reference to the desired type, typically the name of the
     *             various being loaded, e.g. a Sensor reference
     * @param m    Manager used to check name for validity and existence
     * @return the requested NamedBean or null if name was null
     */
    public <T extends NamedBean> T checkedNamedBeanReference(
        @CheckForNull String name, @Nonnull T type, @Nonnull Manager<T> m) {
        if ( name == null || name.isEmpty() ) {
            return null;
        }
        return m.getNamedBean(name);
    }

    /**
     * Service method to load a NamedBeanHandle to a NamedBean by name, check it
     * for validity, and if need be notify about errors.
     * <p>
     * The name can be empty (method returns null), but if present, has to
     * resolve to an existing bean.
     *
     * @param <T>  The type of NamedBean to return a handle for
     * @param name System name, User name, empty string or null
     * @param type A reference to the desired type, typically the name of the
     *             various being loaded, e.g. a Sensor reference
     * @param m    Manager used to check name for validity and existence
     * @return a handle for the requested NamedBean or null
     */
    public <T extends NamedBean> NamedBeanHandle<T> checkedNamedBeanHandle(
        @CheckForNull String name, @Nonnull T type, @Nonnull Manager<T> m ) {
        if ( name == null || name.isEmpty() ) {
            return null;
        }
        T nb = m.getNamedBean(name);
        if (nb == null) {
            return null;
        }
        return InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(name, nb);
    }

    /**
     * Service method to reference to a NamedBean by name, and if need be notify
     * about errors.
     * <p>
     * The name can be empty (method returns null), but if present, has to
     * resolve to an existing bean. or new).
     *
     * @param <T>  The type of the NamedBean
     * @param name System name, User name, empty string or null
     * @param type A reference to the desired type, typically the name of the
     *             various being loaded, e.g. a Sensor reference; may have null
     *             value, but has to be typed
     * @param m    Manager used to check name for validity and existence
     * @return name if a matching NamedBean can be found or null
     */
    public <T extends NamedBean> String checkedNamedBeanName(@CheckForNull String name, T type, @Nonnull Manager<T> m) {
        if ( name == null || name.isEmpty() ) {
            return null;
        }
        NamedBean nb = m.getNamedBean(name);
        if (nb == null) {
            return null;
        }
        return name;
    }

    /**
     * Load the comment attribute into a NamedBean from one element of a list of
     * Elements defining NamedBeans
     *
     * @param t        The NamedBean being loaded
     * @param beanList List, where each entry is an Element
     * @param i        index of Element in list to examine
     */
    void loadComment(NamedBean t, List<Element> beanList, int i) {
        loadComment(t, beanList.get(i));
    }

    /**
     * Load the comment attribute into a NamedBean from an Element defining a
     * NamedBean
     *
     * @param t    The NamedBean being loaded
     * @param elem The existing Element
     */
    void loadComment(NamedBean t, @Nonnull Element elem) {
        // load comment, if present
        String c = elem.getChildText(STR_COMMENT);
        if (c != null) {
            t.setComment(c);
        }
    }

    /**
     * Convenience method to get a String value from an Attribute in an Element
     * defining a NamedBean.
     *
     * @param elem existing Element
     * @param name name of desired Attribute
     * @return attribute value or null if name is not an attribute of elem
     */
    String getAttributeString( @Nonnull Element elem, String name) {
        Attribute a = elem.getAttribute(name);
        if (a != null) {
            return a.getValue();
        } else {
            return null;
        }
    }

    /**
     * Convenience method to get a boolean value from an Attribute in an Element
     * defining a NamedBean.
     *
     * @param elem existing Element
     * @param name name of desired Attribute
     * @param def  default value for attribute
     * @return value of attribute name or def if name is not an attribute of
     *         elem
     */
    boolean getAttributeBool( @Nonnull Element elem, String name, boolean def) {
        String v = getAttributeString(elem, name);
        if (v == null) {
            return def;
        } else if (def) {
            return !v.equals(STR_FALSE);
        } else {
            return v.equals(STR_TRUE);
        }
    }

    /**
     * Store all key/value properties.
     *
     * @param t    The NamedBean being loaded
     * @param elem The existing Element
     */
    void storeProperties( @Nonnull NamedBean t, @Nonnull Element elem) {
        java.util.Set<String> s = t.getPropertyKeys();
        if (s.isEmpty()) {
            return;
        }
        Element ret = new Element(STR_PROPERTIES);
        elem.addContent(ret);
        s.forEach( key -> {
            Object value = t.getProperty(key);
            Element p = new Element(STR_PROPERTY);
            ret.addContent(p);
            p.addContent(new Element(STR_KEY).setText(key));
            if (value != null) {
                p.addContent(new Element(STR_VALUE)
                        .setAttribute(STR_CLASS, value.getClass().getName())
                        .setText(value.toString())
                );
            }
        });
    }

    /**
     * Load all key/value properties
     *
     * @param t    The NamedBean being loaded
     * @param elem The existing Element
     */
    void loadProperties(NamedBean t, Element elem) {
        Element p = elem.getChild(STR_PROPERTIES);
        if (p == null) {
            return;
        }
        p.getChildren(STR_PROPERTY).forEach( e -> {
            try {
                Class<?> cl;
                Constructor<?> ctor;

                // create key string
                String key = e.getChild("key").getText();

                // check for non-String key.  Warn&proceed if found.
                // Pre-JMRI 4.3, keys in NamedBean parameters could be Objects
                // constructed from Strings, similar to the value code below.
                if (!(e.getChild(STR_KEY).getAttributeValue(STR_CLASS) == null
                        || e.getChild(STR_KEY).getAttributeValue(STR_CLASS).isEmpty()
                        || e.getChild(STR_KEY).getAttributeValue(STR_CLASS).equals("java.lang.String"))) {

                    log.warn("NamedBean {} property key of invalid non-String type {} not supported",
                            t.getSystemName(), e.getChild("key").getAttributeValue(STR_CLASS));
                }

                // create value object
                Object value = null;
                if (e.getChild(STR_VALUE) != null) {
                    cl = Class.forName(e.getChild(STR_VALUE).getAttributeValue(STR_CLASS));
                    ctor = cl.getConstructor(String.class);
                    value = ctor.newInstance(e.getChild(STR_VALUE).getText());
                }

                // store
                t.setProperty(key, value);
            } catch (ClassNotFoundException | NoSuchMethodException
                    | InstantiationException | IllegalAccessException
                    | java.lang.reflect.InvocationTargetException ex) {
                log.error("Error loading properties", ex);
            }
        });
    }

    /**
     * Load all attribute properties from a list.
     * TODO make abstract (remove logging) and move method to XmlAdapter so it can be used from PanelEditorXml et al
     *
     * @param list list of Elements read from xml
     * @param perNode Top-level XML element containing the private, single-node elements of the description.
     *                always null in current application, included to use for Element panel in jmri.jmrit.display
     * @return true if the load was successful
     */
    boolean loadInAdapter(List<Element> list, Element perNode) {
        boolean result = true;
        for (Element item : list) {
            // get the class, hence the adapter object to do loading
            String adapterName = item.getAttribute(STR_CLASS).getValue();
            log.debug("load via {}", adapterName);
            try {
                XmlAdapter adapter = (XmlAdapter) Class.forName(adapterName).getDeclaredConstructor().newInstance();
                // and do it
                adapter.load(item, perNode);
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException
                    | IllegalAccessException | java.lang.reflect.InvocationTargetException
                    | JmriConfigureXmlException e) {
                log.error("Exception while loading {}: {}", item.getName(), e, e);
                result = false;
            }
        }
        return result;
    }

    private static final org.slf4j.Logger log =
        org.slf4j.LoggerFactory.getLogger(AbstractNamedBeanManagerConfigXML.class);

}
