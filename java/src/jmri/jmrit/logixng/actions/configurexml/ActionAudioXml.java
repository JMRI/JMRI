package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.actions.ActionAudio;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionAudioXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class ActionAudioXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionAudioXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleAudioSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionAudio p = (ActionAudio) o;

        Element element = new Element("ActionAudio");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var audio = p.getAudio();
        if (audio != null) {
            element.addContent(new Element("audio").addContent(audio.getName()));
        }

        element.addContent(new Element("addressing").addContent(p.getAddressing().name()));
        element.addContent(new Element("reference").addContent(p.getReference()));
        element.addContent(new Element("localVariable").addContent(p.getLocalVariable()));
        element.addContent(new Element("formula").addContent(p.getFormula()));

        element.addContent(new Element("stateAddressing").addContent(p.getOperationAddressing().name()));
        element.addContent(new Element("audioState").addContent(p.getOperation().name()));
        element.addContent(new Element("stateReference").addContent(p.getOperationReference()));
        element.addContent(new Element("stateLocalVariable").addContent(p.getOperationLocalVariable()));
        element.addContent(new Element("stateFormula").addContent(p.getOperationFormula()));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionAudio h = new ActionAudio(sys, uname);

        loadCommon(h, shared);

        Element audioName = shared.getChild("audio");
        if (audioName != null) {
            Audio t = InstanceManager.getDefault(AudioManager.class).getAudio(audioName.getTextTrim());
            if (t != null) h.setAudio(t);
            else h.removeAudio();
        }

        try {
            Element elem = shared.getChild("addressing");
            if (elem != null) {
                h.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            elem = shared.getChild("reference");
            if (elem != null) h.setReference(elem.getTextTrim());

            elem = shared.getChild("localVariable");
            if (elem != null) h.setLocalVariable(elem.getTextTrim());

            elem = shared.getChild("formula");
            if (elem != null) h.setFormula(elem.getTextTrim());


            elem = shared.getChild("stateAddressing");
            if (elem != null) {
                h.setOperationAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            Element audioState = shared.getChild("audioState");
            if (audioState != null) {
                h.setOperation(ActionAudio.Operation.valueOf(audioState.getTextTrim()));
            }

            elem = shared.getChild("stateReference");
            if (elem != null) h.setOperationReference(elem.getTextTrim());

            elem = shared.getChild("stateLocalVariable");
            if (elem != null) h.setOperationLocalVariable(elem.getTextTrim());

            elem = shared.getChild("stateFormula");
            if (elem != null) h.setOperationFormula(elem.getTextTrim());

        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionAudioXml.class);
}
