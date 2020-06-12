package jmri.jmrit.logixng.digital.actions.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.logixng.FemaleAnalogActionSocket;
import jmri.jmrit.logixng.FemaleAnalogExpressionSocket;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.digital.actions.DoAnalogAction;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class DoAnalogActionXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DoAnalogActionXml() {
    }

    /**
     * Default implementation for storing the contents of a DoAnalogAction
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        DoAnalogAction p = (DoAnalogAction) o;

        Element element = new Element("do-analog-action");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        String systemName;
        
        FemaleAnalogExpressionSocket expressionSocket = p.getAnalogExpressionSocket();
        if (expressionSocket.isConnected()) {
            systemName = expressionSocket.getConnectedSocket().getSystemName();
        } else {
            systemName = p.getAnalogExpressionSocketSystemName();
        }
        if (systemName != null) {
            element.addContent(new Element("expressionSystemName").addContent(systemName));
        }

        FemaleAnalogActionSocket actionSocket = p.getAnalogActionSocket();
        if (actionSocket.isConnected()) {
            systemName = actionSocket.getConnectedSocket().getSystemName();
        } else {
            systemName = p.getAnalogActionSocketSystemName();
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
        DoAnalogAction h = new DoAnalogAction(sys, uname);

        loadCommon(h, shared);

        Element expressionSystemNameElement = shared.getChild("expressionSystemName");
        if (expressionSystemNameElement != null) {
            h.setAnalogExpressionSocketSystemName(expressionSystemNameElement.getTextTrim());
        }
        Element actionSystemNameElement = shared.getChild("actionSystemName");
        if (actionSystemNameElement != null) {
            h.setAnalogActionSocketSystemName(actionSystemNameElement.getTextTrim());
        }
        
        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
    private final static Logger log = LoggerFactory.getLogger(DoAnalogActionXml.class);
}
