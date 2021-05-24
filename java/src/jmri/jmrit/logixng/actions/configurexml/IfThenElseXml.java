package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.IfThenElse;

import org.jdom2.Attribute;
import org.jdom2.Element;

import jmri.jmrit.logixng.MaleSocket;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class IfThenElseXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        IfThenElse p = (IfThenElse) o;

        Element element = new Element("IfThenElse");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        element.setAttribute("type", p.getType().name());
        
        Element e2 = new Element("IfSocket");
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

        e2 = new Element("ThenSocket");
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

        e2 = new Element("ElseSocket");
        e2.addContent(new Element("socketName").addContent(p.getChild(2).getName()));
        socket = p.getElseActionSocket().getConnectedSocket();
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getElseActionSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        
        String typeStr = shared.getAttribute("type").getValue();
        
        /*
        To not cause problems during testing, these are accepted
        for now. But they should be removed before the production
        version. These where changed to CamelCase after people
        had started testing LogixNG.
        Remove TRIGGER_ACTION and CONTINOUS_ACTION in if-then-else-4.23.1.xsd
        as well.
        */
        if ("TRIGGER_ACTION".equals(typeStr)) typeStr = "ExecuteOnChange";
        if ("CONTINOUS_ACTION".equals(typeStr)) typeStr = "AlwaysExecute";
        if ("TriggerAction".equals(typeStr)) typeStr = "ExecuteOnChange";
        if ("ContinuousAction".equals(typeStr)) typeStr = "AlwaysExecute";
        
        IfThenElse.Type type = IfThenElse.Type.valueOf(typeStr);
        
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        IfThenElse h = new IfThenElse(sys, uname);
        h.setType(type);

        loadCommon(h, shared);
        
        Element socketName = shared.getChild("IfSocket").getChild("socketName");
        h.getChild(0).setName(socketName.getTextTrim());
        Element socketSystemName = shared.getChild("IfSocket").getChild("systemName");
        if (socketSystemName != null) {
            h.setIfExpressionSocketSystemName(socketSystemName.getTextTrim());
        }
        
        socketName = shared.getChild("ThenSocket").getChild("socketName");
        h.getChild(1).setName(socketName.getTextTrim());
        socketSystemName = shared.getChild("ThenSocket").getChild("systemName");
        if (socketSystemName != null) {
            h.setThenActionSocketSystemName(socketSystemName.getTextTrim());
        }
        
        socketName = shared.getChild("ElseSocket").getChild("socketName");
        h.getChild(2).setName(socketName.getTextTrim());
        socketSystemName = shared.getChild("ElseSocket").getChild("systemName");
        if (socketSystemName != null) {
            h.setElseActionSocketSystemName(socketSystemName.getTextTrim());
        }
        
        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IfThenElseXml.class);
}
