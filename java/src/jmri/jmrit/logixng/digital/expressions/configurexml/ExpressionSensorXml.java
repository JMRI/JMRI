package jmri.jmrit.logixng.digital.expressions.configurexml;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import jmri.jmrit.logixng.digital.expressions.ExpressionSensor;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
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

        Element element = new Element("expression-sensor");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        NamedBeanHandle sensor = p.getSensor();
        if (sensor != null) {
            element.addContent(new Element("sensor").addContent(sensor.getName()));
        }
        element.addContent(new Element("is_isNot").addContent(p.get_Is_IsNot().name()));
        element.addContent(new Element("sensorState").addContent(p.getSensorState().name()));

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionSensor h = new ExpressionSensor(sys, uname);

        loadCommon(h, shared);

        Element sensorName = shared.getChild("sensor");
        if (sensorName != null) {
            Sensor t = InstanceManager.getDefault(SensorManager.class).getSensor(sensorName.getTextTrim());
            if (t != null) h.setSensor(t);
            else h.removeSensor();
        }

        Element is_IsNot = shared.getChild("is_isNot");
        if (is_IsNot != null) {
            h.set_Is_IsNot(Is_IsNot_Enum.valueOf(is_IsNot.getTextTrim()));
        }

        Element sensorState = shared.getChild("sensorState");
        if (sensorState != null) {
            h.setSensorState(ExpressionSensor.SensorState.valueOf(sensorState.getTextTrim()));
        }

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSensorXml.class);
}
