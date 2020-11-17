package jmri.jmrit.logixng.actions.configurexml;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jmri.InstanceManager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.ActionTimer;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ActionTimerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionTimerXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionTimer p = (ActionTimer) o;

        Element element = new Element("action-timer");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        Element e = new Element("expressions");
        for (int i=0; i < p.getNumExpressions(); i++) {
            Element e2 = new Element("socket");
            e2.addContent(new Element("socketName").addContent(p.getExpressionSocket(i).getName()));
            MaleSocket socket = p.getExpressionSocket(i).getConnectedSocket();
            String socketSystemName;
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

        e = new Element("actions");
        for (int i=0; i < p.getNumActions(); i++) {
            Element e2 = new Element("socket");
            e2.addContent(new Element("delay").addContent(Long.toString(p.getDelay(i))));
            e2.addContent(new Element("socketName").addContent(p.getActionSocket(i).getName()));
            MaleSocket socket = p.getActionSocket(i).getConnectedSocket();
            String socketSystemName;
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
        element.addContent(new Element("unit").addContent(p.getUnit().name()));
        
        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        List<Map.Entry<String, String>> expressionSystemNames = new ArrayList<>();
        
        Element expressionElement = shared.getChild("expressions");
        for (Element socketElement : expressionElement.getChildren()) {
            String socketName = socketElement.getChild("socketName").getTextTrim();
            Element systemNameElement = socketElement.getChild("systemName");
            String systemName = null;
            if (systemNameElement != null) {
                systemName = systemNameElement.getTextTrim();
            }
            expressionSystemNames.add(new AbstractMap.SimpleEntry<>(socketName, systemName));
        }
        
        List<ActionTimer.ActionData> actionDataList = new ArrayList<>();
        
        Element actionElement = shared.getChild("actions");
        for (Element socketElement : actionElement.getChildren()) {
            String socketName = socketElement.getChild("socketName").getTextTrim();
            Element systemNameElement = socketElement.getChild("systemName");
            String systemName = null;
            if (systemNameElement != null) {
                systemName = systemNameElement.getTextTrim();
            }
            Element delayElement = socketElement.getChild("delay");
            int delay = 0;
            if (delayElement != null) {
                delay = Integer.parseInt(delayElement.getText());
            }
            actionDataList.add(new ActionTimer.ActionData(delay, socketName, systemName));
        }
        
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionTimer h = new ActionTimer(sys, uname, expressionSystemNames, actionDataList);
        
        loadCommon(h, shared);
        
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
        
        Element unit = shared.getChild("unit");
        if (unit != null) {
            h.setUnit(ActionTimer.Unit.valueOf(unit.getTextTrim()));
        }
        
        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionTimerXml.class);
}
