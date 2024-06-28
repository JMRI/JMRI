package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionSensorEdge;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;

import org.jdom2.Element;

/**
 * Handle XML configuration for ExpressionSensorEdgeXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class ExpressionSensorEdgeXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ExpressionSensorEdgeXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleSensorSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExpressionSensorEdge p = (ExpressionSensorEdge) o;

        Element element = new Element("ExpressionSensorEdge");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Sensor>();
        var selectEnumFromStateXml = new LogixNG_SelectEnumXml<ExpressionSensorEdge.SensorState>();
        var selectEnumToStateXml = new LogixNG_SelectEnumXml<ExpressionSensorEdge.SensorState>();
        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));
        element.addContent(selectEnumFromStateXml.store(p.getSelectEnumFromState(), "fromState"));
        element.addContent(selectEnumToStateXml.store(p.getSelectEnumToState(), "toState"));
        element.addContent(new Element("onlyTrueOnce").addContent(p.getOnlyTrueOnce() ? "yes" : "no"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionSensorEdge h = new ExpressionSensorEdge(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Sensor>();
        var selectEnumFromStateXml = new LogixNG_SelectEnumXml<ExpressionSensorEdge.SensorState>();
        var selectEnumToStateXml = new LogixNG_SelectEnumXml<ExpressionSensorEdge.SensorState>();

        try {
            selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        } catch (IllegalArgumentException e) {
            log.error("Error during loading Sensor Edge expression {} due to: {}", h.getDisplayName(), e.getMessage());
        }

        selectEnumFromStateXml.load(shared.getChild("fromState"), h.getSelectEnumFromState());
        selectEnumToStateXml.load(shared.getChild("toState"), h.getSelectEnumToState());

        Element onlyTrueOnceElem = shared.getChild("onlyTrueOnce");
        if (onlyTrueOnceElem != null) {
            h.setOnlyTrueOnce("yes".equals(onlyTrueOnceElem.getTextTrim()));
        }

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSensorEdgeXml.class);
}
