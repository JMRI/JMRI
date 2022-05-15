package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.ActionClock;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectIntegerXml;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionClock objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2021
 * @author Dave Sand Copyright (C) 2021
 */
public class ActionClockXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionClockXml() {
    }

    /**
     * Default implementation for storing the contents of a clock action.
     *
     * @param o Object to store, of type ActionClock
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionClock p = (ActionClock) o;

        var selectEnumXml = new LogixNG_SelectEnumXml<ActionClock.ClockState>();
        var selectTimeXml = new LogixNG_SelectIntegerXml();

        Element element = new Element("ActionClock");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(selectEnumXml.store(p.getSelectEnum(), "state"));
        element.addContent(selectTimeXml.store(p.getSelectTime(), "time"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionClock h = new ActionClock(sys, uname);

        var selectEnumXml = new LogixNG_SelectEnumXml<ActionClock.ClockState>();
        var selectTimeXml = new LogixNG_SelectIntegerXml();

        loadCommon(h, shared);

        selectEnumXml.load(shared.getChild("state"), h.getSelectEnum());
        selectEnumXml.loadLegacy(
                shared, h.getSelectEnum(),
                null,
                "clockState",
                null,
                null,
                null);

        selectTimeXml.load(shared.getChild("time"), h.getSelectTime());
        selectTimeXml.loadLegacy(
                shared, h.getSelectTime(),
                null,
                "setTime",
                null,
                null,
                null);

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionClockXml.class);
}
