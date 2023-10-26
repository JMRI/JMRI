package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.AnalogActionManager;
import jmri.jmrit.logixng.actions.AnalogActionMemory;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class AnalogActionMemoryXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public AnalogActionMemoryXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        AnalogActionMemory p = (AnalogActionMemory) o;

        Element element = new Element("AnalogActionMemory");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Memory>();
        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        AnalogActionMemory h;
        h = new AnalogActionMemory(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Memory>();
        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "memory");

        InstanceManager.getDefault(AnalogActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnalogActionMemoryXml.class);
}
