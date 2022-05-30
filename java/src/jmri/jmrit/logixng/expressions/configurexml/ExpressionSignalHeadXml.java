package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.expressions.ExpressionSignalHead;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ExpressionSignalHeadXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ExpressionSignalHeadXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ExpressionSignalHeadXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExpressionSignalHead p = (ExpressionSignalHead) o;

        Element element = new Element("ExpressionSignalHead");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<SignalHead>();
        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));

        element.addContent(new Element("queryAddressing").addContent(p.getQueryAddressing().name()));
        element.addContent(new Element("queryType").addContent(p.getQueryType().name()));
        element.addContent(new Element("queryReference").addContent(p.getQueryReference()));
        element.addContent(new Element("queryLocalVariable").addContent(p.getQueryLocalVariable()));
        element.addContent(new Element("queryFormula").addContent(p.getQueryFormula()));

        element.addContent(new Element("appearanceAddressing").addContent(p.getAppearanceAddressing().name()));
        element.addContent(new Element("appearance").addContent(Integer.toString(p.getAppearance())));
        element.addContent(new Element("appearanceReference").addContent(p.getAppearanceReference()));
        element.addContent(new Element("appearanceLocalVariable").addContent(p.getAppearanceLocalVariable()));
        element.addContent(new Element("appearanceFormula").addContent(p.getAppearanceFormula()));

        var selectExampleNamedBeanXml = new LogixNG_SelectNamedBeanXml<SignalHead>();
        element.addContent(selectExampleNamedBeanXml.store(p.getSelectExampleNamedBean(), "exampleNamedBean"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionSignalHead h = new ExpressionSignalHead(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<SignalHead>();
        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "signalHead");

        try {
            Element elem = shared.getChild("queryAddressing");
            if (elem != null) {
                h.setQueryAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            Element queryType = shared.getChild("queryType");
            if (queryType != null) {
                h.setQueryType(ExpressionSignalHead.QueryType.valueOf(queryType.getTextTrim()));
            }

            elem = shared.getChild("queryReference");
            if (elem != null) h.setQueryReference(elem.getTextTrim());

            elem = shared.getChild("queryLocalVariable");
            if (elem != null) h.setQueryLocalVariable(elem.getTextTrim());

            elem = shared.getChild("queryFormula");
            if (elem != null) h.setQueryFormula(elem.getTextTrim());


            elem = shared.getChild("appearanceAddressing");
            if (elem != null) {
                h.setAppearanceAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            Element appearanceElement = shared.getChild("appearance");
            if (appearanceElement != null) {
                try {
                    int appearance = Integer.parseInt(appearanceElement.getTextTrim());
                    h.setAppearance(appearance);
                } catch (NumberFormatException e) {
                    log.error("cannot parse apperance: {}", appearanceElement.getTextTrim(), e);
                }
            }

            elem = shared.getChild("appearanceReference");
            if (elem != null) h.setAppearanceReference(elem.getTextTrim());

            elem = shared.getChild("appearanceLocalVariable");
            if (elem != null) h.setAppearanceLocalVariable(elem.getTextTrim());

            elem = shared.getChild("appearanceFormula");
            if (elem != null) h.setAppearanceFormula(elem.getTextTrim());

        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }

        var selectExampleNamedBeanXml = new LogixNG_SelectNamedBeanXml<SignalHead>();
        selectExampleNamedBeanXml.load(shared.getChild("exampleNamedBean"), h.getSelectExampleNamedBean());
        selectExampleNamedBeanXml.loadLegacy(shared, h.getSelectExampleNamedBean(), "exampleSignalHead", null, null, null, null);

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSignalHeadXml.class);
}
