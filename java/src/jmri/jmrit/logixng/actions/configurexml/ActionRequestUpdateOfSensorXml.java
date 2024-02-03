package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.ActionRequestUpdateOfSensor;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionRequestUpdateOfSensor objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2024
 */
public class ActionRequestUpdateOfSensorXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionRequestUpdateOfSensorXml() {
    }

    /**
     * Default implementation for storing the contents of a ActionRequestUpdateOfSensor
     *
     * @param o Object to store, of type ActionRequestUpdateOfSensor
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionRequestUpdateOfSensor p = (ActionRequestUpdateOfSensor) o;

        Element element = new Element("ActionRequestUpdateOfSensor");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Sensor>();
        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionRequestUpdateOfSensor h = new ActionRequestUpdateOfSensor(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Sensor>();
        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionRequestUpdateOfSensorXml.class);
}
