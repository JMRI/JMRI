// AbstractNamedBeanManagerConfigXML.java

package jmri.managers.configurexml;

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
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * @version $Revision: 1.4 $
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
     * @param t The NamedBean being stored
     * @param elem The JDOM element for storing the NamedBean
     */
    protected void storeCommon(NamedBean t, Element elem) {
        storeUserName(t, elem);
        storeComment(t, elem);
    }
    
    /**
     * Load common items:
     * <ul>
     * <li>comment
     * </ul>
     * The username is not loaded, because it 
     * had to be provided in the ctor earlier.
     *
     * @param t The NamedBean being loaded
     * @param elem The JDOM element containing the NamedBean
     */
    protected void loadCommon(NamedBean t, Element elem) {
        loadComment(t, elem);
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
     * Store the username parameter from a NamedBean.
     * <ul>
     * <li>Before 2.9.6, this was an attribute
     * <li>Starting in 2.9.6, this was stored as both attribute and element
     * <li>Starting in 3.1/2.11.1, this will be just an element
     * </ul>
     * @param t The NamedBean being stored
     * @param elem The JDOM element for storing the NamedBean
     */
    void storeUserName(NamedBean t, Element elem) {
        String uname = t.getUserName();
        if (uname!=null && uname.length() > 0) {
            elem.setAttribute("userName", uname); // doing this for compatibility during 2.9.* series
            elem.addContent(new Element("userName").addContent(uname));
        }
    }
    
    /**
     * Get the username attribute from one element of
     * a list of Elements defining NamedBeans
     * @param beanList List, where each entry is an Element
     * @param i index of Element in list to examine
     */
    protected String getUserName(List<Element> beanList, int i) {
        return getUserName(beanList.get(i));
    }
    
    /**
     * Get the user name from an Element defining a NamedBean
     * <ul>
     * <li>Before 2.9.6, this was an attribute
     * <li>Starting in 2.9.6, this was stored as both attribute and element
     * <li>Starting in 3.1/2.11.1, this will be just an element
     * </ul>
     * @param elem The existing Element
     */
    protected String getUserName(Element elem) {
        if ( elem.getChild("userName") != null) {
            elem.getChild("userName").getText();
        }
        if ( elem.getAttribute("userName") != null) {
            return elem.getAttribute("userName").getValue();
        } return null;
    }

    /**
     * Get the system name from an Element defining a NamedBean
     * <ul>
     * <li>Before 2.9.6, this was an attribute
     * <li>Starting in 2.9.6, this was stored as both attribute and element
     * <li>Starting in 3.1/2.10.1, this will be just an element
     * </ul>
     * @param elem The existing Element
     */
    protected String getSystemName(Element elem) {
        if ( elem.getChild("systemName") != null) {
            elem.getChild("systemName").getText();
        }
        if ( elem.getAttribute("systemName") != null) {
            return elem.getAttribute("systemName").getValue();
        } return null;
    }

    /**
     * Load the comment attribute into a NamedBean
     * from one element of
     * a list of Elements defining NamedBeans
     * @param t The NamedBean being loaded
     * @param beanList List, where each entry is an Element
     * @param i index of Element in list to examine
     */
    void loadComment(NamedBean t, List<Element> beanList, int i) {
        loadComment(t, beanList.get(i));
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