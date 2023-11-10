package jmri.jmrit.logixng.expressions.configurexml;

import java.util.ResourceBundle;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionBlock;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectStringXml;

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

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Block>();
        var selectEnumXml = new LogixNG_SelectEnumXml<ExpressionBlock.BlockState>();
        var selectStringXml = new LogixNG_SelectStringXml();

        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));

        element.addContent(new Element("is_isNot").addContent(p.get_Is_IsNot().name()));

        element.addContent(selectEnumXml.store(p.getSelectEnum(), "blockStateData"));
        element.addContent(selectStringXml.store(p.getSelectBlockValue(), "blockValueData"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionBlock h = new ExpressionBlock(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Block>();
        var selectEnumXml = new LogixNG_SelectEnumXml<ExpressionBlock.BlockState>();
            var selectStringXml = new LogixNG_SelectStringXml();

        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "block");

        Element is_IsNot = shared.getChild("is_isNot");
        if (is_IsNot != null) {
            h.set_Is_IsNot(Is_IsNot_Enum.valueOf(is_IsNot.getTextTrim()));
        }

        selectEnumXml.load(shared.getChild("blockStateData"), h.getSelectEnum());
        selectEnumXml.loadLegacy(
                shared,
                h.getSelectEnum(),
                "stateAddressing",
                "blockState",
                "stateReference",
                "stateLocalVariable",
                "stateFormula");

        selectStringXml.load(shared.getChild("blockValueData"), h.getSelectBlockValue());
        selectStringXml.loadLegacy(
                shared,
                h.getSelectBlockValue(),
                "dataAddressing",
                "blockValue",
                "dataReference",
                "dataLocalVariable",
                "dataFormula");

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionBlockXml.class);
}
