package jmri.jmrix.grapevine.configurexml;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.jmrix.grapevine.SerialSignalHead;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for Grapevine SerialSignalHead objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2007, 2008
 */
public class SerialSignalHeadXml extends AbstractNamedBeanManagerConfigXML {

    private GrapevineSystemConnectionMemo memo = null;

    public SerialSignalHeadXml() {
       memo = InstanceManager.getDefault(GrapevineSystemConnectionMemo.class);
    }

    /**
     * Default implementation for storing the contents of a Grapevine
     * SerialSignalHead
     *
     * @param o Object to store, of type SerialSignalHead
     * @return Element containing the complete info
     */
    @Override
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
            h = new SerialSignalHead(sys,memo);
        } else {
            h = new SerialSignalHead(sys, a.getValue(),memo);
        }

        loadCommon(h, shared);

        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
        return true;
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    private final static Logger log = LoggerFactory.getLogger(SerialSignalHeadXml.class);
}
