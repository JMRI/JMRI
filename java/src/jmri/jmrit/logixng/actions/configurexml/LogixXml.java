package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.NamedBeanHandle;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.Logix;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class LogixXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Logix p = (Logix) o;

        Element element = new Element("Logix");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        Element e2 = new Element("ExpressionSocket");
        e2.addContent(new Element("socketName").addContent(p.getChild(0).getName()));
        MaleSocket socket = p.getExpressionSocket().getConnectedSocket();
        String socketSystemName;
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getExpressionSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);

        e2 = new Element("ActionSocket");
        e2.addContent(new Element("socketName").addContent(p.getChild(1).getName()));
        socket = p.getActionSocket().getConnectedSocket();
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getActionSocketSystemName();
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
        Logix h = new Logix(sys, uname);

        loadCommon(h, shared);

        Element socketName = shared.getChild("ExpressionSocket").getChild("socketName");
        h.getChild(0).setName(socketName.getTextTrim());
        Element socketSystemName = shared.getChild("ExpressionSocket").getChild("systemName");
        if (socketSystemName != null) {
            h.setExpressionSocketSystemName(socketSystemName.getTextTrim());
        }
        
        socketName = shared.getChild("ActionSocket").getChild("socketName");
        h.getChild(1).setName(socketName.getTextTrim());
        socketSystemName = shared.getChild("ActionSocket").getChild("systemName");
        if (socketSystemName != null) {
            h.setActionSocketSystemName(socketSystemName.getTextTrim());
        }
        
        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionLightXml.class);
}
