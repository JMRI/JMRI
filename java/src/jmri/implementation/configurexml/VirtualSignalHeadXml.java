package jmri.implementation.configurexml;

import org.apache.log4j.Logger;
import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.implementation.VirtualSignalHead;

import org.jdom.Element;

/**
 * Handle XML configuration for VirtualSignalHead objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2005, 2008
 * @version $Revision$
 */
public class VirtualSignalHeadXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

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
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);
        
        return element;
    }

    /**
     * Create a VirtualSignalHead
     * @param element Top level Element to unpack.
     * @return true if successful
     */
    public boolean load(Element element) {
        // put it together
        String sys = getSystemName(element);
        String uname = getUserName(element);
        SignalHead h;
        if (uname == null)
            h = new VirtualSignalHead(sys);
        else
            h = new VirtualSignalHead(sys, uname);

        loadCommon(h, element);
        
        InstanceManager.signalHeadManagerInstance().register(h);
        return true;
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    static Logger log = Logger.getLogger(VirtualSignalHeadXml.class.getName());
}
