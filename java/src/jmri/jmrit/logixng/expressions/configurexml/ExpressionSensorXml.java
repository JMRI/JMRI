package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionSensor;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;

import org.jdom2.Element;

/**
 * Handle XML configuration for ExpressionSensorXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ExpressionSensorXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ExpressionSensorXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleSensorSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExpressionSensor p = (ExpressionSensor) o;

        Element element = new Element("ExpressionSensor");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Sensor>();
        var selectEnumXml = new LogixNG_SelectEnumXml<ExpressionSensor.SensorState>();
        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));
        element.addContent(new Element("is_isNot").addContent(p.get_Is_IsNot().name()));
        element.addContent(selectEnumXml.store(p.getSelectEnum(), "state"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionSensor h = new ExpressionSensor(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Sensor>();
        var selectEnumXml = new LogixNG_SelectEnumXml<ExpressionSensor.SensorState>();
        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "sensor");

        Element is_IsNot = shared.getChild("is_isNot");
        if (is_IsNot != null) {
            h.set_Is_IsNot(Is_IsNot_Enum.valueOf(is_IsNot.getTextTrim()));
        }

        selectEnumXml.load(shared.getChild("state"), h.getSelectEnum());
        selectEnumXml.loadLegacy(
                shared, h.getSelectEnum(),
                "stateAddressing",
                "sensorState",
                "stateReference",
                "stateLocalVariable",
                "stateFormula");

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSensorXml.class);
}
