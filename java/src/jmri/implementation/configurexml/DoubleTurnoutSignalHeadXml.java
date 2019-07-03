package jmri.implementation.configurexml;

import java.util.List;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.implementation.DoubleTurnoutSignalHead;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for DoubleTurnoutSignalHead objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008
 */
public class DoubleTurnoutSignalHeadXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DoubleTurnoutSignalHeadXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * DoubleTurnoutSignalHead.
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        DoubleTurnoutSignalHead p = (DoubleTurnoutSignalHead) o;

        Element element = new Element("signalhead");
        element.setAttribute("class", this.getClass().getName());

        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(addTurnoutElement(p.getGreen(), "green"));
        element.addContent(addTurnoutElement(p.getRed(), "red"));

        return element;
    }

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

    @Override
    public boolean load(Element shared, Element perNode) {
        List<Element> l = shared.getChildren("turnoutname");
        if (l.size() == 0) {
            l = shared.getChildren("turnout");
        }
        NamedBeanHandle<Turnout> green = loadTurnout(l.get(0));
        NamedBeanHandle<Turnout> red = loadTurnout(l.get(1));
        // put it together
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        SignalHead h;
        if (uname == null) {
            h = new DoubleTurnoutSignalHead(sys, green, red);
        } else {
            h = new DoubleTurnoutSignalHead(sys, uname, green, red);
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

    /**
     * Process stored signal head output (turnout).
     * <p>
     * Needs to handle two types of element: turnoutname is new form; turnout is
     * old form.
     *
     * @param o xml object defining a turnout on an SE8C signal head
     * @return named bean for the turnout
     */
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
                log.warn("Failed to provide Turnout \"{}\" in sendStatus", name);
                return null;
            }            
        }
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    private final static Logger log = LoggerFactory.getLogger(DoubleTurnoutSignalHeadXml.class);

}
