package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.DigitalBooleanActionManager;
import jmri.jmrit.logixng.actions.DigitalBooleanOnChange;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class DigitalBooleanOnChangeXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DigitalBooleanOnChangeXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        DigitalBooleanOnChange p = (DigitalBooleanOnChange) o;

        Element element = new Element("OnChange");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        element.setAttribute("trigger", p.getTrigger().name());
        
        Element e2 = new Element("Socket");
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

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        
        Attribute triggerAttribute = shared.getAttribute("trigger");
        DigitalBooleanOnChange.Trigger trigger = DigitalBooleanOnChange.Trigger.valueOf(triggerAttribute.getValue());
        
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        DigitalBooleanOnChange h = new DigitalBooleanOnChange(sys, uname, trigger);

        loadCommon(h, shared);
        
        Element socketName = shared.getChild("Socket").getChild("socketName");
        h.getChild(0).setName(socketName.getTextTrim());
        Element socketSystemName = shared.getChild("Socket").getChild("systemName");
        if (socketSystemName != null) {
            h.setActionSocketSystemName(socketSystemName.getTextTrim());
        }
        
        InstanceManager.getDefault(DigitalBooleanActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OnChangeActionXml.class);
}
