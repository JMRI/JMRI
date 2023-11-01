package jmri.jmrit.logixng.expressions.configurexml;

import java.util.ResourceBundle;
import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.Transit;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionTransit;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;
import org.jdom2.Element;

/**
 * Handle XML configuration for ExpressionTransitXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright (C) 2023
 */
public class ExpressionTransitXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.logixng.actions.ActionBundle");

    public ExpressionTransitXml() {
    }

    /**
     * Default implementation for storing the contents of a ExpressionTransit
     *
     * @param o Object to store, of type ExpressionTransit
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExpressionTransit p = (ExpressionTransit) o;

        Element element = new Element("ExpressionTransit");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Transit>();
        var selectEnumXml = new LogixNG_SelectEnumXml<ExpressionTransit.TransitState>();

        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));

        element.addContent(new Element("is_isNot").addContent(p.get_Is_IsNot().name()));

        element.addContent(selectEnumXml.store(p.getSelectEnum(), "transitStateData"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionTransit h = new ExpressionTransit(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Transit>();
        var selectEnumXml = new LogixNG_SelectEnumXml<ExpressionTransit.TransitState>();

        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "transit");

        Element is_IsNot = shared.getChild("is_isNot");
        if (is_IsNot != null) {
            h.set_Is_IsNot(Is_IsNot_Enum.valueOf(is_IsNot.getTextTrim()));
        }

        selectEnumXml.load(shared.getChild("transitStateData"), h.getSelectEnum());
        selectEnumXml.loadLegacy(
                shared,
                h.getSelectEnum(),
                "stateAddressing",
                "transitState",
                "stateReference",
                "stateLocalVariable",
                "stateFormula");

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionTransitXml.class);
}
