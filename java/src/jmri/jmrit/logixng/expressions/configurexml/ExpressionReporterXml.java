package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.expressions.ExpressionReporter;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
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

        var reporter = p.getReporter();
        if (reporter != null) {
            element.addContent(new Element("reporter").addContent(reporter.getName()));
        }

        element.addContent(new Element("reporterValue").addContent(p.getReporterValue().name()));
        element.addContent(new Element("reporterOperation").addContent(p.getReporterOperation().name()));
        element.addContent(new Element("compareTo").addContent(p.getCompareTo().name()));

        element.addContent(new Element("caseInsensitive").addContent(p.getCaseInsensitive() ? "yes" : "no"));

        element.addContent(new Element("constant").addContent(p.getConstantValue()));

        var memory = p.getMemory();
        if (memory != null) {
            element.addContent(new Element("memory").addContent(memory.getName()));
        }

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

        Element reporterName = shared.getChild("reporter");
        if (reporterName != null) {
            Reporter m = InstanceManager.getDefault(ReporterManager.class).getReporter(reporterName.getTextTrim());
            if (m != null) h.setReporter(m);
            else h.removeReporter();
        }

        Element memoryName = shared.getChild("memory");
        if (memoryName != null) {
            Memory m = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName.getTextTrim());
            if (m != null) h.setMemory(m);
            else h.removeMemory();
        }

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
