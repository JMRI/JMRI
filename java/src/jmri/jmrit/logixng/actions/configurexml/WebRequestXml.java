package jmri.jmrit.logixng.actions.configurexml;

import java.util.List;

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

        element.addContent(new Element("localVariableForResponseCode").addContent(p.getLocalVariableForResponseCode()));
        element.addContent(new Element("localVariableForReplyContent").addContent(p.getLocalVariableForReplyContent()));
        element.addContent(new Element("localVariableForCookies").addContent(p.getLocalVariableForCookies()));

        Element parameters = new Element("Parameters");
        for (WebRequest.Parameter parameter : p.getParameters()) {
            Element elementParameter = new Element("Parameter");
            elementParameter.addContent(new Element("name").addContent(parameter.getName()));
            elementParameter.addContent(new Element("type").addContent(parameter.getType().name()));
            elementParameter.addContent(new Element("data").addContent(parameter.getData()));
            parameters.addContent(elementParameter);
        }
        element.addContent(parameters);

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

        Element elem = shared.getChild("localVariableForResponseCode");
        if (elem != null) h.setLocalVariableForResponseCode(elem.getTextTrim());

        elem = shared.getChild("localVariableForReplyContent");
        if (elem != null) h.setLocalVariableForReplyContent(elem.getTextTrim());

        elem = shared.getChild("localVariableForCookies");
        if (elem != null) h.setLocalVariableForCookies(elem.getTextTrim());

        List<Element> parameters = shared.getChild("Parameters").getChildren();  // NOI18N
        log.debug("Found {} dataList", parameters.size() );  // NOI18N

        for (Element e : parameters) {
            Element elementName = e.getChild("name");
            if (elementName == null) throw new IllegalArgumentException("Element 'name' does not exists");

            SymbolTable.InitialValueType type = SymbolTable.InitialValueType.LocalVariable;
            Element elementType = e.getChild("type");
            if (elementType != null) {
                type = SymbolTable.InitialValueType.valueOf(elementType.getTextTrim());
            }

            Element elementData = e.getChild("data");
            if (elementData == null) throw new IllegalArgumentException("Element 'data' does not exists");

            h.getParameters().add(new WebRequest.Parameter(elementName.getTextTrim(), type, elementData.getTextTrim()));
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebRequestXml.class);
}
