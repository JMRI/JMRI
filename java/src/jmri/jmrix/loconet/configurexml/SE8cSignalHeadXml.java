package jmri.jmrix.loconet.configurexml;

import org.apache.log4j.Logger;
import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.jmrix.loconet.SE8cSignalHead;

import jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML;

import java.util.List;
import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 * Handle XML configuration for loconet.SE8cSignalHead objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2008
 * @version $Revision$
 */
public class SE8cSignalHeadXml extends AbstractNamedBeanManagerConfigXML {

    public SE8cSignalHeadXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * SE8cSignalHead
     * @param o Object to store, of type SE8cSignalHead
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        SE8cSignalHead p = (SE8cSignalHead)o;

        Element element = new Element("signalhead");
        element.setAttribute("class", this.getClass().getName());

        // include contents
        element.setAttribute("systemName", p.getSystemName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);
        
        // store the turnout number, not a name, as that's needed when recreating
        element.addContent(addTurnoutElement(p.getNumber()));

        return element;
    }

    Element addTurnoutElement(int number) {
        Element el = new Element("turnout");
        el.setAttribute("systemName", ""+number);

        return el;
    }

    /**
     * Create a SE8cSignalHead
     * @param element Top level Element to unpack.
     * @return true if successful
     */
    @SuppressWarnings("unchecked")
	public boolean load(Element element) {
        List<Element> l = element.getChildren("turnout");
        int turnout = loadTurnout(l.get(0));
        // put it together
        String uname = getUserName(element);
        SignalHead h;
        if (uname == null)
            h = new jmri.implementation.SE8cSignalHead(turnout);
        else
            h = new jmri.implementation.SE8cSignalHead(turnout, uname);

        loadCommon(h, element);
        
        InstanceManager.signalHeadManagerInstance().register(h);
        return true;
    }

    int loadTurnout(Object o) {
        Element e = (Element)o;

        // in this case, the systemName attribute is a number
        try {
            return e.getAttribute("systemName").getIntValue();
        } catch (DataConversionException ex) {
            log.warn("Can't read turnout number for SE8cSignalHead because of "+ex);
            return 0;
        }
     }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    static Logger log = Logger.getLogger(SE8cSignalHeadXml.class.getName());
}
