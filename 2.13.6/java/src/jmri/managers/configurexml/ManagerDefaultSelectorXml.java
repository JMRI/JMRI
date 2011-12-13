package jmri.managers.configurexml;

import org.jdom.Element;
import java.util.*;

import jmri.configurexml.*;
import jmri.managers.ManagerDefaultSelector;

/**
 * Handle XML persistence of ManagerDefaultSelector
 * <P>
 * This class is named as being the persistent form of the
 * ManagerDefaultSelector class, but there's no object of that
 * form created or used.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2010
 * @version $Revision$
 * @since 2.9.7
 */
public class ManagerDefaultSelectorXml extends AbstractXmlAdapter {

    public ManagerDefaultSelectorXml() {
    }

    /**
     * Default implementation for storing the static contents of a
     * ManagerDefaultSelector
     * @param o Object to store, of type ManagerDefaultSelector
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element e = new Element("managerdefaults");
        e.setAttribute("class", getClass().getName());
        for (Class<?> c : ManagerDefaultSelector.instance.defaults.keySet()) {
            String n = ManagerDefaultSelector.instance.defaults.get(c);
            Element p = new Element("setting");
            Element key = new Element("key");
            key.addContent(c.getName());
            Element value = new Element("value");
            value.addContent(n);
            p.addContent(key);
            p.addContent(value);
            e.addContent(p);
        }
        return e;
    }

    public boolean load(Element e) {
        @SuppressWarnings("unchecked")
        List<Element> list = e.getChildren("setting");
        
        for (Element s : list) {
            String name = s.getChild("value").getText();
            String className = s.getChild("key").getText();
            Class<?> c = null;
            try {
                c = Class.forName(className);
            } catch (java.lang.ClassNotFoundException ex) {
                continue;
            } catch (java.lang.NoClassDefFoundError ex) {
                continue;
            }
            jmri.managers.ManagerDefaultSelector.instance.setDefault(c,name);
            
        }
        // put into effect
        jmri.managers.ManagerDefaultSelector.instance.configure();
        jmri.InstanceManager.configureManagerInstance().registerPref(jmri.managers.ManagerDefaultSelector.instance);
    	return true;
    }

    /**
     * Doesn't need to do anything, shouldn't get invoked
     * @param element Top level Element to unpack.
     * @param o  PanelEditor as an Object
     */
    public void load(Element element, Object o) {
    }

}