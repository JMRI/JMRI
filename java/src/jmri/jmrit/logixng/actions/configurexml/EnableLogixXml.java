package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.EnableLogix;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;
import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class EnableLogixXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public EnableLogixXml() {
    }

    /**
     * Default implementation for storing the contents of a EnableLogix
     *
     * @param o Object to store, of type EnableLogix
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        EnableLogix p = (EnableLogix) o;

        Element element = new Element("EnableLogix");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Logix>();
        var selectEnumXml = new LogixNG_SelectEnumXml<EnableLogix.Operation>();

        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));
        element.addContent(selectEnumXml.store(p.getSelectEnum(), "operation"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        EnableLogix h = new EnableLogix(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Logix>();
        var selectEnumXml = new LogixNG_SelectEnumXml<EnableLogix.Operation>();

        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "logix");

        selectEnumXml.load(shared.getChild("operation"), h.getSelectEnum());
        selectEnumXml.loadLegacy(
                shared, h.getSelectEnum(),
                "operationAddressing",
                "operationDirect",
                "operationReference",
                "operationLocalVariable",
                "operationFormula");

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EnableLogixXml.class);
}
