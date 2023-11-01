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

    public enum StoreNamedBean { Name, SystemName }

    /**
     * Default implementation for storing the contents of a LogixNG_SelectNamedBean
     *
     * @param selectNamedBean the LogixNG_SelectTable object
     * @param tagName the name of the element
     * @return Element containing the complete info
     */
    public Element store(LogixNG_SelectNamedBean<E> selectNamedBean, String tagName) {
        return store(selectNamedBean, tagName, StoreNamedBean.Name);
    }

    public Element store(LogixNG_SelectNamedBean<E> selectNamedBean,
            String tagName, StoreNamedBean storeNamedBean) {
        Element namedBeanElement = new Element(tagName);

        LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

        namedBeanElement.addContent(new Element("addressing").addContent(selectNamedBean.getAddressing().name()));
        NamedBeanHandle<E> namedBeanHandle = selectNamedBean.getNamedBean();
        if (namedBeanHandle != null) {
            String name;
            switch (storeNamedBean) {
                case Name:
                    name = namedBeanHandle.getName();
                    break;
                case SystemName:
                    name = namedBeanHandle.getBean() != null
                            ? namedBeanHandle.getBean().getSystemName() : null;
                    break;
                default:
                    throw new IllegalArgumentException("storeNamedBean has unknown value: "+storeNamedBean.name());
            }
            if (name != null) {
                namedBeanElement.addContent(new Element("name").addContent(name));
            }
        }
        if (selectNamedBean.getReference() != null && !selectNamedBean.getReference().isEmpty()) {
            namedBeanElement.addContent(new Element("reference").addContent(selectNamedBean.getReference()));
        }
        var memory = selectNamedBean.getMemory();
        if (memory != null) {
            namedBeanElement.addContent(new Element("memory").addContent(memory.getName()));
        }
        namedBeanElement.addContent(new Element("listenToMemory").addContent(selectNamedBean.getListenToMemory() ? "yes" : "no"));
        if (selectNamedBean.getLocalVariable() != null && !selectNamedBean.getLocalVariable().isEmpty()) {
            namedBeanElement.addContent(new Element("localVariable").addContent(selectNamedBean.getLocalVariable()));
        }
        if (selectNamedBean.getFormula() != null && !selectNamedBean.getFormula().isEmpty()) {
            namedBeanElement.addContent(new Element("formula").addContent(selectNamedBean.getFormula()));
        }

        if (selectNamedBean.getAddressing() == NamedBeanAddressing.Table) {
            namedBeanElement.addContent(selectTableXml.store(selectNamedBean.getSelectTable(), "table"));
        }

        return namedBeanElement;
    }

    public void load(Element namedBeanElement, LogixNG_SelectNamedBean<E> selectNamedBean) throws JmriConfigureXmlException {
        load(namedBeanElement, selectNamedBean, false);
    }

    public void load(Element namedBeanElement, LogixNG_SelectNamedBean<E> selectNamedBean, boolean delayedLookup) throws JmriConfigureXmlException {

        if (namedBeanElement != null) {

            LogixNG_SelectTableXml selectTableXml = new LogixNG_SelectTableXml();

            try {
                Element elem = namedBeanElement.getChild("addressing");
                if (elem != null) {
                    selectNamedBean.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
                }

                elem = namedBeanElement.getChild("name");
                if (elem != null) {
                    if (delayedLookup) {
                        selectNamedBean.setDelayedNamedBean(elem.getTextTrim());
                    } else {
                        String name = elem.getTextTrim();
                        E t = selectNamedBean.getManager().getNamedBean(name);
                        if (t != null) selectNamedBean.setNamedBean(name, t);
                        else selectNamedBean.removeNamedBean();
                    }
                }

                elem = namedBeanElement.getChild("reference");
                if (elem != null) selectNamedBean.setReference(elem.getTextTrim());

                Element memoryName = namedBeanElement.getChild("memory");
                if (memoryName != null) {
                    Memory m = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName.getTextTrim());
                    if (m != null) selectNamedBean.setMemory(m);
                    else selectNamedBean.removeMemory();
                }

                Element listenToMemoryElem = namedBeanElement.getChild("listenToMemory");
                if (listenToMemoryElem != null) {
                    selectNamedBean.setListenToMemory("yes".equals(listenToMemoryElem.getTextTrim()));
                }

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
        loadLegacy(shared, selectNamedBean, beanElementName, "addressing", "reference", "localVariable", "formula");
    }

    /**
     * This method is for backward compability up to and including 4.99.4.Remove this class after 5.0.
     *
     * @param shared the shared element
     * @param selectNamedBean           the LogixNG_SelectNamedBean
     * @param beanElementName           the name of the element of the bean, for example "turnout"
     * @param addressingElementName     the name of the element of the addressing, for example "addressing"
     * @param referenceElementName      the name of the element of the reference, for example "reference"
     * @param localVariableElementName  the name of the element of the local variable, for example "localVariable"
     * @param formulaElementName        the name of the element of the formula, for example "formula"
     * @throws JmriConfigureXmlException if an exception occurs
     */
    public void loadLegacy(
            Element shared,
            LogixNG_SelectNamedBean<E> selectNamedBean,
            String beanElementName,
            String addressingElementName,
            String referenceElementName,
            String localVariableElementName,
            String formulaElementName
            )
            throws JmriConfigureXmlException {

        Element beanName = shared.getChild(beanElementName);
        if (beanName != null) {
            E t = selectNamedBean.getManager().getNamedBean(beanName.getTextTrim());
            if (t != null) selectNamedBean.setNamedBean(t);
            else selectNamedBean.removeNamedBean();
        }

        try {
            Element elem;

            if (addressingElementName != null) {
                elem = shared.getChild(addressingElementName);
                if (elem != null) {
                    selectNamedBean.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
                }
            }

            if (referenceElementName != null) {
                elem = shared.getChild(referenceElementName);
                if (elem != null) selectNamedBean.setReference(elem.getTextTrim());
            }

            if (localVariableElementName != null) {
                elem = shared.getChild(localVariableElementName);
                if (elem != null) selectNamedBean.setLocalVariable(elem.getTextTrim());
            }

            if (formulaElementName != null) {
                elem = shared.getChild(formulaElementName);
                if (elem != null) selectNamedBean.setFormula(elem.getTextTrim());
            }

        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }
    }


    public interface GetNameFromNamedBeanHandle<E extends NamedBean> {
        public String get(NamedBeanHandle<E> handle);
    }

}
