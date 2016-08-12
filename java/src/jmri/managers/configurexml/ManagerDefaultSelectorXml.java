package jmri.managers.configurexml;

import java.util.List;
import jmri.InstanceManager;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.managers.ManagerDefaultSelector;
import org.jdom2.Element;

/**
 * Handle XML persistence of ManagerDefaultSelector
 * <P>
 * This class is named as being the persistent form of the
 * ManagerDefaultSelector class, but there's no object of that form created or
 * used.
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
     *
     * @param o Object to store, of type ManagerDefaultSelector
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element e = new Element("managerdefaults");
        e.setAttribute("class", getClass().getName());
        for (Class<?> c : InstanceManager.getDefault(ManagerDefaultSelector.class).defaults.keySet()) {
            String n = InstanceManager.getDefault(ManagerDefaultSelector.class).defaults.get(c);
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

    @Override
    public boolean load(Element shared, Element perNode) {
        List<Element> list = shared.getChildren("setting");

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
            InstanceManager.getDefault(ManagerDefaultSelector.class).setDefault(c, name);

        }
        // put into effect
        InstanceManager.getDefault(ManagerDefaultSelector.class).configure();
        InstanceManager.getOptionalDefault(jmri.ConfigureManager.class).registerPref(InstanceManager.getDefault(ManagerDefaultSelector.class));
        return true;
    }

    /**
     * Doesn't need to do anything, shouldn't get invoked
     *
     * @param element Top level Element to unpack.
     * @param o       PanelEditor as an Object
     */
    public void load(Element element, Object o) {
    }

}
