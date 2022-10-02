package jmri.jmrit.logixng.actions.configurexml;

import java.util.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.IfThenElse;

import org.jdom2.Attribute;
import org.jdom2.Element;


/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class IfThenElseXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        IfThenElse p = (IfThenElse) o;

        Element element = new Element("IfThenElse");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.setAttribute("executeType", p.getExecuteType().name());
        element.setAttribute("evaluateType", p.getEvaluateType().name());

        String socketSystemName;
        Element e = new Element("Expressions");
        for (int i=0; i < p.getNumExpressions(); i++) {
            Element e2 = new Element("Socket");
            e2.addContent(new Element("socketName").addContent(p.getExpressionSocket(i).getName()));
            MaleSocket socket = p.getExpressionSocket(i).getConnectedSocket();
            if (socket != null) {
                socketSystemName = socket.getSystemName();
            } else {
                socketSystemName = p.getExpressionSocketSystemName(i);
            }
            if (socketSystemName != null) {
                e2.addContent(new Element("systemName").addContent(socketSystemName));
            }
            e.addContent(e2);
        }
        element.addContent(e);

        e = new Element("Actions");
        for (int i=0; i < p.getNumActions(); i++) {
            Element e2 = new Element("Socket");
            e2.addContent(new Element("socketName").addContent(p.getActionSocket(i).getName()));
            MaleSocket socket = p.getActionSocket(i).getConnectedSocket();
            if (socket != null) {
                socketSystemName = socket.getSystemName();
            } else {
                socketSystemName = p.getActionSocketSystemName(i);
            }
            if (socketSystemName != null) {
                e2.addContent(new Element("systemName").addContent(socketSystemName));
            }
            e.addContent(e2);
        }
        element.addContent(e);

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {

        IfThenElse.ExecuteType executeType = IfThenElse.ExecuteType.ExecuteOnChange;
        IfThenElse.EvaluateType evaluateType = IfThenElse.EvaluateType.EvaluateAll;

        Attribute typeAttr = shared.getAttribute("executeType");
        if (typeAttr != null) {
            String typeStr = typeAttr.getValue();
            executeType = IfThenElse.ExecuteType.valueOf(typeStr);
        }

        typeAttr = shared.getAttribute("evaluateType");
        if (typeAttr != null) {
            String typeStr = typeAttr.getValue();
            evaluateType = IfThenElse.EvaluateType.valueOf(typeStr);
        }

        // For backward compatibility pre JMRI 5.1.5
        typeAttr = shared.getAttribute("type");
        if (typeAttr != null) {
            String typeStr = typeAttr.getValue();
            executeType = IfThenElse.ExecuteType.valueOf(typeStr);
        }

        String sys = getSystemName(shared);
        String uname = getUserName(shared);

        List<Map.Entry<String, String>> expressionSystemNames = new ArrayList<>();

        Element expressionElement = shared.getChild("Expressions");
        if (expressionElement != null) {
            for (Element socketElement : expressionElement.getChildren()) {
                String socketName = socketElement.getChild("socketName").getTextTrim();
                Element systemNameElement = socketElement.getChild("systemName");
                String systemName = null;
                if (systemNameElement != null) {
                    systemName = systemNameElement.getTextTrim();
                }
                expressionSystemNames.add(new AbstractMap.SimpleEntry<>(socketName, systemName));
            }
        }

        List<Map.Entry<String, String>> actionSystemNames = new ArrayList<>();

        Element actionElement = shared.getChild("Actions");
        if (actionElement != null) {
            for (Element socketElement : actionElement.getChildren()) {
                String socketName = socketElement.getChild("socketName").getTextTrim();
                Element systemNameElement = socketElement.getChild("systemName");
                String systemName = null;
                if (systemNameElement != null) {
                    systemName = systemNameElement.getTextTrim();
                }
                actionSystemNames.add(new AbstractMap.SimpleEntry<>(socketName, systemName));
            }
        }

        // For backwards compability up until 5.1.3
        if (shared.getChild("IfSocket") != null) {
            String socketName = shared.getChild("IfSocket").getChild("socketName").getTextTrim();
            Element socketSystemName = shared.getChild("IfSocket").getChild("systemName");
            String systemName = null;
            if (socketSystemName != null) {
                systemName = socketSystemName.getTextTrim();
            }
            expressionSystemNames.add(new AbstractMap.SimpleEntry<>(socketName, systemName));
        }
        if (shared.getChild("ThenSocket") != null) {
            String socketName = shared.getChild("ThenSocket").getChild("socketName").getTextTrim();
            Element socketSystemName = shared.getChild("ThenSocket").getChild("systemName");
            String systemName = null;
            if (socketSystemName != null) {
                systemName = socketSystemName.getTextTrim();
            }
            actionSystemNames.add(new AbstractMap.SimpleEntry<>(socketName, systemName));
        }
        if (shared.getChild("ElseSocket") != null) {
            String socketName = shared.getChild("ElseSocket").getChild("socketName").getTextTrim();
            Element socketSystemName = shared.getChild("ElseSocket").getChild("systemName");
            String systemName = null;
            if (socketSystemName != null) {
                systemName = socketSystemName.getTextTrim();
            }
            actionSystemNames.add(new AbstractMap.SimpleEntry<>(socketName, systemName));
        }
        // For backwards compability up until 5.1.3

        IfThenElse h = new IfThenElse(sys, uname, expressionSystemNames, actionSystemNames);
        h.setExecuteType(executeType);
        h.setEvaluateType(evaluateType);

        loadCommon(h, shared);

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IfThenElseXml.class);
}
