package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.Error;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class ErrorXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ErrorXml() {
    }

    /**
     * Default implementation for storing the contents of an Error
     *
     * @param o Object to store, of type Error
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Error p = (Error) o;

        Element element = new Element("Error");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(new Element("message").addContent(p.getMessage()));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        Error h = new Error(sys, uname);

        loadCommon(h, shared);

        Element elem = shared.getChild("message");  // NOI18N
        h.setMessage((elem != null) ? elem.getValue() : "");

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ErrorXml.class);
}
