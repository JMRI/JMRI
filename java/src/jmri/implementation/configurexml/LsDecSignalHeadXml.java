package jmri.implementation.configurexml;

import java.util.List;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.implementation.LsDecSignalHead;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for LsDecSignalHead objects.
 *
 * This file is part of JMRI.
 *
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 *
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2008
 * @author Petr Koud'a Copyright: Copyright (c) 2007
 */
public class LsDecSignalHeadXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public LsDecSignalHeadXml() {
    }

    /**
     * Default implementation for storing the contents of a LsDecSignalHead.
     *
     * @param o Object to store, of type LsDecSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        LsDecSignalHead p = (LsDecSignalHead) o;

        Element element = new Element("signalhead");
        element.setAttribute("class", this.getClass().getName());

        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(addTurnoutElement(p.getGreen().getName(), p.getGreenState()));
        element.addContent(addTurnoutElement(p.getYellow().getName(), p.getYellowState()));
        element.addContent(addTurnoutElement(p.getRed().getName(), p.getRedState()));
        element.addContent(addTurnoutElement(p.getFlashGreen().getName(), p.getFlashGreenState()));
        element.addContent(addTurnoutElement(p.getFlashYellow().getName(), p.getFlashYellowState()));
        element.addContent(addTurnoutElement(p.getFlashRed().getName(), p.getFlashRedState()));
        element.addContent(addTurnoutElement(p.getDark().getName(), p.getDarkState()));

        return element;
    }

    Element addTurnoutElement(String name, int s) {
        int state = s;

        Element el = new Element("turnout");
        el.setAttribute("systemName", name);
        if (state == Turnout.THROWN) {
            el.setAttribute("state", "THROWN");
        } else {
            el.setAttribute("state", "CLOSED");
        }

        return el;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        List<Element> l = shared.getChildren("turnout");
        NamedBeanHandle<Turnout> green = loadTurnout(l.get(0));
        NamedBeanHandle<Turnout> yellow = loadTurnout(l.get(1));
        NamedBeanHandle<Turnout> red = loadTurnout(l.get(2));
        NamedBeanHandle<Turnout> flashgreen = loadTurnout(l.get(3));
        NamedBeanHandle<Turnout> flashyellow = loadTurnout(l.get(4));
        NamedBeanHandle<Turnout> flashred = loadTurnout(l.get(5));
        NamedBeanHandle<Turnout> dark = loadTurnout(l.get(6));
        int greenstatus = loadTurnoutStatus(l.get(0));
        int yellowstatus = loadTurnoutStatus(l.get(1));
        int redstatus = loadTurnoutStatus(l.get(2));
        int flashgreenstatus = loadTurnoutStatus(l.get(3));
        int flashyellowstatus = loadTurnoutStatus(l.get(4));
        int flashredstatus = loadTurnoutStatus(l.get(5));
        int darkstatus = loadTurnoutStatus(l.get(6));

        // put it together
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        SignalHead h;
        if (uname == null) {
            h = new LsDecSignalHead(sys, green, greenstatus, yellow, yellowstatus, red, redstatus, flashgreen, flashgreenstatus, flashyellow, flashyellowstatus, flashred, flashredstatus, dark, darkstatus);
        } else {
            h = new LsDecSignalHead(sys, uname, green, greenstatus, yellow, yellowstatus, red, redstatus, flashgreen, flashgreenstatus, flashyellow, flashyellowstatus, flashred, flashredstatus, dark, darkstatus);
        }

        loadCommon(h, shared);

        SignalHead existingBean =
                InstanceManager.getDefault(jmri.SignalHeadManager.class)
                        .getBeanBySystemName(sys);

        if ((existingBean != null) && (existingBean != h)) {
            log.error("systemName is already registered: {}", sys);
        } else {
            InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
        }

        return true;
    }

    NamedBeanHandle<Turnout> loadTurnout(Object o) {
        Element e = (Element) o;
        String name = e.getAttribute("systemName").getValue();
        Turnout t = InstanceManager.turnoutManagerInstance().getTurnout(name);
        if (t != null) {
            return jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(name, t);
        } else {
            log.warn("Failed to find turnout {}. Check connection and configuration", name);
            return null;
        }
    }

    int loadTurnoutStatus(Object o) {
        Element e = (Element) o;
        String rState = e.getAttribute("state").getValue();
        int tSetState = Turnout.CLOSED;
        if (rState.equals("THROWN")) {
            tSetState = Turnout.THROWN;
        }
        return tSetState;
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    private final static Logger log = LoggerFactory.getLogger(LsDecSignalHeadXml.class);

}
