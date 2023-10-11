package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.actions.ActionOBlock;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for OBlock objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright (C) 2021
 */
public class ActionOBlockXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionOBlockXml() {
    }

    /**
     * Default implementation for storing the contents of an OBlock
     *
     * @param o Object to store, of type ActionOBlock
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionOBlock p = (ActionOBlock) o;

        Element element = new Element("ActionOBlock");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<OBlock>();
        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));

        var selectMemoryNamedBeanXml = new LogixNG_SelectNamedBeanXml<Memory>();
        element.addContent(selectMemoryNamedBeanXml.store(p.getSelectMemoryNamedBean(), "memoryNamedBean"));

        var selectEnumXml = new LogixNG_SelectEnumXml<ActionOBlock.DirectOperation>();
        element.addContent(selectEnumXml.store(p.getSelectEnum(), "operation"));

        element.addContent(new Element("dataAddressing").addContent(p.getDataAddressing().name()));
        element.addContent(new Element("dataReference").addContent(p.getDataReference()));
        element.addContent(new Element("dataLocalVariable").addContent(p.getDataLocalVariable()));
        element.addContent(new Element("dataFormula").addContent(p.getDataFormula()));

        element.addContent(new Element("oblockValue").addContent(p.getOBlockValue()));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionOBlock h = new ActionOBlock(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<OBlock>();
        var selectEnumXml = new LogixNG_SelectEnumXml<ActionOBlock.DirectOperation>();

        var selectMemoryNamedBeanXml = new LogixNG_SelectNamedBeanXml<Memory>();

        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "oblock");
        selectMemoryNamedBeanXml.load(shared.getChild("memoryNamedBean"), h.getSelectMemoryNamedBean());

        selectEnumXml.load(shared.getChild("operation"), h.getSelectEnum());
        selectEnumXml.loadLegacy(
                shared, h.getSelectEnum(),
                "operationAddressing",
                "operationDirect",
                "operationReference",
                "operationLocalVariable",
                "operationFormula");

        try {
            Element elem = shared.getChild("dataAddressing");
            if (elem != null) {
                h.setDataAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            elem = shared.getChild("dataReference");
            if (elem != null) h.setDataReference(elem.getTextTrim());

            elem = shared.getChild("dataLocalVariable");
            if (elem != null) h.setDataLocalVariable(elem.getTextTrim());

            elem = shared.getChild("dataFormula");
            if (elem != null) h.setDataFormula(elem.getTextTrim());


            elem = shared.getChild("oblockValue");
            if (elem != null) h.setOBlockValue(elem.getTextTrim());

        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionOBlockXml.class);
}
