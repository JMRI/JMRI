package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.expressions.ExpressionMemory;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectTableXml;

import org.jdom2.Element;

/**
 * Handle XML configuration for ExpressionMemory objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ExpressionMemoryXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ExpressionMemoryXml() {
    }

    /**
     * Default implementation for storing the contents of a ExpressionMemory
     *
     * @param o Object to store, of type ExpressionMemory
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExpressionMemory p = (ExpressionMemory) o;

        LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

        Element element = new Element("ExpressionMemory");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Memory>();
        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));

        var selectOtherMemoryNamedBeanXml = new LogixNG_SelectNamedBeanXml<Memory>();
        element.addContent(selectOtherMemoryNamedBeanXml.store(p.getSelectOtherMemoryNamedBean(), "otherMemoryNamedBean"));

        String variableName = p.getLocalVariable();
        if (variableName != null) {
            element.addContent(new Element("variable").addContent(variableName));
        }

        element.addContent(new Element("compareTo").addContent(p.getCompareTo().name()));
        element.addContent(new Element("memoryOperation").addContent(p.getMemoryOperation().name()));
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
        ExpressionMemory h = new ExpressionMemory(sys, uname);

        LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Memory>();
        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "memory", null, null, null, null);

        var selectOtherMemoryNamedBeanXml = new LogixNG_SelectNamedBeanXml<Memory>();
        selectOtherMemoryNamedBeanXml.load(shared.getChild("otherMemoryNamedBean"), h.getSelectOtherMemoryNamedBean());
        selectOtherMemoryNamedBeanXml.loadLegacy(shared, h.getSelectOtherMemoryNamedBean(), "otherMemory", null, null, null, null);

        Element variableName = shared.getChild("variable");
        if (variableName != null) {
            h.setLocalVariable(variableName.getTextTrim());
        }

        Element constant = shared.getChild("constant");
        if (constant != null) {
            h.setConstantValue(constant.getText());
        }

        Element regEx = shared.getChild("regEx");
        if (regEx != null) {
            h.setRegEx(regEx.getText());
        }

        Element memoryOperation = shared.getChild("memoryOperation");
        if (memoryOperation != null) {
            h.setMemoryOperation(ExpressionMemory.MemoryOperation.valueOf(memoryOperation.getTextTrim()));
        }

        Element compareTo = shared.getChild("compareTo");
        if (compareTo != null) {
            h.setCompareTo(ExpressionMemory.CompareTo.valueOf(compareTo.getTextTrim()));
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

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionTurnoutXml.class);
}
