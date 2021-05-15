package jmri.jmrit.logixng.actions.configurexml;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jmri.InstanceManager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.Sequence;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2020
 */
public class SequenceXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public SequenceXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Sequence p = (Sequence) o;

        Element element = new Element("Sequence");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        Element e2 = new Element("StartSocket");
        e2.addContent(new Element("socketName").addContent(p.getChild(0).getName()));
        MaleSocket socket = p.getStartExpressionSocket().getConnectedSocket();
        String socketSystemName;
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getStartExpressionSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);

        e2 = new Element("StopSocket");
        e2.addContent(new Element("socketName").addContent(p.getChild(1).getName()));
        socket = p.getStopExpressionSocket().getConnectedSocket();
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getStopExpressionSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);

        e2 = new Element("ResetSocket");
        e2.addContent(new Element("socketName").addContent(p.getChild(2).getName()));
        socket = p.getResetExpressionSocket().getConnectedSocket();
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getResetExpressionSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);

        Element e = new Element("Expressions");
        for (int i=0; i < p.getNumExpressions(); i++) {
            e2 = new Element("Socket");
            e2.addContent(new Element("socketName").addContent(p.getExpressionSocket(i).getName()));
            socket = p.getExpressionSocket(i).getConnectedSocket();
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
            e2 = new Element("Socket");
            e2.addContent(new Element("socketName").addContent(p.getActionSocket(i).getName()));
            socket = p.getActionSocket(i).getConnectedSocket();
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

        element.addContent(new Element("startImmediately").addContent(p.getStartImmediately() ? "yes" : "no"));
        element.addContent(new Element("runContinuously").addContent(p.getRunContinuously() ? "yes" : "no"));
        
        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        List<Map.Entry<String, String>> expressionSystemNames = new ArrayList<>();
        
        Element socketNameElement = shared.getChild("StartSocket").getChild("socketName");
        String startSocketName = socketNameElement.getTextTrim();
        Element socketSystemNameElement = shared.getChild("StartSocket").getChild("systemName");
        String startSocketSystemName = null;
        if (socketSystemNameElement != null) {
            startSocketSystemName = socketSystemNameElement.getTextTrim();
        }
        
        socketNameElement = shared.getChild("StopSocket").getChild("socketName");
        String stopSocketName = socketNameElement.getTextTrim();
        socketSystemNameElement = shared.getChild("StopSocket").getChild("systemName");
        String stopSocketSystemName = null;
        if (socketSystemNameElement != null) {
            stopSocketSystemName = socketSystemNameElement.getTextTrim();
        }
        
        socketNameElement = shared.getChild("ResetSocket").getChild("socketName");
        String resetSocketName = socketNameElement.getTextTrim();
        socketSystemNameElement = shared.getChild("ResetSocket").getChild("systemName");
        String resetSocketSystemName = null;
        if (socketSystemNameElement != null) {
            resetSocketSystemName = socketSystemNameElement.getTextTrim();
        }
        
        Element expressionElement = shared.getChild("Expressions");
        for (Element socketElement : expressionElement.getChildren()) {
            String socketName = socketElement.getChild("socketName").getTextTrim();
            Element systemNameElement = socketElement.getChild("systemName");
            String systemName = null;
            if (systemNameElement != null) {
                systemName = systemNameElement.getTextTrim();
            }
            expressionSystemNames.add(new AbstractMap.SimpleEntry<>(socketName, systemName));
        }
        
        List<Map.Entry<String, String>> actionSystemNames = new ArrayList<>();
        
        Element actionElement = shared.getChild("Actions");
        for (Element socketElement : actionElement.getChildren()) {
            String socketName = socketElement.getChild("socketName").getTextTrim();
            Element systemNameElement = socketElement.getChild("systemName");
            String systemName = null;
            if (systemNameElement != null) {
                systemName = systemNameElement.getTextTrim();
            }
            actionSystemNames.add(new AbstractMap.SimpleEntry<>(socketName, systemName));
        }
        
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        Sequence h = new Sequence(sys, uname, expressionSystemNames, actionSystemNames);
        
        loadCommon(h, shared);
        
        h.getChild(0).setName(startSocketName);
        h.setStartExpressionSocketSystemName(startSocketSystemName);
        
        h.getChild(1).setName(stopSocketName);
        h.setStopExpressionSocketSystemName(stopSocketSystemName);
        
        h.getChild(2).setName(resetSocketName);
        h.setResetExpressionSocketSystemName(resetSocketSystemName);
        
        Element startImmediately = shared.getChild("startImmediately");
        if (startImmediately != null) {
            h.setStartImmediately("yes".equals(startImmediately.getTextTrim()));
        } else {
            h.setStartImmediately(false);
        }
        
        Element runContinuously = shared.getChild("runContinuously");
        if (runContinuously != null) {
            h.setRunContinuously("yes".equals(runContinuously.getTextTrim()));
        } else {
            h.setRunContinuously(false);
        }
        
        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SequenceXml.class);
}
