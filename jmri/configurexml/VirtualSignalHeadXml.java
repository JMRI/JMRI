package jmri.configurexml;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.VirtualSignalHead;
import jmri.configurexml.AbstractNamedBeanManagerConfigXML;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle XML configuration for VirtualSignalHead objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2005, 2008
 * @version $Revision: 1.4 $
 */
public class VirtualSignalHeadXml extends AbstractNamedBeanManagerConfigXML {

    public VirtualSignalHeadXml() {}

    /**
     * Default implementation for storing the contents of a
     * VirtualSignalHead
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        VirtualSignalHead p = (VirtualSignalHead)o;

        Element element = new Element("signalhead");
        element.setAttribute("class", this.getClass().getName());

        // include contents
        element.setAttribute("systemName", p.getSystemName());

        storeCommon(p, element);
        
        return element;
    }

    /**
     * Create a VirtualSignalHead
     * @param element Top level Element to unpack.
     */
    public void load(Element element) {
        // put it together
        String sys = element.getAttribute("systemName").getValue();
        Attribute a = element.getAttribute("userName");
        SignalHead h;
        if (a == null)
            h = new VirtualSignalHead(sys);
        else
            h = new VirtualSignalHead(sys, a.getValue());

        loadCommon(h, element);
        
        InstanceManager.signalHeadManagerInstance().register(h);
        return;
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(VirtualSignalHeadXml.class.getName());
}