package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.ActionClockRate;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectDoubleXml;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionClockRate objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2021
 * @author Dave Sand Copyright (C) 2021
 */
public class ActionClockRateXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionClockRateXml() {
    }

    /**
     * Default implementation for storing the contents of a clock action.
     *
     * @param o Object to store, of type ActionClockRate
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionClockRate p = (ActionClockRate) o;

        var selectEnumXml = new LogixNG_SelectEnumXml<ActionClockRate.ClockState>();
        var selectSpeedXml = new LogixNG_SelectDoubleXml();

        Element element = new Element("ActionClockRate");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(selectEnumXml.store(p.getSelectEnum(), "state"));
        element.addContent(selectSpeedXml.store(p.getSelectSpeed(), "rate"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionClockRate h = new ActionClockRate(sys, uname);

        var selectEnumXml = new LogixNG_SelectEnumXml<ActionClockRate.ClockState>();
        var selectRateXml = new LogixNG_SelectDoubleXml();

        loadCommon(h, shared);

        selectEnumXml.load(shared.getChild("state"), h.getSelectEnum());

        selectRateXml.load(shared.getChild("rate"), h.getSelectSpeed());

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionClockRateXml.class);
}
