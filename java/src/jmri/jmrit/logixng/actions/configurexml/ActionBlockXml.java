package jmri.jmrit.logixng.actions.configurexml;

import java.awt.GraphicsEnvironment;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.Block;
import jmri.BlockManager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.actions.ActionBlock;
import jmri.jmrit.logixng.actions.ActionTurnout;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;
import jmri.jmrit.logixng.util.parser.ParserException;

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

        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));
        element.addContent(selectEnumXml.store(p.getSelectEnum(), "operation"));

        element.addContent(new Element("dataAddressing").addContent(p.getDataAddressing().name()));
        element.addContent(new Element("dataReference").addContent(p.getDataReference()));
        element.addContent(new Element("dataLocalVariable").addContent(p.getDataLocalVariable()));
        element.addContent(new Element("dataFormula").addContent(p.getDataFormula()));

        element.addContent(new Element("blockValue").addContent(p.getBlockValue()));

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


            elem = shared.getChild("blockValue");
            if (elem != null) h.setBlockValue(elem.getTextTrim());

            // deprecated 4.23.5 remove 4.25.1
            elem = shared.getChild("blockConstant");
            if (elem != null) h.setBlockValue(elem.getTextTrim());

            // deprecated 4.23.5 remove 4.25.1
            elem = shared.getChild("blockMemory");
            if (elem != null) {
                String memoryName = elem.getTextTrim();
                h.setBlockValue(">>> " + elem.getTextTrim() + " <<<");
                if (!GraphicsEnvironment.isHeadless() && !Boolean.getBoolean("jmri.test.no-dialogs")) {
                    JOptionPane.showMessageDialog(null,
                            rb.getString("ActionBlock_MemoryChange"),
                            rb.getString("ActionBlock_MemoryTitle") + " " + memoryName,
                            JOptionPane.WARNING_MESSAGE);
                }
            }

        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionBlockXml.class);
}
