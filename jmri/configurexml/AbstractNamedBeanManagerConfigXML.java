package jmri.configurexml;

import jmri.NamedBean;

import java.util.List;
import org.jdom.Element;
import org.jdom.Attribute;

/**
 * Provides services for
 * configuring NamedBean manager storage.
 * <P>
 * Not a full abstract implementation by any means, rather
 * this class provides various common service routines
 * to eventual type-specific subclasses.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008
 * @version $Revision: 1.1 $
 * @since 2.3.1
 */
public abstract class AbstractNamedBeanManagerConfigXML implements XmlAdapter {

    public AbstractNamedBeanManagerConfigXML() {
    }

    /**
     * Store the comment parameter from a NamedBean
     * @param t The NamedBean being stored
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
     * Store the username parameter from a NamedBean
     * @param t The NamedBean being stored
     * @param elem The JDOM element for storing the NamedBean
     */
    void storeUserName(NamedBean t, Element elem) {
        String uname = t.getUserName();
        if (uname!=null) elem.setAttribute("userName", uname);
    }
    
    /**
     * Get the username attribute from one element of
     * a list of Elements defining NamedBeans
     * @param beanList List, where each entry is an Element
     * @param i index of Element in list to examine
     */
    String getUserName(List beanList, int i) {
        return getUserName((Element)(beanList.get(i)));
    }
    
    /**
     * Get the username attribute from an Element defining a NamedBean
     * @param elem The existing Element
     */
    String getUserName(Element elem) {
        String userName = null;
        if ( elem.getAttribute("userName") != null)
        userName = elem.getAttribute("userName").getValue();
        return userName;
    }

    /**
     * Load the comment attribute into a NamedBean
     * from one element of
     * a list of Elements defining NamedBeans
     * @param t The NamedBean being loaded
     * @param beanList List, where each entry is an Element
     * @param i index of Element in list to examine
     */
    void loadComment(NamedBean t, List beanList, int i) {
        loadComment(t, (Element)(beanList.get(i)));
    }
    
    /**
     * Load the comment attribute into a NamedBean
     * from an Element defining a NamedBean
     * @param t The NamedBean being loaded
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
     * Get an attribute string value from one element of
     * a list of Elements defining NamedBeans
     * @param beanList List, where each entry is an Element
     * @param i index of Element in list to examine
     * @param name name of desired Attribute
     */
    String getAttributeString(List beanList, int i, String name) {
        return getAttributeString((Element)(beanList.get(i)), name);
    }

    /**
     * Get an attribute string value from an Element defining a NamedBean
     * @param elem The existing Element
     * @param name name of desired Attribute
     */
    String getAttributeString(Element elem, String name) {
        Attribute a = elem.getAttribute(name);
        if (a!=null)
            return a.getValue();
        else
            return null;
    }
    
    /**
     * Get an attribute boolean value from one element of
     * a list of Elements defining NamedBeans
     * @param beanList List, where each entry is an Element
     * @param i Index of Element in list to examine
     * @param name Name of desired Attribute
     * @param def Default value for attribute
     */
    boolean getAttributeBool(List beanList, int i, String name, boolean def) {
        return getAttributeBool((Element)(beanList.get(i)), name, def);
    }

    /**
     * Get an attribute boolean value from an Element defining a NamedBean
     * @param elem The existing Element
     * @param name Name of desired Attribute
     * @param def Default value for attribute
     */
    boolean getAttributeBool(Element elem, String name, boolean def) {
        String v = getAttributeString(elem, name);
        if (v == null)
            return def;
        else
            if (def) {
                return !v.equals("false");
            } else {
                return v.equals("true");
            }
    }
}