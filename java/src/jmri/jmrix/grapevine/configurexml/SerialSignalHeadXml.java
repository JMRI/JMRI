// SerialSignalHeadXml.java

package jmri.jmrix.grapevine.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.jmrix.grapevine.SerialSignalHead;
import jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle XML configuration for Grapevine SerialSignalHead objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2007, 2008
 * @version $Revision$
 */
public class SerialSignalHeadXml extends AbstractNamedBeanManagerConfigXML {

    public SerialSignalHeadXml() {}

    /**
     * Default implementation for storing the contents of a
     * Grapevine SerialSignalHead
     * @param o Object to store, of type SerialSignalHead
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        SerialSignalHead p = (SerialSignalHead)o;

        Element element = new Element("signalhead");
        element.setAttribute("class", this.getClass().getName());

        // include contents
        element.setAttribute("systemName", p.getSystemName());

        storeCommon(p, element);

        return element;
    }

    /**
     * Create a Grapevine SerialSignalHead
     * @param element Top level Element to unpack.
     * @return true if successful
     */
	public boolean load(Element element) {
        String sys = element.getAttribute("systemName").getValue();
        Attribute a = element.getAttribute("userName");
        SignalHead h;
        if (a == null)
            h = new SerialSignalHead(sys);
        else
            h = new SerialSignalHead(sys, a.getValue());
        
        loadCommon(h, element);
        
        InstanceManager.signalHeadManagerInstance().register(h);
        return true;
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    static Logger log = LoggerFactory.getLogger(SerialSignalHeadXml.class.getName());
}
