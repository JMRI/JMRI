package jmri.configurexml;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.TripleTurnoutSignalHead;
import jmri.Turnout;
import com.sun.java.util.collections.List;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle XML configuration for TripleTurnoutSignalHead objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.4 $
 */
public class TripleTurnoutSignalHeadXml implements XmlAdapter {

    public TripleTurnoutSignalHeadXml() {}

    /**
     * Default implementation for storing the contents of a
     * TripleTurnoutSignalHead
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        TripleTurnoutSignalHead p = (TripleTurnoutSignalHead)o;

        Element element = new Element("signalhead");
        element.addAttribute("class", this.getClass().getName());

        // include contents
        element.addAttribute("systemName", p.getSystemName());
        if (p.getUserName() != null) element.addAttribute("userName", p.getUserName());

        element.addContent(addTurnoutElement(p.getGreen()));
        element.addContent(addTurnoutElement(p.getYellow()));
        element.addContent(addTurnoutElement(p.getRed()));

        return element;
    }

    Element addTurnoutElement(Turnout to) {
        String user = to.getUserName();
        String sys = to.getSystemName();

        Element el = new Element("turnout");
        el.addAttribute("systemName", sys);
        el.addAttribute("userName", user);

        return el;
    }

    /**
     * Create a TripleTurnoutSignalHead
     * @param element Top level Element to unpack.
     */
    public void load(Element element) {
        List l = element.getChildren();
        Turnout green = loadTurnout(l.get(0));
        Turnout yellow = loadTurnout(l.get(1));
        Turnout red = loadTurnout(l.get(2));
        // put it together
        String sys = element.getAttribute("systemName").getValue();
        Attribute a = element.getAttribute("userName");
        SignalHead h;
        if (a == null)
            h = new TripleTurnoutSignalHead(sys, green, yellow, red);
        else
            h = new TripleTurnoutSignalHead(sys, a.getValue(), green, yellow, red);
        InstanceManager.signalHeadManagerInstance().register(h);
        return;
    }

    Turnout loadTurnout(Object o) {
        Element e = (Element)o;

        // we don't create the Turnout, we just look it up.
        String sys = e.getAttribute("systemName").getValue();
        return InstanceManager.turnoutManagerInstance().getBySystemName(sys);
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TripleTurnoutSignalHeadXml.class.getName());
}