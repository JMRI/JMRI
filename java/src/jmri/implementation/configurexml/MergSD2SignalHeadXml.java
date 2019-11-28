package jmri.implementation.configurexml;

import java.util.List;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.implementation.MergSD2SignalHead;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for MergSD2SignalHead objects.
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
 * @author Kevin Dickerson Copyright: Copyright (c) 2009
 */
public class MergSD2SignalHeadXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public MergSD2SignalHeadXml() {
    }

    /**
     * Default implementation for storing the contents of a MergSD2SignalHead.
     *
     * @param o Object to store, of type MergSD2SignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        MergSD2SignalHead p = (MergSD2SignalHead) o;

        Element element = new Element("signalhead");
        element.setAttribute("class", this.getClass().getName());

        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        element.setAttribute("aspects", p.getAspects() + "");
        if (p.getFeather()) {
            element.setAttribute("feather", "yes");
        }

        storeCommon(p, element);
        int aspects = p.getAspects();
        //@TODO could re-arange this so that it falls through
        switch (aspects) {
            case 2:
                element.addContent(addTurnoutElement(p.getInput1(), "input1"));
                if (!p.getHome()) {
                    element.setAttribute("home", "no");
                }
                break;
            case 3:
                element.addContent(addTurnoutElement(p.getInput1(), "input1"));
                element.addContent(addTurnoutElement(p.getInput2(), "input2"));
                break;
            case 4:
                element.addContent(addTurnoutElement(p.getInput1(), "input1"));
                element.addContent(addTurnoutElement(p.getInput2(), "input2"));
                element.addContent(addTurnoutElement(p.getInput3(), "input3"));
                break;
            default:
                log.error("incorrect number of aspects " + aspects + " for Signal " + p.getDisplayName());
        }

        return element;
    }

    Element addTurnoutElement(NamedBeanHandle<Turnout> to, String which) {
        Element el = new Element("turnoutname");
        el.setAttribute("defines", which);
        el.addContent(to.getName());
        return el;
    }

    Element addSingleTurnoutElement(Turnout to) {
        String user = to.getUserName();
        String sys = to.getSystemName();

        Element el = new Element("turnout");
        el.setAttribute("systemName", sys);
        if (user != null) {
            el.setAttribute("userName", user);
        }
        return el;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        int aspects = 2;
        List<Element> l = shared.getChildren("turnoutname");
        if (l.size() == 0) {
            l = shared.getChildren("turnout");
            aspects = l.size() + 1;
        }
        NamedBeanHandle<Turnout> input1 = null;
        NamedBeanHandle<Turnout> input2 = null;
        NamedBeanHandle<Turnout> input3 = null;
        String yesno = "";
        boolean feather = false;
        boolean home = true;

        // put it together
        String sys = getSystemName(shared);
        String uname = getUserName(shared);

        if (shared.getAttribute("feather") != null) {
            yesno = shared.getAttribute("feather").getValue();
        }
        if ((yesno != null) && (!yesno.equals(""))) {
            if (yesno.equals("yes")) {
                feather = true;
            } else if (yesno.equals("no")) {
                feather = false;
            }
        }

        if (shared.getAttribute("home") != null) {
            yesno = shared.getAttribute("home").getValue();
        }
        if ((yesno != null) && (!yesno.equals(""))) {
            if (yesno.equals("yes")) {
                home = true;
            } else if (yesno.equals("no")) {
                home = false;
            }
        }
        try {
            aspects = shared.getAttribute("aspects").getIntValue();
        } catch (org.jdom2.DataConversionException e) {
            log.warn("Could not parse level attribute!");
        } catch (NullPointerException e) {  // considered normal if the attribute not present
        }

        SignalHead h;
        //int aspects = l.size()+1;  //Number of aspects is equal to the number of turnouts used plus 1.
        //@TODO could re-arange this so that it falls through
        switch (aspects) {
            case 2:
                input1 = loadTurnout(l.get(0));
                break;
            case 3:
                input1 = loadTurnout(l.get(0));
                input2 = loadTurnout(l.get(1));
                break;
            case 4:
                input1 = loadTurnout(l.get(0));
                input2 = loadTurnout(l.get(1));
                input3 = loadTurnout(l.get(2));
                break;
            default:
                log.error("incorrect number of aspects {} when loading Signal {}", aspects, sys);
        }
        if (uname == null) {
            h = new MergSD2SignalHead(sys, aspects, input1, input2, input3, feather, home);
        } else {
            h = new MergSD2SignalHead(sys, uname, aspects, input1, input2, input3, feather, home);
        }

        loadCommon(h, shared);

        SignalHead existingBean = InstanceManager.getDefault(jmri.SignalHeadManager.class)
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

        if (e.getName().equals("turnout")) {
            String name = e.getAttribute("systemName").getValue();
            Turnout t;
            if (e.getAttribute("userName") != null
                    && !e.getAttribute("userName").getValue().equals("")) {
                name = e.getAttribute("userName").getValue();
                t = InstanceManager.turnoutManagerInstance().getTurnout(name);
            } else {
                t = InstanceManager.turnoutManagerInstance().getBySystemName(name);
            }
            if (t != null) {
                return jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(name, t);
            } else {
                log.warn("Failed to find turnout {}. Check connection and configuration", name);
                return null;
            }
        } else {
            String name = e.getText();
            try {
                Turnout t = InstanceManager.turnoutManagerInstance().provideTurnout(name);
                return jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(name, t);
            } catch (IllegalArgumentException ex) {
                log.warn("Failed to provide Turnout \"{}\" in loadTurnout", name);
                return null;
            }
        }
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    private final static Logger log = LoggerFactory.getLogger(MergSD2SignalHeadXml.class);

}
