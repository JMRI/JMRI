package jmri.jmrit.logixng.expressions.configurexml;

import java.util.*;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.expressions.DigitalFormula;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class DigitalFormulaXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DigitalFormulaXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        DigitalFormula p = (DigitalFormula) o;

        Element element = new Element("DigitalFormula");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        Element e = new Element("Expressions");
        for (int i=0; i < p.getChildCount(); i++) {
            Element e2 = new Element("Socket");
            e2.addContent(new Element("socketName").addContent(p.getChild(i).getName()));
            MaleSocket socket = p.getChild(i).getConnectedSocket();
            
            String socketSystemName;
            String socketManager;
            if (socket != null) {
                socketSystemName = socket.getSystemName();
                socketManager = socket.getManager().getClass().getName();
            } else {
                socketSystemName = p.getExpressionSystemName(i);
                socketManager = p.getExpressionManager(i);
            }
            if (socketSystemName != null) {
                e2.addContent(new Element("systemName").addContent(socketSystemName));
            }
            if (socketManager != null) {
                e2.addContent(new Element("manager").addContent(socketManager));
            }
            e.addContent(e2);
        }
        
        element.addContent(e);
        
        element.addContent(new Element("formula").addContent(p.getFormula()));

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        List<DigitalFormula.SocketData> expressionSystemNames = new ArrayList<>();
        
        Element actionElement = shared.getChild("Expressions");
        for (Element socketElement : actionElement.getChildren()) {
            String socketName = socketElement.getChild("socketName").getTextTrim();
            Element systemNameElement = socketElement.getChild("systemName");
            String systemName = null;
            if (systemNameElement != null) {
                systemName = systemNameElement.getTextTrim();
            }
            Element managerElement = socketElement.getChild("manager");
            String manager = null;
            if (managerElement != null) {
                manager = managerElement.getTextTrim();
            }
            expressionSystemNames.add(new DigitalFormula.SocketData(socketName, systemName, manager));
        }
        
        // put it together
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        
        DigitalFormula h = new DigitalFormula(sys, uname, expressionSystemNames);

        loadCommon(h, shared);

        Element formula = shared.getChild("formula");
        if (formula != null) {
            try {
                h.setFormula(formula.getTextTrim());
            } catch (ParserException e) {
                log.error("cannot set formula: " + formula.getTextTrim(), e);
            }
        }

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DigitalFormulaXml.class);
}
