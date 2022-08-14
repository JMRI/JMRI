package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.actions.ActionSignalMast;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionSignalMastXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ActionSignalMastXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionSignalMastXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalMast
     *
     * @param o Object to store, of type TripleLightSignalMast
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionSignalMast p = (ActionSignalMast) o;

        Element element = new Element("ActionSignalMast");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<SignalMast>();
        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));

        element.addContent(new Element("operationAddressing").addContent(p.getOperationAddressing().name()));
        element.addContent(new Element("operationType").addContent(p.getOperationType().name()));
        element.addContent(new Element("operationReference").addContent(p.getOperationReference()));
        element.addContent(new Element("operationLocalVariable").addContent(p.getOperationLocalVariable()));
        element.addContent(new Element("operationFormula").addContent(p.getOperationFormula()));

        element.addContent(new Element("aspectAddressing").addContent(p.getAspectAddressing().name()));
        element.addContent(new Element("aspect").addContent(p.getAspect()));
        element.addContent(new Element("aspectReference").addContent(p.getAspectReference()));
        element.addContent(new Element("aspectLocalVariable").addContent(p.getAspectLocalVariable()));
        element.addContent(new Element("aspectFormula").addContent(p.getAspectFormula()));

        var selectExampleNamedBeanXml = new LogixNG_SelectNamedBeanXml<SignalMast>();
        element.addContent(selectExampleNamedBeanXml.store(p.getSelectExampleNamedBean(), "exampleNamedBean"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionSignalMast h = new ActionSignalMast(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<SignalMast>();
        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "signalMast");

        try {
            Element elem = shared.getChild("operationAddressing");
            if (elem != null) {
                h.setOperationAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            Element queryType = shared.getChild("operationType");
            if (queryType != null) {
                h.setOperationType(ActionSignalMast.OperationType.valueOf(queryType.getTextTrim()));
            }

            elem = shared.getChild("operationReference");
            if (elem != null) h.setOperationReference(elem.getTextTrim());

            elem = shared.getChild("operationLocalVariable");
            if (elem != null) h.setOperationLocalVariable(elem.getTextTrim());

            elem = shared.getChild("operationFormula");
            if (elem != null) h.setOperationFormula(elem.getTextTrim());


            elem = shared.getChild("aspectAddressing");
            if (elem != null) {
                h.setAspectAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            Element aspectElement = shared.getChild("aspect");
            if (aspectElement != null) {
                try {
                    h.setAspect(aspectElement.getTextTrim());
                } catch (NumberFormatException e) {
                    log.error("cannot parse aspect: {}", aspectElement.getTextTrim(), e);
                }
            }

            elem = shared.getChild("aspectReference");
            if (elem != null) h.setAspectReference(elem.getTextTrim());

            elem = shared.getChild("aspectLocalVariable");
            if (elem != null) h.setAspectLocalVariable(elem.getTextTrim());

            elem = shared.getChild("aspectFormula");
            if (elem != null) h.setAspectFormula(elem.getTextTrim());

        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }

        var selectExampleNamedBeanXml = new LogixNG_SelectNamedBeanXml<SignalMast>();
        selectExampleNamedBeanXml.load(shared.getChild("exampleNamedBean"), h.getSelectExampleNamedBean());
        selectExampleNamedBeanXml.loadLegacy(shared, h.getSelectExampleNamedBean(), "exampleSignalMast", null, null, null, null);

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSignalMastXml.class);
}
