package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionDispatcher;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ExpressionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 * @author Dave Sand Copyright (C) 2021
 */
public class ExpressionDispatcherXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ExpressionDispatcherXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store ExpresssionDispatcher
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExpressionDispatcher p = (ExpressionDispatcher) o;

        Element element = new Element("ExpressionDispatcher");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectEnumXml = new LogixNG_SelectEnumXml<ExpressionDispatcher.DispatcherState>();

        String trainInfoFileName = p.getTrainInfoFileName();
        if (trainInfoFileName != null) {
            element.addContent(new Element("trainInfoFileName").addContent(trainInfoFileName));
        }

        element.addContent(new Element("addressing").addContent(p.getAddressing().name()));
        element.addContent(new Element("reference").addContent(p.getReference()));
        element.addContent(new Element("localVariable").addContent(p.getLocalVariable()));
        element.addContent(new Element("formula").addContent(p.getFormula()));

        element.addContent(new Element("is_isNot").addContent(p.get_Is_IsNot().name()));

        element.addContent(selectEnumXml.store(p.getSelectEnum(), "state"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionDispatcher h = new ExpressionDispatcher(sys, uname);

        loadCommon(h, shared);

        var selectEnumXml = new LogixNG_SelectEnumXml<ExpressionDispatcher.DispatcherState>();

        selectEnumXml.load(shared.getChild("state"), h.getSelectEnum());
        selectEnumXml.loadLegacy(
                shared, h.getSelectEnum(),
                "stateAddressing",
                "dispatcherState",
                "stateReference",
                "stateLocalVariable",
                "stateFormula");

        try {
            Element elem = shared.getChild("trainInfoFileName");
            if (elem != null) {
                h.setTrainInfoFileName(elem.getTextTrim());
            }

            elem = shared.getChild("addressing");
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
        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionDispatcherXml.class);
}
