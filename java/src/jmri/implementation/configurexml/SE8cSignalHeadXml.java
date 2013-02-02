package jmri.implementation.configurexml;

import org.apache.log4j.Logger;
import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.implementation.SE8cSignalHead;
import jmri.Turnout;
import jmri.NamedBeanHandle;

import java.util.List;

import org.jdom.Element;

/**
 * Handle XML configuration for SE8cSignalHead objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @version $Revision$
 */
public class SE8cSignalHeadXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public SE8cSignalHeadXml() {}

    /**
     * Default implementation for storing the contents of a
     * SE8cSignalHead
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        SE8cSignalHead p = (SE8cSignalHead)o;

        Element element = new Element("signalhead");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        // include contents
        element.setAttribute("systemName", p.getSystemName());

        storeCommon(p, element);
        
        element.addContent(addTurnoutElement(p.getLow(), "low"));
        element.addContent(addTurnoutElement(p.getHigh(), "high"));

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
        if (user!=null) el.setAttribute("userName", user);

        return el;
    }

    /**
     * Create a SE8cSignalHead
     * @param element Top level Element to unpack.
     * @return true if successful
     */
    @SuppressWarnings("unchecked")
	public boolean load(Element element) {
        List<Element> l = element.getChildren("turnoutname");
        if (l.size() == 0) l = element.getChildren("turnout");  // older form
        NamedBeanHandle<Turnout> low = loadTurnout(l.get(0));
        NamedBeanHandle<Turnout> high = loadTurnout(l.get(1));
        // put it together
        String sys = getSystemName(element);
        String uname = getUserName(element);
        SignalHead h;
        if (uname == null)
            h = new SE8cSignalHead(sys, low, high);
        else
            h = new SE8cSignalHead(sys, low, high, uname);

        loadCommon(h, element);
        
        InstanceManager.signalHeadManagerInstance().register(h);
        return true;
    }

    /**
     * Needs to handle two types of element:
     *    turnoutname is new form
     *    turnout is old form
     */
    NamedBeanHandle<Turnout> loadTurnout(Object o) {
        Element e = (Element)o;
        
        if (e.getName().equals("turnout")) {
            String name = e.getAttribute("systemName").getValue();
            Turnout t;
            if (e.getAttribute("userName")!=null && 
                    !e.getAttribute("userName").getValue().equals("")) {
                name = e.getAttribute("userName").getValue();
                t = InstanceManager.turnoutManagerInstance().getTurnout(name);
            } else {
                t = InstanceManager.turnoutManagerInstance().getBySystemName(name);
            }
            return jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(name, t);
        } else {
            String name = e.getText();
            Turnout t = InstanceManager.turnoutManagerInstance().provideTurnout(name);
            return jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(name, t);
        }
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    static Logger log = Logger.getLogger(SE8cSignalHeadXml.class.getName());
}
