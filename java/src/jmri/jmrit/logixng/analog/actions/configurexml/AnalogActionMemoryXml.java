package jmri.jmrit.logixng.analog.actions.configurexml;

import jmri.InstanceManager;
import jmri.MemoryManager;
import jmri.NamedBeanHandle;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.AnalogActionManager;
import jmri.jmrit.logixng.analog.actions.AnalogActionMemory;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class AnalogActionMemoryXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public AnalogActionMemoryXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        AnalogActionMemory p = (AnalogActionMemory) o;

        Element element = new Element("analog-action-memory");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        NamedBeanHandle memory = p.getMemory();
        if (memory != null) {
            element.addContent(new Element("memory").addContent(memory.getName()));
        }

        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        AnalogActionMemory h;
        h = new AnalogActionMemory(sys, uname);

        loadCommon(h, shared);

        Element memoryName = shared.getChild("memory");
        if (memoryName != null) {
            h.setMemory(InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName.getTextTrim()));
        }

        // this.checkedNamedBeanReference()
        // <T extends NamedBean> T checkedNamedBeanReference(String name, @Nonnull T type, @Nonnull Manager<T> m) {

        InstanceManager.getDefault(AnalogActionManager.class).registerAction(h);
        return true;
    }
    
//    private final static Logger log = LoggerFactory.getLogger(AnalogActionMemoryXml.class);
}
