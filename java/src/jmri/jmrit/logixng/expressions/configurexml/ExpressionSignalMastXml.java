package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.expressions.ExpressionSignalMast;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ExpressionSignalMastXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ExpressionSignalMastXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ExpressionSignalMastXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalMast
     *
     * @param o Object to store, of type TripleLightSignalMast
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExpressionSignalMast p = (ExpressionSignalMast) o;

        Element element = new Element("ExpressionSignalMast");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        NamedBeanHandle<SignalMast> signalMast = p.getSignalMast();
        if (signalMast != null) {
            element.addContent(new Element("signalMast").addContent(signalMast.getName()));
        }
        
        element.addContent(new Element("addressing").addContent(p.getAddressing().name()));
        element.addContent(new Element("reference").addContent(p.getReference()));
        element.addContent(new Element("localVariable").addContent(p.getLocalVariable()));
        element.addContent(new Element("formula").addContent(p.getFormula()));
        
        element.addContent(new Element("queryAddressing").addContent(p.getQueryAddressing().name()));
        element.addContent(new Element("queryType").addContent(p.getQueryType().name()));
        element.addContent(new Element("queryReference").addContent(p.getQueryReference()));
        element.addContent(new Element("queryLocalVariable").addContent(p.getQueryLocalVariable()));
        element.addContent(new Element("queryFormula").addContent(p.getQueryFormula()));
        
        element.addContent(new Element("aspectAddressing").addContent(p.getAspectAddressing().name()));
        element.addContent(new Element("aspect").addContent(p.getAspect()));
        element.addContent(new Element("aspectReference").addContent(p.getAspectReference()));
        element.addContent(new Element("aspectLocalVariable").addContent(p.getAspectLocalVariable()));
        element.addContent(new Element("aspectFormula").addContent(p.getAspectFormula()));
        
        signalMast = p.getExampleSignalMast();
        if (signalMast != null) {
            element.addContent(new Element("exampleSignalMast").addContent(signalMast.getName()));
        }
        
        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionSignalMast h = new ExpressionSignalMast(sys, uname);

        loadCommon(h, shared);

        Element signalMastName = shared.getChild("signalMast");
        if (signalMastName != null) {
            SignalMast signalMast = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(signalMastName.getTextTrim());
            if (signalMast != null) h.setSignalMast(signalMast);
            else h.removeSignalMast();
        }
        
        try {
            Element elem = shared.getChild("addressing");
            if (elem != null) {
                h.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }
            
            elem = shared.getChild("reference");
            if (elem != null) h.setReference(elem.getTextTrim());
            
            elem = shared.getChild("localVariable");
            if (elem != null) h.setLocalVariable(elem.getTextTrim());
            
            elem = shared.getChild("formula");
            if (elem != null) h.setFormula(elem.getTextTrim());
            
            
            elem = shared.getChild("queryAddressing");
            if (elem != null) {
                h.setQueryAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }
            
            Element queryType = shared.getChild("queryType");
            if (queryType != null) {
                h.setQueryType(ExpressionSignalMast.QueryType.valueOf(queryType.getTextTrim()));
            }
            
            elem = shared.getChild("queryReference");
            if (elem != null) h.setQueryReference(elem.getTextTrim());
            
            elem = shared.getChild("queryLocalVariable");
            if (elem != null) h.setQueryLocalVariable(elem.getTextTrim());
            
            elem = shared.getChild("queryFormula");
            if (elem != null) h.setQueryFormula(elem.getTextTrim());
            
            
            elem = shared.getChild("aspectAddressing");
            if (elem != null) {
                h.setAspectAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }
            
            Element aspectElement = shared.getChild("aspect");
            if (aspectElement != null) {
                try {
                    h.setAspect(aspectElement.getTextTrim());
                } catch (NumberFormatException e) {
                    log.error("cannot parse aspect: " + aspectElement.getTextTrim(), e);
                }
            }
            
            elem = shared.getChild("aspectReference");
            if (elem != null) h.setAspectReference(elem.getTextTrim());
            
            elem = shared.getChild("aspectLocalVariable");
            if (elem != null) h.setAspectLocalVariable(elem.getTextTrim());
            
            elem = shared.getChild("aspectFormula");
            if (elem != null) h.setAspectFormula(elem.getTextTrim());
            
        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }
        
        signalMastName = shared.getChild("exampleSignalMast");
        if (signalMastName != null) {
            SignalMast signalMast = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(signalMastName.getTextTrim());
            if (signalMast != null) h.setExampleSignalMast(signalMast);
            else h.removeSignalMast();
        }
        
        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSignalMastXml.class);
}
