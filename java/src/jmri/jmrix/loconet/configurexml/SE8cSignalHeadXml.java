package jmri.jmrix.loconet.configurexml;

import java.util.List;
import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.jmrix.loconet.SE8cSignalHead;
import jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for loconet.SE8cSignalHead objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2008
 */
public class SE8cSignalHeadXml extends AbstractNamedBeanManagerConfigXML {

    public SE8cSignalHeadXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type SE8cSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        SE8cSignalHead p = (SE8cSignalHead) o;

        Element element = new Element("signalhead"); // NOI18N
        element.setAttribute("class", this.getClass().getName()); // NOI18N

        // include contents
        element.setAttribute("systemName", p.getSystemName()); // NOI18N
        element.addContent(new Element("systemName").addContent(p.getSystemName())); // NOI18N

        storeCommon(p, element);

        // store the turnout number, not a name, as that's needed when recreating
        element.addContent(addTurnoutElement(p.getNumber()));

        return element;
    }

    Element addTurnoutElement(int number) {
        Element el = new Element("turnout"); // NOI18N
        el.setAttribute("systemName", "" + number); // NOI18N

        return el;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        List<Element> l = shared.getChildren("turnout"); // NOI18N
        int turnout = loadTurnout(l.get(0));
        // put it together
        String uname = getUserName(shared);
        SignalHead h;
        if (uname == null) {
            h = new jmri.implementation.SE8cSignalHead(turnout);
        } else {
            h = new jmri.implementation.SE8cSignalHead(turnout, uname);
        }

        loadCommon(h, shared);

        // replace if already present
        SignalHead sh = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(h.getSystemName());
        if (sh != null) InstanceManager.getDefault(jmri.SignalHeadManager.class).deregister(sh);
        
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
        return true;
    }

    int loadTurnout(Object o) {
        Element e = (Element) o;

        // in this case, the systemName attribute is a number
        try {
            return e.getAttribute("systemName").getIntValue(); // NOI18N
        } catch (DataConversionException ex) {
            log.warn("Can't read turnout number for SE8cSignalHead because of " + ex); // NOI18N
            return 0;
        }
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called"); // NOI18N
    }

    private final static Logger log = LoggerFactory.getLogger(SE8cSignalHeadXml.class);
}
