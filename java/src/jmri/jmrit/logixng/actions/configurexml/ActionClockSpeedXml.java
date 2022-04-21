package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.ActionClockSpeed;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectDoubleXml;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionClockSpeed objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2021
 * @author Dave Sand Copyright (C) 2021
 */
public class ActionClockSpeedXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionClockSpeedXml() {
    }

    /**
     * Default implementation for storing the contents of a clock action.
     *
     * @param o Object to store, of type ActionClockSpeed
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionClockSpeed p = (ActionClockSpeed) o;

        var selectEnumXml = new LogixNG_SelectEnumXml<ActionClockSpeed.ClockState>();
        var selectSpeedXml = new LogixNG_SelectDoubleXml();

        Element element = new Element("ActionClockSpeed");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(selectEnumXml.store(p.getSelectEnum(), "state"));
        element.addContent(selectSpeedXml.store(p.getSelectSpeed(), "speed"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionClockSpeed h = new ActionClockSpeed(sys, uname);

        var selectEnumXml = new LogixNG_SelectEnumXml<ActionClockSpeed.ClockState>();
        var selectSpeedXml = new LogixNG_SelectDoubleXml();

        loadCommon(h, shared);

        selectEnumXml.load(shared.getChild("state"), h.getSelectEnum());

        selectSpeedXml.load(shared.getChild("speed"), h.getSelectSpeed());

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionClockSpeedXml.class);
}
