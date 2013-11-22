package jmri.implementation.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.implementation.TripleOutputSignalHead;
import jmri.Turnout;
import jmri.NamedBeanHandle;

import java.util.List;

import org.jdom.Element;

/**
 * Handle XML configuration for TripleOutputSignalHead objects.
 * 
 * @author Suzie Tall based on work by Bob Jacobson
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2008
 * @version $Revision: 22821 $
 */
public class TripleOutputSignalHeadXml extends DoubleTurnoutSignalHeadXml {

    public TripleOutputSignalHeadXml() {}

    /**
     * Default implementation for storing the contents of a
     * TripleOutputSignalHead
     * @param o Object to store, of type TripleOutputSignalHead
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        TripleOutputSignalHead p = (TripleOutputSignalHead)o;

        Element element = new Element("signalhead");
        element.setAttribute("class", this.getClass().getName());

        // include contents
        element.setAttribute("systemName", p.getSystemName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);
        
        element.addContent(addTurnoutElement(p.getGreen(), "green"));
        element.addContent(addTurnoutElement(p.getBlue(), "blue"));
        element.addContent(addTurnoutElement(p.getRed(), "red"));

        return element;
    }

    /**
     * Create a TripleOutputSignalHead
     * @param element Top level Element to unpack.
     * @return true if successful
     */
    @SuppressWarnings("unchecked")
	public boolean load(Element element) {
        List<Element> l = element.getChildren("turnoutname");
        if (l.size() == 0) l = element.getChildren("turnout");
        NamedBeanHandle<Turnout> green = loadTurnout(l.get(0));
        NamedBeanHandle<Turnout> blue = loadTurnout(l.get(1));
        NamedBeanHandle<Turnout> red = loadTurnout(l.get(2));

        // put it together
        String sys = getSystemName(element);
        String uname = getUserName(element);
        SignalHead h;
        if (uname == null)
            h = new TripleOutputSignalHead(sys, green, blue, red);
        else
            h = new TripleOutputSignalHead(sys, uname, green, blue, red);

        loadCommon(h, element);
        
        InstanceManager.signalHeadManagerInstance().register(h);
        return true;
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    static Logger log = LoggerFactory.getLogger(TripleOutputSignalHeadXml.class.getName());
}
