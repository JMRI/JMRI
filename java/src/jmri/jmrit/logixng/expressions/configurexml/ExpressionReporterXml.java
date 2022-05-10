package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.expressions.ExpressionReporter;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionReporterXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ExpressionReporterXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ExpressionReporterXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExpressionReporter p = (ExpressionReporter) o;

        Element element = new Element("ExpressionReporter");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Reporter>();
        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));

        var selectMemoryNamedBeanXml = new LogixNG_SelectNamedBeanXml<Memory>();
        element.addContent(selectMemoryNamedBeanXml.store(p.getSelectMemoryNamedBean(), "memoryNamedBean"));

        element.addContent(new Element("reporterValue").addContent(p.getReporterValue().name()));
        element.addContent(new Element("reporterOperation").addContent(p.getReporterOperation().name()));
        element.addContent(new Element("compareTo").addContent(p.getCompareTo().name()));

        element.addContent(new Element("caseInsensitive").addContent(p.getCaseInsensitive() ? "yes" : "no"));

        element.addContent(new Element("constant").addContent(p.getConstantValue()));

        String variableName = p.getLocalVariable();
        if (variableName != null) {
            element.addContent(new Element("variable").addContent(variableName));
        }

        element.addContent(new Element("regEx").addContent(p.getRegEx()));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionReporter h = new ExpressionReporter(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Reporter>();
        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "reporter", null, null, null, null);

        var selectMemoryNamedBeanXml = new LogixNG_SelectNamedBeanXml<Memory>();
        selectMemoryNamedBeanXml.load(shared.getChild("memoryNamedBean"), h.getSelectMemoryNamedBean());
        selectMemoryNamedBeanXml.loadLegacy(shared, h.getSelectMemoryNamedBean(), "memory", null, null, null, null);

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

        Element reporterValue = shared.getChild("reporterValue");
        if (reporterValue != null) {
            h.setReporterValue(ExpressionReporter.ReporterValue.valueOf(reporterValue.getTextTrim()));
        }

        Element reporterOperation = shared.getChild("reporterOperation");
        if (reporterOperation != null) {
            h.setReporterOperation(ExpressionReporter.ReporterOperation.valueOf(reporterOperation.getTextTrim()));
        }

        Element compareTo = shared.getChild("compareTo");
        if (compareTo != null) {
            h.setCompareTo(ExpressionReporter.CompareTo.valueOf(compareTo.getTextTrim()));
        }

        Element caseInsensitive = shared.getChild("caseInsensitive");
        if (caseInsensitive != null) {
            h.setCaseInsensitive("yes".equals(caseInsensitive.getTextTrim()));
        } else {
            h.setCaseInsensitive(false);
        }

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionTurnoutXml.class);
}
