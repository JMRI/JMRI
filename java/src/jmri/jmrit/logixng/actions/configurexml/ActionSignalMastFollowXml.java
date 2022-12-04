package jmri.jmrit.logixng.actions.configurexml;

import java.util.*;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.ActionSignalMastFollow;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectNamedBeanXml;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionSignalMastXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class ActionSignalMastFollowXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionSignalMastFollowXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalMast
     *
     * @param o Object to store, of type TripleLightSignalMast
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionSignalMastFollow p = (ActionSignalMastFollow) o;

        Element element = new Element("ActionSignalMastFollow");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<SignalMast>();
        element.addContent(selectNamedBeanXml.store(p.getSelectPrimaryMast(), "primaryMast"));
        element.addContent(selectNamedBeanXml.store(p.getSelectSecondaryMast(), "secondaryMast"));

        Element elementAspectMap = new Element("aspectMap");
        for (Map.Entry<String, String> entry : p.getAspectMap().entrySet()) {
            Element apectMapping = new Element("aspectMapping");
            apectMapping.addContent(new Element("primaryAspect").addContent(entry.getKey()));
            apectMapping.addContent(new Element("secondaryAspect").addContent(entry.getValue()));
            elementAspectMap.addContent(apectMapping);
        }
        element.addContent(elementAspectMap);

        element.addContent(new Element("followLitUnlit").addContent(p.getFollowLitUnlit()? "yes" : "no"));
        element.addContent(new Element("followHeldUnheld").addContent(p.getFollowHeldUnheld()? "yes" : "no"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionSignalMastFollow h = new ActionSignalMastFollow(sys, uname);

        loadCommon(h, shared);

        var selectNamedBeanXml = new LogixNG_SelectNamedBeanXml<SignalMast>();
        selectNamedBeanXml.load(shared.getChild("primaryMast"), h.getSelectPrimaryMast());
        selectNamedBeanXml.load(shared.getChild("secondaryMast"), h.getSelectSecondaryMast());

        List<Element> actionList = shared.getChild("aspectMap").getChildren();
        for (int i = 0; i < actionList.size(); i++) {
            Element apectMapping = actionList.get(i);
            String primaryAspect = apectMapping.getChild("primaryAspect").getTextTrim();
            String secondaryAspect = apectMapping.getChild("secondaryAspect").getTextTrim();
            h.getAspectMap().put(primaryAspect, secondaryAspect);
        }

        Element followLitUnlit = shared.getChild("followLitUnlit");
        if (followLitUnlit != null) {
            h.setFollowLitUnlit("yes".equals(followLitUnlit.getTextTrim()));
        } else {
            h.setFollowLitUnlit(false);
        }

        Element followHeldUnheld = shared.getChild("followHeldUnheld");
        if (followHeldUnheld != null) {
            h.setFollowHeldUnheld("yes".equals(followHeldUnheld.getTextTrim()));
        } else {
            h.setFollowHeldUnheld(false);
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSignalMastFollowXml.class);
}
