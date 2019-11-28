package jmri.implementation.configurexml;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.implementation.VirtualSignalHead;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for VirtualSignalHead objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2005, 2008
 */
public class VirtualSignalHeadXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public VirtualSignalHeadXml() {
    }

    /**
     * Default implementation for storing the contents of a VirtualSignalHead.
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        VirtualSignalHead p = (VirtualSignalHead) o;

        Element element = new Element("signalhead");
        element.setAttribute("class", this.getClass().getName());

        // include contents
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // put it together
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        SignalHead h;
        if (uname == null) {
            h = new VirtualSignalHead(sys);
        } else {
            h = new VirtualSignalHead(sys, uname);
        }

        loadCommon(h, shared);

        SignalHead existingBean =
                InstanceManager.getDefault(jmri.SignalHeadManager.class)
                        .getBeanBySystemName(sys);

        if ((existingBean != null) && (existingBean != h)) {
            log.error("systemName is already registered: {}", sys);
        } else {
            InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
        }

        return true;
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    private final static Logger log = LoggerFactory.getLogger(VirtualSignalHeadXml.class);

}
