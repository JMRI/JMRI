// AcelaSignalHeadXml.java

package jmri.jmrix.acela.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.jmrix.acela.AcelaAddress;
import jmri.jmrix.acela.AcelaNode;
import jmri.jmrix.acela.AcelaSignalHead;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle XML configuration for AcelaSignalHead objects.
 * based upon example of Grapevine by Bob Jacobsen
 * @author Bob Coleman Copyright: Copyright (c) 2009
 */
public class AcelaSignalHeadXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public AcelaSignalHeadXml() {}

    /**
     * Default implementation for storing the contents of a
     * Acela AcelaSignalHead
     * @param o Object to store, of type AcelaSignalHead
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        AcelaSignalHead p = (AcelaSignalHead)o;

        Element element = new Element("signalhead");
        element.setAttribute("class", this.getClass().getName());

        // include contents
        element.setAttribute("systemName", p.getSystemName());
        String tu;
        tu = p.getUserName();
        if (tu != null) {
            element.setAttribute("userName", tu);
        }
        AcelaNode sh = AcelaAddress.getNodeFromSystemName(p.getSystemName());
        int rawaddr = AcelaAddress.getBitFromSystemName(p.getSystemName());
        String shtype = sh.getOutputSignalHeadTypeString(rawaddr);
        element.setAttribute("signalheadType", shtype);

        storeCommon(p, element);

        return element;
    }

    /**
     * Create an AcelaSignalHead
     * @param element Top level Element to unpack.
     * @return true if successful
     */
    public boolean load(Element element) {
        String sys = element.getAttribute("systemName").getValue();
        Attribute a = element.getAttribute("userName");
        SignalHead h;
        if (a == null)
            h = new AcelaSignalHead(sys);
        else
            h = new AcelaSignalHead(sys, a.getValue());
        
        Attribute t = element.getAttribute("signalheadType");
        String shtype;
        if (t == null)
            shtype = "UKNOWN";
        else
            shtype = t.getValue();
        
        loadCommon(h, element);
        
        InstanceManager.signalHeadManagerInstance().register(h);

        AcelaNode sh = AcelaAddress.getNodeFromSystemName(sys);
        int rawaddr = AcelaAddress.getBitFromSystemName(sys);
        sh.setOutputSignalHeadTypeString(rawaddr, shtype);

        return true;
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    static Logger log = LoggerFactory.getLogger(AcelaSignalHeadXml.class.getName());
}
