package jmri.jmrit.logixng.expressions.configurexml;

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

        NamedBeanHandle block = p.getBlock();
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

        element.addContent(new Element("blockConstant").addContent(p.getBlockConstant()));
        NamedBeanHandle<Memory> blockMemory = p.getBlockMemory();
        if (blockMemory != null) {
            element.addContent(new Element("blockMemory").addContent(blockMemory.getName()));
        }

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
                h.setBeanState(ExpressionBlock.BlockState.valueOf(blockState.getTextTrim()));
            }

            elem = shared.getChild("stateReference");
            if (elem != null) h.setStateReference(elem.getTextTrim());

            elem = shared.getChild("stateLocalVariable");
            if (elem != null) h.setStateLocalVariable(elem.getTextTrim());

            elem = shared.getChild("stateFormula");
            if (elem != null) h.setStateFormula(elem.getTextTrim());

            elem = shared.getChild("blockConstant");
            if (elem != null) h.setBlockConstant(elem.getTextTrim());

            elem = shared.getChild("blockMemory");
            if (elem != null) h.setBlockMemory(elem.getTextTrim());

        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionBlockXml.class);
}
