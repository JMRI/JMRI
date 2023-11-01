package jmri.jmrit.logixng.actions.configurexml;

import java.util.ResourceBundle;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.ActionBlock;
import jmri.jmrit.logixng.util.configurexml.*;

import org.jdom2.Element;

/**
 * Handle XML configuration for Block objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright (C) 2021
 */
public class ActionBlockXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.logixng.actions.ActionBundle");

    public ActionBlockXml() {
    }

    /**
     * Default implementation for storing the contents of a Block
     *
     * @param o Object to store, of type TriggerBlock
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionBlock p = (ActionBlock) o;

        Element element = new Element("ActionBlock");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Block>();
        var selectEnumXml = new LogixNG_SelectEnumXml<ActionBlock.DirectOperation>();
        var selectStringXml = new LogixNG_SelectStringXml();

        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));
        element.addContent(selectEnumXml.store(p.getSelectEnum(), "operation"));
        element.addContent(selectStringXml.store(p.getSelectBlockValue(), "blockValueData"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionBlock h = new ActionBlock(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Block>();
        var selectEnumXml = new LogixNG_SelectEnumXml<ActionBlock.DirectOperation>();
        var selectStringXml = new LogixNG_SelectStringXml();

        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "block");

        selectEnumXml.load(shared.getChild("operation"), h.getSelectEnum());
        selectEnumXml.loadLegacy(
                shared, h.getSelectEnum(),
                "operationAddressing",
                "operationDirect",
                "operationReference",
                "operationLocalVariable",
                "operationFormula");

        selectStringXml.load(shared.getChild("blockValueData"), h.getSelectBlockValue());
        selectStringXml.loadLegacy(shared, h.getSelectBlockValue(),
                "dataAddressing",
                "blockValue",
                "dataReference",
                "dataLocalVariable",
                "dataFormula");

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionBlockXml.class);
}
