package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.actions.ActionSignalHead;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionSignalHeadXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ActionSignalHeadXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionSignalHeadXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionSignalHead p = (ActionSignalHead) o;

        Element element = new Element("ActionSignalHead");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<SignalHead>();
        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));

        element.addContent(new Element("operationAddressing").addContent(p.getOperationAddressing().name()));
        element.addContent(new Element("operationType").addContent(p.getOperationType().name()));
        element.addContent(new Element("operationReference").addContent(p.getOperationReference()));
        element.addContent(new Element("operationLocalVariable").addContent(p.getOperationLocalVariable()));
        element.addContent(new Element("operationFormula").addContent(p.getOperationFormula()));

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
        ActionSignalHead h = new ActionSignalHead(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<SignalHead>();
        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "signalHead");

        try {
            Element elem = shared.getChild("operationAddressing");
            if (elem != null) {
                h.setOperationAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            Element queryType = shared.getChild("operationType");
            if (queryType != null) {
                h.setOperationType(ActionSignalHead.OperationType.valueOf(queryType.getTextTrim()));
            }

            elem = shared.getChild("operationReference");
            if (elem != null) h.setOperationReference(elem.getTextTrim());

            elem = shared.getChild("operationLocalVariable");
            if (elem != null) h.setOperationLocalVariable(elem.getTextTrim());

            elem = shared.getChild("operationFormula");
            if (elem != null) h.setOperationFormula(elem.getTextTrim());


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

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSignalHeadXml.class);
}
