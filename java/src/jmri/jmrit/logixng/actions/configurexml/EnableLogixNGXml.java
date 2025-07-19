package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.EnableLogixNG;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;
import org.jdom2.Element;

/**
 * Handle XML configuration for EnableLogixNG objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2024
 */
public class EnableLogixNGXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public EnableLogixNGXml() {
    }

    /**
     * Default implementation for storing the contents of a EnableLogixNG
     *
     * @param o Object to store, of type EnableLogixNG
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        EnableLogixNG p = (EnableLogixNG) o;

        Element element = new Element("EnableLogixNG");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<LogixNG>();
        var selectEnumXml = new LogixNG_SelectEnumXml<EnableLogixNG.Operation>();

        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));
        element.addContent(selectEnumXml.store(p.getSelectEnum(), "operation"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        EnableLogixNG h = new EnableLogixNG(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<LogixNG>();
        var selectEnumXml = new LogixNG_SelectEnumXml<EnableLogixNG.Operation>();

        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        selectEnumXml.load(shared.getChild("operation"), h.getSelectEnum());

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EnableLogixXml.class);
}
