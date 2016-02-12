// SerialSignalHeadXml.java
package jmri.jmrix.grapevine.configurexml;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.jmrix.grapevine.SerialSignalHead;
import jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for Grapevine SerialSignalHead objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2007, 2008
 * @version $Revision$
 */
public class SerialSignalHeadXml extends AbstractNamedBeanManagerConfigXML {

    public SerialSignalHeadXml() {
    }

    /**
     * Default implementation for storing the contents of a Grapevine
     * SerialSignalHead
     *
     * @param o Object to store, of type SerialSignalHead
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        SerialSignalHead p = (SerialSignalHead) o;

        Element element = new Element("signalhead");
        element.setAttribute("class", this.getClass().getName());

        // include contents
        element.setAttribute("systemName", p.getSystemName());

        storeCommon(p, element);

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        String sys = shared.getAttribute("systemName").getValue();
        Attribute a = shared.getAttribute("userName");
        SignalHead h;
        if (a == null) {
            h = new SerialSignalHead(sys);
        } else {
            h = new SerialSignalHead(sys, a.getValue());
        }

        loadCommon(h, shared);

        InstanceManager.signalHeadManagerInstance().register(h);
        return true;
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    private final static Logger log = LoggerFactory.getLogger(SerialSignalHeadXml.class.getName());
}
