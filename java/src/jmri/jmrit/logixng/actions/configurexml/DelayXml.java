package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.Delay;
import jmri.jmrit.logixng.util.TimerUnit;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for Delay objects.
 *
 * @author Bob Jacobsen      Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist  Copyright (C) 2021
 */
public class DelayXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DelayXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Delay p = (Delay) o;

        Element element = new Element("Delay");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);
        
        Element e2 = new Element("Socket");
        e2.addContent(new Element("socketName").addContent(p.getChild(0).getName()));
        MaleSocket socket = p.getAnalogExpressionSocket().getConnectedSocket();
        String socketSystemName;
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getAnalogExpressionSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);
        
        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        
        Element socketNameElement = shared.getChild("Socket").getChild("socketName");
        String socketName = socketNameElement.getTextTrim();
        Element socketSystemNameElement = shared.getChild("Socket").getChild("systemName");
        String socketSystemName = null;
        if (socketSystemNameElement != null) {
            socketSystemName = socketSystemNameElement.getTextTrim();
        }
        
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        Delay h = new Delay(sys, uname);
        
        loadCommon(h, shared);
        
        h.getChild(0).setName(socketName);
        h.setAnalogExpressionSocketSystemName(socketSystemName);
        
        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DelayXml.class);
}
