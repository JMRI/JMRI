package jmri.jmrit.logixng.expressions.configurexml;

import java.awt.GraphicsEnvironment;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.Block;
import jmri.BlockManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionBlock;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ExpressionBlockXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright (C) 2021
 */
public class ExpressionBlockXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.logixng.actions.ActionBundle");

    public ExpressionBlockXml() {
    }

    /**
     * Default implementation for storing the contents of a ExpressionBlock
     *
     * @param o Object to store, of type ExpressionBlock
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExpressionBlock p = (ExpressionBlock) o;

        Element element = new Element("ExpressionBlock");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var block = p.getBlock();
        if (block != null) {
            element.addContent(new Element("block").addContent(block.getName()));
        }

        element.addContent(new Element("addressing").addContent(p.getAddressing().name()));
        element.addContent(new Element("reference").addContent(p.getReference()));
        element.addContent(new Element("localVariable").addContent(p.getLocalVariable()));
        element.addContent(new Element("formula").addContent(p.getFormula()));

        element.addContent(new Element("is_isNot").addContent(p.get_Is_IsNot().name()));

        element.addContent(new Element("stateAddressing").addContent(p.getStateAddressing().name()));
        element.addContent(new Element("blockState").addContent(p.getBeanState().name()));
        element.addContent(new Element("stateReference").addContent(p.getStateReference()));
        element.addContent(new Element("stateLocalVariable").addContent(p.getStateLocalVariable()));
        element.addContent(new Element("stateFormula").addContent(p.getStateFormula()));

        element.addContent(new Element("dataAddressing").addContent(p.getDataAddressing().name()));
        element.addContent(new Element("dataReference").addContent(p.getDataReference()));
        element.addContent(new Element("dataLocalVariable").addContent(p.getDataLocalVariable()));
        element.addContent(new Element("dataFormula").addContent(p.getDataFormula()));

        element.addContent(new Element("blockValue").addContent(p.getBlockValue()));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionBlock h = new ExpressionBlock(sys, uname);

        loadCommon(h, shared);

        Element blockName = shared.getChild("block");
        if (blockName != null) {
            Block t = InstanceManager.getDefault(BlockManager.class)
                    .getNamedBean(blockName.getTextTrim());
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


            Element is_IsNot = shared.getChild("is_isNot");
            if (is_IsNot != null) {
                h.set_Is_IsNot(Is_IsNot_Enum.valueOf(is_IsNot.getTextTrim()));
            }


            elem = shared.getChild("stateAddressing");
            if (elem != null) {
                h.setStateAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            Element blockState = shared.getChild("blockState");
            if (blockState != null) {

                String state = blockState.getTextTrim();
                // deprecated 4.23.5 remove 4.25.1
                if (state.equals("MemoryMatches")) {
                    state = "ValueMatches";
                }
                h.setBeanState(ExpressionBlock.BlockState.valueOf(state));
            }

            elem = shared.getChild("stateReference");
            if (elem != null) h.setStateReference(elem.getTextTrim());

            elem = shared.getChild("stateLocalVariable");
            if (elem != null) h.setStateLocalVariable(elem.getTextTrim());

            elem = shared.getChild("stateFormula");
            if (elem != null) h.setStateFormula(elem.getTextTrim());


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
//             if (elem != null) h.setBlockMemory(elem.getTextTrim());
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

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionBlockXml.class);
}
