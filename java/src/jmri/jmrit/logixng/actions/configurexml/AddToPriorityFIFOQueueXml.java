package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.jmrit.logixng.DigitalActionBean;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.AddToPriorityFIFOQueue;
import jmri.jmrit.logixng.actions.PriorityFIFOQueue;

import org.jdom2.Element;


/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class AddToPriorityFIFOQueueXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        AddToPriorityFIFOQueue p = (AddToPriorityFIFOQueue) o;

        Element element = new Element("AddToPriorityFIFOQueue");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        NamedBeanHandle handle = p.getPriorityFIFOQueue();
        if (handle != null) {
            element.addContent(new Element("priorityFIFOQueue").addContent(handle.getName()));
        }

        element.addContent(new Element("priority").addContent(Integer.toString(p.getPriority())));

        Element e2 = new Element("Socket");
        e2.addContent(new Element("socketName").addContent(p.getChild(0).getName()));
        MaleSocket socket = p.getDigitalActionSocket().getConnectedSocket();
        String socketSystemName;
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getDigitalActionSocketSystemName();
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
        AddToPriorityFIFOQueue h = new AddToPriorityFIFOQueue(sys, uname);

        loadCommon(h, shared);

        Element priorityFIFOQueueName = shared.getChild("priorityFIFOQueue");
        if (priorityFIFOQueueName != null) {
            h.setPriorityFIFOQueue(priorityFIFOQueueName.getTextTrim());
        }

        Element priority = shared.getChild("priority");
        if (priority != null) {
            int num;
            try {
                num = Integer.parseInt(priority.getTextTrim());
            } catch (NumberFormatException ex) {
                num = 0;
            }
            h.setPriority(num);
        }
        
        Element socketName = shared.getChild("Socket").getChild("socketName");
        h.getChild(0).setName(socketName.getTextTrim());
        Element socketSystemName = shared.getChild("Socket").getChild("systemName");
        if (socketSystemName != null) {
            h.setDigitalActionSocketSystemName(socketSystemName.getTextTrim());
        }
        
        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AddToPriorityFIFOQueueXml.class);
}
