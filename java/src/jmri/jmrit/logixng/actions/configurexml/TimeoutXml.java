package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.Timeout;

import org.jdom2.Element;

import jmri.jmrit.logixng.util.TimerUnit;
import jmri.jmrit.logixng.util.parser.ParserException;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class TimeoutXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Timeout p = (Timeout) o;

        Element element = new Element("Timeout");
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

        element.addContent(new Element("delayAddressing").addContent(p.getDelayAddressing().name()));
        element.addContent(new Element("delay").addContent(Integer.toString(p.getDelay())));
        element.addContent(new Element("delayReference").addContent(p.getDelayReference()));
        element.addContent(new Element("delayLocalVariable").addContent(p.getDelayLocalVariable()));
        element.addContent(new Element("delayFormula").addContent(p.getDelayFormula()));

        element.addContent(new Element("unit").addContent(p.getUnit().name()));
        
        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        Timeout h = new Timeout(sys, uname);

        loadCommon(h, shared);
        
        Element delayElement = shared.getChild("delay");
        int delay = 0;
        if (delayElement != null) {
            delay = Integer.parseInt(delayElement.getText());
        }
        h.setDelay(delay);
        
        Element unit = shared.getChild("unit");
        if (unit != null) {
            h.setUnit(TimerUnit.valueOf(unit.getTextTrim()));
        }
        
        try {
            Element elem = shared.getChild("delayAddressing");
            if (elem != null) {
                h.setDelayAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }
            
            elem = shared.getChild("delayReference");
            if (elem != null) h.setDelayReference(elem.getTextTrim());
            
            elem = shared.getChild("delayLocalVariable");
            if (elem != null) h.setDelayLocalVariable(elem.getTextTrim());
            
            elem = shared.getChild("delayFormula");
            if (elem != null) h.setDelayFormula(elem.getTextTrim());
            
        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }

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
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeoutXml.class);
}
