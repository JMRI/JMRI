package jmri.jmrit.logixng.expressions.configurexml;

import java.util.ResourceBundle;
import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.Section;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionSection;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;
import org.jdom2.Element;

/**
 * Handle XML configuration for ExpressionSectionXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright (C) 2023
 */
public class ExpressionSectionXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.logixng.actions.ActionBundle");

    public ExpressionSectionXml() {
    }

    /**
     * Default implementation for storing the contents of a ExpressionSection
     *
     * @param o Object to store, of type ExpressionSection
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExpressionSection p = (ExpressionSection) o;

        Element element = new Element("ExpressionSection");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Section>();
        var selectEnumXml = new LogixNG_SelectEnumXml<ExpressionSection.SectionState>();

        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));

        element.addContent(new Element("is_isNot").addContent(p.get_Is_IsNot().name()));

        element.addContent(selectEnumXml.store(p.getSelectEnum(), "sectionStateData"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionSection h = new ExpressionSection(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Section>();
        var selectEnumXml = new LogixNG_SelectEnumXml<ExpressionSection.SectionState>();

        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "section");

        Element is_IsNot = shared.getChild("is_isNot");
        if (is_IsNot != null) {
            h.set_Is_IsNot(Is_IsNot_Enum.valueOf(is_IsNot.getTextTrim()));
        }

        selectEnumXml.load(shared.getChild("sectionStateData"), h.getSelectEnum());
        selectEnumXml.loadLegacy(
                shared,
                h.getSelectEnum(),
                "stateAddressing",
                "sectionState",
                "stateReference",
                "stateLocalVariable",
                "stateFormula");

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSectionXml.class);
}
