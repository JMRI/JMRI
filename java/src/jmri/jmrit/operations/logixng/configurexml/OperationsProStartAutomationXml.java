package jmri.jmrit.operations.logixng.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.util.configurexml.*;
import jmri.jmrit.operations.logixng.OperationsProStartAutomation;

import org.jdom2.Element;

/**
 * Handle XML configuration for OperationsPro_StartAutomation objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2025
 */
public class OperationsProStartAutomationXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public OperationsProStartAutomationXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleSensorSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        OperationsProStartAutomation p = (OperationsProStartAutomation) o;

        Element element = new Element("OperationsProStartAutomation");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        var selectComboBoxXml = new LogixNG_SelectComboBoxXml();

        element.addContent(selectComboBoxXml.store(p.getSelectAutomations(), "automation"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        OperationsProStartAutomation h = new OperationsProStartAutomation(sys, uname);

        loadCommon(h, shared);

        var selectEnumXml = new LogixNG_SelectComboBoxXml();

        selectEnumXml.load(shared.getChild("automation"), h.getSelectAutomations());

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OperationsPro_StartAutomationXml.class);
}
