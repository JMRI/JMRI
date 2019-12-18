package jmri.implementation.configurexml;

import java.util.List;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.implementation.TripleOutputSignalHead;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for TripleOutputSignalHead objects.
 *
 * @author Suzie Tall based on work by Bob Jacobson
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2008
 */
public class TripleOutputSignalHeadXml extends DoubleTurnoutSignalHeadXml {

    public TripleOutputSignalHeadXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * TripleOutputSignalHead.
     *
     * @param o Object to store, of type TripleOutputSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        TripleOutputSignalHead p = (TripleOutputSignalHead) o;

        Element element = new Element("signalhead");
        element.setAttribute("class", this.getClass().getName());

        // include contents
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(addTurnoutElement(p.getGreen(), "green"));
        element.addContent(addTurnoutElement(p.getBlue(), "blue"));
        element.addContent(addTurnoutElement(p.getRed(), "red"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        List<Element> l = shared.getChildren("turnoutname");
        if (l.size() == 0) {
            l = shared.getChildren("turnout");
        }
        NamedBeanHandle<Turnout> green = loadTurnout(l.get(0));
        NamedBeanHandle<Turnout> blue = loadTurnout(l.get(1));
        NamedBeanHandle<Turnout> red = loadTurnout(l.get(2));

        // put it together
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        SignalHead h;
        if (uname == null) {
            h = new TripleOutputSignalHead(sys, green, blue, red);
        } else {
            h = new TripleOutputSignalHead(sys, uname, green, blue, red);
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

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    private final static Logger log = LoggerFactory.getLogger(TripleOutputSignalHeadXml.class);

}
