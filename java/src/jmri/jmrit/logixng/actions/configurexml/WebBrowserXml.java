package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
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

        Element e2 = new Element("Socket");
        e2.addContent(new Element("socketName").addContent(p.getChild(0).getName()));
        MaleSocket socket = p.getStringExpressionSocket().getConnectedSocket();
        String socketSystemName;
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getStringExpressionSocketSystemName();
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
        WebBrowser h = new WebBrowser(sys, uname);

        loadCommon(h, shared);

        Element socketElement = shared.getChild("Socket");
        if (socketElement != null) {
            Element socketName = socketElement.getChild("socketName");
            h.getChild(0).setName(socketName.getTextTrim());
            Element socketSystemName = socketElement.getChild("systemName");
            if (socketSystemName != null) {
                h.setStringExpressionSocketSystemName(socketSystemName.getTextTrim());
            }
        }

        // For backwards compability
        Element expressionSystemNameElement = shared.getChild("expressionSystemName");
        if (expressionSystemNameElement != null) {
            h.setStringExpressionSocketSystemName(expressionSystemNameElement.getTextTrim());
        }
        // For backwards compability

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DoStringActionXml.class);
}
