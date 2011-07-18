package jmri.managers.configurexml;

import jmri.InstanceManager;
import jmri.SignalMast;
import jmri.managers.DefaultSignalMastManager;

import java.util.List;

import org.jdom.Element;

/**
 * Handle XML configuration for a DefaultSignalMastManager objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * @version $Revision$
 */
public class DefaultSignalMastManagerXml 
            extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DefaultSignalMastManagerXml() {}

    /**
     * Default implementation for storing the contents of a
     * DefaultSignalMastManager
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        DefaultSignalMastManager m = (DefaultSignalMastManager)o;

        Element element = new Element("signalmasts");
        element.setAttribute("class", this.getClass().getName());

        // include contents
        List<String> names = m.getSystemNameList();
        for (int i = 0; i < names.size(); i++) {
            Element e = new Element("signalmast");
            SignalMast p = m.getSignalMast(names.get(i));
            e.setAttribute("systemName", p.getSystemName()); // deprecated for 2.9.* series
            e.addContent(new Element("systemName").addContent(p.getSystemName()));
            storeCommon(p, e);
            element.addContent(e);
        }
        return element;
    }

    /**
     * Create a DefaultSignalMastManager
     * @param element Top level Element to unpack.
     * @return true if successful
     */
    public boolean load(Element element) {
        // loop over contained signalmast elements
        @SuppressWarnings("unchecked")
        List<Element> list = element.getChildren("signalmast");

        for (int i = 0; i < list.size(); i++) {
            SignalMast m;
            Element e = list.get(i);
            String sys = getSystemName(e);
            m = InstanceManager.signalMastManagerInstance()
                        .provideSignalMast(sys);
            
            if (getUserName(e) != null)
                m.setUserName(getUserName(e));
            
            loadCommon(m, e);
        }
        
        return true;
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultSignalMastManagerXml.class.getName());
}