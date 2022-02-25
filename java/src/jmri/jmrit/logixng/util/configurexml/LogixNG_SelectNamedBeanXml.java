package jmri.jmrit.logixng.util.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Xml class for jmri.jmrit.logixng.util.LogixNG_SelectNamedBean.
 *
 * @param <E> the type of the named bean
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class LogixNG_SelectNamedBeanXml<E extends NamedBean> {

    /**
     * Default implementation for storing the contents of a LogixNG_SelectNamedBean
     *
     * @param selectNamedBean the LogixNG_SelectTable object
     * @param tagName the name of the element
     * @return Element containing the complete info
     */
    public Element store(LogixNG_SelectNamedBean<E> selectNamedBean, String tagName) {
        Element namedBeanElement = new Element(tagName);

        LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

        namedBeanElement.addContent(new Element("addressing").addContent(selectNamedBean.getAddressing().name()));
        NamedBeanHandle<E> table = selectNamedBean.getNamedBean();
        if (table != null) {
            namedBeanElement.addContent(new Element("name").addContent(table.getName()));
        }
        namedBeanElement.addContent(new Element("reference").addContent(selectNamedBean.getReference()));
        namedBeanElement.addContent(new Element("localVariable").addContent(selectNamedBean.getLocalVariable()));
        namedBeanElement.addContent(new Element("formula").addContent(selectNamedBean.getFormula()));

        if (selectNamedBean.getAddressing() == NamedBeanAddressing.Table) {
            namedBeanElement.addContent(selectTableXml.store(selectNamedBean.getSelectTable(), "table"));
        }

        return namedBeanElement;
    }

    public void load(Element namedBeanElement, LogixNG_SelectNamedBean<E> selectNamedBean) throws JmriConfigureXmlException {

        if (namedBeanElement != null) {

            LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

            try {
                Element elem = namedBeanElement.getChild("addressing");
                if (elem != null) {
                    selectNamedBean.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
                }

                elem = namedBeanElement.getChild("name");
                if (elem != null) {
                    E t = selectNamedBean.getManager().getNamedBean(elem.getTextTrim());
                    if (t != null) selectNamedBean.setNamedBean(t);
                    else selectNamedBean.removeNamedBean();
                }

                elem = namedBeanElement.getChild("reference");
                if (elem != null) selectNamedBean.setReference(elem.getTextTrim());

                elem = namedBeanElement.getChild("localVariable");
                if (elem != null) selectNamedBean.setLocalVariable(elem.getTextTrim());

                elem = namedBeanElement.getChild("formula");
                if (elem != null) selectNamedBean.setFormula(elem.getTextTrim());

                if (namedBeanElement.getChild("table") != null) {
                    selectTableXml.load(namedBeanElement.getChild("table"), selectNamedBean.getSelectTable());
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
     * @param selectNamedBean the LogixNG_SelectNamedBean
     * @param beanElementName the name of the element of the bean, for example "turnout"
     * @throws JmriConfigureXmlException if an exception occurs
     */
    public void loadLegacy(
            Element shared,
            LogixNG_SelectNamedBean<E> selectNamedBean,
            String beanElementName)
            throws JmriConfigureXmlException {

        Element beanName = shared.getChild(beanElementName);
        if (beanName != null) {
            E t = selectNamedBean.getManager().getNamedBean(beanName.getTextTrim());
            if (t != null) selectNamedBean.setNamedBean(t);
            else selectNamedBean.removeNamedBean();
        }

        try {
            Element elem = shared.getChild("addressing");
            if (elem != null) {
                selectNamedBean.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            elem = shared.getChild("reference");
            if (elem != null) selectNamedBean.setReference(elem.getTextTrim());

            elem = shared.getChild("localVariable");
            if (elem != null) selectNamedBean.setLocalVariable(elem.getTextTrim());

            elem = shared.getChild("formula");
            if (elem != null) selectNamedBean.setFormula(elem.getTextTrim());

        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }
    }

}
