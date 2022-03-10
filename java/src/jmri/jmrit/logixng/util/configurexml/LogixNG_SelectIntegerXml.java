package jmri.jmrit.logixng.util.configurexml;

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

        Element enumElement = new Element(tagName);

        enumElement.addContent(new Element("addressing").addContent(selectInt.getAddressing().name()));
        enumElement.addContent(new Element("value").addContent(Integer.toString(selectInt.getValue())));
        enumElement.addContent(new Element("reference").addContent(selectInt.getReference()));
        enumElement.addContent(new Element("localVariable").addContent(selectInt.getLocalVariable()));
        enumElement.addContent(new Element("formula").addContent(selectInt.getFormula()));

        if (selectInt.getAddressing() == NamedBeanAddressing.Table) {
            enumElement.addContent(selectTableXml.store(selectInt.getSelectTable(), "table"));
        }

        return enumElement;
    }

    public void load(Element enumElement, LogixNG_SelectInteger selectInt)
            throws JmriConfigureXmlException {

        if (enumElement != null) {

            LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

            try {
                Element elem = enumElement.getChild("addressing");
                if (elem != null) {
                    selectInt.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
                }

                elem = enumElement.getChild("value");
                if (elem != null) {
                    selectInt.setValue(Integer.parseInt(elem.getTextTrim()));
                }

                elem = enumElement.getChild("reference");
                if (elem != null) selectInt.setReference(elem.getTextTrim());

                elem = enumElement.getChild("localVariable");
                if (elem != null) selectInt.setLocalVariable(elem.getTextTrim());

                elem = enumElement.getChild("formula");
                if (elem != null) selectInt.setFormula(elem.getTextTrim());

                if (enumElement.getChild("table") != null) {
                    selectTableXml.load(enumElement.getChild("table"), selectInt.getSelectTable());
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
            if (elem != null) {
                selectInt.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            elem = shared.getChild(referenceElementName);
            if (elem != null) selectInt.setReference(elem.getTextTrim());

            elem = shared.getChild(localVariableElementName);
            if (elem != null) selectInt.setLocalVariable(elem.getTextTrim());

            elem = shared.getChild(formulaElementName);
            if (elem != null) selectInt.setFormula(elem.getTextTrim());

        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }
    }

}
