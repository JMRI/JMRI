package jmri.jmrit.logixng.actions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.actions.ActionOBlock;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for OBlock objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright (C) 2021
 */
public class ActionOBlockXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionOBlockXml() {
    }

    /**
     * Default implementation for storing the contents of an OBlock
     *
     * @param o Object to store, of type ActionOBlock
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionOBlock p = (ActionOBlock) o;

        Element element = new Element("ActionOBlock");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        NamedBeanHandle oblock = p.getOBlock();
        if (oblock != null) {
            element.addContent(new Element("oblock").addContent(oblock.getName()));
        }

        element.addContent(new Element("addressing").addContent(p.getAddressing().name()));
        element.addContent(new Element("reference").addContent(p.getReference()));
        element.addContent(new Element("localVariable").addContent(p.getLocalVariable()));
        element.addContent(new Element("formula").addContent(p.getFormula()));

        element.addContent(new Element("operationAddressing").addContent(p.getOperationAddressing().name()));
        element.addContent(new Element("operationDirect").addContent(p.getOperationDirect().name()));
        element.addContent(new Element("operationReference").addContent(p.getOperationReference()));
        element.addContent(new Element("operationLocalVariable").addContent(p.getOperationLocalVariable()));
        element.addContent(new Element("operationFormula").addContent(p.getLockFormula()));

        element.addContent(new Element("oblockConstant").addContent(p.getOBlockConstant()));
        NamedBeanHandle<Memory> oblockMemory = p.getOBlockMemory();
        if (oblockMemory != null) {
            element.addContent(new Element("oblockMemory").addContent(oblockMemory.getName()));
        }

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionOBlock h = new ActionOBlock(sys, uname);

        loadCommon(h, shared);

        Element oblockName = shared.getChild("oblock");
        if (oblockName != null) {
            OBlock t = InstanceManager.getDefault(OBlockManager.class).getNamedBean(oblockName.getTextTrim());
            if (t != null) h.setOBlock(t);
            else h.removeOBlock();
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


            elem = shared.getChild("operationAddressing");
            if (elem != null) {
                h.setOperationAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            elem = shared.getChild("operationDirect");
            if (elem != null) {
                h.setOperationDirect(ActionOBlock.DirectOperation.valueOf(elem.getTextTrim()));
            }

            elem = shared.getChild("operationReference");
            if (elem != null) h.setOperationReference(elem.getTextTrim());

            elem = shared.getChild("operationLocalVariable");
            if (elem != null) h.setOperationLocalVariable(elem.getTextTrim());

            elem = shared.getChild("operationFormula");
            if (elem != null) h.setOperationFormula(elem.getTextTrim());

            elem = shared.getChild("oblockConstant");
            if (elem != null) h.setOBlockConstant(elem.getTextTrim());

            elem = shared.getChild("oblockMemory");
            if (elem != null) h.setOBlockMemory(elem.getTextTrim());

        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionOBlockXml.class);
}
