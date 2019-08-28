package jmri.jmrit.logixng.digital.actions.configurexml;

import java.lang.reflect.Field;
import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.digital.actions.HoldAnything;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.logixng.DigitalActionBean;
import jmri.jmrit.logixng.MaleSocket;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class HoldAnythingXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public HoldAnythingXml() {
    }
/*
    @SuppressWarnings("unchecked")  // Reflection does not support generics
    private List<ActionMany.ActionEntry> getActionEntry(ActionMany actionMany)
            throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException {
        
        Field f = actionMany.getClass().getDeclaredField("actionEntries");
        f.setAccessible(true);
        return (List<ActionMany.ActionEntry>) f.get(actionMany);
    }
*/
    /**
     * Default implementation for storing the contents of a ActionMany
     *
     * @param o Object to store, of type ActionMany
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        HoldAnything p = (HoldAnything) o;

        Element element = new Element("hold-anything");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);
/*
        Element e = new Element("actions");
        for (int i=0; i < p.getChildCount(); i++) {
            Element e2 = new Element("socket");
            e2.addContent(new Element("socketName").addContent(p.getChild(i).getName()));
            MaleSocket socket = p.getChild(i).getConnectedSocket();
            if (socket != null) {
                e2.addContent(new Element("systemName").addContent(socket.getSystemName()));
            }
            e.addContent(e2);
        }
        element.addContent(e);
*/        
/*
        for (int i=0; i < p.getChildCount(); i++) {
//            try {
//                    log.debug("action system name is " + entry.getSystemName());  // NOI18N
                if (p.getChild(i).isConnected()) {
//                    Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(p.getChild(i).getConnectedSocket());
//                    if (e != null) {
//                        element.addContent(e);
//                    }
                }
//            } catch (Exception e) {
//                log.error("Error storing action: {}", e, e);
//            }
        }
*/        
//        element.addContent(addTurnoutElement(p.getLow(), "low"));
//        element.addContent(addTurnoutElement(p.getHigh(), "high"));

        return element;
    }
/*
    Element addTurnoutElement(NamedBeanHandle<Turnout> to, String which) {
        Element el = new Element("turnoutname");
        el.setAttribute("defines", which);
        el.addContent(to.getName());
        return el;
    }

    Element addTurnoutElement(Turnout to) {
        String user = to.getUserName();
        String sys = to.getSystemName();

        Element el = new Element("turnout");
        el.setAttribute("systemName", sys);
        if (user != null) {
            el.setAttribute("userName", user);
        }

        return el;
    }
*/
    @Override
    public boolean load(Element shared, Element perNode) {
//        List<Element> l = shared.getChildren("turnoutname");
/*        
        if (l.size() == 0) {
            l = shared.getChildren("turnout");  // older form
        }
        NamedBeanHandle<Turnout> low = loadTurnout(l.get(0));
        NamedBeanHandle<Turnout> high = loadTurnout(l.get(1));
*/        
        // put it together
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        DigitalActionBean h;
        if (uname == null) {
            h = new HoldAnything(sys);
        } else {
            h = new HoldAnything(sys, uname);
        }

        loadCommon(h, shared);

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
/*
    /.**
     * Process stored signal head output (turnout).
     * <p>
     * Needs to handle two types of element: turnoutname is new form; turnout is
     * old form.
     *
     * @param o xml object defining a turnout on an SE8C signal head
     * @return named bean for the turnout
     *./
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
            return jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(name, t);
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
*/
    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    private final static Logger log = LoggerFactory.getLogger(HoldAnythingXml.class);
}
