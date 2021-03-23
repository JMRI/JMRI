package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.logixng.FemaleStringActionSocket;
import jmri.jmrit.logixng.FemaleStringExpressionSocket;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.WebBrowser;

import org.jdom2.Element;

/**
 * Handle XML configuration for WebBrowser objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2020
 */
public class WebBrowserXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public WebBrowserXml() {
    }

    /**
     * Default implementation for storing the contents of a WebBrowser
     *
     * @param o Object to store, of type WebBrowser
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        WebBrowser p = (WebBrowser) o;

        Element element = new Element("WebBrowser");
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

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        WebBrowser h = new WebBrowser(sys, uname);

        loadCommon(h, shared);

        Element expressionSystemNameElement = shared.getChild("expressionSystemName");
        if (expressionSystemNameElement != null) {
            h.setStringExpressionSocketSystemName(expressionSystemNameElement.getTextTrim());
        }
        
        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DoStringActionXml.class);
}
