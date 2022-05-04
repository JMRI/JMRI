package jmri.jmrit.logixng.util.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.util.LogixNG_SelectInteger;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Xml class for jmri.jmrit.logixng.util.LogixNG_SelectInteger.
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class LogixNG_SelectIntegerXml {

    /**
     * Default implementation for storing the contents of a LogixNG_SelectEnum
     *
     * @param selectInt the LogixNG_SelectInteger object
     * @param tagName the name of the element
     * @return Element containing the complete info
     */
    public Element store(LogixNG_SelectInteger selectInt, String tagName) {

        LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

        Element intElement = new Element(tagName);

        intElement.addContent(new Element("addressing").addContent(selectInt.getAddressing().name()));
        intElement.addContent(new Element("value").addContent(Integer.toString(selectInt.getValue())));
        if (selectInt.getReference() != null && !selectInt.getReference().isEmpty()) {
            intElement.addContent(new Element("reference").addContent(selectInt.getReference()));
        }
        var memory = selectInt.getMemory();
        if (memory != null) {
            intElement.addContent(new Element("memory").addContent(memory.getName()));
        }
        intElement.addContent(new Element("listenToMemory").addContent(selectInt.getListenToMemory() ? "yes" : "no"));
        if (selectInt.getLocalVariable() != null && !selectInt.getLocalVariable().isEmpty()) {
            intElement.addContent(new Element("localVariable").addContent(selectInt.getLocalVariable()));
        }
        if (selectInt.getFormula() != null && !selectInt.getFormula().isEmpty()) {
            intElement.addContent(new Element("formula").addContent(selectInt.getFormula()));
        }

        if (selectInt.getAddressing() == NamedBeanAddressing.Table) {
            intElement.addContent(selectTableXml.store(selectInt.getSelectTable(), "table"));
        }

        return intElement;
    }

    public void load(Element intElement, LogixNG_SelectInteger selectInt)
            throws JmriConfigureXmlException {

        if (intElement != null) {

            LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

            try {
                Element elem = intElement.getChild("addressing");
                if (elem != null) {
                    selectInt.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
                }

                elem = intElement.getChild("value");
                if (elem != null) {
                    selectInt.setValue(Integer.parseInt(elem.getTextTrim()));
                }

                elem = intElement.getChild("reference");
                if (elem != null) selectInt.setReference(elem.getTextTrim());

                Element memoryName = intElement.getChild("memory");
                if (memoryName != null) {
                    Memory m = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName.getTextTrim());
                    if (m != null) selectInt.setMemory(m);
                    else selectInt.removeMemory();
                }

                Element listenToMemoryElem = intElement.getChild("listenToMemory");
                if (listenToMemoryElem != null) {
                    selectInt.setListenToMemory("yes".equals(listenToMemoryElem.getTextTrim()));
                }

                elem = intElement.getChild("localVariable");
                if (elem != null) selectInt.setLocalVariable(elem.getTextTrim());

                elem = intElement.getChild("formula");
                if (elem != null) selectInt.setFormula(elem.getTextTrim());

                if (intElement.getChild("table") != null) {
                    selectTableXml.load(intElement.getChild("table"), selectInt.getSelectTable());
                }

            } catch (ParserException e) {
                throw new JmriConfigureXmlException(e);
            }
        }
    }

    /**
     * This method is for backward compability up to and including 4.99.4.Remove this class after 5.0.
     *
     * @param shared the shared element
     * @param selectInt the LogixNG_SelectEnum
     * @param addressingElementName the name of the element of the addressing
     * @param valueElementName the name of the element of the integer
     * @param referenceElementName the name of the element of the reference
     * @param localVariableElementName the name of the element of the local variable
     * @param formulaElementName the name of the element of the formula
     * @throws JmriConfigureXmlException if an exception occurs
     */
    public void loadLegacy(
            Element shared,
            LogixNG_SelectInteger selectInt,
            String addressingElementName,
            String valueElementName,
            String referenceElementName,
            String localVariableElementName,
            String formulaElementName)
            throws JmriConfigureXmlException {

        Element name = shared.getChild(valueElementName);
        if (name != null) {
            selectInt.setValue(Integer.parseInt(name.getTextTrim()));
        }

        try {
            Element elem = shared.getChild(addressingElementName);
            if (addressingElementName != null && elem != null) {
                selectInt.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            elem = shared.getChild(referenceElementName);
            if (referenceElementName != null && elem != null) selectInt.setReference(elem.getTextTrim());

            elem = shared.getChild(localVariableElementName);
            if (localVariableElementName != null && elem != null) selectInt.setLocalVariable(elem.getTextTrim());

            elem = shared.getChild(formulaElementName);
            if (formulaElementName != null && elem != null) selectInt.setFormula(elem.getTextTrim());

        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }
    }

}
