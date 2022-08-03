package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.ActionListenOnBeansLocalVariable;
import jmri.jmrit.logixng.actions.NamedBeanType;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Handle XML configuration for ActionListenOnBeansLocalVariable objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class ActionListenOnBeansLocalVariableXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionListenOnBeansLocalVariableXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionListenOnBeansLocalVariable p = (ActionListenOnBeansLocalVariable) o;

        Element element = new Element("ActionListenOnBeansLocalVariable");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(new Element("namedBeanType").addContent(p.getNamedBeanType().name()));

        element.addContent(new Element("localVariableBeanToListenOn").addContent(p.getLocalVariableBeanToListenOn()));

        element.addContent(new Element("localVariableNamedBean").addContent(p.getLocalVariableNamedBean()));
        element.addContent(new Element("localVariableEvent").addContent(p.getLocalVariableEvent()));
        element.addContent(new Element("localVariableNewValue").addContent(p.getLocalVariableNewValue()));

        element.setAttribute("listenOnAllProperties",
                p.getListenOnAllProperties()? "yes" : "no");  // NOI18N

        Element e2 = new Element("Socket");
        e2.addContent(new Element("socketName").addContent(p.getChild(0).getName()));
        MaleSocket socket = p.getExecuteSocket().getConnectedSocket();
        String socketSystemName;
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getExecuteSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {

        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionListenOnBeansLocalVariable h = new ActionListenOnBeansLocalVariable(sys, uname);

        loadCommon(h, shared);

        Element namedBeanTypeElement = shared.getChild("namedBeanType");
        NamedBeanType namedBeanType =
                NamedBeanType.valueOf(namedBeanTypeElement.getTextTrim());
        h.setNamedBeanType(namedBeanType);

        Element variableName = shared.getChild("localVariableBeanToListenOn");
        if (variableName != null) {
            h.setLocalVariableBeanToListenOn(variableName.getTextTrim());
        }

        variableName = shared.getChild("localVariableNamedBean");
        if (variableName != null) {
            h.setLocalVariableNamedBean(variableName.getTextTrim());
        }

        variableName = shared.getChild("localVariableEvent");
        if (variableName != null) {
            h.setLocalVariableEvent(variableName.getTextTrim());
        }

        variableName = shared.getChild("localVariableNewValue");
        if (variableName != null) {
            h.setLocalVariableNewValue(variableName.getTextTrim());
        }

        String listenOnAllProperties = "no";
        Attribute attribute = shared.getAttribute("listenOnAllProperties");
        if (attribute != null) {  // NOI18N
            listenOnAllProperties = attribute.getValue();  // NOI18N
        }
        h.setListenOnAllProperties("yes".equals(listenOnAllProperties));

        Element socketNameElement = shared.getChild("Socket").getChild("socketName");
        String socketName = socketNameElement.getTextTrim();
        Element socketSystemNameElement = shared.getChild("Socket").getChild("systemName");
        String socketSystemName = null;
        if (socketSystemNameElement != null) {
            socketSystemName = socketSystemNameElement.getTextTrim();
        }

        h.getChild(0).setName(socketName);
        h.setExecuteSocketSystemName(socketSystemName);

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionListenOnBeansLocalVariableXml.class);
}
