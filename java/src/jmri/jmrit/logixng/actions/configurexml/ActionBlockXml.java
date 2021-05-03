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

        NamedBeanHandle block = p.getBlock();
        if (block != null) {
            element.addContent(new Element("block").addContent(block.getName()));
        }

        element.addContent(new Element("addressing").addContent(p.getAddressing().name()));
        element.addContent(new Element("reference").addContent(p.getReference()));
        element.addContent(new Element("localVariable").addContent(p.getLocalVariable()));
        element.addContent(new Element("formula").addContent(p.getFormula()));

        element.addContent(new Element("operationAddressing").addContent(p.getOperationAddressing().name()));
        element.addContent(new Element("operationDirect").addContent(p.getOperationDirect().name()));
        element.addContent(new Element("operationReference").addContent(p.getOperationReference()));
        element.addContent(new Element("operationLocalVariable").addContent(p.getOperationLocalVariable()));
        element.addContent(new Element("operationFormula").addContent(p.getOperationFormula()));

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

        Element blockName = shared.getChild("block");
        if (blockName != null) {
            Block t = InstanceManager.getDefault(BlockManager.class).getNamedBean(blockName.getTextTrim());
            if (t != null) h.setBlock(t);
            else h.removeBlock();
        }

        try {
            Element elem = shared.getChild("addressing");
            if (elem != null) {
                h.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            elem = shared.getChild("reference");
            if (elem != null) h.setReference(elem.getTextTrim());

            elem = shared.getChild("localVariable");
            if (elem != null) h.setLocalVariable(elem.getTextTrim());

            elem = shared.getChild("formula");
            if (elem != null) h.setFormula(elem.getTextTrim());


            elem = shared.getChild("operationAddressing");
            if (elem != null) {
                h.setOperationAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            elem = shared.getChild("operationDirect");
            if (elem != null) {
                String oper = elem.getTextTrim();
                // deprecated 4.23.5 remove 4.25.1
                if (oper.equals("SetToConstant") || oper.equals("CopyFromMemory") || oper.equals("CopyToMemory")) {
                    oper = "SetValue";
                }
                h.setOperationDirect(ActionBlock.DirectOperation.valueOf(oper));
            }

            elem = shared.getChild("operationReference");
            if (elem != null) h.setOperationReference(elem.getTextTrim());

            elem = shared.getChild("operationLocalVariable");
            if (elem != null) h.setOperationLocalVariable(elem.getTextTrim());

            elem = shared.getChild("operationFormula");
            if (elem != null) h.setOperationFormula(elem.getTextTrim());


            elem = shared.getChild("dataAddressing");
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
