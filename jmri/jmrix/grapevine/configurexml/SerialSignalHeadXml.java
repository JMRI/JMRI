// SerialSignalHeadXml.java

package jmri.jmrix.grapevine.configurexml;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.configurexml.XmlAdapter;
import jmri.jmrix.grapevine.SerialSignalHead;
import java.util.List;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle XML configuration for Grapevine SerialSignalHead objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2007
 * @version $Revision: 1.1 $
 */
public class SerialSignalHeadXml implements XmlAdapter {

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
        if (p.getUserName() != null) element.setAttribute("userName", p.getUserName());

        return element;
    }

    /**
     * Create a Grapevine SerialSignalHead
     * @param element Top level Element to unpack.
     */
    public void load(Element element) {
        List l = element.getChildren();
        String sys = element.getAttribute("systemName").getValue();
        Attribute a = element.getAttribute("userName");
        SignalHead h;
        if (a == null)
            h = new SerialSignalHead(sys);
        else
            h = new SerialSignalHead(sys, a.getValue());
        InstanceManager.signalHeadManagerInstance().register(h);
        return;
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialSignalHeadXml.class.getName());
}