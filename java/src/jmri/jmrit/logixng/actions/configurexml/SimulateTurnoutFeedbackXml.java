package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.SimulateTurnoutFeedback;

import org.jdom2.Element;

/**
 * Handle XML configuration for SimulateTurnoutFeedback objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class SimulateTurnoutFeedbackXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public SimulateTurnoutFeedbackXml() {
    }

    /**
     * Default implementation for storing the contents of a SimulateTurnoutFeedback
     *
     * @param o Object to store, of type Many
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        SimulateTurnoutFeedback p = (SimulateTurnoutFeedback) o;

        Element element = new Element("SimulateTurnoutFeedback");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {

        // put it together
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        SimulateTurnoutFeedback h = new SimulateTurnoutFeedback(sys, uname);

        loadCommon(h, shared);

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);

        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ManyXml.class);

}
