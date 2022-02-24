package jmri.jmrit.logixng.util.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.util.LogixNG_SelectEnum;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Xml class for jmri.jmrit.logixng.util.LogixNG_SelectEnum.
 *
 * @param <E> the type of enum
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class LogixNG_SelectEnumXml<E extends Enum<?>> {

    /**
     * Default implementation for storing the contents of a LogixNG_SelectEnum
     *
     * @param selectEnum the LogixNG_SelectTable object
     * @param tagName the name of the element
     * @return Element containing the complete info
     */
    public Element store(LogixNG_SelectEnum<E> selectEnum, String tagName) {

        LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

        Element enumElement = new Element(tagName);

        enumElement.addContent(new Element("addressing").addContent(selectEnum.getAddressing().name()));
        enumElement.addContent(new Element("enum").addContent(selectEnum.getEnum().name()));
        enumElement.addContent(new Element("reference").addContent(selectEnum.getReference()));
        enumElement.addContent(new Element("localVariable").addContent(selectEnum.getLocalVariable()));
        enumElement.addContent(new Element("formula").addContent(selectEnum.getFormula()));

        if (selectEnum.getAddressing() == NamedBeanAddressing.Table) {
            enumElement.addContent(selectTableXml.store(selectEnum.getSelectTable(), "table"));
        }

        return enumElement;
    }

    public void load(Element enumElement, LogixNG_SelectEnum<E> selectEnum)
            throws JmriConfigureXmlException {

        if (enumElement != null) {

            LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

            try {
                Element elem = enumElement.getChild("addressing");
                if (elem != null) {
                    selectEnum.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
                }

                elem = enumElement.getChild("enum");
                if (elem != null) {
                    selectEnum.setEnum(selectEnum.getEnum(elem.getTextTrim()));
                }

                elem = enumElement.getChild("reference");
                if (elem != null) selectEnum.setReference(elem.getTextTrim());

                elem = enumElement.getChild("localVariable");
                if (elem != null) selectEnum.setLocalVariable(elem.getTextTrim());

                elem = enumElement.getChild("formula");
                if (elem != null) selectEnum.setFormula(elem.getTextTrim());

                if (enumElement.getChild("table") != null) {
                    selectTableXml.load(enumElement.getChild("table"), selectEnum.getSelectTable());
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
     * @param selectEnum the LogixNG_SelectEnum
     * @param addressingElementName the name of the element of the addressing, for example "state"
     * @param enumElementName the name of the element of the enum, for example "state"
     * @param referenceElementName the name of the element of the reference, for example "state"
     * @param localVariableElementName the name of the element of the local variable, for example "state"
     * @param formulaElementName the name of the element of the formula, for example "state"
     * @throws JmriConfigureXmlException if an exception occurs
     */
    public void loadLegacy(
            Element shared,
            LogixNG_SelectEnum<E> selectEnum,
            String addressingElementName,
            String enumElementName,
            String referenceElementName,
            String localVariableElementName,
            String formulaElementName)
            throws JmriConfigureXmlException {

        Element name = shared.getChild(enumElementName);
        if (name != null) {
            selectEnum.setEnum(selectEnum.getEnum(name.getTextTrim()));
        }

        try {
            Element elem = shared.getChild(addressingElementName);
            if (elem != null) {
                selectEnum.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            elem = shared.getChild(referenceElementName);
            if (elem != null) selectEnum.setReference(elem.getTextTrim());

            elem = shared.getChild(localVariableElementName);
            if (elem != null) selectEnum.setLocalVariable(elem.getTextTrim());

            elem = shared.getChild(formulaElementName);
            if (elem != null) selectEnum.setFormula(elem.getTextTrim());

        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }
    }

}
