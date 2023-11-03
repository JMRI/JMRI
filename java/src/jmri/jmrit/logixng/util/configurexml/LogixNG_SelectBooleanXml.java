package jmri.jmrit.logixng.util.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.util.LogixNG_SelectBoolean;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Xml class for jmri.jmrit.logixng.util.LogixNG_SelectBoolean.
 *
 * @author Daniel Bergqvist (C) 2023
 */
public class LogixNG_SelectBooleanXml {

    /**
     * Default implementation for storing the contents of a LogixNG_SelectBoolean
     *
     * @param selectBoolean the LogixNG_SelectBoolean object
     * @param tagName the name of the element
     * @return Element containing the complete info
     */
    public Element store(LogixNG_SelectBoolean selectBoolean, String tagName) {

        LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

        Element intElement = new Element(tagName);

        intElement.addContent(new Element("addressing").addContent(selectBoolean.getAddressing().name()));
        intElement.addContent(new Element("value").addContent(selectBoolean.getValue() ? "yes" : "no"));
        if (selectBoolean.getReference() != null && !selectBoolean.getReference().isEmpty()) {
            intElement.addContent(new Element("reference").addContent(selectBoolean.getReference()));
        }
        var memory = selectBoolean.getMemory();
        if (memory != null) {
            intElement.addContent(new Element("memory").addContent(memory.getName()));
        }
        intElement.addContent(new Element("listenToMemory").addContent(selectBoolean.getListenToMemory() ? "yes" : "no"));
        if (selectBoolean.getLocalVariable() != null && !selectBoolean.getLocalVariable().isEmpty()) {
            intElement.addContent(new Element("localVariable").addContent(selectBoolean.getLocalVariable()));
        }
        if (selectBoolean.getFormula() != null && !selectBoolean.getFormula().isEmpty()) {
            intElement.addContent(new Element("formula").addContent(selectBoolean.getFormula()));
        }

        if (selectBoolean.getAddressing() == NamedBeanAddressing.Table) {
            intElement.addContent(selectTableXml.store(selectBoolean.getSelectTable(), "table"));
        }

        return intElement;
    }

    public void load(Element booleanElement, LogixNG_SelectBoolean selectBoolean)
            throws JmriConfigureXmlException {

        if (booleanElement != null) {

            LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

            try {
                Element elem = booleanElement.getChild("addressing");
                if (elem != null) {
                    selectBoolean.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
                }

                elem = booleanElement.getChild("value");
                if (elem != null) {
                    selectBoolean.setValue("yes".equals(elem.getTextTrim()));
                }

                elem = booleanElement.getChild("reference");
                if (elem != null) selectBoolean.setReference(elem.getTextTrim());

                Element memoryName = booleanElement.getChild("memory");
                if (memoryName != null) {
                    Memory m = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName.getTextTrim());
                    if (m != null) selectBoolean.setMemory(m);
                    else selectBoolean.removeMemory();
                }

                Element listenToMemoryElem = booleanElement.getChild("listenToMemory");
                if (listenToMemoryElem != null) {
                    selectBoolean.setListenToMemory("yes".equals(listenToMemoryElem.getTextTrim()));
                }

                elem = booleanElement.getChild("localVariable");
                if (elem != null) selectBoolean.setLocalVariable(elem.getTextTrim());

                elem = booleanElement.getChild("formula");
                if (elem != null) selectBoolean.setFormula(elem.getTextTrim());

                if (booleanElement.getChild("table") != null) {
                    selectTableXml.load(booleanElement.getChild("table"), selectBoolean.getSelectTable());
                }

            } catch (ParserException e) {
                throw new JmriConfigureXmlException(e);
            }
        }
    }

}
