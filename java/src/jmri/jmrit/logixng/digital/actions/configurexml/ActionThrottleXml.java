package jmri.jmrit.logixng.digital.actions.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.digital.actions.ActionThrottle;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.logixng.MaleSocket;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ActionThrottleXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionThrottleXml() {
    }

    /**
     * Default implementation for storing the contents of a ActionThrottle
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionThrottle p = (ActionThrottle) o;

        Element element = new Element("throttle");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        Element e2 = new Element("locoAddressSocket");
        e2.addContent(new Element("socketName").addContent(p.getChild(0).getName()));
        MaleSocket socket = p.getLocoAddressSocket().getConnectedSocket();
        String socketSystemName;
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getLocoAddressSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);

        e2 = new Element("locoSpeedSocket");
        e2.addContent(new Element("socketName").addContent(p.getChild(1).getName()));
        socket = p.getLocoSpeedSocket().getConnectedSocket();
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getLocoSpeedSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);

        e2 = new Element("locoDirectionSocket");
        e2.addContent(new Element("socketName").addContent(p.getChild(1).getName()));
        socket = p.getLocoDirectionSocket().getConnectedSocket();
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getLocoDirectionSocketSystemName();
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
        ActionThrottle h = new ActionThrottle(sys, uname);

        loadCommon(h, shared);
        
        Element socketName = shared.getChild("locoAddressSocket").getChild("socketName");
        h.getChild(0).setName(socketName.getTextTrim());
        Element socketSystemName = shared.getChild("locoAddressSocket").getChild("systemName");
        if (socketSystemName != null) {
            h.setLocoAddressSocketSystemName(socketSystemName.getTextTrim());
        }
        
        socketName = shared.getChild("locoSpeedSocket").getChild("socketName");
        h.getChild(1).setName(socketName.getTextTrim());
        socketSystemName = shared.getChild("locoSpeedSocket").getChild("systemName");
        if (socketSystemName != null) {
            h.setLocoSpeedSocketSystemName(socketSystemName.getTextTrim());
        }
        
        socketName = shared.getChild("locoDirectionSocket").getChild("socketName");
        h.getChild(1).setName(socketName.getTextTrim());
        socketSystemName = shared.getChild("locoDirectionSocket").getChild("systemName");
        if (socketSystemName != null) {
            h.setLocoDirectionSocketSystemName(socketSystemName.getTextTrim());
        }
        
        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static Logger log = LoggerFactory.getLogger(ActionThrottleXml.class);
}
