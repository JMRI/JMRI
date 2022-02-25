package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.expressions.ExpressionLocalVariable;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectTableXml;

import org.jdom2.Element;

/**
 * Handle XML configuration for ExpressionLocalVariable objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ExpressionLocalVariableXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ExpressionLocalVariableXml() {
    }

    /**
     * Default implementation for storing the contents of a ExpressionLocalVariable
     *
     * @param o Object to store, of type ExpressionLocalVariable
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExpressionLocalVariable p = (ExpressionLocalVariable) o;

        LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

        Element element = new Element("ExpressionLocalVariable");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        String variableName = p.getLocalVariable();
        if (variableName != null) {
            element.addContent(new Element("variable").addContent(variableName));
        }
        String otherVariableName = p.getOtherLocalVariable();
        if (otherVariableName != null) {
            element.addContent(new Element("otherVariable").addContent(otherVariableName));
        }

        NamedBeanHandle<Memory> memoryName = p.getMemory();
        if (memoryName != null) {
            element.addContent(new Element("memory").addContent(memoryName.getName()));
        }

        element.addContent(new Element("compareTo").addContent(p.getCompareTo().name()));
        element.addContent(new Element("variableOperation").addContent(p.getVariableOperation().name()));
        element.addContent(new Element("caseInsensitive").addContent(p.getCaseInsensitive() ? "yes" : "no"));

        element.addContent(new Element("constant").addContent(p.getConstantValue()));
        element.addContent(new Element("regEx").addContent(p.getRegEx()));

        element.addContent(selectTableXml.store(p.getSelectTable(), "table"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionLocalVariable h = new ExpressionLocalVariable(sys, uname);

        LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

        loadCommon(h, shared);

        Element variableName = shared.getChild("variable");
        if (variableName != null) {
            h.setLocalVariable(variableName.getTextTrim());
        }

        Element otherVariableName = shared.getChild("otherVariable");
        if (otherVariableName != null) {
            h.setOtherLocalVariable(otherVariableName.getTextTrim());
        }

        Element memoryName = shared.getChild("memory");
        if (memoryName != null) {
            Memory t = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName.getTextTrim());
            if (t != null) h.setMemory(t);
            else h.removeMemory();
        }

        Element constant = shared.getChild("constant");
        if (constant != null) {
            h.setConstantValue(constant.getText());
        }

        Element regEx = shared.getChild("regEx");
        if (regEx != null) {
            h.setRegEx(regEx.getText());
        }

        Element compareTo = shared.getChild("compareTo");
        if (compareTo != null) {
            h.setCompareTo(ExpressionLocalVariable.CompareTo.valueOf(compareTo.getTextTrim()));
        }

        Element variableOperation = shared.getChild("variableOperation");
        if (variableOperation != null) {
            h.setVariableOperation(ExpressionLocalVariable.VariableOperation.valueOf(variableOperation.getTextTrim()));
        }

        Element caseInsensitive = shared.getChild("caseInsensitive");
        if (caseInsensitive != null) {
            h.setCaseInsensitive("yes".equals(caseInsensitive.getTextTrim()));
        } else {
            h.setCaseInsensitive(false);
        }

        selectTableXml.load(shared.getChild("table"), h.getSelectTable());

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionLocalVariableXml.class);
}
