package jmri.implementation.configurexml;

import java.util.List;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.implementation.QuadOutputSignalHead;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for QuadOutputSignalHead objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 */
public class QuadOutputSignalHeadXml extends TripleTurnoutSignalHeadXml {

    public QuadOutputSignalHeadXml() {
    }

    /**
     * Default implementation for storing the contents of a QuadOutputSignalHead.
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        QuadOutputSignalHead p = (QuadOutputSignalHead) o;

        Element element = new Element("signalhead");
        element.setAttribute("class", this.getClass().getName());

        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(addTurnoutElement(p.getGreen(), "green"));
        element.addContent(addTurnoutElement(p.getYellow(), "yellow"));
        element.addContent(addTurnoutElement(p.getRed(), "red"));
        element.addContent(addTurnoutElement(p.getLunar(), "lunar"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        List<Element> l = shared.getChildren("turnoutname");
        if (l.size() == 0) {
            l = shared.getChildren("turnout");
        }
        NamedBeanHandle<Turnout> green = loadTurnout(l.get(0));
        NamedBeanHandle<Turnout> yellow = loadTurnout(l.get(1));
        NamedBeanHandle<Turnout> red = loadTurnout(l.get(2));
        NamedBeanHandle<Turnout> lunar = loadTurnout(l.get(3));

        // put it together
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        SignalHead h;
        if (uname == null) {
            h = new QuadOutputSignalHead(sys, green, yellow, red, lunar);
        } else {
            h = new QuadOutputSignalHead(sys, uname, green, yellow, red, lunar);
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

    private final static Logger log = LoggerFactory.getLogger(QuadOutputSignalHeadXml.class);

}
