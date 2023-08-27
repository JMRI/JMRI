package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionAudio;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ExpressionAudioXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2023
 */
public class ExpressionAudioXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ExpressionAudioXml() {
    }

    /**
     * Default implementation for storing the contents of a ExpressionAudio
     *
     * @param o Object to store, of type TripleAudioSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ExpressionAudio p = (ExpressionAudio) o;

        Element element = new Element("ExpressionAudio");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Audio>();
        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));

        element.addContent(new Element("is_isNot").addContent(p.get_Is_IsNot().name()));

        element.addContent(new Element("stateAddressing").addContent(p.getStateAddressing().name()));
        element.addContent(new Element("audioState").addContent(p.getBeanState().name()));
        element.addContent(new Element("stateReference").addContent(p.getStateReference()));
        element.addContent(new Element("stateLocalVariable").addContent(p.getStateLocalVariable()));
        element.addContent(new Element("stateFormula").addContent(p.getStateFormula()));

        element.addContent(new Element("checkOnlyOnChange").addContent(p.isCheckOnlyOnChange()? "yes" : "no"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ExpressionAudio h = new ExpressionAudio(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Audio>();
        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());

        try {
            Element is_IsNot = shared.getChild("is_isNot");
            if (is_IsNot != null) {
                h.set_Is_IsNot(Is_IsNot_Enum.valueOf(is_IsNot.getTextTrim()));
            }

            Element elem = shared.getChild("stateAddressing");
            if (elem != null) {
                h.setStateAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            Element audioState = shared.getChild("audioState");
            if (audioState != null) {
                h.setBeanState(ExpressionAudio.AudioState.valueOf(audioState.getTextTrim()));
            }

            elem = shared.getChild("stateReference");
            if (elem != null) h.setStateReference(elem.getTextTrim());

            elem = shared.getChild("stateLocalVariable");
            if (elem != null) h.setStateLocalVariable(elem.getTextTrim());

            elem = shared.getChild("stateFormula");
            if (elem != null) h.setStateFormula(elem.getTextTrim());

            Element checkOnlyOnChangeElem = shared.getChild("checkOnlyOnChange");
            if (checkOnlyOnChangeElem != null) {
                h.setCheckOnlyOnChange("yes".equals(checkOnlyOnChangeElem.getTextTrim()));
            }

        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionAudioXml.class);
}
