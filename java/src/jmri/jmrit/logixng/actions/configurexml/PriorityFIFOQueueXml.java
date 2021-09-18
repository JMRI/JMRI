package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.PriorityFIFOQueue;

import org.jdom2.Element;

/**
 * Handle XML configuration for PriorityFIFOQueue objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class PriorityFIFOQueueXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public PriorityFIFOQueueXml() {
    }

    /**
     * Default implementation for storing the contents of a clock action.
     *
     * @param o Object to store, of type PriorityFIFOQueue
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        PriorityFIFOQueue p = (PriorityFIFOQueue) o;

        Element element = new Element("PriorityFIFOQueue");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(new Element("numPriorities").addContent(Integer.toString(p.getNumPriorities())));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        PriorityFIFOQueue h = new PriorityFIFOQueue(sys, uname);

        loadCommon(h, shared);

        Element numPriorities = shared.getChild("numPriorities");
        if (numPriorities != null) {
            int num;
            try {
                num = Integer.parseInt(numPriorities.getTextTrim());
            } catch (NumberFormatException ex) {
                num = 0;
            }
            h.setNumPriorities(num);
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PriorityFIFOQueueXml.class);
}
