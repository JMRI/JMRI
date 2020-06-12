package jmri.jmrit.logixng.digital.actions.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.logixng.FemaleStringActionSocket;
import jmri.jmrit.logixng.FemaleStringExpressionSocket;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.digital.actions.DoStringAction;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class DoStringActionXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DoStringActionXml() {
    }

    /**
     * Default implementation for storing the contents of a DoStringAction
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        DoStringAction p = (DoStringAction) o;

        Element element = new Element("do-string-action");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        String systemName;
        
        FemaleStringExpressionSocket expressionSocket = p.getStringExpressionSocket();
        if (expressionSocket.isConnected()) {
            systemName = expressionSocket.getConnectedSocket().getSystemName();
        } else {
            systemName = p.getStringExpressionSocketSystemName();
        }
        if (systemName != null) {
            element.addContent(new Element("expressionSystemName").addContent(systemName));
        }

        FemaleStringActionSocket actionSocket = p.getStringActionSocket();
        if (actionSocket.isConnected()) {
            systemName = actionSocket.getConnectedSocket().getSystemName();
        } else {
            systemName = p.getStringActionSocketSystemName();
        }
        if (systemName != null) {
            element.addContent(new Element("actionSystemName").addContent(systemName));
        }

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        DoStringAction h = new DoStringAction(sys, uname);

        loadCommon(h, shared);

        Element expressionSystemNameElement = shared.getChild("expressionSystemName");
        if (expressionSystemNameElement != null) {
            h.setStringExpressionSocketSystemName(expressionSystemNameElement.getTextTrim());
        }
        Element actionSystemNameElement = shared.getChild("actionSystemName");
        if (actionSystemNameElement != null) {
            h.setStringActionSocketSystemName(actionSystemNameElement.getTextTrim());
        }
        
        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static Logger log = LoggerFactory.getLogger(DoStringActionXml.class);
}
