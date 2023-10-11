package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.ActionAudio;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;
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

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Audio>();
        var selectEnumXml = new LogixNG_SelectEnumXml<ActionAudio.Operation>();

        element.addContent(selectNamedBeanXml.store(p.getSelectNamedBean(), "namedBean"));
        element.addContent(selectEnumXml.store(p.getSelectEnum(), "state"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionAudio h = new ActionAudio(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<Audio>();
        var selectEnumXml = new LogixNG_SelectEnumXml<ActionAudio.Operation>();

        selectNamedBeanXml.load(shared.getChild("namedBean"), h.getSelectNamedBean());
        selectNamedBeanXml.loadLegacy(shared, h.getSelectNamedBean(), "audio");

        selectEnumXml.load(shared.getChild("state"), h.getSelectEnum());
        selectEnumXml.loadLegacy(
                shared, h.getSelectEnum(),
                "stateAddressing",
                "audioState",
                "stateReference",
                "stateLocalVariable",
                "stateFormula");

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionAudioXml.class);
}
