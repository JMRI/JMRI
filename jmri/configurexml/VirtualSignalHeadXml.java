package jmri.configurexml;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.VirtualSignalHead;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle XML configuration for VirtualSignalHead objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2005
 * @version $Revision: 1.2 $
 */
public class VirtualSignalHeadXml implements XmlAdapter {

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
        element.addAttribute("class", this.getClass().getName());

        // include contents
        element.addAttribute("systemName", p.getSystemName());
        if (p.getUserName() != null) element.addAttribute("userName", p.getUserName());

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
        InstanceManager.signalHeadManagerInstance().register(h);
        return;
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(VirtualSignalHeadXml.class.getName());
}