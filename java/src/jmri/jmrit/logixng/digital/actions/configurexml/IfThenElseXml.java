package jmri.jmrit.logixng.digital.actions.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.digital.actions.IfThenElse;
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
public class IfThenElseXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public IfThenElseXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        IfThenElse p = (IfThenElse) o;

        Element element = new Element("if-then-else");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        element.setAttribute("type", p.getType().name());
        
        Element e2 = new Element("ifSocket");
        e2.addContent(new Element("socketName").addContent(p.getChild(0).getName()));
        MaleSocket socket = p.getIfExpressionSocket().getConnectedSocket();
        String socketSystemName;
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getIfExpressionSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);

        e2 = new Element("thenSocket");
        e2.addContent(new Element("socketName").addContent(p.getChild(1).getName()));
        socket = p.getThenActionSocket().getConnectedSocket();
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getThenActionSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);

        e2 = new Element("elseSocket");
        e2.addContent(new Element("socketName").addContent(p.getChild(2).getName()));
        socket = p.getElseActionSocket().getConnectedSocket();
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getElseExpressionSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        
        Attribute typeAttribute = shared.getAttribute("type");
        IfThenElse.Type type = IfThenElse.Type.valueOf(typeAttribute.getValue());
        
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        IfThenElse h = new IfThenElse(sys, uname, type);

        loadCommon(h, shared);
        
        Element socketName = shared.getChild("ifSocket").getChild("socketName");
        h.getChild(0).setName(socketName.getTextTrim());
        Element socketSystemName = shared.getChild("ifSocket").getChild("systemName");
        if (socketSystemName != null) {
            h.setIfExpressionSocketSystemName(socketSystemName.getTextTrim());
        }
        
        socketName = shared.getChild("thenSocket").getChild("socketName");
        h.getChild(1).setName(socketName.getTextTrim());
        socketSystemName = shared.getChild("thenSocket").getChild("systemName");
        if (socketSystemName != null) {
            h.setThenActionSocketSystemName(socketSystemName.getTextTrim());
        }
        
        socketName = shared.getChild("elseSocket").getChild("socketName");
        h.getChild(2).setName(socketName.getTextTrim());
        socketSystemName = shared.getChild("elseSocket").getChild("systemName");
        if (socketSystemName != null) {
            h.setElseActionSocketSystemName(socketSystemName.getTextTrim());
        }
        
        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static Logger log = LoggerFactory.getLogger(IfThenElseXml.class);
}
