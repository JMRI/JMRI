package jmri.jmrit.logixng.expressions.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.expressions.TriggerOnce;

import org.jdom2.Element;

import jmri.jmrit.logixng.DigitalExpressionBean;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class TriggerOnceXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public TriggerOnceXml() {
    }
    
    /**
     * Default implementation for storing the contents of a ActionMany
     *
     * @param o Object to store, of type ActionMany
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        TriggerOnce p = (TriggerOnce) o;

        Element element = new Element("TriggerOnce");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        Element e2 = new Element("Socket");
        e2.addContent(new Element("socketName").addContent(p.getChild(0).getName()));
        MaleSocket socket = p.getChild(0).getConnectedSocket();
        String socketSystemName;
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getChildSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);
        
        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        TriggerOnce h;
        if (uname == null) {
            h = new TriggerOnce(sys, null);
        } else {
            h = new TriggerOnce(sys, uname);
        }

        loadCommon(h, shared);

        Element socketElement = shared.getChild("Socket");
        String socketName = socketElement.getChild("socketName").getTextTrim();
        Element systemNameElement = socketElement.getChild("systemName");
        String systemName = null;
        if (systemNameElement != null) {
            systemName = systemNameElement.getTextTrim();
        }
        h.getChild(0).setName(socketName);
        h.setChildSocketSystemName(systemName);
        
        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TriggerOnceXml.class);
}
