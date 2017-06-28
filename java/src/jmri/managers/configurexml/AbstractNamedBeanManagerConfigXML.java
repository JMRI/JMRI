package jmri.managers.configurexml;

import java.lang.reflect.Constructor;
import java.util.List;
import javax.annotation.Nonnull;
import jmri.NamedBean;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides services for configuring NamedBean manager storage.
 * <P>
 * Not a full abstract implementation by any means, rather this class provides
 * various common service routines to eventual type-specific subclasses.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * @since 2.3.1
 */
public abstract class AbstractNamedBeanManagerConfigXML extends jmri.configurexml.AbstractXmlAdapter {

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
    protected void storeCommon(NamedBean t, Element elem) {
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
    void storeComment(NamedBean t, Element elem) {
        // add comment, if present
        if (t.getComment() != null) {
            Element c = new Element("comment");
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
    void storeUserName(NamedBean t, Element elem) {
        String uname = t.getUserName();
        if (uname != null && uname.length() > 0) {
            elem.addContent(new Element("userName").addContent(uname));
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
     * Get the user name from an Element defining a NamedBean.
     * <ul>
     * <li>Before 2.9.6, this was stored as an attribute
     * <li>Starting in 2.9.6, this was stored as both attribute and element
     * <li>Starting in 3.1/2.11.1, this is stored as an element
     * </ul>
     *
     * @param elem The existing Element
     * @return the user name of bean or null
     */
    protected String getUserName(Element elem) {
        if (elem.getChild("userName") != null) {
            return elem.getChild("userName").getText();
        }
        if (elem.getAttribute("userName") != null) {
            return elem.getAttribute("userName").getValue();
        }
        return null;
    }

    /**
     * Get the system name from an Element defining a NamedBean
     * <ul>
     * <li>Before 2.9.6, this was stored as an attribute
     * <li>Starting in 2.9.6, this was stored as both attribute and element
     * <li>Starting in 3.1/2.10.1, this is stored as an element
     * </ul>
     *
     * @param elem The existing Element
     * @return the system name or null if not defined
     */
    protected String getSystemName(Element elem) {
        if (elem.getChild("systemName") != null) {
            return elem.getChild("systemName").getText();
        }
        if (elem.getAttribute("systemName") != null) {
            return elem.getAttribute("systemName").getValue();
        }
        return null;
    }

    /**
     * Common service routine to check for and report on
     * normalization (errors) in the incoming NamedBean's 
     * name(s)
     * <p>
     * If NamedBeam.normalizeUserName changes, this may want to be updated.
     * <p>
     * Right now, this just logs. Someday, perhaps it should notify
     * upward of found issues by throwing an exception.
     *
     * Package-level access to allow testing
     *
     * @param rawSystemName The proposed system name string, before normalization
     * @param rawUserName The proposed user name string, before normalization
     * @param manager The NamedBeanManager that will be storing this
     */
    void checkNameNormalization(@Nonnull String rawSystemName, String rawUserName, @Nonnull jmri.Manager manager) {
        // just check and log
        if (rawUserName!= null) {
            String normalizedUserName = NamedBean.normalizeUserName(rawUserName);
            if (! rawUserName.equals(normalizedUserName)) {
                log.warn("Requested user name \"{}\" for system name \"{}\" was normalized to \"{}\"",
                        rawUserName, rawSystemName, normalizedUserName);
            }
            
            NamedBean bean = manager.getBeanByUserName(normalizedUserName);
            if (bean != null && !bean.getSystemName().equals(rawSystemName)) {
                log.warn("User name \"{}\" already exists as system name \"{}\"", normalizedUserName, bean.getSystemName());
            }
        }
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
    void loadComment(NamedBean t, Element elem) {
        // load comment, if present
        String c = elem.getChildText("comment");
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
    String getAttributeString(Element elem, String name) {
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
    boolean getAttributeBool(Element elem, String name, boolean def) {
        String v = getAttributeString(elem, name);
        if (v == null) {
            return def;
        } else if (def) {
            return !v.equals("false");
        } else {
            return v.equals("true");
        }
    }

    /**
     * Store all key/value properties.
     *
     * @param t    The NamedBean being loaded
     * @param elem The existing Element
     */
    void storeProperties(NamedBean t, Element elem) {
        java.util.Set<String> s = t.getPropertyKeys();
        if (s.isEmpty()) {
            return;
        }
        Element ret = new Element("properties");
        elem.addContent(ret);
        for (String key : s) {
            Object value = t.getProperty(key);
            Element p = new Element("property");
            ret.addContent(p);
            p.addContent(new Element("key")
                    .setText(key)
            );
            if (value != null) {
                p.addContent(new Element("value")
                        .setAttribute("class", value.getClass().getName())
                        .setText(value.toString())
                );
            }
        }
    }

    /**
     * Load all key/value properties
     *
     * @param t    The NamedBean being loaded
     * @param elem The existing Element
     */
    void loadProperties(NamedBean t, Element elem) {
        Element p = elem.getChild("properties");
        if (p == null) {
            return;
        }
        for (Object next : p.getChildren("property")) {
            Element e = (Element) next;

            try {
                Class<?> cl;
                Constructor<?> ctor;

                // create key string
                String key = e.getChild("key").getText();

                // check for non-String key.  Warn&proceed if found.
                // Pre-JMRI 4.3, keys in NamedBean parameters could be Objects
                // constructed from Strings, similar to the value code below.
                if (!(e.getChild("key").getAttributeValue("class") == null
                        || e.getChild("key").getAttributeValue("class").equals("")
                        || e.getChild("key").getAttributeValue("class").equals("java.lang.String"))) {

                    log.warn("NamedBean {} property key of invalid non-String type {} not supported",
                            t.getSystemName(), e.getChild("key").getAttributeValue("class"));
                }

                // create value object
                Object value = null;
                if (e.getChild("value") != null) {
                    cl = Class.forName(e.getChild("value").getAttributeValue("class"));
                    ctor = cl.getConstructor(new Class<?>[]{String.class});
                    value = ctor.newInstance(new Object[]{e.getChild("value").getText()});
                }

                // store
                t.setProperty(key, value);
            } catch (ClassNotFoundException | NoSuchMethodException
                    | InstantiationException | IllegalAccessException
                    | java.lang.reflect.InvocationTargetException ex) {
                log.error("Error loading properties", ex);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractNamedBeanManagerConfigXML.class.getName());
}
