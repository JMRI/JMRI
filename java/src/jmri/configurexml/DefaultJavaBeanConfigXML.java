// DefaultJavaBeanConfigXML.java

package jmri.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.beans.*;
import java.lang.reflect.*;

import org.jdom.Element;
import org.jdom.Attribute;

/**
 * Provides services for storing Java Beans to XML
 * using reflection.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * @version $Revision$
 * @since 2.3.1
 */
public class DefaultJavaBeanConfigXML extends jmri.configurexml.AbstractXmlAdapter {

    public DefaultJavaBeanConfigXML() {
    }

    public boolean load(Element e) throws Exception {
        return true;
    }
    
    public void load(Element e, Object o) throws Exception {
    }
    
    Object unpack(Element e) throws Exception {
        String classname = e.getAttributeValue("beanClass");
        
        Class<?> cl = Class.forName(classname);
        Constructor<?> ctor = cl.getConstructor(new Class<?>[] {});

        Object o = ctor.newInstance(new Object[] {});
        
        try {
            // reflect through and add parameters
            BeanInfo b = Introspector.getBeanInfo(o.getClass());
            PropertyDescriptor[] properties = b.getPropertyDescriptors();
            
            // add properties
            @SuppressWarnings("unchecked")
            List<Element> children = e.getChildren("property");
            for (int i = 0; i<children.size(); i++) {
                // unpack XML
                Element property = children.get(i);
                Element eName = property.getChild("name");
                Element eValue = property.getChild("value");
                String name = eName.getText();
                String value = eValue.getText();
                String type = eName.getAttributeValue("type");
                
                // find matching method
                for (int j = 0; j<properties.length; j++) {
                    if (properties[j].getName().equals(name)) {
                        // match, set this one by first finding method
                        Method m = properties[j].getWriteMethod();
                        
                        // sort by type
                        if (type.equals("class java.lang.String"))
                            m.invoke(o,new Object[]{value});
                        else if (type.equals("int"))
                            m.invoke(o,new Object[]{Integer.valueOf(value)});
                        else
                            log.error("Can't handle type: "+type);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Partial load due to exception: "+ex);
            ex.printStackTrace();
        }

        return o;
    }
    
    public Element store(Object o) {
        Element e = new Element("javabean");
        e.setAttribute("class", this.getClass().getName());
        e.setAttribute("beanClass", o.getClass().getName());
        
        try {
            // reflect through and add parameters
            BeanInfo b = Introspector.getBeanInfo(o.getClass());
            PropertyDescriptor[] properties = b.getPropertyDescriptors();
            
            for (int i = 0; i<properties.length; i++) {
                if (properties[i].getName().equals("class")) {
                    // we skip this one
                    continue;
                }
                if (properties[i].getPropertyType() == null) {
                    log.warn("skipping property with null type: "+properties[i].getName());
                    continue;
                }
                Element p = new Element("property");
                Element n = new Element("name");
                n.addContent(properties[i].getName());
                n.setAttribute("type",properties[i].getPropertyType().toString()); 
                p.addContent(n);
                Element v = new Element("value");
                if (properties[i].getReadMethod() != null) {
                    Object value = properties[i].getReadMethod().invoke(o, (Object[]) null);
                    if (value != null) {
                        v.addContent(value.toString());
                    }
                }
                p.addContent(v);
                e.addContent(p);
            }
        } catch (Exception ex) {
            log.error("Partial store due to exception: "+ex);
            ex.printStackTrace();
        }

        return e;
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

    static Logger log = LoggerFactory.getLogger(DefaultJavaBeanConfigXML.class.getName());
}
