package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.WebRequest;
import jmri.jmrit.logixng.actions.WebRequest.RequestMethodType;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectStringXml;

import org.jdom2.Element;

/**
 * Handle XML configuration for WebRequest objects.
 *
 * @author Bob Jacobsen      Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist  Copyright (C) 2023
 */
public class WebRequestXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public WebRequestXml() {
    }

    /**
     * Default implementation for storing the contents of a WebRequest
     *
     * @param o Object to store, of type WebRequest
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        WebRequest p = (WebRequest) o;

        Element element = new Element("WebRequest");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

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

        var selectRequestMethodXml = new LogixNG_SelectEnumXml<RequestMethodType>();
        var selectUrlXml = new LogixNG_SelectStringXml();
        var selectUserAgentXml = new LogixNG_SelectStringXml();

        element.addContent(selectRequestMethodXml.store(p.getSelectRequestMethod(), "requestMethod"));
        element.addContent(selectUrlXml.store(p.getSelectUrl(), "url"));
        element.addContent(selectUserAgentXml.store(p.getSelectUserAgent(), "userAgent"));

        element.addContent(new Element("localVariableForPostContent").addContent(p.getLocalVariableForPostContent()));
        element.addContent(new Element("localVariableForResponseCode").addContent(p.getLocalVariableForResponseCode()));
        element.addContent(new Element("localVariableForReplyContent").addContent(p.getLocalVariableForReplyContent()));
        element.addContent(new Element("_localVariableForCookies").addContent(p.getLocalVariableForCookies()));

        //DANIEL --- Store parameters!!!

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
        WebRequest h = new WebRequest(sys, uname);

        loadCommon(h, shared);

        h.getChild(0).setName(socketName);
        h.setSocketSystemName(socketSystemName);

        var selectRequestMethodXml = new LogixNG_SelectEnumXml<RequestMethodType>();
        var selectUrlXml = new LogixNG_SelectStringXml();
        var selectUserAgentXml = new LogixNG_SelectStringXml();

        selectRequestMethodXml.load(shared.getChild("requestMethod"), h.getSelectRequestMethod());
        selectUrlXml.load(shared.getChild("url"), h.getSelectUrl());
        selectUserAgentXml.load(shared.getChild("userAgent"), h.getSelectUserAgent());

        Element elem = shared.getChild("localVariableForPostContent");
        if (elem != null) h.setLocalVariableForPostContent(elem.getTextTrim());

        elem = shared.getChild("localVariableForResponseCode");
        if (elem != null) h.setLocalVariableForResponseCode(elem.getTextTrim());

        elem = shared.getChild("localVariableForReplyContent");
        if (elem != null) h.setLocalVariableForReplyContent(elem.getTextTrim());

        elem = shared.getChild("_localVariableForCookies");
        if (elem != null) h.setLocalVariableForCookies(elem.getTextTrim());

        //DANIEL --- Load parameters!!!

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebRequestXml.class);
}
