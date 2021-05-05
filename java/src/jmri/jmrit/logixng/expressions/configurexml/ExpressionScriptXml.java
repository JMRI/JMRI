package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.expressions.ExpressionScript;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ExpressionSimpleScript objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class ExpressionScriptXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ExpressionScriptXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalMast
     *
     * @param o Object to store, of type TripleLightSignalMast
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExpressionScript p = (ExpressionScript) o;

        Element element = new Element("ExpressionScript");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(new Element("operationAddressing").addContent(p.getOperationAddressing().name()));
        element.addContent(new Element("operationType").addContent(p.getOperationType().name()));
        element.addContent(new Element("operationReference").addContent(p.getOperationReference()));
        element.addContent(new Element("operationLocalVariable").addContent(p.getOperationLocalVariable()));
        element.addContent(new Element("operationFormula").addContent(p.getOperationFormula()));
        
        element.addContent(new Element("scriptAddressing").addContent(p.getScriptAddressing().name()));
        element.addContent(new Element("script").addContent(p.getScript()));
        element.addContent(new Element("scriptReference").addContent(p.getScriptReference()));
        element.addContent(new Element("scriptLocalVariable").addContent(p.getScriptLocalVariable()));
        element.addContent(new Element("scriptFormula").addContent(p.getScriptFormula()));
        
        element.addContent(new Element("registerListenerScript").addContent(p.getRegisterListenerScript()));
        element.addContent(new Element("unregisterListenerScript").addContent(p.getUnregisterListenerScript()));
        
        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionScript h = new ExpressionScript(sys, uname);

        loadCommon(h, shared);

        try {
            Element elem = shared.getChild("operationAddressing");
            if (elem != null) {
                h.setOperationAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }
            
            Element queryType = shared.getChild("operationType");
            if (queryType != null) {
                h.setOperationType(ExpressionScript.OperationType.valueOf(queryType.getTextTrim()));
            }
            
            elem = shared.getChild("operationReference");
            if (elem != null) h.setOperationReference(elem.getTextTrim());
            
            elem = shared.getChild("operationLocalVariable");
            if (elem != null) h.setOperationLocalVariable(elem.getTextTrim());
            
            elem = shared.getChild("operationFormula");
            if (elem != null) h.setOperationFormula(elem.getTextTrim());
            
            
            elem = shared.getChild("scriptAddressing");
            if (elem != null) {
                h.setScriptAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }
            
            Element scriptElement = shared.getChild("script");
            if (scriptElement != null) {
                h.setScript(scriptElement.getText());
            }
            
            elem = shared.getChild("scriptReference");
            if (elem != null) h.setScriptReference(elem.getTextTrim());
            
            elem = shared.getChild("scriptLocalVariable");
            if (elem != null) h.setScriptLocalVariable(elem.getTextTrim());
            
            elem = shared.getChild("scriptFormula");
            if (elem != null) h.setScriptFormula(elem.getTextTrim());
            
            Element registerListenerScriptElement = shared.getChild("registerListenerScript");
            if (registerListenerScriptElement != null) {
                h.setRegisterListenerScript(registerListenerScriptElement.getText());
            }
            
            Element unregisterListenerScriptElement = shared.getChild("unregisterListenerScript");
            if (unregisterListenerScriptElement != null) {
                h.setUnregisterListenerScript(unregisterListenerScriptElement.getText());
            }
            
        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }
        
        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionScriptXml.class);
}
