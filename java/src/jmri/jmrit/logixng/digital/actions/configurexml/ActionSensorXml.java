package jmri.jmrit.logixng.digital.actions.configurexml;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.SensorManager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.digital.actions.ActionSensor;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ActionSensorXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionSensorXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleSensorSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionSensor p = (ActionSensor) o;

        Element element = new Element("action-sensor");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        NamedBeanHandle sensor = p.getSensor();
        if (sensor != null) {
            element.addContent(new Element("sensor").addContent(sensor.getName()));
        }
        
        element.addContent(new Element("sensorState").addContent(p.getSensorState().name()));

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionSensor h = new ActionSensor(sys, uname);

        loadCommon(h, shared);

        Element sensorName = shared.getChild("sensor");
        if (sensorName != null) {
            h.setSensor(InstanceManager.getDefault(SensorManager.class).getSensor(sensorName.getTextTrim()));
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static Logger log = LoggerFactory.getLogger(ActionSensorXml.class);
}
