package jmri.jmrix.loconet.configurexml;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.configurexml.XmlAdapter;
import jmri.jmrix.loconet.SE8cSignalHead;

import com.sun.java.util.collections.List;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 * Handle XML configuration for loconet.SE8cSignalHead objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.1 $
 */
public class SE8cSignalHeadXml implements XmlAdapter {

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
        element.addAttribute("class", this.getClass().getName());

        // include contents
        element.addAttribute("systemName", p.getSystemName());
        if (p.getUserName() != null) element.addAttribute("userName", p.getUserName());

        // store the turnout number, not a name, as that's needed when recreating
        element.addContent(addTurnoutElement(p.getNumber()));

        return element;
    }

    Element addTurnoutElement(int number) {
        Element el = new Element("turnout");
        el.addAttribute("systemName", ""+number);

        return el;
    }

    /**
     * Create a SE8cSignalHead
     * @param element Top level Element to unpack.
     */
    public void load(Element element) {
        List l = element.getChildren();
        int turnout = loadTurnout(l.get(0));
        // put it together
        Attribute a = element.getAttribute("userName");
        SignalHead h;
        if (a == null)
            h = new SE8cSignalHead(turnout);
        else
            h = new SE8cSignalHead(turnout, a.getValue());
        InstanceManager.signalHeadManagerInstance().register(h);
        return;
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SE8cSignalHeadXml.class.getName());
}