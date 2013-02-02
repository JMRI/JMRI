package jmri.implementation.configurexml;

import org.apache.log4j.Logger;
import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.implementation.QuadOutputSignalHead;
import jmri.Turnout;

import jmri.NamedBeanHandle;

import java.util.List;

import org.jdom.Element;

/**
 * Handle XML configuration for QuadOutputSignalHead objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * @version $Revision$
 */
public class QuadOutputSignalHeadXml extends TripleTurnoutSignalHeadXml {

    public QuadOutputSignalHeadXml() {}

    /**
     * Default implementation for storing the contents of a
     * QuadOutputSignalHead
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        QuadOutputSignalHead p = (QuadOutputSignalHead)o;

        Element element = new Element("signalhead");
        element.setAttribute("class", this.getClass().getName());

        // include contents
        element.setAttribute("systemName", p.getSystemName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);
        
        element.addContent(addTurnoutElement(p.getGreen(), "green"));
        element.addContent(addTurnoutElement(p.getYellow(), "yellow"));
        element.addContent(addTurnoutElement(p.getRed(), "red"));
        element.addContent(addTurnoutElement(p.getLunar(), "lunar"));

        return element;
    }

    /**
     * Create a QuadOutputSignalHead
     * @param element Top level Element to unpack.
     * @return true if successful
     */
    @SuppressWarnings("unchecked")
	public boolean load(Element element) {
        List<Element> l = element.getChildren("turnoutname");
        if (l.size() == 0) l = element.getChildren("turnout");
        NamedBeanHandle<Turnout> green = loadTurnout(l.get(0));
        NamedBeanHandle<Turnout> yellow = loadTurnout(l.get(1));
        NamedBeanHandle<Turnout> red = loadTurnout(l.get(2));
        NamedBeanHandle<Turnout> lunar = loadTurnout(l.get(3));

        // put it together
        String sys = getSystemName(element);
        String uname = getUserName(element);
        SignalHead h;
        if (uname == null)
            h = new QuadOutputSignalHead(sys, green, yellow, red, lunar);
        else
            h = new QuadOutputSignalHead(sys, uname, green, yellow, red, lunar);

        loadCommon(h, element);
        
        InstanceManager.signalHeadManagerInstance().register(h);
        return true;
    }

    static Logger log = Logger.getLogger(TripleTurnoutSignalHeadXml.class.getName());
}
