package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.ActionClock;

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

        Element element = new Element("ActionClock");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(new Element("clockState").addContent(p.getBeanState().name()));

        if (p.getBeanState() == ActionClock.ClockState.SetClock) {
            element.addContent(new Element("setTime").addContent(Integer.toString(p.getClockTime())));
        }

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionClock h = new ActionClock(sys, uname);

        loadCommon(h, shared);

        Element clockState = shared.getChild("clockState");
        if (clockState != null) {
            h.setBeanState(ActionClock.ClockState.valueOf(clockState.getTextTrim()));
        }

        Element setTime = shared.getChild("setTime");
        if (setTime != null) {
            int time;
            try {
                time = Integer.parseInt(setTime.getTextTrim());
            } catch (NumberFormatException ex) {
                time = 0;
            }
            h.setClockTime(time);
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionClockXml.class);
}
