package jmri.jmrit.logixng.util.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.util.LogixNG_SelectDouble;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Xml class for jmri.jmrit.logixng.util.LogixNG_SelectInteger.
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class LogixNG_SelectDoubleXml {

    /**
     * Default implementation for storing the contents of a LogixNG_SelectEnum
     *
     * @param selectDouble the LogixNG_SelectInteger object
     * @param tagName the name of the element
     * @return Element containing the complete info
     */
    public Element store(LogixNG_SelectDouble selectDouble, String tagName) {

        LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

        Element doubleElement = new Element(tagName);

        doubleElement.addContent(new Element("addressing").addContent(selectDouble.getAddressing().name()));
        doubleElement.addContent(new Element("value").addContent(selectDouble.formatValue(selectDouble.getValue())));
        if (selectDouble.getReference() != null && !selectDouble.getReference().isEmpty()) {
            doubleElement.addContent(new Element("reference").addContent(selectDouble.getReference()));
        }
        var memory = selectDouble.getMemory();
        if (memory != null) {
            doubleElement.addContent(new Element("memory").addContent(memory.getName()));
        }
        doubleElement.addContent(new Element("listenToMemory").addContent(selectDouble.getListenToMemory() ? "yes" : "no"));
        if (selectDouble.getLocalVariable() != null && !selectDouble.getLocalVariable().isEmpty()) {
            doubleElement.addContent(new Element("localVariable").addContent(selectDouble.getLocalVariable()));
        }
        if (selectDouble.getFormula() != null && !selectDouble.getFormula().isEmpty()) {
            doubleElement.addContent(new Element("formula").addContent(selectDouble.getFormula()));
        }

        if (selectDouble.getAddressing() == NamedBeanAddressing.Table) {
            doubleElement.addContent(selectTableXml.store(selectDouble.getSelectTable(), "table"));
        }

        return doubleElement;
    }

    public void load(Element doubleElement, LogixNG_SelectDouble selectDouble)
            throws JmriConfigureXmlException {

        if (doubleElement != null) {

            LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

            try {
                Element elem = doubleElement.getChild("addressing");
                if (elem != null) {
                    selectDouble.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
                }

                elem = doubleElement.getChild("value");
                if (elem != null) {
                    selectDouble.setValue(Double.parseDouble(elem.getTextTrim()));
                }

                elem = doubleElement.getChild("reference");
                if (elem != null) selectDouble.setReference(elem.getTextTrim());

                Element memoryName = doubleElement.getChild("memory");
                if (memoryName != null) {
                    Memory m = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName.getTextTrim());
                    if (m != null) selectDouble.setMemory(m);
                    else selectDouble.removeMemory();
                }

                Element listenToMemoryElem = doubleElement.getChild("listenToMemory");
                if (listenToMemoryElem != null) {
                    selectDouble.setListenToMemory("yes".equals(listenToMemoryElem.getTextTrim()));
                }

                elem = doubleElement.getChild("localVariable");
                if (elem != null) selectDouble.setLocalVariable(elem.getTextTrim());

                elem = doubleElement.getChild("formula");
                if (elem != null) selectDouble.setFormula(elem.getTextTrim());

                if (doubleElement.getChild("table") != null) {
                    selectTableXml.load(doubleElement.getChild("table"), selectDouble.getSelectTable());
                }

            } catch (ParserException e) {
                throw new JmriConfigureXmlException(e);
            }
        }
    }

}
