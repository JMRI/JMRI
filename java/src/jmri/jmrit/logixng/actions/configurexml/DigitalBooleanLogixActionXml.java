package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.DigitalBooleanActionManager;
import jmri.jmrit.logixng.actions.DigitalBooleanLogixAction;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class DigitalBooleanLogixActionXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DigitalBooleanLogixActionXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        DigitalBooleanLogixAction p = (DigitalBooleanLogixAction) o;

        Element element = new Element("LogixAction");
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

        DigitalBooleanLogixAction.When trigger = DigitalBooleanLogixAction.When.Either;
        try {
            trigger = DigitalBooleanLogixAction.When.valueOf(triggerAttribute.getValue());
        } catch (IllegalArgumentException e) {
            // Handle backwards compability pre JMRI 5.5.5
            switch (triggerAttribute.getValue()) {
                case "CHANGE_TO_TRUE":
                    trigger = DigitalBooleanLogixAction.When.True;
                    break;
                case "CHANGE_TO_FALSE":
                    trigger = DigitalBooleanLogixAction.When.False;
                    break;
                case "CHANGE":
                    trigger = DigitalBooleanLogixAction.When.Either;
                    break;
                default:
                    throw e;
            }
        }

        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        DigitalBooleanLogixAction h = new DigitalBooleanLogixAction(sys, uname, trigger);

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
