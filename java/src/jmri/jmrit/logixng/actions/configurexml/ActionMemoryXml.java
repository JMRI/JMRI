package jmri.jmrit.logixng.actions.configurexml;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBeanHandle;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.actions.ActionMemory;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ActionMemoryXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ActionMemoryXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionMemory p = (ActionMemory) o;

        Element element = new Element("action-memory");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        NamedBeanHandle<Memory> memory = p.getMemory();
        if (memory != null) {
            element.addContent(new Element("memory").addContent(memory.getName()));
        }
        
        NamedBeanHandle<Memory> otherMemoryName = p.getOtherMemory();
        if (otherMemoryName != null) {
            element.addContent(new Element("otherMemory").addContent(otherMemoryName.getName()));
        }
        
        element.addContent(new Element("memoryOperation").addContent(p.getMemoryOperation().name()));
        
        element.addContent(new Element("data").addContent(p.getData()));

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionMemory h = new ActionMemory(sys, uname);

        loadCommon(h, shared);

        Element memoryName = shared.getChild("memory");
        if (memoryName != null) {
            Memory t = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName.getTextTrim());
            if (t != null) h.setMemory(t);
            else h.removeMemory();
        }

        Element otherMemoryName = shared.getChild("otherMemory");
        if (otherMemoryName != null) {
            Memory t = InstanceManager.getDefault(MemoryManager.class).getMemory(otherMemoryName.getTextTrim());
            if (t != null) h.setOtherMemory(t);
            else h.removeOtherMemory();
        }

        Element queryType = shared.getChild("memoryOperation");
        if (queryType != null) {
            try {
                h.setMemoryOperation(ActionMemory.MemoryOperation.valueOf(queryType.getTextTrim()));
            } catch (ParserException e) {
                log.error("cannot set memory operation: " + queryType.getTextTrim(), e);
            }
        }

        Element data = shared.getChild("data");
        if (data != null) {
            try {
                h.setData(data.getTextTrim());
            } catch (ParserException e) {
                log.error("cannot set data: " + data.getTextTrim(), e);
            }
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionMemoryXml.class);
}
