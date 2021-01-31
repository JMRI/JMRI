package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.ExecuteDelayed;
import jmri.jmrit.logixng.util.TimerUnit;

import org.jdom2.Element;

/**
 * Handle XML configuration for ExecuteDelayed objects.
 *
 * @author Bob Jacobsen      Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist  Copyright (C) 2021
 */
public class ExecuteDelayedXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ExecuteDelayedXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExecuteDelayed p = (ExecuteDelayed) o;

        Element element = new Element("executeDelayed");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);
        
        Element e2 = new Element("socket");
        e2.addContent(new Element("socketName").addContent(p.getChild(0).getName()));
        MaleSocket socket = p.getSocket().getConnectedSocket();
        String socketSystemName;
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);
        
        element.addContent(new Element("delay").addContent(Integer.toString(p.getDelay())));
        element.addContent(new Element("unit").addContent(p.getUnit().name()));
        element.addContent(new Element("resetIfAlreadyStarted").addContent(p.getResetIfAlreadyStarted() ? "yes" : "no"));  // NOI18N
        
        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        
        Element socketNameElement = shared.getChild("socket").getChild("socketName");
        String socketName = socketNameElement.getTextTrim();
        Element socketSystemNameElement = shared.getChild("socket").getChild("systemName");
        String socketSystemName = null;
        if (socketSystemNameElement != null) {
            socketSystemName = socketSystemNameElement.getTextTrim();
        }
        
        Element delayElement = shared.getChild("delay");
        int delay = 0;
        if (delayElement != null) {
            delay = Integer.parseInt(delayElement.getText());
        }
        
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExecuteDelayed h = new ExecuteDelayed(sys, uname);
        
        loadCommon(h, shared);
        
        h.getChild(0).setName(socketName);
        h.setSocketSystemName(socketSystemName);
        h.setDelay(delay);
        
        Element unit = shared.getChild("unit");
        if (unit != null) {
            h.setUnit(TimerUnit.valueOf(unit.getTextTrim()));
        }
        
        String resetIfAlreadyStarted = "no";
        if (shared.getChild("resetIfAlreadyStarted") != null) {  // NOI18N
            resetIfAlreadyStarted = shared.getChild("resetIfAlreadyStarted").getTextTrim();  // NOI18N
        }
        h.setResetIfAlreadyStarted("yes".equals(resetIfAlreadyStarted));
        
        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExecuteDelayedXml.class);
}
