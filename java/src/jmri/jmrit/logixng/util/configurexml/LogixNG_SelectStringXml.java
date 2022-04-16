package jmri.jmrit.logixng.util.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.util.LogixNG_SelectString;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Xml class for jmri.jmrit.logixng.util.LogixNG_SelectString.
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class LogixNG_SelectStringXml {

    /**
     * Default implementation for storing the contents of a LogixNG_SelectEnum
     *
     * @param selectStr the LogixNG_SelectString object
     * @param tagName the name of the element
     * @return Element containing the complete info
     */
    public Element store(LogixNG_SelectString selectStr, String tagName) {

        LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

        Element enumElement = new Element(tagName);

        enumElement.addContent(new Element("addressing").addContent(selectStr.getAddressing().name()));
        enumElement.addContent(new Element("value").addContent(selectStr.getValue()));
        enumElement.addContent(new Element("reference").addContent(selectStr.getReference()));
        enumElement.addContent(new Element("localVariable").addContent(selectStr.getLocalVariable()));
        enumElement.addContent(new Element("formula").addContent(selectStr.getFormula()));

        if (selectStr.getAddressing() == NamedBeanAddressing.Table) {
            enumElement.addContent(selectTableXml.store(selectStr.getSelectTable(), "table"));
        }

        return enumElement;
    }

    public void load(Element enumElement, LogixNG_SelectString selectStr)
            throws JmriConfigureXmlException {

        if (enumElement != null) {

            LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

            try {
                Element elem = enumElement.getChild("addressing");
                if (elem != null) {
                    selectStr.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
                }

                elem = enumElement.getChild("value");
                if (elem != null) {
                    selectStr.setValue(elem.getTextTrim());
                }

                elem = enumElement.getChild("reference");
                if (elem != null) selectStr.setReference(elem.getTextTrim());

                elem = enumElement.getChild("localVariable");
                if (elem != null) selectStr.setLocalVariable(elem.getTextTrim());

                elem = enumElement.getChild("formula");
                if (elem != null) selectStr.setFormula(elem.getTextTrim());

                if (enumElement.getChild("table") != null) {
                    selectTableXml.load(enumElement.getChild("table"), selectStr.getSelectTable());
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
     * @param selectStr the LogixNG_SelectEnum
     * @param addressingElementName the name of the element of the addressing
     * @param valueElementName the name of the element of the string
     * @param referenceElementName the name of the element of the reference
     * @param localVariableElementName the name of the element of the local variable
     * @param formulaElementName the name of the element of the formula
     * @throws JmriConfigureXmlException if an exception occurs
     */
    public void loadLegacy(
            Element shared,
            LogixNG_SelectString selectStr,
            String addressingElementName,
            String valueElementName,
            String referenceElementName,
            String localVariableElementName,
            String formulaElementName)
            throws JmriConfigureXmlException {

        Element name = shared.getChild(valueElementName);
        if (name != null) {
            selectStr.setValue(name.getTextTrim());
        }

        try {
            Element elem = shared.getChild(addressingElementName);
            if (elem != null) {
                selectStr.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            elem = shared.getChild(referenceElementName);
            if (elem != null) selectStr.setReference(elem.getTextTrim());

            elem = shared.getChild(localVariableElementName);
            if (elem != null) selectStr.setLocalVariable(elem.getTextTrim());

            elem = shared.getChild(formulaElementName);
            if (elem != null) selectStr.setFormula(elem.getTextTrim());

        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }
    }

}
